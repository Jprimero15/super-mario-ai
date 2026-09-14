extends CharacterBody2D
class_name MaryouEnemy

signal player_contact(enemy: MaryouEnemy)
signal stomped(enemy: MaryouEnemy)

const SPRITE_FRAMES := preload("res://assets/sprites/enemy_frames.tres")
const ENEMY_NAMES := ["fire", "water", "thunder", "shadow"]

var kind := 0
var speed := 70.0
var size := 32.0
var defeated := false
var base_y := 0.0
var phase := 0.0
var attack_timer := 0.0
var hurt_timer := 0.0
var hit_area: Area2D
var patrol_origin := 0.0
var patrol_range := 78.0
var animated_sprite: AnimatedSprite2D

func setup(enemy_kind: int) -> void:
	kind = posmod(enemy_kind, ENEMY_NAMES.size())
	var sizes := [34.0, 36.0, 34.0, 36.0]
	size = sizes[kind]
	speed = MaryouDifficultyCurve.enemy_speed(kind, MaryouDifficultyCurve.tier_for_steps(int(position.x / 32.0)))
	patrol_range = 72.0 + float(kind) * 8.0
	collision_layer = 4
	collision_mask = 2

	var shape := RectangleShape2D.new()
	shape.size = Vector2(size, size)
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)

	animated_sprite = AnimatedSprite2D.new()
	animated_sprite.sprite_frames = SPRITE_FRAMES
	animated_sprite.animation = "%s_idle" % ENEMY_NAMES[kind]
	animated_sprite.position = Vector2(0, -4)
	animated_sprite.scale = Vector2(0.25, 0.25)
	animated_sprite.texture_filter = CanvasItem.TEXTURE_FILTER_LINEAR
	add_child(animated_sprite)

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
	_set_pose("idle")

func tick(delta: float, player_x: float) -> void:
	if defeated:
		velocity.y += 1600.0 * delta
		move_and_slide()
		return

	attack_timer = maxf(attack_timer - delta, 0.0)
	hurt_timer = maxf(hurt_timer - delta, 0.0)
	phase += delta * (0.8 + speed / 320.0)

	var direction := signf(player_x - position.x)
	if is_zero_approx(direction): direction = 1.0

	match kind:
		0, 3:
			var target_x := patrol_origin + sin(phase) * patrol_range
			direction = signf(target_x - position.x)
			if is_zero_approx(direction): direction = 1.0
			velocity.x = direction * speed * (0.72 if kind == 3 else 1.0)
			velocity.y += 1500.0 * delta
		1:
			velocity.x = direction * speed * 0.65
			var target_y := base_y - absf(sin(phase)) * 75.0
			velocity.y = (target_y - position.y) * 8.0
		2:
			var target_x := patrol_origin + sin(phase) * patrol_range
			var patrol_direction := signf(target_x - position.x)
			if absf(player_x - position.x) < 240.0:
				patrol_direction = direction
			if is_zero_approx(patrol_direction): patrol_direction = 1.0
			velocity.x = patrol_direction * (speed + (55.0 if absf(player_x - position.x) < 240.0 else 0.0))
			velocity.y += 1500.0 * delta

	move_and_slide()
	if is_instance_valid(animated_sprite):
		animated_sprite.flip_h = direction < 0.0
		_update_animation(delta, player_x)

func _update_animation(_delta: float, player_x: float) -> void:
	if hurt_timer > 0.0:
		_set_pose("hurt")
		return
	if attack_timer > 0.0:
		_set_pose("attack")
		return
	if absf(player_x - position.x) < 150.0 and fmod(phase, 2.4) < 0.08:
		attack_timer = 0.28
		_set_pose("attack")
		return
	_set_pose("idle")

func _set_pose(pose: String) -> void:
	if not is_instance_valid(animated_sprite): return
	var animation_name := "%s_%s" % [ENEMY_NAMES[kind], pose]
	if animated_sprite.animation != animation_name:
		animated_sprite.animation = animation_name
		animated_sprite.frame = 0
		animated_sprite.play()

func _on_player_entered(body: Node2D) -> void:
	if defeated or not body is MaryouPlayer: return
	var player := body as MaryouPlayer
	if player.velocity.y > 50.0 and player.position.y < position.y - 8.0:
		defeat()
		player.velocity.y = -400.0
		stomped.emit(self)
	else:
		hurt_timer = 0.18
		_set_pose("hurt")
		player_contact.emit(self)

func defeat() -> void:
	if defeated: return
	defeated = true
	if is_instance_valid(hit_area): hit_area.set_deferred("monitoring", false)
	velocity = Vector2(velocity.x * 0.2, -330.0)
	_set_pose("faint")
