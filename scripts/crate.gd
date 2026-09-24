extends StaticBody2D
class_name MaryouCrate

@export var stomp_coin_reward := 1

var broken := false

func _ready() -> void:
	collision_layer = 2
	collision_mask = 1
	var trigger := Area2D.new()
	trigger.name = "StompTrigger"
	trigger.collision_layer = 0
	trigger.collision_mask = 1
	trigger.monitoring = true
	var shape := CollisionShape2D.new()
	var box := RectangleShape2D.new()
	box.size = Vector2(54.0, 54.0)
	shape.shape = box
	trigger.add_child(shape)
	add_child(trigger)
	trigger.body_entered.connect(_on_trigger_body_entered)

func _on_trigger_body_entered(body: Node2D) -> void:
	if broken or not body is MaryouPlayer:
		return
	var player := body as MaryouPlayer
	if player.global_position.y >= global_position.y or player.velocity.y < 0.0:
		return
	broken = true
	get_node("/root/ScoreManager").add_coin()
	get_node("/root/AudioManager").play_sfx("stomp")
	queue_free()
