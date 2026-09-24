extends Node2D
class_name MaryouGame

const PlayerScene = preload("res://scripts/player.gd")
const WorldScene = preload("res://scripts/world_generator.gd")
const HUDScene = preload("res://scripts/hud.gd")
const TILES := preload("res://assets/world/tiles.svg")
const FAR_TEXTURE := preload("res://assets/world/parallax/far.png")
const MID_TEXTURE := preload("res://assets/world/parallax/middle.png")
const BACK_TEXTURE := preload("res://assets/world/parallax/back.png")

const WORLD_HEIGHT: float = 720.0
const GROUND_Y: float = 560.0
const GROUND_TILE_SIZE: float = 32.0

# The source art is intentionally small. Draw it larger as a single shared
# visual band so the three layers stay compact and aligned.
const BACKGROUND_HEIGHT: float = 360.0
const BACK_PARALLAX: float = 0.10
const FAR_PARALLAX: float = 0.20
const MID_PARALLAX: float = 0.32

var player: MaryouPlayer
var world: MaryouWorldGenerator
var enemies: Node2D
var hud: MaryouHUD
var steps: int = 0
var hit_lock: bool = false
var run_finishing: bool = false
var restart_pending: bool = false
var lifecycle_token: int = 0
var background_time: float = 0.0
var last_milestone := 0

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
	_check_milestone()
	_wire_enemies()
	hud.update_stats(steps, ScoreManager.coins, MaryouDifficultyCurve.tier_for_steps(steps), player.shielded, ScoreManager.best_steps)
	queue_redraw()

func _process(delta: float) -> void:
	# Keep ambient background motion smooth without forcing the gameplay
	# generator/physics to redraw just because a decorative particle moved.
	background_time = fmod(background_time + delta, 10000.0)
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
	hud.update_stats(steps, ScoreManager.coins, MaryouDifficultyCurve.tier_for_steps(steps), player.shielded, ScoreManager.best_steps)

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

func _check_milestone() -> void:
	var milestone := (steps / 500) * 500
	if milestone >= 500 and milestone > last_milestone:
		last_milestone = milestone
		AudioManager.play_sfx("milestone")
		if is_instance_valid(hud):
			hud.show_milestone(milestone)

func _on_coin() -> void:
	ScoreManager.add_coin()
	if is_instance_valid(player):
		player.coin_burst()

func _on_hazard(_player: MaryouPlayer) -> void:
	if restart_pending or run_finishing:
		return
	_finish_run(0.86)

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
		_finish_run(0.90)
	else:
		_juice(0.05, 0.9, lifecycle_token)
	var token := lifecycle_token
	await get_tree().create_timer(0.35, true, false, true).timeout
	if token != lifecycle_token or restart_pending or run_finishing:
		return
	hit_lock = false

func _finish_run(slowdown: float = 1.0) -> void:
	if restart_pending or run_finishing or not is_instance_valid(player) or player.dead or not ScoreManager.run_active:
		return
	run_finishing = true
	var token := lifecycle_token
	ScoreManager.finish_run()
	player.kill()
	get_tree().paused = false
	Engine.time_scale = slowdown
	if is_instance_valid(player.camera):
		var tween := create_tween().set_parallel(true)
		tween.tween_property(player.camera, "zoom", Vector2(0.98, 0.98), 0.22).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	await get_tree().create_timer(0.14, true, false, true).timeout
	if token != lifecycle_token or restart_pending:
		return
	Engine.time_scale = 1.0
	if is_instance_valid(hud):
		hud.show_game_over(ScoreManager.steps, ScoreManager.best_steps, ScoreManager.new_best, ScoreManager.coins, ScoreManager.total_coins)

func _toggle_pause() -> void:
	if restart_pending or run_finishing or not is_instance_valid(player) or player.dead:
		return
	var value: bool = not get_tree().paused
	get_tree().paused = value
	if is_instance_valid(hud):
		hud.show_pause(value, ScoreManager.steps, ScoreManager.best_steps)

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
	var cam_x: float = 640.0
	var cam_y: float = 500.0
	var camera_zoom := Vector2.ONE
	if is_instance_valid(player) and is_instance_valid(player.camera):
		cam_x = player.camera.global_position.x
		cam_y = player.camera.global_position.y
		camera_zoom = player.camera.zoom
	elif is_instance_valid(player):
		cam_x = player.global_position.x

	# Full world-space backdrop. This removes the large empty band visible in
	# the old screenshot while leaving the actual platform/obstacle art on top.
	var world_view_width := get_viewport_rect().size.x / maxf(camera_zoom.x, 0.01)
	var world_view_height := get_viewport_rect().size.y / maxf(camera_zoom.y, 0.01)
	draw_rect(
		Rect2(cam_x - world_view_width * 1.5, cam_y - world_view_height * 1.5, world_view_width * 3.0, world_view_height * 3.0),
		Color("#9ad65d")
	)

	# Soft atmospheric bands behind the forest.
	var tier := MaryouDifficultyCurve.tier_for_steps(ScoreManager.steps)
	var sky_color := Color("#bce87b") if tier < 4 else Color("#a6c66c")
	draw_rect(
		Rect2(cam_x - world_view_width * 1.5, GROUND_Y - BACKGROUND_HEIGHT - 80.0, world_view_width * 3.0, 90.0),
		sky_color
	)
	draw_rect(
		Rect2(cam_x - world_view_width * 1.5, GROUND_Y - 80.0, world_view_width * 3.0, 80.0),
		Color("#6da94a")
	)

	# Farthest -> nearest, matching the intended Back -> Far -> Middle stack.
	_draw_parallax_layer(BACK_TEXTURE, cam_x, BACK_PARALLAX, Color(0.76, 0.86, 0.58, 1.0))
	_draw_parallax_layer(FAR_TEXTURE, cam_x, FAR_PARALLAX, Color(0.88, 0.96, 0.66, 1.0))
	_draw_parallax_layer(MID_TEXTURE, cam_x, MID_PARALLAX, Color.WHITE)

	# Speed-sensitive atmosphere makes the increasing pace visible without extra nodes.
	if is_instance_valid(player):
		var speed_ratio := clampf((absf(player.velocity.x) - 300.0) / 240.0, 0.0, 1.0)
		if speed_ratio > 0.15:
			for i in range(6):
				var fi := float(i)
				var sx := cam_x + 300.0 + fposmod(fi * 97.0 + background_time * (110.0 + speed_ratio * 180.0), 420.0)
				var sy := GROUND_Y - 90.0 - fi * 36.0
				draw_line(Vector2(sx, sy), Vector2(sx - (18.0 + speed_ratio * 28.0), sy), Color(1.0, 1.0, 1.0, 0.12 + speed_ratio * 0.10), 2.0)

	# A proper foreground/soil band makes the play surface read as a world
	# instead of a floating strip above the dark screen area.
	draw_rect(
		Rect2(cam_x - world_view_width * 1.5, GROUND_Y, world_view_width * 3.0, world_view_height * 1.5),
		Color("#243b2b")
	)
	draw_rect(
		Rect2(cam_x - world_view_width * 1.5, GROUND_Y, world_view_width * 3.0, 7.0),
		Color("#5f9c49")
	)
	draw_line(
		Vector2(cam_x - world_view_width * 1.5, GROUND_Y + 8.0),
		Vector2(cam_x + world_view_width * 1.5, GROUND_Y + 8.0),
		Color("#365b36"),
		2.0
	)

	# Lightweight animated atmosphere: deterministic fireflies and drifting
	# leaves. They are drawn in world space, so they remain stable with camera
	# movement and add life without spawning dozens of Nodes.
	for i in range(18):
		var fi := float(i)
		var x := cam_x - world_view_width * 0.5 + fposmod(fi * 137.0 + background_time * (8.0 + fmod(fi, 3.0) * 4.0), world_view_width)
		var y := GROUND_Y - 105.0 - fmod(fi * 41.0 + sin(background_time * 0.7 + fi) * 16.0, 250.0)
		var pulse := 0.45 + 0.35 * sin(background_time * 2.2 + fi)
		draw_circle(Vector2(x, y), 2.0, Color(1.0, 0.93, 0.43, pulse))

	for i in range(10):
		var fi := float(i)
		var x := cam_x - world_view_width * 0.5 + fposmod(fi * 191.0 + background_time * (14.0 + fi * 0.8), world_view_width)
		var y := GROUND_Y - 35.0 - fmod(fi * 53.0 + background_time * (7.0 + fi * 0.3), 300.0)
		var drift := sin(background_time * 1.4 + fi) * 10.0
		draw_line(Vector2(x, y), Vector2(x + 7.0 + drift, y + 3.0), Color(0.42, 0.72, 0.30, 0.70), 2.0)

func _draw_parallax_layer(texture: Texture2D, cam_x: float, parallax: float, tint: Color) -> void:
	if texture == null:
		return
	var source_size := texture.get_size()
	if source_size.x <= 0.0 or source_size.y <= 0.0:
		return

	var scale_factor := BACKGROUND_HEIGHT / source_size.y
	var draw_size := source_size * scale_factor
	var phase := cam_x * parallax
	var offset := fposmod(phase, draw_size.x)
	var first_x := cam_x * (1.0 - parallax) - offset - draw_size.x

	var camera_zoom := Vector2.ONE
	if is_instance_valid(player) and is_instance_valid(player.camera):
		camera_zoom = player.camera.zoom
	var visible_world_width := get_viewport_rect().size.x / maxf(camera_zoom.x, 0.01)
	var copies := int(ceil(visible_world_width / draw_size.x)) + 4

	for i in range(copies):
		var x := first_x + float(i) * draw_size.x
		var rect := Rect2(x, GROUND_Y - BACKGROUND_HEIGHT, draw_size.x, BACKGROUND_HEIGHT)
		draw_texture_rect(texture, rect, false, tint)
