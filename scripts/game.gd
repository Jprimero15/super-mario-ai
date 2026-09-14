extends Node2D

const PlayerScene := preload("res://scripts/player.gd")
const WorldScene := preload("res://scripts/world_generator.gd")
const HUDScene := preload("res://scripts/hud.gd")
const WORLD_HEIGHT := 720.0
const GROUND_Y := 560.0

var player: MaryouPlayer
var world: MaryouWorldGenerator
var enemies: Node2D
var hud: MaryouHUD
var steps := 0
var hit_lock := false
var touch_points: Dictionary = {}
var touch_jump_pressed := false

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_PAUSABLE
	ScoreManager.reset_run()
	enemies = Node2D.new()
	enemies.name = "Enemies"
	add_child(enemies)
	world = WorldScene.new()
	world.name = "WorldGenerator"
	world.setup(enemies)
	world.coin_collected.connect(_on_coin)
	world.shield_collected.connect(_on_shield)
	world.hazard_hit.connect(_on_hazard)
	add_child(world)

	player = PlayerScene.new()
	player.name = "Player"
	player.position = Vector2(180, GROUND_Y - 35)
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

func _process(delta: float) -> void:
	if Input.is_action_just_pressed("pause"):
		_toggle_pause()
		return
	var distance := maxi(0, int(player.position.x / 32.0))
	steps = maxi(steps, distance)
	ScoreManager.steps = steps
	var speed := MaryouDifficultyCurve.speed_for_steps(steps)
	var left := Input.is_action_pressed("move_left") or _touch_held(0)
	var right := Input.is_action_pressed("move_right") or _touch_held(1)
	var jump_held := Input.is_action_pressed("jump") or _touch_held(2)
	player.tick(delta, speed, left, right, touch_jump_pressed, jump_held)
	touch_jump_pressed = false
	world.generate_until(player.position.x, steps)
	_wire_enemies()
	if player.position.y > WORLD_HEIGHT + 80.0:
		_finish_run()
	hud.update_stats(ScoreManager.score(), ScoreManager.lives, ScoreManager.coins, ScoreManager.combo, MaryouDifficultyCurve.tier_for_steps(steps), int(player.position.x / 32.0))
	queue_redraw()

func _wire_enemies() -> void:
	for child in enemies.get_children():
		if not is_instance_valid(child) or not child is MaryouEnemy:
			continue
		var enemy := child as MaryouEnemy
		if enemy.has_meta("maryou_wired"):
			continue
		enemy.player_contact.connect(_on_enemy_contact)
		enemy.stomped.connect(_on_enemy_stomp)
		enemy.set_meta("maryou_wired", true)

func _on_coin() -> void:
	ScoreManager.add_coin()

func _on_shield() -> void:
	player.activate_shield()

func _on_hazard() -> void:
	_take_damage()

func _on_enemy_contact(_enemy: MaryouEnemy) -> void:
	_take_damage()

func _on_enemy_stomp(_enemy: MaryouEnemy) -> void:
	ScoreManager.add_stomp()
	_juice(0.06, 0.88)

func _take_damage() -> void:
	if hit_lock or ScoreManager.lives <= 0 or player.dead:
		return
	hit_lock = true
	if player.take_hit():
		var dead_now := ScoreManager.damage()
		if dead_now:
			_finish_run()
		else:
			player.position += Vector2(110.0, -60.0)
			player.velocity = Vector2(MaryouDifficultyCurve.speed_for_steps(steps) * 0.75, -360.0)
			_juice(0.08, 0.82)
	else:
		_juice(0.05, 0.9)
	await get_tree().create_timer(1.15, true, false, true).timeout
	hit_lock = false

func _finish_run() -> void:
	if player.dead:
		return
	ScoreManager.finish_run()
	player.kill()
	get_tree().paused = false
	hud.show_game_over(ScoreManager.score(), ScoreManager.best_score)

func _toggle_pause() -> void:
	if player.dead:
		return
	var value := not get_tree().paused
	get_tree().paused = value
	hud.show_pause(value, ScoreManager.score(), ScoreManager.best_score)

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
		var size := get_viewport_rect().size
		var p := event.position
		if event.pressed:
			var zone := 2
			if p.y >= size.y * 0.78:
				if p.x < size.x * 0.22:
					zone = 0
				elif p.x < size.x * 0.46:
					zone = 1
				touch_points[event.index] = zone
				if zone == 2:
					touch_jump_pressed = true
			else:
				touch_points[event.index] = -1
		else:
			touch_points.erase(event.index)
	elif event is InputEventScreenDrag and touch_points.has(event.index):
		var size := get_viewport_rect().size
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
	var cam_x := player.position.x if is_instance_valid(player) else 640.0
	draw_rect(Rect2(cam_x - 1500.0, -300.0, 3500.0, 1100.0), Color("#101827"))
	for i in range(10):
		var x := cam_x - 1100.0 + float(i) * 260.0
		var y := 350.0 + sin(float(i) * 0.9) * 28.0
		draw_circle(Vector2(x, y), 80.0, Color("#162238"))
