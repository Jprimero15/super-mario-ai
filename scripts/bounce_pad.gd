extends Area2D

@export var impulse := -920.0
var cooldown := 0.0

func _ready() -> void:
	collision_layer = 0
	collision_mask = 1
	body_entered.connect(_on_body_entered)

func _physics_process(delta: float) -> void:
	cooldown = maxf(0.0, cooldown - delta)

func _on_body_entered(body: Node2D) -> void:
	if cooldown > 0.0 or not body is MaryouPlayer:
		return
	cooldown = 0.18
	var player := body as MaryouPlayer
	player.velocity.y = impulse
	$AnimatedSprite2D.play("bounce")
