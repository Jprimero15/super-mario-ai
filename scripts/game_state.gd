extends Node

var has_checkpoint := false
var checkpoint_position := Vector2.ZERO
var respawn_requested := false

func set_checkpoint(position: Vector2) -> void:
	has_checkpoint = true
	checkpoint_position = position

func request_checkpoint_respawn() -> void:
	respawn_requested = has_checkpoint

func consume_checkpoint_respawn() -> Vector2:
	if not respawn_requested or not has_checkpoint:
		respawn_requested = false
		return Vector2.INF
	var position := checkpoint_position
	respawn_requested = false
	has_checkpoint = false
	checkpoint_position = Vector2.ZERO
	return position

func clear_checkpoint() -> void:
	has_checkpoint = false
	checkpoint_position = Vector2.ZERO
	respawn_requested = false
