extends Node2D
class_name MaryouWorldGenerator

signal coin_collected
signal hazard_hit
signal checkpoint_reached(position: Vector2)

const CHUNK_WIDTH := 640.0
const TILE := 32.0
const GROUND_Y := 560.0
const ChunkScript := preload("res://scripts/chunk.gd")
const HazardScript := preload("res://scripts/hazard.gd")
const EnemyScript := preload("res://scripts/enemy.gd")
const CoinFrames := preload("res://assets/sprites/coin_frames.tres")
const PipeTexture := preload("res://assets/sprites/pipe.svg")

const OBSTACLE_SCENES := [
	preload("res://scenes/obstacles/platform.tscn"),
	preload("res://scenes/obstacles/spikes.tscn"),
	preload("res://scenes/obstacles/crumbling_platform.tscn"),
	preload("res://scenes/obstacles/small_crumble.tscn"),
	preload("res://scenes/obstacles/conveyor.tscn"),
	preload("res://scenes/obstacles/saw.tscn"),
	preload("res://scenes/obstacles/lava.tscn"),
	preload("res://scenes/obstacles/rock.tscn"),
	preload("res://scenes/obstacles/pit.tscn"),
	preload("res://scenes/obstacles/mace.tscn"),
	preload("res://scenes/obstacles/acid.tscn"),
	preload("res://scenes/obstacles/crate.tscn"),
	preload("res://scenes/obstacles/bounce_pad.tscn"),
	preload("res://scenes/obstacles/checkpoint.tscn")
]

var generated_to := -1
var active_chunks: Dictionary = {}
var enemy_root: Node2D
var run_seed: int = 0

func setup(enemies: Node2D) -> void:
	enemy_root = enemies
	run_seed = int(Time.get_ticks_usec()) + int(Time.get_unix_time_from_system() * 1000.0)

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
	rng.seed = run_seed + chunk_index * 7919
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
			if tile.intersects(hole):
				blocked = true
				break
		if not blocked:
			_add_solid(root, tile)
			root.solids.append(tile)
	root.holes = holes
	for hole in holes:
		_add_hole_warning(root, hole)

	if chunk_index > 0:
		var obstacle_count := 1
		if distance_steps >= 350: obstacle_count = 2
		if distance_steps >= 1000: obstacle_count = 3
		for obstacle_slot in range(obstacle_count):
			var obstacle_index := _choose_obstacle_index(rng, distance_steps, chunk_index, obstacle_slot)
			var obstacle_scene: PackedScene = OBSTACLE_SCENES[obstacle_index]
			for attempt in range(10):
				var obstacle_x := start_x + float(rng.randi_range(8, 18)) * TILE
				var obstacle_y := GROUND_Y - 32.0
				if obstacle_index in [0, 2, 3, 4, 5, 7, 9, 11, 13]:
					obstacle_y = GROUND_Y - 34.0
				var obstacle_rect := Rect2(obstacle_x - 28.0, obstacle_y - 32.0, 56.0, 64.0)
				if _safe(obstacle_rect, occupied, holes, 36.0) and obstacle_x > safe_gap_until:
					_add_obstacle(root, obstacle_scene, Vector2(obstacle_x, obstacle_y), obstacle_index)
					occupied.append(obstacle_rect)
					break

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
		if _safe(Rect2(coin_pos - Vector2(15, 15), Vector2(30, 30)), occupied, holes, 10.0):
			_add_coin(root, coin_pos)

	var count := MaryouDifficultyCurve.enemy_count(distance_steps)
	if chunk_index == 0: count = 0
	for i in range(count):
		for attempt in range(8):
			var enemy_x := start_x + float(rng.randi_range(11, 18)) * TILE
			var enemy_rect := Rect2(enemy_x - 18.0, GROUND_Y - 58.0, 36.0, 58.0)
			if _safe(enemy_rect, occupied, holes, 48.0):
				var enemy := EnemyScript.new()
				enemy.position = Vector2(enemy_x, GROUND_Y - 40.0)
				enemy.setup(MaryouDifficultyCurve.enemy_kind(distance_steps, i, rng))
				enemy_root.add_child(enemy)
				occupied.append(enemy_rect)
				break

func _choose_obstacle_index(rng: RandomNumberGenerator, distance_steps: int, chunk_index: int, slot: int) -> int:
	if chunk_index % 6 == 0 and slot == 0:
		return 13
	var tier := MaryouDifficultyCurve.tier_for_steps(distance_steps)
	var pool: Array[int] = [0, 1, 2, 3, 4, 5, 7, 11, 12]
	if tier >= 2:
		pool.append_array([6, 8, 9])
	if tier >= 3:
		pool.append(10)
	var index := pool[rng.randi_range(0, pool.size() - 1)]
	return index

func _add_obstacle(parent: Node2D, scene: PackedScene, position: Vector2, index: int) -> void:
	var obstacle := scene.instantiate()
	obstacle.name = "Obstacle_%02d_%s" % [index, scene.resource_path.get_file().get_basename()]
	obstacle.position = position
	parent.add_child(obstacle)
	if obstacle is MaryouCheckpoint:
		var checkpoint := obstacle as MaryouCheckpoint
		checkpoint.checkpoint_reached.connect(func(checkpoint_position: Vector2): checkpoint_reached.emit(checkpoint_position))
	elif obstacle.has_signal("hit_player"):
		obstacle.hit_player.connect(func(hit_player: MaryouPlayer): hazard_hit.emit(hit_player))

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
	sprite.texture = preload("res://assets/world/obstacles.svg")
	sprite.region_enabled = true
	sprite.region_rect = Rect2(0.0, 128.0, 64.0, 64.0)
	sprite.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	sprite.position = Vector2(hole.position.x + hole.size.x * 0.5, GROUND_Y - 18.0)
	sprite.scale = Vector2(hole.size.x / 64.0, 0.72)
	parent.add_child(sprite)

func _add_hazard(parent: Node2D, pipe: Rect2) -> void:
	var area := HazardScript.new()
	var hazard_height := maxf(10.0, pipe.size.y - 20.0)
	area.position = pipe.position + Vector2(pipe.size.x * 0.5, pipe.size.y * 0.5 + 10.0)
	area.setup(Vector2(pipe.size.x + 8.0, hazard_height))
	area.hit_player.connect(func(_hit_player: MaryouPlayer): hazard_hit.emit(_hit_player))
	parent.add_child(area)

func _add_coin(parent: Node2D, position: Vector2) -> void:
	var coin := Area2D.new()
	coin.name = "Coin"
	coin.collision_layer = 0
	coin.collision_mask = 1
	coin.monitoring = true
	coin.position = position

	var shape := CircleShape2D.new()
	shape.radius = 15.0
	var collider := CollisionShape2D.new()
	collider.shape = shape
	coin.add_child(collider)

	var sprite := AnimatedSprite2D.new()
	sprite.sprite_frames = CoinFrames
	sprite.animation = &"spin"
	sprite.autoplay = &"spin"
	sprite.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	sprite.scale = Vector2(0.55, 0.55)
	coin.add_child(sprite)

	coin.body_entered.connect(func(body: Node2D) -> void:
		if body is MaryouPlayer:
			coin_collected.emit()
			coin.queue_free()
	)
	parent.add_child(coin)

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
