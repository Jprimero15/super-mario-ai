extends Area2D

class_name MaryouCheckpoint

signal checkpoint_reached(position: Vector2)
var active := false

func _ready() -> void:
	collision_layer = 0
	collision_mask = 1
	monitoring = true
	body_entered.connect(_on_body_entered)

func _on_body_entered(body: Node2D) -> void:
	if active or not body is MaryouPlayer:
		return
	active = true
	get_node("/root/GameState").set_checkpoint(global_position)
	$AnimatedSprite2D.play("checkpoint_active")
	checkpoint_reached.emit(global_position)
