extends CharacterBody2D
class_name MaryouPlayer

const GRAVITY := 1850.0
const JUMP_VELOCITY := -720.0
const SIDE_ACCEL := 2100.0
const MAX_SIDE_SPEED := 190.0
const WIDTH := 38.0
const HEIGHT := 54.0

var dead := false
var shielded := false
var shield_time := 0.0
var squash := 1.0
var stretch := 1.0
var jump_held := false

func _ready() -> void:
	z_index = 10
	var shape := RectangleShape2D.new()
	shape.size = Vector2(WIDTH, HEIGHT)
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)
	var camera := Camera2D.new()
	camera.position = Vector2(250, -30)
	camera.enabled = true
	camera.position_smoothing_enabled = true
	camera.position_smoothing_speed = 6.0
	add_child(camera)
	queue_redraw()

func get_rect() -> Rect2:
	return Rect2(position - Vector2(WIDTH * 0.5, HEIGHT * 0.5), Vector2(WIDTH, HEIGHT))

func tick(delta: float, target_speed: float, left: bool, right: bool, jump: bool, allow_input := true) -> void:
	if dead:
		velocity.y += GRAVITY * delta
		position += velocity * delta
		queue_redraw()
		return
	velocity.x = target_speed
	if allow_input:
		var direction := Input.get_axis("move_left", "move_right")
		if left:
			direction -= 1.0
		if right:
			direction += 1.0
		if direction != 0.0:
			velocity.x += direction * SIDE_ACCEL * delta
			velocity.x = clamp(velocity.x, target_speed - MAX_SIDE_SPEED, target_speed + MAX_SIDE_SPEED)
		else:
			velocity.x = move_toward(velocity.x, target_speed, SIDE_ACCEL * delta)
	if allow_input and jump and is_on_floor():
		velocity.y = JUMP_VELOCITY
		stretch = 1.18
		jump_held = true
	elif not jump:
		jump_held = false
	if jump_held and velocity.y < -260.0:
		velocity.y -= 430.0 * delta
	velocity.y += GRAVITY * delta
	move_and_slide()
	if is_on_floor():
		stretch = move_toward(stretch, 1.0, delta * 5.0)
	else:
		stretch = move_toward(stretch, 1.05, delta * 2.0)
	squash = move_toward(squash, 1.0, delta * 7.0)
	if shield_time > 0.0:
		shield_time -= delta
		if shield_time <= 0.0:
			shielded = false
	queue_redraw()

func take_hit() -> bool:
	if dead:
		return false
	if shielded:
		shielded = false
		shield_time = 0.0
		velocity.y = -260.0
		squash = 0.82
		queue_redraw()
		return false
	return true

func activate_shield() -> void:
	shielded = true
	shield_time = 8.0
	queue_redraw()

func kill() -> void:
	if dead:
		return
	dead = true
	velocity = Vector2(velocity.x * 0.35, -420.0)
	squash = 0.72
	queue_redraw()

func _draw() -> void:
	var body := Rect2(-WIDTH * 0.5, -HEIGHT * 0.5, WIDTH, HEIGHT)
	var scaled_body := Rect2(body.position.x, body.position.y + (HEIGHT * (1.0 - stretch)) * 0.25, body.size.x * squash, body.size.y * stretch)
	draw_style_box(_box(Color("#2d6cdf"), 12.0), scaled_body)
	draw_circle(Vector2(-8, -18), 10, Color("#f4c7a1"))
	draw_circle(Vector2(8, -18), 10, Color("#f4c7a1"))
	draw_circle(Vector2(-5, -20), 2.5, Color("#1f2530"))
	draw_circle(Vector2(5, -20), 2.5, Color("#1f2530"))
	draw_rect(Rect2(-14, 18, 10, 7), Color("#202938"))
	draw_rect(Rect2(4, 18, 10, 7), Color("#202938"))
	if shielded:
		draw_arc(Vector2.ZERO, 34.0, 0.0, TAU, 32, Color(0.35, 0.9, 0.85, 0.75), 3.0)

func _box(color: Color, radius: float) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = color
	box.corner_radius_top_left = int(radius)
	box.corner_radius_top_right = int(radius)
	box.corner_radius_bottom_left = int(radius)
	box.corner_radius_bottom_right = int(radius)
	return box
