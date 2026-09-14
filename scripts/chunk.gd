extends Node2D
class_name MaryouChunk

var solids: Array[Rect2] = []
var holes: Array[Rect2] = []
var pipes: Array[Rect2] = []

func _draw() -> void:
	# Natural grassy running land with darker soil below the surface.
	for rect in solids:
		draw_rect(rect, Color("#5b3f2b"))
		draw_rect(Rect2(rect.position, Vector2(rect.size.x, 9.0)), Color("#78ad52"))
		draw_line(rect.position, Vector2(rect.end.x, rect.position.y), Color("#9bca6b"), 2.0)
	for rect in holes:
		draw_rect(rect, Color("#25402d"))
		draw_line(Vector2(rect.position.x, rect.position.y), Vector2(rect.end.x, rect.position.y), Color("#6b9b4d"), 3.0)
	# Classic green pipe obstacles: tall, solid and visually readable.
	for rect in pipes:
		draw_style_box(_box(Color("#3aaf65"), 8), rect)
		draw_rect(Rect2(rect.position.x - 4.0, rect.position.y, rect.size.x + 8.0, 12.0), Color("#70d487"))
		draw_line(Vector2(rect.position.x + 6.0, rect.position.y + 15.0), Vector2(rect.position.x + 6.0, rect.end.y), Color("#2b814d"), 3.0)

func _box(color: Color, radius: int) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = color
	box.corner_radius_top_left = radius
	box.corner_radius_top_right = radius
	box.corner_radius_bottom_left = radius
	box.corner_radius_bottom_right = radius
	return box
