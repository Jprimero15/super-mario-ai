extends Area2D

@export var spin_speed := 5.5

func _ready() -> void:
	add_to_group("hazards")

func _process(delta: float) -> void:
	$AnimatedSprite2D.rotation += spin_speed * delta