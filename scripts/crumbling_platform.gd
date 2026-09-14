extends AnimatableBody2D

@export var crumble_delay := 0.55
var triggered := false

func _ready() -> void:
	add_to_group("hazards")
	$Trigger.body_entered.connect(_on_body_entered)

func _on_body_entered(body: Node2D) -> void:
	if triggered or not body is MaryouPlayer:
		return
	triggered = true
	var tween := create_tween()
	tween.tween_property($Sprite2D, "rotation", 0.08, 0.12)
	tween.tween_property($Sprite2D, "rotation", -0.08, 0.12)
	tween.tween_property($Sprite2D, "rotation", 0.0, 0.08)
	await get_tree().create_timer(crumble_delay, true, false, true).timeout
	$CollisionShape2D.set_deferred("disabled", true)
	$Sprite2D.visible = false
	await get_tree().create_timer(0.2, true, false, true).timeout
	queue_free()