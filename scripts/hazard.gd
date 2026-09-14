extends Area2D
class_name MaryouHazard

signal hit_player(player: MaryouPlayer)

@export var damage: int = 1

func _ready() -> void:
	add_to_group("hazards")
	collision_layer = 0
	collision_mask = 1
	monitoring = true
	if not body_entered.is_connected(_on_body_entered):
		body_entered.connect(_on_body_entered)

func setup(size: Vector2) -> void:
	var shape := RectangleShape2D.new()
	shape.size = size
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)

func _on_body_entered(body: Node2D) -> void:
	if body is MaryouPlayer:
		var player := body as MaryouPlayer
		player.take_damage(damage)
		hit_player.emit(player)