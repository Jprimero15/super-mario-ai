extends Node

@export var spin_speed := 5.5

func _process(delta: float) -> void:
	get_parent().get_node("AnimatedSprite2D").rotation += spin_speed * delta