extends Area2D
class_name MaryouCollectible

signal collected(kind: String)

const COIN_FRAMES := preload("res://assets/sprites/coin_frames.tres")

var kind := "coin"
var active := true
var animated_sprite: AnimatedSprite2D

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
	if kind == "coin":
		animated_sprite = AnimatedSprite2D.new()
		animated_sprite.sprite_frames = COIN_FRAMES
		animated_sprite.animation = &"spin"
		animated_sprite.texture_filter = CanvasItem.TEXTURE_FILTER_NEAREST
		animated_sprite.scale = Vector2(0.55, 0.55)
		add_child(animated_sprite)
	body_entered.connect(_on_body_entered)
	queue_redraw()

func _on_body_entered(body: Node2D) -> void:
	if not active or not body is MaryouPlayer: return
	active = false
	collected.emit(kind)
	queue_free()

func _draw() -> void:
	if kind == "shield":
		draw_circle(Vector2.ZERO, 14.0, Color("#59d9c4"))
		draw_arc(Vector2.ZERO, 18.0, 0.0, TAU, 24, Color("#d9fff8"), 3.0)
