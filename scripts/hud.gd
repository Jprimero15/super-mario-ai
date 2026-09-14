extends CanvasLayer
class_name MaryouHUD

signal pause_pressed
signal restart_pressed
signal back_pressed

var score_label: Label
var status_label: Label
var tier_label: Label
var overlay: ColorRect
var panel: PanelContainer
var title_label: Label
var info_label: Label
var settings_panel: PanelContainer
var music_slider: HSlider
var sfx_slider: HSlider

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_build()

func _build() -> void:
	var root := Control.new()
	root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	root.mouse_filter = Control.MOUSE_FILTER_IGNORE
	add_child(root)

	var top := MarginContainer.new()
	top.set_anchors_preset(Control.PRESET_TOP_WIDE)
	top.offset_left = 20.0
	top.offset_top = 16.0
	top.offset_right = -20.0
	top.offset_bottom = 92.0
	top.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(top)
	var top_row := HBoxContainer.new()
	top_row.add_theme_constant_override("separation", 16)
	top.add_child(top_row)

	var stats_box := VBoxContainer.new()
	stats_box.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	top_row.add_child(stats_box)
	score_label = _label("SCORE 0", 24)
	stats_box.add_child(score_label)
	status_label = _label("LIVES 3   COINS 0   ×1", 18)
	stats_box.add_child(status_label)

	var right_box := VBoxContainer.new()
	right_box.custom_minimum_size = Vector2(180, 0)
	top_row.add_child(right_box)
	tier_label = _label("TIER 1   0m", 18)
	tier_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	right_box.add_child(tier_label)
	var pause := Button.new()
	pause.text = "PAUSE"
	pause.custom_minimum_size = Vector2(0, 48)
	pause.process_mode = Node.PROCESS_MODE_ALWAYS
	pause.mouse_default_cursor_shape = Control.CURSOR_POINTING_HAND
	pause.pressed.connect(func(): pause_pressed.emit())
	right_box.add_child(pause)

	var controls := HBoxContainer.new()
	controls.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	controls.offset_left = 24.0
	controls.offset_top = -92.0
	controls.offset_right = -24.0
	controls.offset_bottom = -20.0
	controls.add_theme_constant_override("separation", 12)
	controls.mouse_filter = Control.MOUSE_FILTER_STOP
	root.add_child(controls)
	_add_control_button(controls, "◀", "move_left")
	_add_control_button(controls, "▶", "move_right")
	var spacer := Control.new()
	spacer.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	controls.add_child(spacer)
	_add_control_button(controls, "JUMP", "jump", true)

	overlay = ColorRect.new()
	overlay.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	overlay.color = Color(0.03, 0.05, 0.09, 0.72)
	overlay.mouse_filter = Control.MOUSE_FILTER_STOP
	overlay.visible = false
	root.add_child(overlay)

	panel = _make_panel("PAUSED")
	root.add_child(panel)
	settings_panel = _make_settings()
	root.add_child(settings_panel)

func _label(text: String, size: int) -> Label:
	var label := Label.new()
	label.text = text
	label.add_theme_font_size_override("font_size", size)
	label.add_theme_color_override("font_color", Color("#f4f6fb"))
	return label

func _add_control_button(parent: HBoxContainer, text: String, action: String, expand := false) -> void:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(86 if not expand else 140, 64)
	if expand:
		button.size_flags_horizontal = Control.SIZE_SHRINK_END
	button.add_theme_font_size_override("font_size", 20)
	button.mouse_default_cursor_shape = Control.CURSOR_POINTING_HAND
	button.button_down.connect(func(): Input.action_press(action))
	button.button_up.connect(func(): Input.action_release(action))
	parent.add_child(button)

func _make_panel(title: String) -> PanelContainer:
	var p := PanelContainer.new()
	p.set_anchors_preset(Control.PRESET_CENTER)
	p.offset_left = -210.0
	p.offset_top = -175.0
	p.offset_right = 210.0
	p.offset_bottom = 175.0
	p.visible = false
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 12)
	p.add_child(box)
	title_label = _label(title, 32)
	title_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(title_label)
	info_label = _label("", 18)
	info_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(info_label)
	_add_button(box, "Resume", func(): pause_pressed.emit())
	_add_button(box, "Restart", func(): restart_pressed.emit())
	_add_button(box, "Settings", func(): _show_settings(true))
	_add_button(box, "Back", func(): back_pressed.emit())
	return p

func _make_settings() -> PanelContainer:
	var p := PanelContainer.new()
	p.set_anchors_preset(Control.PRESET_CENTER)
	p.offset_left = -210.0
	p.offset_top = -190.0
	p.offset_right = 210.0
	p.offset_bottom = 190.0
	p.visible = false
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 10)
	p.add_child(box)
	var title := _label("SETTINGS", 30)
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(title)
	box.add_child(_label("Music volume", 18))
	music_slider = HSlider.new()
	music_slider.min_value = 0.0
	music_slider.max_value = 1.0
	music_slider.value = AudioManager.music_volume if has_node("/root/AudioManager") else 0.8
	music_slider.value_changed.connect(func(v): if has_node("/root/AudioManager"): AudioManager.set_music_volume(v))
	box.add_child(music_slider)
	box.add_child(_label("SFX volume", 18))
	sfx_slider = HSlider.new()
	sfx_slider.min_value = 0.0
	sfx_slider.max_value = 1.0
	sfx_slider.value = AudioManager.sfx_volume if has_node("/root/AudioManager") else 0.9
	sfx_slider.value_changed.connect(func(v): if has_node("/root/AudioManager"): AudioManager.set_sfx_volume(v))
	box.add_child(sfx_slider)
	_add_button(box, "Close", func(): _show_settings(false))
	return p

func _add_button(parent: VBoxContainer, text: String, callback: Callable) -> void:
	var button := Button.new()
	button.text = text
	button.custom_minimum_size = Vector2(0, 50)
	button.pressed.connect(callback)
	parent.add_child(button)

func show_pause(value: bool, score: int, best: int) -> void:
	if settings_panel.visible:
		settings_panel.visible = false
	overlay.visible = value
	panel.visible = value
	title_label.text = "PAUSED"
	info_label.text = "Score %d   Best %d" % [score, best]
	if value:
		panel.modulate = Color(1, 1, 1, 0)
		var tween := create_tween()
		tween.tween_property(panel, "modulate", Color.WHITE, 0.16)

func show_game_over(score: int, best: int) -> void:
	overlay.visible = true
	panel.visible = true
	title_label.text = "RUN COMPLETE"
	info_label.text = "Score %d   Best %d" % [score, best]

func update_stats(score: int, lives: int, coins: int, combo: int, tier: int, meters: int) -> void:
	score_label.text = "SCORE %d" % score
	status_label.text = "LIVES %d   COINS %d   ×%d" % [lives, coins, maxi(1, combo)]
	tier_label.text = "TIER %d   %dm" % [tier, meters]

func _show_settings(value: bool) -> void:
	settings_panel.visible = value
	if value:
		panel.visible = false
		overlay.visible = true
	else:
		panel.visible = true
