extends CharacterBody2D
class_name MaryouPlayer

@export_category("Movement")
@export var gravity := 1850.0
@export var terminal_velocity := 1050.0
@export var jump_velocity := -720.0
@export var jump_cut_velocity := -260.0
@export var side_accel := 2400.0
@export var side_decel := 3000.0
@export var max_side_speed := 170.0
@export var coyote_time := 0.11
@export var jump_buffer_time := 0.13

# Jungle-boy atlas: 1024x1024, 4x4 grid, 256x256 frames.
const SPRITE_CELL := 256.0
const WIDTH := 32.0
const HEIGHT := 50.0
const BODY_OFFSET_Y := 2.0
const HIT_INVULNERABILITY := 1.15
const JUNGLE_BOY_ATLAS := "res://assets/sprites/maryou_jungle_boy.png"
const LEGACY_SPRITE_FRAMES := preload("res://assets/sprites/maryou_frames.tres")
const GAME_ZOOM := Vector2(1.20, 1.20)

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
var was_airborne := false
var landing_timer := 0.0

func _ready() -> void:
	z_index = 10
	collision_layer = 1
	collision_mask = 2

	var shape := RectangleShape2D.new()
	shape.size = Vector2(WIDTH, HEIGHT)
	var collider := CollisionShape2D.new()
	collider.shape = shape
	collider.position.y = BODY_OFFSET_Y
	add_child(collider)

	animated_sprite = AnimatedSprite2D.new()
	animated_sprite.sprite_frames = _build_sprite_frames()
	animated_sprite.animation = &"idle"
	animated_sprite.position = Vector2(0, -2)
	# Each atlas cell is 256px; scale it to match the existing gameplay body.
	animated_sprite.scale = Vector2(0.25, 0.25)
	animated_sprite.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR
	animated_sprite.speed_scale = 1.0
	animated_sprite.flip_h = false
	add_child(animated_sprite)

	camera = Camera2D.new()
	camera.position = Vector2(250, -30)
	camera.zoom = GAME_ZOOM
	camera.enabled = true
	camera.position_smoothing_enabled = true
	camera.position_smoothing_speed = 7.0
	add_child(camera)

func _build_sprite_frames() -> SpriteFrames:
	# Use the new 256px atlas when it has been added to the repository.
	# Keeping the legacy resource as a fallback makes the game boot safely
	# before the new PNG is committed.
	if ResourceLoader.exists(JUNGLE_BOY_ATLAS):
		var texture := load(JUNGLE_BOY_ATLAS) as Texture2D
		if texture != null:
			return _create_jungle_boy_frames(texture)
	return LEGACY_SPRITE_FRAMES

func _create_jungle_boy_frames(texture: Texture2D) -> SpriteFrames:
	var frames := SpriteFrames.new()
	frames.remove_animation(&"default")

	# Rows in the atlas:
	# 0 idle, 1 run, 2 jump, 3 land. Each row contains four 256x256 frames.
	_add_atlas_animation(frames, "idle", texture, 0, true, 4.0)
	_add_atlas_animation(frames, "run", texture, 1, true, 10.0)
	_add_atlas_animation(frames, "jump", texture, 2, false, 8.0)
	_add_atlas_animation(frames, "land", texture, 3, false, 10.0)

	# These gameplay states are kept as aliases/short poses so existing damage
	# and death logic never requests a missing animation.
	_add_single_frame_animation(frames, "hurt", texture, 2, 1)
	_add_single_frame_animation(frames, "dead", texture, 3, 0)
	_add_single_frame_animation(frames, "fall", texture, 2, 2)

	return frames

func _add_atlas_animation(frames: SpriteFrames, name: StringName, atlas: Texture2D, row: int, loop: bool, speed: float) -> void:
	frames.add_animation(name)
	frames.set_animation_loop(name, loop)
	frames.set_animation_speed(name, speed)
	for column in 4:
		var atlas_texture := AtlasTexture.new()
		atlas_texture.atlas = atlas
		atlas_texture.region = Rect2(column * SPRITE_CELL, row * SPRITE_CELL, SPRITE_CELL, SPRITE_CELL)
		atlas_texture.filter_clip = true
		frames.add_frame(name, atlas_texture)

func _add_single_frame_animation(frames: SpriteFrames, name: StringName, atlas: Texture2D, row: int, column: int) -> void:
	frames.add_animation(name)
	frames.set_animation_loop(name, false)
	frames.set_animation_speed(name, 1.0)
	var atlas_texture := AtlasTexture.new()
	atlas_texture.atlas = atlas
	atlas_texture.region = Rect2(column * SPRITE_CELL, row * SPRITE_CELL, SPRITE_CELL, SPRITE_CELL)
	atlas_texture.filter_clip = true
	frames.add_frame(name, atlas_texture)

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
		velocity.y = minf(velocity.y + gravity * delta, terminal_velocity)
		move_and_slide()
		_set_animation("dead")
		queue_redraw()
		return

	if landing_timer > 0.0:
		landing_timer = maxf(0.0, landing_timer - delta)

	if jump_pressed:
		jump_buffer_timer = jump_buffer_time
	else:
		jump_buffer_timer = maxf(0.0, jump_buffer_timer - delta)

	if is_on_floor():
		coyote_timer = coyote_time
	else:
		coyote_timer = maxf(0.0, coyote_timer - delta)

	var direction := 0.0
	if left: direction -= 1.0
	if right: direction += 1.0
	direction = clampf(direction, -1.0, 1.0)

	var desired_x := target_speed + direction * max_side_speed
	if direction != 0.0:
		velocity.x = move_toward(velocity.x, desired_x, side_accel * delta)
	else:
		velocity.x = move_toward(velocity.x, target_speed, side_decel * delta)

	if jump_buffer_timer > 0.0 and coyote_timer > 0.0:
		velocity.y = jump_velocity
		jump_buffer_timer = 0.0
		coyote_timer = 0.0
		stretch = 1.18

	if not jump_held and velocity.y < jump_cut_velocity:
		velocity.y = jump_cut_velocity

	velocity.y = minf(velocity.y + gravity * delta, terminal_velocity)
	move_and_slide()

	var now_on_floor := is_on_floor()
	var just_landed := now_on_floor and was_airborne
	if just_landed:
		landing_timer = 0.38
		squash = 0.84
		_set_animation("land", true)
	was_airborne = not now_on_floor

	if now_on_floor:
		squash = move_toward(squash, 1.0, delta * 8.0)
	else:
		squash = move_toward(squash, 1.0, delta * 4.0)
	stretch = move_toward(stretch, 1.0, delta * 6.0)

	if hit_invulnerability > 0.0:
		_set_animation("hurt")
	elif not now_on_floor:
		_set_animation("jump" if velocity.y < 0.0 else "fall")
	elif landing_timer > 0.0:
		# Let the four-frame landing animation finish before returning to idle/run.
		if not animated_sprite.is_playing():
			landing_timer = 0.0
	elif absf(velocity.x) > 45.0:
		_set_animation("run")
	else:
		_set_animation("idle")

	if is_instance_valid(animated_sprite):
		if right:
			animated_sprite.flip_h = false
		elif left:
			animated_sprite.flip_h = true
		else:
			animated_sprite.flip_h = false
		if last_animation == "run":
			animated_sprite.speed_scale = clampf(absf(velocity.x) / 180.0, 0.85, 1.75)
		else:
			animated_sprite.speed_scale = 1.0
	queue_redraw()

func _set_animation(name: String, force_restart: bool = false) -> void:
	if not is_instance_valid(animated_sprite) or not animated_sprite.sprite_frames.has_animation(name):
		return
	if last_animation == name and not force_restart:
		return
	last_animation = name
	animated_sprite.play(name)

func take_damage(amount: int = 1) -> bool:
	if amount <= 0 or dead or hit_invulnerability > 0.0:
		return false
	if shielded:
		shielded = false
		shield_time = 0.0
		hit_invulnerability = HIT_INVULNERABILITY
		velocity.y = -300.0
		squash = 0.82
		shake(4.0, 0.12)
		_set_animation("hurt", true)
		queue_redraw()
		return false
	hit_invulnerability = HIT_INVULNERABILITY
	squash = 0.82
	velocity.y = minf(velocity.y, -240.0)
	shake(8.0, 0.16)
	_set_animation("hurt", true)
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
	if dead:
		return
	dead = true
	velocity = Vector2(velocity.x * 0.35, -420.0)
	squash = 0.72
	shake(10.0, 0.2)
	_set_animation("dead", true)
	queue_redraw()

func _draw() -> void:
	if shielded:
		draw_arc(Vector2.ZERO, 34.0, 0.0, TAU, 32, Color(0.35, 0.9, 0.85, 0.75), 3.0)
