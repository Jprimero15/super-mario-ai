extends Area2D
class_name MaryouHazard

signal hit_player

func setup(size: Vector2) -> void:
	collision_layer = 0
	collision_mask = 1
	monitoring = true
	var shape := RectangleShape2D.new()
	shape.size = size
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)
	body_entered.connect(_on_body_entered)

func _on_body_entered(body: Node2D) -> void:
	if body is MaryouPlayer:
		hit_player.emit()
