extends CharacterBody2D
class_name MaryouEnemy

var tier := 0
var speed := 70.0
var size := 30.0
var defeated := false

func setup(enemy_tier: int) -> void:
	tier = enemy_tier
	size = [28.0, 36.0, 48.0][clamp(tier, 0, 2)]
	speed = [62.0, 72.0, 84.0][clamp(tier, 0, 2)]
	var shape := RectangleShape2D.new()
	shape.size = Vector2(size, size)
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)
	z_index = 7
	queue_redraw()

func tick(delta: float, player_x: float) -> void:
	if defeated:
		velocity.y += 1600.0 * delta
		position += velocity * delta
		return
	velocity.x = -speed if player_x < position.x else speed
	velocity.y += 1500.0 * delta
	move_and_slide()
	queue_redraw()

func defeat() -> void:
	defeated = true
	velocity = Vector2(velocity.x * 0.2, -330.0)

func _draw() -> void:
	var half := size * 0.5
	draw_circle(Vector2(0, -half * 0.12), half, Color("#ef6a71"))
	draw_circle(Vector2(-half * 0.32, -half * 0.18), half * 0.13, Color("#ffffff"))
	draw_circle(Vector2(half * 0.32, -half * 0.18), half * 0.13, Color("#ffffff"))
	draw_circle(Vector2(-half * 0.32, -half * 0.18), half * 0.06, Color("#202938"))
	draw_circle(Vector2(half * 0.32, -half * 0.18), half * 0.06, Color("#202938"))
	draw_rect(Rect2(-half * 0.55, half * 0.24, half * 1.1, half * 0.16), Color("#202938"))
