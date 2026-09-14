extends CharacterBody2D
class_name MaryouEnemy

signal player_contact(enemy: MaryouEnemy)
signal stomped(enemy: MaryouEnemy)

var kind := 0
var speed := 70.0
var size := 30.0
var defeated := false
var base_y := 0.0
var phase := 0.0
var hit_area: Area2D
var shielded := false
var patrol_origin := 0.0

func setup(enemy_kind: int) -> void:
	kind = clampi(enemy_kind, 0, 5)
	var sizes := [30.0, 34.0, 32.0, 36.0, 31.0, 33.0]
	size = sizes[kind]
	speed = MaryouDifficultyCurve.enemy_speed(kind, MaryouDifficultyCurve.tier_for_steps(int(position.x / 32.0)))
	shielded = kind == 3
	collision_layer = 4
	collision_mask = 2
	var shape := RectangleShape2D.new()
	shape.size = Vector2(size, size)
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)
	hit_area = Area2D.new()
	hit_area.collision_layer = 0
	hit_area.collision_mask = 1
	hit_area.monitoring = true
	var hit_shape := CollisionShape2D.new()
	var hit_box := RectangleShape2D.new()
	hit_box.size = Vector2(size + 10.0, size + 10.0)
	hit_shape.shape = hit_box
	hit_area.add_child(hit_shape)
	hit_area.body_entered.connect(_on_player_entered)
	add_child(hit_area)
	base_y = position.y
	patrol_origin = position.x
	phase = position.x * 0.02
	z_index = 7
	queue_redraw()

func tick(delta: float, player_x: float) -> void:
	if defeated:
		velocity.y += 1600.0 * delta
		move_and_slide()
		queue_redraw()
		return
	var direction := -1.0 if player_x < position.x else 1.0
	match kind:
		0:
			velocity.x = direction * speed
			velocity.y += 1500.0 * delta
		1:
			phase += delta * 5.0
			velocity.x = direction * speed * 0.65
			var target_y := base_y - absf(sin(phase)) * 75.0
			velocity.y = (target_y - position.y) * 8.0
		2:
			velocity.x = direction * speed
			velocity.y += 1500.0 * delta
			if absf(player_x - position.x) < 240.0:
				velocity.x = direction * (speed + 55.0)
			velocity.y += 1500.0 * delta
		3:
			velocity.x = direction * speed * 0.8
			velocity.y += 1500.0 * delta
		4:
			phase += delta * 4.0
			velocity.x = sin(phase) * speed + direction * speed * 0.45
			velocity.y = (base_y - 30.0 + sin(phase * 1.7) * 38.0 - position.y) * 7.0
		5:
			velocity.x = direction * speed * 0.55
			velocity.y += 1500.0 * delta
			if is_on_floor() and absf(player_x - position.x) < 300.0:
				velocity.y = -520.0
	move_and_slide()
	queue_redraw()

func _on_player_entered(body: Node2D) -> void:
	if defeated or not body is MaryouPlayer:
		return
	var player := body as MaryouPlayer
	if player.velocity.y > 50.0 and player.position.y < position.y - 8.0:
		if shielded:
			player.velocity.y = -330.0
			player.take_hit()
			return
		defeat()
		player.velocity.y = -400.0
		stomped.emit(self)
	else:
		player_contact.emit(self)

func defeat() -> void:
	if defeated:
		return
	defeated = true
	if is_instance_valid(hit_area):
		hit_area.set_deferred("monitoring", false)
	velocity = Vector2(velocity.x * 0.2, -330.0)
	queue_redraw()

func _draw() -> void:
	var half := size * 0.5
	var body_colors := [Color("#ef6a71"), Color("#d58aff"), Color("#ff9a54"), Color("#6fd8d0"), Color("#6fa4ff"), Color("#f4cf5c")]
	var body_color: Color = body_colors[kind]
	draw_circle(Vector2(0, -half * 0.12), half, body_color)
	if kind == 1:
		draw_arc(Vector2.ZERO, half + 5.0, 0.0, TAU, 24, Color("#dcbcff"), 3.0)
	elif kind == 2:
		draw_line(Vector2(-half, -half), Vector2(half, half), Color("#ffd0a8"), 3.0)
		draw_line(Vector2(half, -half), Vector2(-half, half), Color("#ffd0a8"), 3.0)
	elif kind == 3:
		draw_arc(Vector2.ZERO, half + 7.0, 0.0, TAU, 28, Color("#b8fff5"), 5.0)
	elif kind == 4:
		draw_circle(Vector2(0, -half - 3.0), 6.0, Color("#d8e7ff"))
	elif kind == 5:
		draw_line(Vector2(-half, half - 2.0), Vector2(half, half - 2.0), Color("#fff0a0"), 4.0)
	draw_circle(Vector2(-half * 0.32, -half * 0.18), half * 0.13, Color.WHITE)
	draw_circle(Vector2(half * 0.32, -half * 0.18), half * 0.13, Color.WHITE)
	draw_circle(Vector2(-half * 0.32, -half * 0.18), half * 0.06, Color("#202938"))
	draw_circle(Vector2(half * 0.32, -half * 0.18), half * 0.06, Color("#202938"))
