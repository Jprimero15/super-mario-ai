extends CharacterBody2D
class_name MaryouPlayer

@export_category("Movement")
@export var gravity := 1850.0
@export var jump_velocity := -720.0
@export var side_accel := 2100.0
@export var max_side_speed := 190.0
@export var coyote_time := 0.12
@export var jump_buffer_time := 0.14

const WIDTH := 38.0
const HEIGHT := 54.0
const HIT_INVULNERABILITY := 1.15

var dead := false
var shielded := false
var shield_time := 0.0
var hit_invulnerability := 0.0
var coyote_timer := 0.0
var jump_buffer_timer := 0.0
var squash := 1.0
var stretch := 1.0
var camera: Camera2D
var shake_time := 0.0
var shake_strength := 0.0

func _ready() -> void:
	z_index = 10
	collision_layer = 1
	collision_mask = 2
	var shape := RectangleShape2D.new()
	shape.size = Vector2(WIDTH, HEIGHT)
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)
	camera = Camera2D.new()
	camera.position = Vector2(250, -30)
	camera.enabled = true
	camera.position_smoothing_enabled = true
	camera.position_smoothing_speed = 7.0
	add_child(camera)

func tick(delta: float, target_speed: float, left: bool, right: bool, jump_pressed: bool, jump_held: bool) -> void:
	hit_invulnerability = maxf(0.0, hit_invulnerability - delta)
	if shield_time > 0.0:
		shield_time = maxf(0.0, shield_time - delta)
		if shield_time == 0.0:
			shielded = false
	if shake_time > 0.0:
		shake_time = maxf(0.0, shake_time - delta)
		if is_instance_valid(camera):
			camera.offset = Vector2(randf_range(-shake_strength, shake_strength), randf_range(-shake_strength, shake_strength))
	elif is_instance_valid(camera):
		camera.offset = camera.offset.lerp(Vector2.ZERO, minf(delta * 14.0, 1.0))
	if dead:
		velocity.y += gravity * delta
		move_and_slide()
		queue_redraw()
		return

	if jump_pressed:
		jump_buffer_timer = jump_buffer_time
	else:
		jump_buffer_timer = maxf(0.0, jump_buffer_timer - delta)
	if is_on_floor():
		coyote_timer = coyote_time
	else:
		coyote_timer = maxf(0.0, coyote_timer - delta)

	velocity.x = target_speed
	var direction := Input.get_axis("move_left", "move_right")
	if left: direction -= 1.0
	if right: direction += 1.0
	if direction != 0.0:
		velocity.x = clampf(velocity.x + direction * side_accel * delta, target_speed - max_side_speed, target_speed + max_side_speed)
	else:
		velocity.x = move_toward(velocity.x, target_speed, side_accel * delta)

	if jump_buffer_timer > 0.0 and coyote_timer > 0.0:
		velocity.y = jump_velocity
		jump_buffer_timer = 0.0
		coyote_timer = 0.0
		stretch = 1.18
	if not jump_held and velocity.y < -260.0:
		velocity.y = -260.0
	velocity.y += gravity * delta
	move_and_slide()
	if is_on_floor(): squash = move_toward(squash, 1.0, delta * 8.0)
	stretch = move_toward(stretch, 1.0, delta * 6.0)
	queue_redraw()

func take_hit() -> bool:
	if dead or hit_invulnerability > 0.0: return false
	if shielded:
		shielded = false
		shield_time = 0.0
		hit_invulnerability = HIT_INVULNERABILITY
		velocity.y = -300.0
		squash = 0.82
		shake(4.0, 0.12)
		queue_redraw()
		return false
	hit_invulnerability = HIT_INVULNERABILITY
	squash = 0.82
	velocity.y = minf(velocity.y, -240.0)
	shake(8.0, 0.16)
	queue_redraw()
	return true

func activate_shield() -> void:
	shielded = true
	shield_time = 8.0
	queue_redraw()

func shake(strength: float, duration: float) -> void:
	shake_strength = maxf(shake_strength, strength)
	shake_time = maxf(shake_time, duration)

func kill() -> void:
	if dead: return
	dead = true
	velocity = Vector2(velocity.x * 0.35, -420.0)
	squash = 0.72
	shake(10.0, 0.2)
	queue_redraw()

func _draw() -> void:
	var body := Rect2(-WIDTH * 0.5, -HEIGHT * 0.5, WIDTH, HEIGHT)
	var scaled := Rect2(body.position.x, body.position.y + HEIGHT * (1.0 - stretch) * 0.25, body.size.x * squash, body.size.y * stretch)
	var body_color := Color("#2d6cdf")
	if hit_invulnerability > 0.0 and not dead and int(hit_invulnerability * 12.0) % 2 == 0: body_color = Color("#6f9cf4")
	draw_style_box(_box(body_color, 12), scaled)
	draw_circle(Vector2(-8, -18), 10, Color("#f4c7a1"))
	draw_circle(Vector2(8, -18), 10, Color("#f4c7a1"))
	draw_circle(Vector2(-5, -20), 2.5, Color("#1f2530"))
	draw_circle(Vector2(5, -20), 2.5, Color("#1f2530"))
	draw_rect(Rect2(-14, 18, 10, 7), Color("#202938"))
	draw_rect(Rect2(4, 18, 10, 7), Color("#202938"))
	if shielded: draw_arc(Vector2.ZERO, 34.0, 0.0, TAU, 32, Color(0.35, 0.9, 0.85, 0.75), 3.0)

func _box(color: Color, radius: int) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = color
	box.corner_radius_top_left = radius
	box.corner_radius_top_right = radius
	box.corner_radius_bottom_left = radius
	box.corner_radius_bottom_right = radius
	return box
