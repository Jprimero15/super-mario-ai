extends Node2D
class_name MaryouChunk

var solids: Array[Rect2] = []
var holes: Array[Rect2] = []
var pipes: Array[Rect2] = []

func _ready() -> void:
	queue_redraw()

func _draw() -> void:
	# Natural grassy running land with darker soil below the surface.
	for rect in solids:
		draw_rect(rect, Color("#5b3f2b"))
		draw_rect(Rect2(rect.position, Vector2(rect.size.x, 9.0)), Color("#78ad52"))
		draw_line(rect.position, Vector2(rect.end.x, rect.position.y), Color("#9bca6b"), 2.0)

	# Designed pits: deep layered shafts instead of a flat/empty black gap.
	# The lower layers fade toward the center, giving the hole a visible depth cue.
	for rect in holes:
		var lip := Rect2(rect.position + Vector2(0.0, -2.0), Vector2(rect.size.x, 7.0))
		draw_rect(Rect2(rect.position, Vector2(rect.size.x, 120.0)), Color("#18241f"))
		draw_rect(Rect2(rect.position + Vector2(3.0, 12.0), Vector2(maxf(0.0, rect.size.x - 6.0), 70.0)), Color("#111719"))
		draw_rect(Rect2(rect.position + Vector2(7.0, 30.0), Vector2(maxf(0.0, rect.size.x - 14.0), 38.0)), Color("#080d10"))
		draw_rect(lip, Color("#416b3d"))
	# Small rim highlights make the opening readable on mobile screens.
	for x in range(int(rect.position.x) + 5, int(rect.end.x), 12):
		draw_line(Vector2(x, rect.position.y + 4.0), Vector2(x + 5.0, rect.position.y + 9.0), Color("#8ab85d"), 2.0)

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
