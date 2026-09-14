extends Node2D

const PlayerScene := preload("res://scripts/player.gd")
const EnemyScene := preload("res://scripts/enemy.gd")
const CHUNK_WIDTH := 640.0
const TILE := 32.0
const GROUND_Y := 560.0
const WORLD_HEIGHT := 720.0
const MAX_CHUNKS := 7
const SEED := 20260914

var player: MaryouPlayer
var world_root: Node2D
var enemy_root: Node2D
var ui_root: CanvasLayer
var score_label: Label
var status_label: Label
var tier_label: Label
var pause_panel: PanelContainer
var menu_panel: PanelContainer
var touch_hint: Label
var run_started := false
var paused := false
var game_over := false
var steps := 0
var coins := 0
var lives := 3
var combo := 0
var best_score := 0
var speed := 300.0
var generated_to := -1
var active_chunks: Dictionary = {}
var coin_rects: Array[Dictionary] = []
var pipe_rects: Array[Rect2] = []
var power_rects: Array[Rect2] = []
var rng := RandomNumberGenerator.new()
var touch_left := false
var touch_right := false
var touch_jump := false
var jump_was_pressed := false

func _ready() -> void:
	rng.seed = SEED
	world_root = Node2D.new()
	world_root.name = "World"
	add_child(world_root)
	enemy_root = Node2D.new()
	enemy_root.name = "Enemies"
	add_child(enemy_root)
	player = PlayerScene.new()
	player.name = "Player"
	player.position = Vector2(180, GROUND_Y - 35)
	add_child(player)
	build_ui()
	load_records()
	for chunk in range(-1, 4):
		generate_chunk(chunk)
	run_started = true
	queue_redraw()

func _process(delta: float) -> void:
	if Input.is_action_just_pressed("pause") and not game_over:
		set_paused(not paused)
	if paused:
		queue_redraw()
		return
	if game_over:
		queue_redraw()
		return

	speed = min(520.0, 300.0 + float(steps / 100) * 22.0)
	var jump_pressed := Input.is_action_pressed("jump") or touch_jump
	var left := Input.is_action_pressed("move_left") or touch_left
	var right := Input.is_action_pressed("move_right") or touch_right
	player.tick(delta, speed, left, right, jump_pressed)
	update_world()
	update_enemies(delta)
	resolve_collectibles()
	resolve_hazards()
	if player.position.x >= float((steps + 1) * 32):
		steps += 1
	if player.position.y > WORLD_HEIGHT + 80.0:
		lose_life(true)
	update_ui()
	queue_redraw()

func update_world() -> void:
	var target_chunk := int(floor(player.position.x / CHUNK_WIDTH)) + 3
	while generated_to < target_chunk:
		generated_to += 1
		generate_chunk(generated_to)
	var prune_before := player.position.x - CHUNK_WIDTH * 5.0
	for key in active_chunks.keys().duplicate():
		if float(key) * CHUNK_WIDTH + CHUNK_WIDTH < prune_before:
			active_chunks[key].queue_free()
			active_chunks.erase(key)
	coin_rects = coin_rects.filter(func(item): return item.rect.end.x >= prune_before)
	pipe_rects = pipe_rects.filter(func(rect): return rect.end.x >= prune_before)
	power_rects = power_rects.filter(func(rect): return rect.end.x >= prune_before)

func generate_chunk(chunk: int) -> void:
	if active_chunks.has(chunk):
		return
	var root := Node2D.new()
	root.name = "Chunk_%d" % chunk
	world_root.add_child(root)
	active_chunks[chunk] = root
	var local_rng := RandomNumberGenerator.new()
	local_rng.seed = SEED + chunk * 7919
	var start_x := float(chunk) * CHUNK_WIDTH
	var occupied: Array[Rect2] = []
	var holes: Array[Rect2] = []
	for i in range(20):
		if local_rng.randf() < min(0.18, 0.08 + max(chunk, 0) * 0.0004):
			var hx := start_x + float(i * 32 + 320)
			var hw := float(local_rng.randi_range(32, 64))
			holes.append(Rect2(hx, GROUND_Y, hw, TILE))
	for x_i in range(20):
		var x := start_x + float(x_i) * TILE
		var blocked := false
		for hole in holes:
			if Rect2(x, GROUND_Y, TILE, TILE).intersects(hole):
				blocked = true
				break
		if not blocked:
			add_solid(root, Rect2(x, GROUND_Y, TILE, TILE))

	var pipe_count := local_rng.randi_range(0, 2 if chunk > 1 else 1)
	for i in range(pipe_count):
		var px := start_x + float(local_rng.randi_range(5, 18)) * TILE
		var h := float(local_rng.randi_range(2, 3)) * TILE
		var pipe := Rect2(px, GROUND_Y - h, TILE, h)
		if is_safe_rect(pipe, occupied, holes, 28.0):
			occupied.append(pipe)
			pipe_rects.append(pipe)
			add_solid(root, pipe)

	for i in range(9):
		var cx := start_x + float(2 + i * 3) * TILE
		var cy := GROUND_Y - float(local_rng.randi_range(80, 200))
		var coin := Rect2(cx, cy, 20, 20)
		if is_safe_rect(coin, occupied, holes, 8.0):
			occupied.append(coin)
			coin_rects.append({"rect": coin, "taken": false})

	if chunk >= 3 and local_rng.randf() < 0.12:
		var power := Rect2(start_x + float(local_rng.randi_range(15, 22)) * TILE, GROUND_Y - 110, 26, 26)
		if is_safe_rect(power, occupied, holes, 18.0):
			power_rects.append(power)

	var monster_count := 0 if chunk < 2 else local_rng.randi_range(1, 2)
	for i in range(monster_count):
		var mx := start_x + float(local_rng.randi_range(7, 25)) * TILE
		var m := MaryouEnemy.new()
		var tier := 0 if steps < 200 else (1 if steps < 400 else 2)
		m.position = Vector2(mx, GROUND_Y - 48)
		m.setup(tier)
		enemy_root.add_child(m)

func add_solid(parent: Node2D, rect: Rect2) -> void:
	var body := StaticBody2D.new()
	var shape := RectangleShape2D.new()
	shape.size = rect.size
	var collider := CollisionShape2D.new()
	collider.shape = shape
	collider.position = rect.size * 0.5
	body.position = rect.position
	body.add_child(collider)
	parent.add_child(body)

func is_safe_rect(rect: Rect2, occupied: Array[Rect2], holes: Array[Rect2], padding: float) -> bool:
	for hole in holes:
		if rect.grow(padding).intersects(hole):
			return false
	for other in occupied:
		if rect.grow(padding).intersects(other):
			return false
	return true

func update_enemies(delta: float) -> void:
	for enemy in enemy_root.get_children():
		if not is_instance_valid(enemy):
			continue
		enemy.tick(delta, player.position.x)
		if enemy.position.x < player.position.x - 900.0 or enemy.position.y > WORLD_HEIGHT + 200.0:
			enemy.queue_free()
			continue
		if not enemy.defeated and player.get_rect().intersects(Rect2(enemy.position - Vector2(24,24), Vector2(48,48))):
			if player.velocity.y > 50.0 and player.position.y < enemy.position.y:
				enemy.defeat()
			combo += 1
			player.velocity.y = -400.0
			steps += 2
			continue
			lose_life(false)

func resolve_collectibles() -> void:
	for item in coin_rects:
		if item.taken:
			continue
		if player.position.distance_to(item.rect.get_center()) < 34.0:
			item.taken = true
			coins += 1
			combo += 1
	for power in power_rects.duplicate():
		if player.position.distance_to(power.get_center()) < 36.0:
			player.activate_shield()
			power_rects.erase(power)

func resolve_hazards() -> void:
	for pipe in pipe_rects:
		var expanded := pipe.grow(3.0)
		if player.get_rect().intersects(expanded):
			var player_bottom := player.position.y + 27.0
			if player.velocity.y > 40.0 and player_bottom <= pipe.position.y + 14.0:
				player.position.y = pipe.position.y - 28.0
				player.velocity.y = 0.0
			else:
				lose_life(false)
				return

func lose_life(fell: bool) -> void:
	if game_over:
		return
	if player.take_hit():
		lives -= 1
		combo = 0
		if lives <= 0 or fell:
			finish_run()
		else:
			player.position.y = GROUND_Y - 80.0
			player.velocity = Vector2(speed * 0.7, -360.0)

func finish_run() -> void:
	game_over = true
	player.kill()
	best_score = max(best_score, current_score())
	save_records()
	show_menu(true)

func current_score() -> int:
	return steps * 5 + coins * 25 + combo * 10

func load_records() -> void:
	var cfg := ConfigFile.new()
	if cfg.load("user://records.cfg") == OK:
		best_score = int(cfg.get_value("records", "best_score", 0))

func save_records() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("records", "best_score", best_score)
	cfg.save("user://records.cfg")

func build_ui() -> void:
	ui_root = CanvasLayer.new()
	add_child(ui_root)
	var top := HBoxContainer.new()
	top.position = Vector2(28, 22)
	top.add_theme_constant_override("separation", 12)
	ui_root.add_child(top)
	score_label = make_stat("SCORE 0")
	status_label = make_stat("♥ 3   ◈ 0   STEP 0")
	top.add_child(score_label)
	top.add_child(status_label)
	tier_label = make_stat("TIER 1")
	tier_label.position = Vector2(1040, 28)
	ui_root.add_child(tier_label)

	var pause := Button.new()
	pause.text = "Ⅱ"
	pause.position = Vector2(1190, 20)
	pause.size = Vector2(60, 52)
	pause.pressed.connect(func(): set_paused(not paused))
	ui_root.add_child(pause)

	touch_hint = make_stat("←     →          JUMP")
	touch_hint.position = Vector2(470, 648)
	ui_root.add_child(touch_hint)

	pause_panel = make_panel("PAUSED", "Resume", "Restart", "Menu")
	pause_panel.visible = false
	ui_root.add_child(pause_panel)
	menu_panel = make_panel("RUN COMPLETE", "Restart", "Back")
	menu_panel.visible = false
	ui_root.add_child(menu_panel)

func make_stat(text: String) -> Label:
	var label := Label.new()
	label.text = text
	label.add_theme_font_size_override("font_size", 22)
	label.add_theme_color_override("font_color", Color("#f4f6fb"))
	return label

func make_panel(title: String, primary: String, secondary: String, tertiary := "") -> PanelContainer:
	var panel := PanelContainer.new()
	panel.position = Vector2(430, 215)
	panel.size = Vector2(420, 290)
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 18)
	panel.add_child(box)
	var heading := Label.new()
	heading.text = title
	heading.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	heading.add_theme_font_size_override("font_size", 34)
	box.add_child(heading)
	var info := Label.new()
	info.text = "Keep the run moving.\nAvoid the edges and use the shield wisely."
	info.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	info.add_theme_font_size_override("font_size", 17)
	box.add_child(info)
	for label in [primary, secondary, tertiary]:
		if label == "":
			continue
		var b := Button.new()
		b.text = label
		b.custom_minimum_size = Vector2(0, 52)
		box.add_child(b)
		if title == "PAUSED":
			if label == "Resume": b.pressed.connect(func(): set_paused(false))
			elif label == "Restart": b.pressed.connect(restart_run)
			elif label == "Menu": b.pressed.connect(func(): show_menu(true))
		else:
			if label == "Restart": b.pressed.connect(restart_run)
			else: b.pressed.connect(func(): show_menu(false))
	return panel

func set_paused(value: bool) -> void:
	if game_over:
		return
	paused = value
	pause_panel.visible = paused

func show_menu(value: bool) -> void:
	menu_panel.visible = value

func restart_run() -> void:
	get_tree().reload_current_scene()

func update_ui() -> void:
	score_label.text = "SCORE %d" % current_score()
	status_label.text = "♥ %d   ◈ %d   STEP %d   ×%d" % [lives, coins, steps, combo]
	var tier := 1 if steps < 200 else (2 if steps < 400 else 3)
	tier_label.text = "TIER %d   %d m" % [tier, steps]

func _input(event: InputEvent) -> void:
	if event is InputEventScreenTouch:
		var p := event.position
		if event.pressed:
			if p.y > 590.0 and p.x < 180.0: touch_left = true
			elif p.y > 590.0 and p.x > 180.0 and p.x < 360.0: touch_right = true
			elif p.y > 590.0 and p.x >= 360.0: touch_jump = true
		else:
			touch_left = false
			touch_right = false
			touch_jump = false
	elif event is InputEventScreenDrag:
		var p := event.position
		touch_left = p.y > 590.0 and p.x < 180.0
		touch_right = p.y > 590.0 and p.x >= 180.0 and p.x < 360.0
		touch_jump = p.y > 590.0 and p.x >= 360.0

func _draw() -> void:
	var cam_x := player.position.x if is_instance_valid(player) else 0.0
	var sky := Rect2(cam_x - 900, -100, 2200, 800)
	draw_rect(sky, Color("#101827"))
	for i in range(8):
		var mx := cam_x - 700 + i * 220
		var my := 365 + int(sin(float(i)) * 26.0)
		draw_colored_polygon(PackedVector2Array([Vector2(mx, my + 120), Vector2(mx + 90, my), Vector2(mx + 190, my + 120)]), Color("#17243a"))
	for coin in coin_rects:
		if coin.taken: continue
		var r: Rect2 = coin.rect
		draw_circle(r.get_center(), 10.0, Color("#ffd76a"))
		draw_circle(r.get_center(), 5.0, Color("#fff0a3"))
	for hole in active_holes():
		draw_rect(hole, Color("#070b12"))
		draw_rect(Rect2(hole.position.x, GROUND_Y + TILE - 6, hole.size.x, 6), Color("#03050a"))
	for pipe in pipe_rects:
		draw_rect(pipe, Color("#35b87a"), true)
		draw_rect(Rect2(pipe.position.x - 5, pipe.position.y, pipe.size.x + 10, 16), Color("#5ed49a"), true)
	for power in power_rects:
		draw_circle(power.get_center(), 14, Color("#68d8d1"))
		draw_circle(power.get_center(), 8, Color("#d8ffff"))

func active_holes() -> Array[Rect2]:
	var result: Array[Rect2] = []
	for chunk in active_chunks.keys():
		var start_x := float(chunk) * CHUNK_WIDTH
		var local_rng := RandomNumberGenerator.new()
		local_rng.seed = SEED + chunk * 7919
		for i in range(20):
			if local_rng.randf() < min(0.18, 0.08 + max(chunk, 0) * 0.0004):
				result.append(Rect2(start_x + float(i * 32 + 320), GROUND_Y, float(local_rng.randi_range(32,64)), TILE))
	return result
