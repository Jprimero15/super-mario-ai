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
const SPRITE_FRAMES := preload("res://assets/sprites/maryou_frames.tres")

var dead := false
var shielded := false
var shield_time := 0.0
var hit_invulnerability := 0.0
var coyote_timer := 0.0
var jump_buffer_timer := 0.0
var squash := 1.0
var stretch := 1.0
var camera: Camera2D
var animated_sprite: AnimatedSprite2D
var shake_time := 0.0
var shake_strength := 0.0
var last_animation := ""

func _ready() -> void:
	z_index = 10
	collision_layer = 1
	collision_mask = 2
	var shape := RectangleShape2D.new()
	shape.size = Vector2(WIDTH, HEIGHT)
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)
	animated_sprite = AnimatedSprite2D.new()
	animated_sprite.sprite_frames = SPRITE_FRAMES
	animated_sprite.animation = &"idle"
	animated_sprite.position = Vector2(0, -2)
	animated_sprite.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
	add_child(animated_sprite)
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
		if shield_time == 0.0: shielded = false
	if shake_time > 0.0:
		shake_time = maxf(0.0, shake_time - delta)
		if is_instance_valid(camera): camera.offset = Vector2(randf_range(-shake_strength, shake_strength), randf_range(-shake_strength, shake_strength))
	elif is_instance_valid(camera): camera.offset = camera.offset.lerp(Vector2.ZERO, minf(delta * 14.0, 1.0))
	if dead:
		velocity.y += gravity * delta
		move_and_slide()
		_set_animation("dead")
		queue_redraw()
		return
	if jump_pressed: jump_buffer_timer = jump_buffer_time
	else: jump_buffer_timer = maxf(0.0, jump_buffer_timer - delta)
	if is_on_floor(): coyote_timer = coyote_time
	else: coyote_timer = maxf(0.0, coyote_timer - delta)
	velocity.x = target_speed
	var direction := Input.get_axis("move_left", "move_right")
	if left: direction -= 1.0
	if right: direction += 1.0
	if direction != 0.0:
		velocity.x = clampf(velocity.x + direction * side_accel * delta, target_speed - max_side_speed, target_speed + max_side_speed)
	else: velocity.x = move_toward(velocity.x, target_speed, side_accel * delta)
	if jump_buffer_timer > 0.0 and coyote_timer > 0.0:
		velocity.y = jump_velocity
		jump_buffer_timer = 0.0
		coyote_timer = 0.0
		stretch = 1.18
	if not jump_held and velocity.y < -260.0: velocity.y = -260.0
	velocity.y += gravity * delta
	move_and_slide()
	if is_on_floor(): squash = move_toward(squash, 1.0, delta * 8.0)
	stretch = move_toward(stretch, 1.0, delta * 6.0)
	if hit_invulnerability > 0.0: _set_animation("hurt")
	elif not is_on_floor(): _set_animation("jump" if velocity.y < 0.0 else "fall")
	elif absf(velocity.x) > target_speed + 25.0: _set_animation("run")
	else: _set_animation("idle")
	if is_instance_valid(animated_sprite) and absf(direction) > 0.01: animated_sprite.flip_h = direction < 0.0
	queue_redraw()

func _set_animation(name: String) -> void:
	if not is_instance_valid(animated_sprite) or last_animation == name: return
	last_animation = name
	animated_sprite.play(name)

func take_hit() -> bool:
	if dead or hit_invulnerability > 0.0: return false
	if shielded:
		shielded = false
		shield_time = 0.0
		hit_invulnerability = HIT_INVULNERABILITY
		velocity.y = -300.0
		squash = 0.82
		shake(4.0, 0.12)
		_set_animation("hurt")
		queue_redraw()
		return false
	hit_invulnerability = HIT_INVULNERABILITY
	squash = 0.82
	velocity.y = minf(velocity.y, -240.0)
	shake(8.0, 0.16)
	_set_animation("hurt")
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
	_set_animation("dead")
	queue_redraw()

func _draw() -> void:
	if shielded: draw_arc(Vector2.ZERO, 34.0, 0.0, TAU, 32, Color(0.35, 0.9, 0.85, 0.75), 3.0)
