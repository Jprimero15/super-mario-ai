extends "res://scripts/game.gd"

func _draw() -> void:
	# Keep the canonical background/parallax renderer from game.gd so the
	# panorama stays aligned to the gameplay ground line and camera.
	super._draw()

	# Forest-specific foreground solids are drawn after the background.
	var cam_x: float = player.camera.global_position.x if is_instance_valid(player) and is_instance_valid(player.camera) else (player.position.x if is_instance_valid(player) else 640.0)
	var camera_zoom := player.camera.zoom if is_instance_valid(player) and is_instance_valid(player.camera) else Vector2.ONE
	var visible_world_width := get_viewport_rect().size.x / maxf(camera_zoom.x, 0.01)
	var first_visible_x: float = cam_x - visible_world_width * 0.75 - 64.0
	var last_visible_x: float = cam_x + visible_world_width * 0.75 + 64.0
	if not is_instance_valid(world):
		return
	for chunk_value in world.active_chunks.values():
		if not is_instance_valid(chunk_value):
			continue
		var chunk: MaryouChunk = chunk_value as MaryouChunk
		if chunk == null:
			continue
		for solid in chunk.solids:
			if solid.end.x < first_visible_x or solid.position.x > last_visible_x:
				continue
			draw_texture_rect_region(TILES, solid, Rect2(0.0, 0.0, 64.0, 64.0))
