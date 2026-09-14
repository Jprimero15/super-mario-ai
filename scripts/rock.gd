extends Area2D
class_name MaryouRock

signal hit_player(player: MaryouPlayer)

@export var roll_speed := 115.0
@export var roll_range := 120.0

var origin_x := 0.0
var direction := 1.0

func _ready() -> void:
	origin_x = position.x
	collision_layer = 0
	collision_mask = 1
	monitoring = true
	body_entered.connect(_on_body_entered)

func _physics_process(delta: float) -> void:
	position.x += direction * roll_speed * delta
	rotation += direction * roll_speed * delta / 22.0
	if position.x >= origin_x + roll_range:
		direction = -1.0
	elif position.x <= origin_x - roll_range:
		direction = 1.0

func _on_body_entered(body: Node2D) -> void:
	if not body is MaryouPlayer:
		return
	var player := body as MaryouPlayer
	if player.take_damage(1):
		hit_player.emit(player)
