extends Node2D
class_name MaryouChunk

var solids: Array[Rect2] = []
var holes: Array[Rect2] = []
var pipes: Array[Rect2] = []

func _draw() -> void:
	for rect in solids:
		draw_rect(rect, Color("#2b3b55"))
		draw_line(rect.position, Vector2(rect.end.x, rect.position.y), Color("#526b8d"), 2.0)
	for rect in holes:
		draw_rect(rect, Color("#101827"))
	for rect in pipes:
		draw_style_box(_box(Color("#3aaf83"), 8), rect)
		draw_rect(Rect2(rect.position.x - 3.0, rect.position.y, rect.size.x + 6.0, 12.0), Color("#5fd1a0"))

func _box(color: Color, radius: int) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = color
	box.corner_radius_top_left = radius
	box.corner_radius_top_right = radius
	box.corner_radius_bottom_left = radius
	box.corner_radius_bottom_right = radius
	return box
