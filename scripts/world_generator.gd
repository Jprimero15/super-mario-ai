extends Node2D
class_name MaryouWorldGenerator

signal coin_collected
signal shield_collected
signal hazard_hit

const CHUNK_WIDTH := 640.0
const TILE := 32.0
const GROUND_Y := 560.0
const SEED := 20260914
const ChunkScript := preload("res://scripts/chunk.gd")
const CollectibleScript := preload("res://scripts/collectible.gd")
const HazardScript := preload("res://scripts/hazard.gd")
const EnemyScript := preload("res://scripts/enemy.gd")
const PipeTexture := preload("res://assets/sprites/pipe.svg")
const ENVIRONMENT := preload("res://assets/world/environment.svg")
const OBSTACLES := preload("res://assets/world/obstacles.svg")

var generated_to := -1
var active_chunks: Dictionary = {}
var enemy_root: Node2D

func setup(enemies: Node2D) -> void:
	enemy_root = enemies

func generate_until(player_x: float, distance_steps: int) -> void:
	var target := int(floor(player_x / CHUNK_WIDTH)) + 3
	while generated_to < target:
		generated_to += 1
		_generate_chunk(generated_to, distance_steps)
	_prune(player_x)

func _generate_chunk(chunk_index: int, distance_steps: int) -> void:
	if active_chunks.has(chunk_index): return
	var root := ChunkScript.new()
	root.name = "Chunk_%d" % chunk_index
	add_child(root)
	active_chunks[chunk_index] = root
	var rng := RandomNumberGenerator.new()
	rng.seed = SEED + chunk_index * 7919
	var start_x := float(chunk_index) * CHUNK_WIDTH
	var occupied: Array[Rect2] = []
	var holes: Array[Rect2] = []
	var hole_chance := MaryouDifficultyCurve.hole_chance(distance_steps)
	var safe_gap_until := start_x + 300.0
	for i in range(20):
		var x := start_x + float(i) * TILE
		if x < safe_gap_until: continue
		if i > 7 and i < 19 and rng.randf() < hole_chance:
			var width := float(rng.randi_range(32, 64))
			var hole := Rect2(x, GROUND_Y, width, TILE)
			holes.append(hole)
			safe_gap_until = hole.end.x + 96.0
	for i in range(20):
		var tile := Rect2(start_x + float(i) * TILE, GROUND_Y, TILE, TILE)
		var blocked := false
		for hole in holes:
			if tile.intersects(hole): blocked = true; break
		if not blocked:
			_add_solid(root, tile)
			root.solids.append(tile)
	root.holes = holes
	for hole in holes:
		_add_hole_warning(root, hole)

	if chunk_index > 0:
		var decor_count := rng.randi_range(2, 4)
		for i in range(decor_count):
			var decor_x := start_x + float(rng.randi_range(2, 19)) * TILE
			if rng.randf() < 0.58:
				_add_environment(root, decor_x, 0)
			else:
				_add_environment(root, decor_x, 160)

	var pipe_attempts := 1 + int(distance_steps / 500)
	var pipes_placed := 0
	for i in range(pipe_attempts):
		if rng.randf() > MaryouDifficultyCurve.pipe_chance(distance_steps): continue
		var pipe := Rect2(start_x + float(rng.randi_range(9, 18)) * TILE, GROUND_Y - float(rng.randi_range(2, 3)) * TILE, TILE, float(rng.randi_range(2, 3)) * TILE)
		if pipe.position.x < safe_gap_until: continue
		if _safe(pipe, occupied, holes, 42.0):
			occupied.append(pipe)
			root.pipes.append(pipe)
			_add_solid(root, pipe)
			_add_hazard(root, pipe)
			pipes_placed += 1
	if chunk_index > 0 and pipes_placed == 0 and rng.randf() < 0.78:
		var fallback := Rect2(start_x + float(rng.randi_range(13, 17)) * TILE, GROUND_Y - 64.0, TILE, 64.0)
		if _safe(fallback, occupied, holes, 42.0):
			occupied.append(fallback)
			root.pipes.append(fallback)
			_add_solid(root, fallback)
			_add_hazard(root, fallback)

	var coin_count := rng.randi_range(2, 4)
	for i in range(coin_count):
		var coin_pos := Vector2(start_x + float(rng.randi_range(7, 18)) * TILE, GROUND_Y - float(rng.randi_range(92, 190)))
		if _safe(Rect2(coin_pos - Vector2(15, 15), Vector2(30, 30)), occupied, holes, 10.0): _add_collectible(root, coin_pos, "coin")
	if distance_steps >= 650 and rng.randf() < 0.22:
		var power_pos := Vector2(start_x + float(rng.randi_range(14, 19)) * TILE, GROUND_Y - 120.0)
		if _safe(Rect2(power_pos - Vector2(15, 15), Vector2(30, 30)), occupied, holes, 10.0): _add_collectible(root, power_pos, "shield")

	var count := MaryouDifficultyCurve.enemy_count(distance_steps)
	if chunk_index == 0: count = 0
	for i in range(count):
		for attempt in range(8):
			var enemy_x := start_x + float(rng.randi_range(11, 18)) * TILE
			var enemy_rect := Rect2(enemy_x - 18.0, GROUND_Y - 58.0, 36.0, 58.0)
			if _safe(enemy_rect, occupied, holes, 48.0):
				var enemy := EnemyScript.new()
				enemy.position = Vector2(enemy_x, GROUND_Y - 40.0)
				enemy.setup(MaryouDifficultyCurve.enemy_kind(distance_steps, i))
				enemy_root.add_child(enemy)
				occupied.append(enemy_rect)
				break

func _add_solid(parent: Node2D, rect: Rect2) -> void:
	var body := StaticBody2D.new()
	body.collision_layer = 2
	body.collision_mask = 1
	body.position = rect.position + rect.size * 0.5
	var shape := RectangleShape2D.new()
	shape.size = rect.size
	var collider := CollisionShape2D.new()
	collider.shape = shape
	body.add_child(collider)
	if rect.position.y < GROUND_Y and rect.size.y > TILE:
		var pipe_sprite := Sprite2D.new()
		pipe_sprite.texture = PipeTexture
		pipe_sprite.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
		pipe_sprite.scale = Vector2(rect.size.x / 32.0, rect.size.y / 96.0)
		body.add_child(pipe_sprite)
	parent.add_child(body)

func _add_hole_warning(parent: Node2D, hole: Rect2) -> void:
	var sprite := Sprite2D.new()
	sprite.texture = OBSTACLES
	sprite.region_enabled = true
	sprite.region_rect = Rect2(200.0, 8.0, 58.0, 60.0)
	sprite.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	sprite.position = Vector2(hole.position.x + hole.size.x * 0.5, GROUND_Y - 18.0)
	sprite.scale = Vector2(hole.size.x / 58.0, 0.72)
	parent.add_child(sprite)

func _add_environment(parent: Node2D, x: float, source_y: float) -> void:
	var sprite := Sprite2D.new()
	sprite.texture = ENVIRONMENT
	sprite.region_enabled = true
	if source_y == 0.0:
		if int(x / TILE) % 3 == 0:
			sprite.region_rect = Rect2(0.0, 0.0, 88.0, 142.0)
		else:
			sprite.region_rect = Rect2(160.0, 0.0, 96.0, 126.0)
		sprite.position = Vector2(x, GROUND_Y - 63.0)
	else:
		sprite.region_rect = Rect2(256.0, 0.0, 104.0, 126.0)
		sprite.position = Vector2(x, GROUND_Y - 63.0)
	sprite.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	sprite.scale = Vector2(0.78, 0.78)
	sprite.z_index = 1
	parent.add_child(sprite)

func _add_hazard(parent: Node2D, pipe: Rect2) -> void:
	var area := HazardScript.new()
	var hazard_height := maxf(10.0, pipe.size.y - 20.0)
	area.position = pipe.position + Vector2(pipe.size.x * 0.5, pipe.size.y * 0.5 + 10.0)
	area.setup(Vector2(pipe.size.x + 8.0, hazard_height))
	area.hit_player.connect(func(): hazard_hit.emit())
	parent.add_child(area)

func _add_collectible(parent: Node2D, position: Vector2, kind: String) -> void:
	var item := CollectibleScript.new()
	item.position = position
	item.setup(kind)
	item.collected.connect(_on_collectible)
	parent.add_child(item)

func _on_collectible(kind: String) -> void:
	if kind == "shield": shield_collected.emit()
	else: coin_collected.emit()

func _safe(rect: Rect2, occupied: Array[Rect2], holes: Array[Rect2], padding: float) -> bool:
	var expanded := rect.grow(padding)
	for hole in holes:
		if expanded.intersects(hole): return false
	for other in occupied:
		if expanded.intersects(other): return false
	return true

func _prune(player_x: float) -> void:
	var prune_before := player_x - CHUNK_WIDTH * 5.0
	for key in active_chunks.keys().duplicate():
		var root: Node = active_chunks[key]
		if is_instance_valid(root) and float(key) * CHUNK_WIDTH + CHUNK_WIDTH < prune_before:
			root.queue_free()
			active_chunks.erase(key)
	for child in enemy_root.get_children():
		if is_instance_valid(child) and child.position.x < prune_before: child.queue_free()
