extends Node2D

const PlayerScene = preload("res://scripts/player.gd")
const WorldScene = preload("res://scripts/world_generator.gd")
const HUDScene = preload("res://scripts/hud.gd")
const BACKGROUNDS := preload("res://assets/world/backgrounds.svg")
const TILES := preload("res://assets/world/tiles.svg")
const WORLD_HEIGHT: float = 720.0
const GROUND_Y: float = 560.0
const BACKDROP_WIDTH: float = 720.0
const BACKGROUND_TILE_SIZE: float = 256.0
const GROUND_TILE_SIZE: float = 32.0

var player: MaryouPlayer
var world: MaryouWorldGenerator
var enemies: Node2D
var hud: MaryouHUD
var steps: int = 0
var hit_lock: bool = false
var run_finishing: bool = false
var restart_pending: bool = false
var lifecycle_token: int = 0

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_PAUSABLE
	Engine.time_scale = 1.0
	get_tree().paused = false
	lifecycle_token += 1
	if OS.has_feature("android"):
		DisplayServer.screen_set_orientation(DisplayServer.SCREEN_SENSOR_LANDSCAPE)

	var checkpoint_spawn := GameState.consume_checkpoint_respawn()
	var spawning_at_checkpoint := checkpoint_spawn != Vector2.INF
	ScoreManager.reset_run()

	enemies = Node2D.new()
	enemies.name = "Enemies"
	add_child(enemies)
	world = WorldScene.new()
	world.name = "WorldGenerator"
	add_child(world)
	world.setup(enemies)
	world.coin_collected.connect(_on_coin)
	world.hazard_hit.connect(_on_hazard)
	world.checkpoint_reached.connect(_on_checkpoint_reached)

	player = PlayerScene.new()
	player.name = "Player"
	player.position = Vector2(180, GROUND_Y - 30.0)
	if spawning_at_checkpoint:
		player.position = checkpoint_spawn + Vector2(-12.0, -38.0)
		steps = maxi(0, int(player.position.x / GROUND_TILE_SIZE))
		ScoreManager.steps = steps
	add_child(player)

	hud = HUDScene.new()
	hud.name = "HUD"
	hud.pause_pressed.connect(_toggle_pause)
	hud.restart_pressed.connect(_restart)
	hud.back_pressed.connect(_back_from_overlay)
	add_child(hud)

	world.generate_until(player.position.x, steps)
	_wire_enemies()
	hud.update_stats(steps, ScoreManager.coins, MaryouDifficultyCurve.tier_for_steps(steps), player.shielded)
	queue_redraw()

func _physics_process(delta: float) -> void:
	if restart_pending or run_finishing or not is_instance_valid(player) or not is_instance_valid(world) or not is_instance_valid(hud):
		return
	if Input.is_action_just_pressed("pause"):
		_toggle_pause()
		return

	var distance: int = maxi(0, int(player.position.x / GROUND_TILE_SIZE))
	steps = maxi(steps, distance)
	ScoreManager.steps = steps
	var speed: float = MaryouDifficultyCurve.speed_for_steps(steps)
	var left: bool = Input.is_action_pressed("move_left")
	var right: bool = Input.is_action_pressed("move_right")
	var jump_held: bool = Input.is_action_pressed("jump")
	var jump_pressed: bool = Input.is_action_just_pressed("jump")
	if jump_pressed:
		AudioManager.play_sfx("jump")
	player.tick(delta, speed, left, right, jump_pressed, jump_held)

	world.generate_until(player.position.x, steps)
	_wire_enemies()
	for child in enemies.get_children():
		if is_instance_valid(child) and child is MaryouEnemy:
			var enemy: MaryouEnemy = child as MaryouEnemy
			enemy.tick(delta, player.position.x)

	if player.position.y > WORLD_HEIGHT + 80.0:
		_finish_run()
	hud.update_stats(steps, ScoreManager.coins, MaryouDifficultyCurve.tier_for_steps(steps), player.shielded)
	queue_redraw()

func _wire_enemies() -> void:
	for child in enemies.get_children():
		if not is_instance_valid(child) or not child is MaryouEnemy:
			continue
		var enemy: MaryouEnemy = child as MaryouEnemy
		if enemy.has_meta("maryou_wired"):
			continue
		enemy.player_contact.connect(_on_enemy_contact)
		enemy.stomped.connect(_on_enemy_stomp)
		enemy.set_meta("maryou_wired", true)

func _on_coin() -> void:
	ScoreManager.add_coin()

func _on_hazard(_player: MaryouPlayer) -> void:
	if restart_pending or run_finishing:
		return
	_juice(0.04, 0.94, lifecycle_token)
	_finish_run()

func _on_checkpoint_reached(_position: Vector2) -> void:
	if is_instance_valid(hud):
		hud.show_checkpoint_notice()

func _on_enemy_contact(_enemy: MaryouEnemy) -> void:
	_take_damage()

func _on_enemy_stomp(_enemy: MaryouEnemy) -> void:
	if restart_pending or run_finishing:
		return
	ScoreManager.add_stomp()
	if is_instance_valid(player):
		player.shake(5.0, 0.10)
	_juice(0.06, 0.88, lifecycle_token)

func _take_damage() -> void:
	if restart_pending or run_finishing or hit_lock or not ScoreManager.run_active or not is_instance_valid(player) or player.dead:
		return
	hit_lock = true
	AudioManager.play_sfx("hit")
	if player.take_damage(1):
		_finish_run()
	else:
		_juice(0.05, 0.9, lifecycle_token)
	var token := lifecycle_token
	await get_tree().create_timer(0.35, true, false, true).timeout
	if token != lifecycle_token or restart_pending or run_finishing:
		return
	hit_lock = false

func _finish_run() -> void:
	if restart_pending or run_finishing or not is_instance_valid(player) or player.dead or not ScoreManager.run_active:
		return
	run_finishing = true
	var token := lifecycle_token
	ScoreManager.finish_run()
	player.kill()
	get_tree().paused = false
	Engine.time_scale = 1.0
	if is_instance_valid(player.camera):
		var tween := create_tween().set_parallel(true)
		tween.tween_property(player.camera, "zoom", Vector2(0.98, 0.98), 0.22).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	await get_tree().create_timer(0.14, true, false, true).timeout
	if token != lifecycle_token or restart_pending:
		return
	if is_instance_valid(hud):
		hud.show_game_over(ScoreManager.steps, ScoreManager.best_steps)

func _toggle_pause() -> void:
	if restart_pending or run_finishing or not is_instance_valid(player) or player.dead:
		return
	var value: bool = not get_tree().paused
	get_tree().paused = value
	if is_instance_valid(hud): hud.show_pause(value, ScoreManager.steps, ScoreManager.best_steps)

func _restart() -> void:
	if restart_pending:
		return
	restart_pending = true
	lifecycle_token += 1
	hit_lock = true
	run_finishing = true
	Engine.time_scale = 1.0
	get_tree().paused = false
	GameState.request_checkpoint_respawn()
	call_deferred("_reload_scene_safely")

func _reload_scene_safely() -> void:
	Engine.time_scale = 1.0
	get_tree().paused = false
	get_tree().reload_current_scene()

func _back_from_overlay() -> void:
	Engine.time_scale = 1.0
	get_tree().paused = false
	GameState.clear_checkpoint()
	get_tree().quit()

func _juice(duration: float, time_scale: float, token: int) -> void:
	if token != lifecycle_token or restart_pending:
		return
	Engine.time_scale = time_scale
	await get_tree().create_timer(duration, true, false, true).timeout
	if token == lifecycle_token and not restart_pending:
		Engine.time_scale = 1.0

func _draw() -> void:
	var cam_x: float = player.position.x if is_instance_valid(player) else 640.0
	var season: int = posmod(steps / 500, 4)
	var source_x: float = float(season) * BACKGROUND_TILE_SIZE
	var first_x: float = floorf((cam_x - 1600.0) / BACKDROP_WIDTH) * BACKDROP_WIDTH
	var source_rect := Rect2(source_x, 0.0, BACKGROUND_TILE_SIZE, BACKGROUND_TILE_SIZE)
	# Keep the source tile orientation stable. Alternating draw transforms caused
	# visible seams/glitches while the camera moved and during scene reloads.
	for i in range(6):
		var x: float = first_x + float(i) * BACKDROP_WIDTH
		draw_texture_rect_region(BACKGROUNDS, Rect2(x, -40.0, BACKDROP_WIDTH, 720.0), source_rect)

	var first_visible_x: float = cam_x - 1700.0
	var last_visible_x: float = cam_x + 1900.0
	if not is_instance_valid(world):
		return
	for chunk_value in world.active_chunks.values():
		if not is_instance_valid(chunk_value):
			continue
		var chunk: MaryouChunk = chunk_value as MaryouChunk
		if chunk == null:
			continue
		for solid in chunk.solids:
			if solid.end.x < first_visible_x or solid.position.x > last_visible_x:
				continue
			draw_texture_rect_region(TILES, solid, Rect2(0.0, 0.0, 64.0, 64.0))
