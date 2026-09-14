extends CanvasLayer
class_name MaryouHUD

signal pause_pressed
signal restart_pressed
signal back_pressed

var steps_label: Label
var status_label: Label
var tier_label: Label
var overlay: ColorRect
var panel: PanelContainer
var title_label: Label
var info_label: Label
var settings_panel: PanelContainer
var music_slider: HSlider
var sfx_slider: HSlider
var compact_controls := false

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
	top.offset_top = 14.0
	top.offset_right = -20.0
	top.offset_bottom = 96.0
	top.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(top)
	var top_row := HBoxContainer.new()
	top_row.add_theme_constant_override("separation", 12)
	top.add_child(top_row)
	var stats := VBoxContainer.new()
	stats.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	top_row.add_child(stats)
	steps_label = _label("STEPS 0", 26)
	stats.add_child(steps_label)
	status_label = _label("COINS 0", 17)
	stats.add_child(status_label)
	var right := VBoxContainer.new()
	right.custom_minimum_size = Vector2(170, 0)
	top_row.add_child(right)
	tier_label = _label("TIER 1", 17)
	tier_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	right.add_child(tier_label)
	var pause := _button("PAUSE", 46)
	pause.process_mode = Node.PROCESS_MODE_ALWAYS
	pause.pressed.connect(func(): pause_pressed.emit())
	right.add_child(pause)

	var controls := HBoxContainer.new()
	controls.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	controls.offset_left = 18.0
	controls.offset_top = -102.0
	controls.offset_right = -18.0
	controls.offset_bottom = -16.0
	controls.add_theme_constant_override("separation", 10)
	controls.mouse_filter = Control.MOUSE_FILTER_STOP
	root.add_child(controls)
	_add_control_button(controls, "‹", "move_left", 82)
	_add_control_button(controls, "›", "move_right", 82)
	var spacer := Control.new()
	spacer.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	controls.add_child(spacer)
	_add_control_button(controls, "JUMP", "jump", 138)

	overlay = ColorRect.new()
	overlay.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	overlay.color = Color(0.02, 0.04, 0.08, 0.76)
	overlay.mouse_filter = Control.MOUSE_FILTER_STOP
	overlay.visible = false
	root.add_child(overlay)
	panel = _make_panel()
	root.add_child(panel)
	settings_panel = _make_settings()
	root.add_child(settings_panel)

func _label(text: String, size: int) -> Label:
	var label := Label.new()
	label.text = text
	label.add_theme_font_size_override("font_size", size)
	label.add_theme_color_override("font_color", Color("#f5f7ff"))
	return label

func _button(text: String, height: int) -> Button:
	var b := Button.new()
	b.text = text
	b.custom_minimum_size = Vector2(0, height)
	b.add_theme_font_size_override("font_size", 18)
	b.add_theme_color_override("font_color", Color("#f5f7ff"))
	b.add_theme_stylebox_override("normal", _box(Color("#1a2740"), 14))
	b.add_theme_stylebox_override("hover", _box(Color("#243758"), 14))
	b.add_theme_stylebox_override("pressed", _box(Color("#315080"), 14))
	return b

func _add_control_button(parent: HBoxContainer, text: String, action: String, width: int) -> void:
	var button := _button(text, 68)
	button.custom_minimum_size = Vector2(width, 68)
	button.add_theme_font_size_override("font_size", 28 if action != "jump" else 20)
	button.button_down.connect(func(): Input.action_press(action))
	button.button_up.connect(func(): Input.action_release(action))
	button.focus_mode = Control.FOCUS_NONE
	parent.add_child(button)

func _make_panel() -> PanelContainer:
	var p := PanelContainer.new()
	p.set_anchors_preset(Control.PRESET_CENTER)
	p.offset_left = -210.0
	p.offset_top = -170.0
	p.offset_right = 210.0
	p.offset_bottom = 170.0
	p.add_theme_stylebox_override("panel", _box(Color("#111d31"), 22))
	p.visible = false
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 12)
	p.add_child(box)
	title_label = _label("PAUSED", 32)
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
	p.offset_left = -220.0
	p.offset_top = -205.0
	p.offset_right = 220.0
	p.offset_bottom = 205.0
	p.add_theme_stylebox_override("panel", _box(Color("#111d31"), 22))
	p.visible = false
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 10)
	p.add_child(box)
	var title := _label("SETTINGS", 30)
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(title)
	box.add_child(_label("Music volume", 17))
	music_slider = HSlider.new()
	music_slider.min_value = 0.0
	music_slider.max_value = 1.0
	music_slider.step = 0.01
	music_slider.value = AudioManager.music_volume
	music_slider.value_changed.connect(func(v): AudioManager.set_music_volume(v))
	box.add_child(music_slider)
	box.add_child(_label("SFX volume", 17))
	sfx_slider = HSlider.new()
	sfx_slider.min_value = 0.0
	sfx_slider.max_value = 1.0
	sfx_slider.step = 0.01
	sfx_slider.value = AudioManager.sfx_volume
	sfx_slider.value_changed.connect(func(v): AudioManager.set_sfx_volume(v))
	box.add_child(sfx_slider)
	box.add_child(_label("Touch controls are intentionally large for phones.", 14))
	_add_button(box, "Close", func(): _show_settings(false))
	return p

func _add_button(parent: VBoxContainer, text: String, callback: Callable) -> void:
	var button := _button(text, 50)
	button.pressed.connect(callback)
	parent.add_child(button)

func show_pause(value: bool, steps: int, best_steps: int) -> void:
	if settings_panel.visible: settings_panel.visible = false
	overlay.visible = value
	panel.visible = value
	title_label.text = "PAUSED"
	info_label.text = "Steps %d   Best %d" % [steps, best_steps]
	_animate_panel(value)

func show_game_over(steps: int, best_steps: int) -> void:
	overlay.visible = true
	panel.visible = true
	title_label.text = "RUN COMPLETE"
	info_label.text = "Steps %d   Best %d" % [steps, best_steps]
	_animate_panel(true)

func update_stats(steps: int, coins: int, tier: int) -> void:
	steps_label.text = "STEPS %d" % steps
	status_label.text = "COINS %d" % coins
	tier_label.text = "TIER %d" % tier

func _show_settings(value: bool) -> void:
	settings_panel.visible = value
	if value:
		panel.visible = false
		overlay.visible = true
	else:
		panel.visible = true

func _animate_panel(value: bool) -> void:
	if not value: return
	panel.scale = Vector2(0.94, 0.94)
	panel.modulate = Color(1, 1, 1, 0)
	var tween := create_tween().set_parallel(true)
	tween.tween_property(panel, "scale", Vector2.ONE, 0.18).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
	tween.tween_property(panel, "modulate", Color.WHITE, 0.16)

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
