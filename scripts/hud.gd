extends CanvasLayer
class_name MaryouHUD

signal pause_pressed
signal restart_pressed
signal back_pressed

var steps_label: Label
var status_label: Label
var tier_label: Label
var checkpoint_notice: Label
var overlay: ColorRect
var panel: PanelContainer
var settings_panel: PanelContainer
var title_label: Label
var info_label: Label
var restart_button: Button
var resume_button: Button
var settings_button: Button
var music_button: Button
var sfx_button: Button
var vibration_button: Button
var settings_close_button: Button
var restart_confirmed := false
var compact := false

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_build()

func _build() -> void:
	var viewport_size: Vector2 = get_viewport().get_visible_rect().size
	compact = viewport_size.x < 900.0 or viewport_size.y < 620.0
	var root := Control.new()
	root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	add_child(root)

	var top_panel := PanelContainer.new()
	top_panel.set_anchors_preset(Control.PRESET_TOP_WIDE)
	top_panel.offset_left = 12.0
	top_panel.offset_top = 8.0
	top_panel.offset_right = -12.0
	top_panel.offset_bottom = 96.0
	top_panel.add_theme_stylebox_override("panel", _box(Color(0.05, 0.10, 0.16, 0.76), 18))
	root.add_child(top_panel)

	var top := MarginContainer.new()
	top.add_theme_constant_override("margin_left", 14)
	top.add_theme_constant_override("margin_right", 14)
	top.add_theme_constant_override("margin_top", 6)
	top.add_theme_constant_override("margin_bottom", 6)
	top_panel.add_child(top)
	var row := HBoxContainer.new()
	row.add_theme_constant_override("separation", 12)
	top.add_child(row)
	var stats := VBoxContainer.new()
	stats.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	row.add_child(stats)
	steps_label = _label("STEPS 0", 30 if not compact else 23)
	stats.add_child(steps_label)
	status_label = _label("COINS 0  •  HP 1", 18 if not compact else 15)
	stats.add_child(status_label)
	var right := VBoxContainer.new()
	right.custom_minimum_size = Vector2(164 if not compact else 138, 0)
	row.add_child(right)
	tier_label = _label("TIER 1", 18 if not compact else 15)
	tier_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	right.add_child(tier_label)
	var pause := _button("PAUSE", 52 if not compact else 46)
	pause.pressed.connect(func():
		AudioManager.play_sfx("ui")
		pause_pressed.emit()
	)
	right.add_child(pause)

	var controls := HBoxContainer.new()
	controls.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	controls.offset_left = 14.0
	controls.offset_top = -98.0
	controls.offset_right = -14.0
	controls.offset_bottom = -12.0
	controls.add_theme_constant_override("separation", 10)
	root.add_child(controls)
	_add_control_button(controls, "‹", "move_left", 104 if not compact else 88, 82 if not compact else 70)
	_add_control_button(controls, "›", "move_right", 104 if not compact else 88, 82 if not compact else 70)
	var spacer := Control.new()
	spacer.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	controls.add_child(spacer)
	_add_control_button(controls, "JUMP", "jump", 170 if not compact else 142, 82 if not compact else 70)

	overlay = ColorRect.new()
	overlay.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	overlay.color = Color(0.02, 0.04, 0.08, 0.80)
	overlay.visible = false
	root.add_child(overlay)

	checkpoint_notice = _label("CHECKPOINT SAVED", 21 if not compact else 18)
	checkpoint_notice.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	checkpoint_notice.set_anchors_preset(Control.PRESET_CENTER_TOP)
	checkpoint_notice.offset_left = -180.0
	checkpoint_notice.offset_top = 116.0
	checkpoint_notice.offset_right = 180.0
	checkpoint_notice.offset_bottom = 152.0
	checkpoint_notice.visible = false
	root.add_child(checkpoint_notice)

	panel = _make_panel(viewport_size)
	root.add_child(panel)
	settings_panel = _make_settings_panel(viewport_size)
	root.add_child(settings_panel)

func _label(text: String, size: int) -> Label:
	var label := Label.new()
	label.text = text
	label.add_theme_font_size_override("font_size", size)
	label.add_theme_color_override("font_color", Color("#f5f7ff"))
	label.add_theme_color_override("font_outline_color", Color(0.02, 0.04, 0.08, 0.92))
	label.add_theme_constant_override("outline_size", 3)
	return label

func _button(text: String, height: int) -> Button:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(0, height)
	button.add_theme_font_size_override("font_size", 19 if height >= 46 else 16)
	button.add_theme_color_override("font_color", Color("#f5f7ff"))
	button.add_theme_color_override("font_hover_color", Color("#ffffff"))
	button.add_theme_stylebox_override("normal", _box(Color("#1a2740"), 16))
	button.add_theme_stylebox_override("hover", _box(Color("#294365"), 16))
	button.add_theme_stylebox_override("pressed", _box(Color("#3c5f8e"), 16))
	button.focus_mode = Control.FOCUS_NONE
	return button

func _add_control_button(parent: HBoxContainer, text: String, action: String, width: int, height: int) -> void:
	var button := _button(text, height)
	button.custom_minimum_size = Vector2(width, height)
	button.add_theme_font_size_override("font_size", 32 if action != "jump" else 22)
	button.button_down.connect(func(): Input.action_press(action))
	button.button_up.connect(func(): Input.action_release(action))
	button.focus_mode = Control.FOCUS_NONE
	button.mouse_filter = Control.MOUSE_FILTER_STOP
	parent.add_child(button)

func _make_panel(viewport_size: Vector2) -> PanelContainer:
	var width := minf(450.0, maxf(300.0, viewport_size.x - 32.0))
	var height := minf(390.0, maxf(300.0, viewport_size.y - 80.0))
	var p := PanelContainer.new()
	p.set_anchors_preset(Control.PRESET_CENTER)
	p.offset_left = -width * 0.5
	p.offset_top = -height * 0.5
	p.offset_right = width * 0.5
	p.offset_bottom = height * 0.5
	p.add_theme_stylebox_override("panel", _box(Color("#111d31"), 24))
	p.visible = false
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 12)
	p.add_child(box)
	title_label = _label("PAUSED", 34 if not compact else 28)
	title_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(title_label)
	info_label = _label("", 19 if not compact else 16)
	info_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	info_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	box.add_child(info_label)
	resume_button = _panel_button(box, "Resume", func(): pause_pressed.emit())
	restart_button = _panel_button(box, "Restart", func(): _request_restart())
	settings_button = _panel_button(box, "Settings", func(): _open_settings())
	_panel_button(box, "Back", func(): back_pressed.emit())
	return p

func _make_settings_panel(viewport_size: Vector2) -> PanelContainer:
	var width := minf(430.0, maxf(300.0, viewport_size.x - 32.0))
	var height := minf(400.0, maxf(310.0, viewport_size.y - 70.0))
	var p := PanelContainer.new()
	p.set_anchors_preset(Control.PRESET_CENTER)
	p.offset_left = -width * 0.5
	p.offset_top = -height * 0.5
	p.offset_right = width * 0.5
	p.offset_bottom = height * 0.5
	p.add_theme_stylebox_override("panel", _box(Color("#111d31"), 24))
	p.visible = false
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 12)
	p.add_child(box)
	var title := _label("SETTINGS", 32 if not compact else 27)
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(title)
	music_button = _panel_button(box, "", func(): _toggle_music())
	sfx_button = _panel_button(box, "", func(): _toggle_sfx())
	vibration_button = _panel_button(box, "", func(): _toggle_vibration())
	settings_close_button = _panel_button(box, "Done", func(): _close_settings())
	_refresh_settings()
	return p

func _panel_button(parent: VBoxContainer, text: String, callback: Callable) -> Button:
	var button := _button(text, 56 if not compact else 50)
	button.pressed.connect(func():
		AudioManager.play_sfx("ui")
		callback.call()
	)
	parent.add_child(button)
	return button

func _refresh_settings() -> void:
	if not is_instance_valid(music_button):
		return
	music_button.text = "Music: ON" if AudioManager.music_volume > 0.01 else "Music: OFF"
	sfx_button.text = "Sound Effects: ON" if AudioManager.sfx_volume > 0.01 else "Sound Effects: OFF"
	vibration_button.text = "Vibration: ON" if AudioManager.vibration_enabled else "Vibration: OFF"

func _open_settings() -> void:
	_release_controls()
	panel.visible = false
	settings_panel.visible = true
	overlay.visible = true
	_refresh_settings()

func _close_settings() -> void:
	settings_panel.visible = false
	panel.visible = true
	overlay.visible = true

func _toggle_music() -> void:
	AudioManager.set_music_volume(0.0 if AudioManager.music_volume > 0.01 else 0.8)
	_refresh_settings()

func _toggle_sfx() -> void:
	AudioManager.set_sfx_volume(0.0 if AudioManager.sfx_volume > 0.01 else 0.9)
	_refresh_settings()

func _toggle_vibration() -> void:
	AudioManager.set_vibration_enabled(not AudioManager.vibration_enabled)
	_refresh_settings()

func show_pause(value: bool, steps: int, best_steps: int) -> void:
	if value:
		_release_controls()
		resume_button.visible = true
		title_label.text = "PAUSED"
		info_label.text = "Steps %d   Best %d" % [steps, best_steps]
		settings_button.visible = true
		overlay.visible = true
		panel.visible = true
		settings_panel.visible = false
	else:
		panel.visible = false
		settings_panel.visible = false
		overlay.visible = false

func show_game_over(steps: int, best_steps: int, new_best: bool = false, run_coins: int = 0, total_coins: int = 0) -> void:
	_release_controls()
	resume_button.visible = false
	settings_button.visible = true
	_restart_button_reset()
	title_label.text = "NEW BEST!" if new_best else "GAME OVER"
	info_label.text = "Steps %d   Best %d\nRun coins %d   Total coins %d" % [steps, best_steps, run_coins, total_coins]
	overlay.visible = true
	panel.visible = true
	settings_panel.visible = false

func update_stats(steps: int, coins: int, tier: int, shielded: bool = false, best_steps: int = 0) -> void:
	steps_label.text = "STEPS %d" % steps
	status_label.text = "COINS %d  •  BEST %d" % [coins, best_steps] if not shielded else "COINS %d  •  SHIELD" % coins
	tier_label.text = "DEEP FOREST" if tier >= 4 else "TIER %d" % tier

func show_checkpoint_notice() -> void:
	checkpoint_notice.visible = true
	checkpoint_notice.modulate = Color.WHITE
	var tween := create_tween()
	tween.tween_property(checkpoint_notice, "modulate", Color(1, 1, 1, 0), 0.7).set_delay(0.15)
	tween.tween_callback(func(): checkpoint_notice.visible = false)

func _request_restart() -> void:
	if restart_confirmed:
		restart_pressed.emit()
		return
	restart_confirmed = true
	restart_button.text = "CONFIRM RESTART"
	get_tree().create_timer(2.5, true).timeout.connect(_restart_button_reset)

func _restart_button_reset() -> void:
	restart_confirmed = false
	if is_instance_valid(restart_button):
		restart_button.text = "Restart"

func _release_controls() -> void:
	Input.action_release("move_left")
	Input.action_release("move_right")
	Input.action_release("jump")

func _exit_tree() -> void:
	_release_controls()

func _box(color: Color, radius: int) -> StyleBoxFlat:
	var box := StyleBoxFlat.new()
	box.bg_color = color
	box.corner_radius_top_left = radius
	box.corner_radius_top_right = radius
	box.corner_radius_bottom_left = radius
	box.corner_radius_bottom_right = radius
	box.content_margin_left = 16.0
	box.content_margin_right = 16.0
	box.content_margin_top = 8.0
	box.content_margin_bottom = 8.0
	return box
