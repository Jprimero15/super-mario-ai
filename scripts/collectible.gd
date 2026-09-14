extends Area2D
class_name MaryouCollectible

signal collected(kind: String)

var kind := "coin"
var active := true

func setup(collectible_kind: String) -> void:
	kind = collectible_kind
	collision_layer = 0
	collision_mask = 1
	monitoring = true
	var shape := CircleShape2D.new()
	shape.radius = 15.0
	var collider := CollisionShape2D.new()
	collider.shape = shape
	add_child(collider)
	body_entered.connect(_on_body_entered)
	queue_redraw()

func _on_body_entered(body: Node2D) -> void:
	if not active or not body is MaryouPlayer:
		return
	active = false
	collected.emit(kind)
	queue_free()

func _draw() -> void:
	if kind == "shield":
		draw_circle(Vector2.ZERO, 14.0, Color("#59d9c4"))
		draw_arc(Vector2.ZERO, 18.0, 0.0, TAU, 24, Color("#d9fff8"), 3.0)
	else:
		draw_circle(Vector2.ZERO, 11.0, Color("#ffd45a"))
		draw_circle(Vector2.ZERO, 6.0, Color("#fff1a6"))
