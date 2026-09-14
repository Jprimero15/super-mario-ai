extends Node

var has_checkpoint := false
var checkpoint_position := Vector2.ZERO

func set_checkpoint(position: Vector2) -> void:
	has_checkpoint = true
	checkpoint_position = position