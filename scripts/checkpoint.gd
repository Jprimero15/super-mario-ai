extends Area2D

signal checkpoint_reached(position: Vector2)
var active := false

func _ready() -> void:
	add_to_group("hazards")
	collision_layer = 0
	collision_mask = 1
	body_entered.connect(_on_body_entered)

func _on_body_entered(body: Node2D) -> void:
	if active or not body is MaryouPlayer:
		return
	active = true
	$AnimatedSprite2D.play("active")
	checkpoint_reached.emit(global_position)