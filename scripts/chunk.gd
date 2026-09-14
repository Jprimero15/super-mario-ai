extends Node2D
class_name MaryouChunk

var solids: Array[Rect2] = []
var holes: Array[Rect2] = []
var pipes: Array[Rect2] = []

func _ready() -> void:
	queue_redraw()

func _draw() -> void:
	# Ground terrain is rendered by game.gd from the actual tiles.svg atlas.
	# Keeping it here too would draw a second, different terrain layer over holes.

	# Designed pits: deep layered shafts instead of a flat/empty black gap.
	# The lower layers fade toward the center, giving the hole a visible depth cue.
	for hole in holes:
		var lip := Rect2(hole.position + Vector2(0.0, -2.0), Vector2(hole.size.x, 7.0))
		draw_rect(Rect2(hole.position, Vector2(hole.size.x, 120.0)), Color("#18241f"))
		draw_rect(Rect2(hole.position + Vector2(3.0, 12.0), Vector2(maxf(0.0, hole.size.x - 6.0), 70.0)), Color("#111719"))
		draw_rect(Rect2(hole.position + Vector2(7.0, 30.0), Vector2(maxf(0.0, hole.size.x - 14.0), 38.0)), Color("#080d10"))
		draw_rect(lip, Color("#416b3d"))
		for x in range(int(hole.position.x) + 5, int(hole.end.x), 12):
			draw_line(Vector2(x, hole.position.y + 4.0), Vector2(x + 5.0, hole.position.y + 9.0), Color("#8ab85d"), 2.0)

	# Pipes remain visually represented by their generated pipe rectangles.
	for pipe in pipes:
		draw_style_box(_box(Color("#3aaf65"), 8), pipe)
		draw_rect(Rect2(pipe.position.x - 4.0, pipe.position.y, pipe.size.x + 8.0, 12.0), Color("#70d487"))
		draw_line(Vector2(pipe.position.x + 6.0, pipe.position.y + 15.0), Vector2(pipe.position.x + 6.0, pipe.end.y), Color("#2b814d"), 3.0)

func _box(color: Color, radius: int) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = color
	box.corner_radius_top_left = radius
	box.corner_radius_top_right = radius
	box.corner_radius_bottom_left = radius
	box.corner_radius_bottom_right = radius
	return box
