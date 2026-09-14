extends Node2D

const PlayerScene = preload("res://scripts/player.gd")
const WorldScene = preload("res://scripts/world_generator.gd")
const HUDScene = preload("res://scripts/hud.gd")
const BACKGROUNDS := preload("res://assets/world/backgrounds.svg")
const TILES := preload("res://assets/world/tiles.svg")
const WORLD_HEIGHT: float = 720.0
const GROUND_Y: float = 560.0
const VIEW_WIDTH: float = 1280.0
const BACKDROP_WIDTH: float = 720.0

var player: MaryouPlayer
var world: MaryouWorldGenerator
var enemies: Node2D
var hud: MaryouHUD
var steps: int = 0
var hit_lock: bool = false
var touch_points: Dictionary = {}
var touch_jump_pressed: bool = false

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_PAUSABLE
	if OS.has_feature("android"):
		DisplayServer.screen_set_orientation(DisplayServer.SCREEN_SENSOR_LANDSCAPE)
	ScoreManager.reset_run()
	enemies = Node2D.new()
	enemies.name = "Enemies"
	add_child(enemies)
	world = WorldScene.new()
	world.name = "WorldGenerator"
	add_child(world)
	world.setup(enemies)
	world.coin_collected.connect(_on_coin)
	world.shield_collected.connect(_on_shield)
	world.hazard_hit.connect(_on_hazard)
	player = PlayerScene.new()
	player.name = "Player"
	player.position = Vector2(180, GROUND_Y - 35.0)
	add_child(player)
	hud = HUDScene.new()
	hud.name = "HUD"
	hud.pause_pressed.connect(_toggle_pause)
	hud.restart_pressed.connect(_restart)
	hud.back_pressed.connect(_back_from_overlay)
	add_child(hud)
	world.generate_until(player.position.x, 0)
	_wire_enemies()
	queue_redraw()

func _physics_process(delta: float) -> void:
	if not is_instance_valid(player) or not is_instance_valid(world) or not is_instance_valid(hud):
		return
	if Input.is_action_just_pressed("pause"):
		_toggle_pause()
		return

	var distance: int = maxi(0, int(player.position.x / 32.0))
	steps = maxi(steps, distance)
	ScoreManager.steps = steps
	var speed: float = MaryouDifficultyCurve.speed_for_steps(steps)
	var left: bool = Input.is_action_pressed("move_left") or _touch_held(0)
	var right: bool = Input.is_action_pressed("move_right") or _touch_held(1)
	var jump_held: bool = Input.is_action_pressed("jump") or _touch_held(2)
	var jump_pressed: bool = Input.is_action_just_pressed("jump") or touch_jump_pressed
	if jump_pressed and has_node("/root/AudioManager"):
		AudioManager.play_sfx("jump")
	player.tick(delta, speed, left, right, jump_pressed, jump_held)
	touch_jump_pressed = false

	world.generate_until(player.position.x, steps)
	_wire_enemies()
	for child in enemies.get_children():
		if is_instance_valid(child) and child is MaryouEnemy:
			var enemy: MaryouEnemy = child as MaryouEnemy
			enemy.tick(delta, player.position.x)

	if player.position.y > WORLD_HEIGHT + 80.0:
		_finish_run()
	hud.update_stats(ScoreManager.score(), 1, ScoreManager.coins, ScoreManager.multiplier(), MaryouDifficultyCurve.tier_for_steps(steps), int(player.position.x / 32.0))
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

func _on_shield() -> void:
	if is_instance_valid(player):
		player.activate_shield()
		if has_node("/root/AudioManager"):
			AudioManager.play_sfx("shield")

func _on_hazard() -> void:
	_take_damage()

func _on_enemy_contact(_enemy: MaryouEnemy) -> void:
	_take_damage()

func _on_enemy_stomp(_enemy: MaryouEnemy) -> void:
	ScoreManager.add_stomp()
	if is_instance_valid(player):
		player.shake(5.0, 0.10)
	_juice(0.06, 0.88)

func _take_damage() -> void:
	if hit_lock or not ScoreManager.run_active or not is_instance_valid(player) or player.dead:
		return
	hit_lock = true
	if player.take_hit():
		_finish_run()
	else:
		# A shield still protects the run from one hit.
		_juice(0.05, 0.9)
	await get_tree().create_timer(0.35, true, false, true).timeout
	hit_lock = false

func _finish_run() -> void:
	if not is_instance_valid(player) or player.dead:
		return
	ScoreManager.finish_run()
	player.kill()
	get_tree().paused = false
	if is_instance_valid(hud):
		hud.show_game_over(ScoreManager.score(), ScoreManager.best_score)

func _toggle_pause() -> void:
	if not is_instance_valid(player) or player.dead:
		return
	var value: bool = not get_tree().paused
	get_tree().paused = value
	if is_instance_valid(hud): hud.show_pause(value, ScoreManager.score(), ScoreManager.best_score)

func _restart() -> void:
	get_tree().paused = false
	get_tree().reload_current_scene()

func _back_from_overlay() -> void:
	get_tree().paused = false
	get_tree().quit()

func _touch_held(zone: int) -> bool:
	for value in touch_points.values():
		if int(value) == zone:
			return true
	return false

func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		var size: Vector2 = get_viewport_rect().size
		var p: Vector2 = event.position
		if event.pressed:
			var zone: int = 2
			if p.y >= size.y * 0.78:
				if p.x < size.x * 0.22:
					zone = 0
				elif p.x < size.x * 0.46:
					zone = 1
				touch_points[event.index] = zone
				if zone == 2:
					touch_jump_pressed = true
		else:
			touch_points.erase(event.index)
	elif event is InputEventScreenDrag and touch_points.has(event.index):
		var size: Vector2 = get_viewport_rect().size
		if event.position.y >= size.y * 0.78:
			if event.position.x < size.x * 0.22:
				touch_points[event.index] = 0
			elif event.position.x < size.x * 0.46:
				touch_points[event.index] = 1
			else:
				touch_points[event.index] = 2

func _juice(duration: float, time_scale: float) -> void:
	Engine.time_scale = time_scale
	await get_tree().create_timer(duration, true, false, true).timeout
	Engine.time_scale = 1.0

func _draw() -> void:
	var cam_x: float = player.position.x if is_instance_valid(player) else 640.0
	var biome: int = mini(steps / 500, 2)
	var source_x: float = float(biome) * 256.0
	var first_x: float = floorf((cam_x - 1600.0) / BACKDROP_WIDTH) * BACKDROP_WIDTH
	for i in range(6):
		var x: float = first_x + float(i) * BACKDROP_WIDTH
		draw_texture_rect_region(BACKGROUNDS, Rect2(x, -40.0, BACKDROP_WIDTH, 720.0), Rect2(source_x, 0.0, 256.0, 256.0))
	var ground_start: float = floorf((cam_x - 1700.0) / 32.0) * 32.0
	for i in range(108):
		var x: float = ground_start + float(i) * 32.0
		draw_texture_rect_region(TILES, Rect2(x, GROUND_Y + 32.0, 32.0, 32.0), Rect2(0.0, 0.0, 64.0, 64.0))
		draw_texture_rect_region(TILES, Rect2(x, GROUND_Y, 32.0, 32.0), Rect2(0.0, 0.0, 64.0, 32.0))
