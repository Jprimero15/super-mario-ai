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
var title_label: Label
var info_label: Label
var settings_panel: PanelContainer
var music_slider: HSlider
var sfx_slider: HSlider
var music_value_label: Label
var sfx_value_label: Label
var restart_button: Button
var resume_button: Button
var music_mute_button: Button
var sfx_mute_button: Button
var compact_controls := false
var restart_confirmed := false
var music_before_mute := 0.8
var sfx_before_mute := 0.9

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_build()

func _build() -> void:
	var viewport_size := get_viewport_rect().size
	compact_controls = viewport_size.x < 900.0 or viewport_size.y < 620.0
	music_before_mute = AudioManager.music_volume
	sfx_before_mute = AudioManager.sfx_volume

	var root := Control.new()
	root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	root.mouse_filter = Control.MOUSE_FILTER_IGNORE
	add_child(root)

	var top := MarginContainer.new()
	top.set_anchors_preset(Control.PRESET_TOP_WIDE)
	top.offset_left = 16.0
	top.offset_top = 10.0
	top.offset_right = -16.0
	top.offset_bottom = 94.0
	top.mouse_filter = Control.MOUSE_FILTER_IGNORE
	root.add_child(top)
	var top_row := HBoxContainer.new()
	top_row.add_theme_constant_override("separation", 10)
	top.add_child(top_row)
	var stats := VBoxContainer.new()
	stats.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	top_row.add_child(stats)
	steps_label = _label("STEPS 0", 28 if not compact_controls else 22)
	stats.add_child(steps_label)
	status_label = _label("COINS 0  •  HP 1", 17 if not compact_controls else 14)
	stats.add_child(status_label)

	var right := VBoxContainer.new()
	right.custom_minimum_size = Vector2(150 if not compact_controls else 128, 0)
	top_row.add_child(right)
	tier_label = _label("TIER 1", 17 if not compact_controls else 15)
	tier_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	right.add_child(tier_label)
	var pause := _button("PAUSE", 44 if not compact_controls else 40)
	pause.process_mode = Node.PROCESS_MODE_ALWAYS
	pause.pressed.connect(func(): pause_pressed.emit())
	right.add_child(pause)

	var controls := HBoxContainer.new()
	controls.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	var control_height := 68 if not compact_controls else 56
	var side_width := 82 if not compact_controls else 72
	var jump_width := 138 if not compact_controls else 112
	controls.offset_left = 14.0
	controls.offset_top = -float(control_height + 12)
	controls.offset_right = -14.0
	controls.offset_bottom = -12.0
	controls.add_theme_constant_override("separation", 8)
	controls.mouse_filter = Control.MOUSE_FILTER_STOP
	root.add_child(controls)
	_add_control_button(controls, "‹", "move_left", side_width, control_height)
	_add_control_button(controls, "›", "move_right", side_width, control_height)
	var spacer := Control.new()
	spacer.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	controls.add_child(spacer)
	_add_control_button(controls, "JUMP", "jump", jump_width, control_height)

	overlay = ColorRect.new()
	overlay.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	overlay.color = Color(0.02, 0.04, 0.08, 0.78)
	overlay.mouse_filter = Control.MOUSE_FILTER_STOP
	overlay.visible = false
	root.add_child(overlay)

	checkpoint_notice = _label("CHECKPOINT SAVED", 20 if not compact_controls else 17)
	checkpoint_notice.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	checkpoint_notice.set_anchors_preset(Control.PRESET_CENTER_TOP)
	checkpoint_notice.offset_left = -160.0
	checkpoint_notice.offset_top = 110.0
	checkpoint_notice.offset_right = 160.0
	checkpoint_notice.offset_bottom = 146.0
	checkpoint_notice.visible = false
	root.add_child(checkpoint_notice)

	panel = _make_panel(viewport_size)
	root.add_child(panel)
	settings_panel = _make_settings(viewport_size)
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
	var b := Button.new()
	b.text = text
	b.custom_minimum_size = Vector2(0, height)
	b.add_theme_font_size_override("font_size", 18 if height >= 44 else 16)
	b.add_theme_color_override("font_color", Color("#f5f7ff"))
	b.add_theme_color_override("font_outline_color", Color(0.02, 0.04, 0.08, 0.9))
	b.add_theme_constant_override("outline_size", 2)
	b.add_theme_stylebox_override("normal", _box(Color("#1a2740"), 14))
	b.add_theme_stylebox_override("hover", _box(Color("#243758"), 14))
	b.add_theme_stylebox_override("pressed", _box(Color("#315080"), 14))
	return b

func _add_control_button(parent: HBoxContainer, text: String, action: String, width: int, height: int) -> void:
	var button := _button(text, height)
	button.custom_minimum_size = Vector2(width, height)
	button.add_theme_font_size_override("font_size", 28 if action != "jump" else 20)
	button.button_down.connect(func(): Input.action_press(action))
	button.button_up.connect(func(): Input.action_release(action))
	button.focus_mode = Control.FOCUS_NONE
	parent.add_child(button)

func _make_panel(viewport_size: Vector2) -> PanelContainer:
	var width := minf(420.0, maxf(280.0, viewport_size.x - 32.0))
	var height := minf(350.0, maxf(270.0, viewport_size.y - 90.0))
	var p := PanelContainer.new()
	p.set_anchors_preset(Control.PRESET_CENTER)
	p.offset_left = -width * 0.5
	p.offset_top = -height * 0.5
	p.offset_right = width * 0.5
	p.offset_bottom = height * 0.5
	p.add_theme_stylebox_override("panel", _box(Color("#111d31"), 22))
	p.visible = false
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 10)
	p.add_child(box)
	title_label = _label("PAUSED", 32 if not compact_controls else 27)
	title_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(title_label)
	info_label = _label("", 18 if not compact_controls else 15)
	info_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(info_label)
	resume_button = _add_button(box, "Resume", func(): pause_pressed.emit())
	restart_button = _add_button(box, "Restart", func(): _request_restart())
	_add_button(box, "Settings", func(): _show_settings(true))
	_add_button(box, "Back", func(): back_pressed.emit())
	return p

func _make_settings(viewport_size: Vector2) -> PanelContainer:
	var width := minf(440.0, maxf(290.0, viewport_size.x - 32.0))
	var height := minf(420.0, maxf(330.0, viewport_size.y - 64.0))
	var p := PanelContainer.new()
	p.set_anchors_preset(Control.PRESET_CENTER)
	p.offset_left = -width * 0.5
	p.offset_top = -height * 0.5
	p.offset_right = width * 0.5
	p.offset_bottom = height * 0.5
	p.add_theme_stylebox_override("panel", _box(Color("#111d31"), 22))
	p.visible = false
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 8)
	p.add_child(box)
	var title := _label("SETTINGS", 30 if not compact_controls else 26)
	title.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(title)

	box.add_child(_label("Music volume", 17))
	var music_row := HBoxContainer.new()
	box.add_child(music_row)
	music_slider = _make_slider(AudioManager.music_volume)
	music_slider.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	music_slider.value_changed.connect(_on_music_slider)
	music_row.add_child(music_slider)
	music_value_label = _label(_percent(AudioManager.music_volume), 14)
	music_value_label.custom_minimum_size = Vector2(48, 0)
	music_value_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	music_row.add_child(music_value_label)
	music_mute_button = _button("Mute", 38)
	music_mute_button.pressed.connect(_toggle_music_mute)
	music_row.add_child(music_mute_button)

	box.add_child(_label("SFX volume", 17))
	var sfx_row := HBoxContainer.new()
	box.add_child(sfx_row)
	sfx_slider = _make_slider(AudioManager.sfx_volume)
	sfx_slider.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	sfx_slider.value_changed.connect(_on_sfx_slider)
	sfx_row.add_child(sfx_slider)
	sfx_value_label = _label(_percent(AudioManager.sfx_volume), 14)
	sfx_value_label.custom_minimum_size = Vector2(48, 0)
	sfx_value_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	sfx_row.add_child(sfx_value_label)
	sfx_mute_button = _button("Mute", 38)
	sfx_mute_button.pressed.connect(_toggle_sfx_mute)
	sfx_row.add_child(sfx_mute_button)

	box.add_child(_label("Touch controls are large and match the actual control buttons.", 14))
	_add_button(box, "Close", func(): _show_settings(false))
	return p

func _make_slider(value: float) -> HSlider:
	var slider := HSlider.new()
	slider.min_value = 0.0
	slider.max_value = 1.0
	slider.step = 0.01
	slider.value = value
	return slider

func _add_button(parent: VBoxContainer, text: String, callback: Callable) -> Button:
	var button := _button(text, 50 if not compact_controls else 46)
	button.pressed.connect(callback)
	parent.add_child(button)
	return button

func show_pause(value: bool, steps: int, best_steps: int) -> void:
	if settings_panel.visible:
		settings_panel.visible = false
	if value:
		resume_button.visible = true
		title_label.text = "PAUSED"
		info_label.text = "Steps %d   Best %d" % [steps, best_steps]
		overlay.visible = true
		panel.visible = true
		_animate_panel(true)
	else:
		_animate_panel(false)

func show_game_over(steps: int, best_steps: int) -> void:
	resume_button.visible = false
	_restart_button_reset()
	overlay.visible = true
	panel.visible = true
	title_label.text = "GAME OVER"
	info_label.text = "Steps %d   Best %d" % [steps, best_steps]
	_animate_panel(true)

func update_stats(steps: int, coins: int, tier: int, shielded: bool = false) -> void:
	steps_label.text = "STEPS %d" % steps
	status_label.text = "COINS %d  •  SHIELD" % coins if shielded else "COINS %d  •  HP 1" % coins
	tier_label.text = "TIER %d" % tier

func show_checkpoint_notice() -> void:
	checkpoint_notice.visible = true
	checkpoint_notice.modulate = Color.WHITE
	checkpoint_notice.position.y = 0.0
	var tween := create_tween().set_parallel(true)
	tween.tween_property(checkpoint_notice, "position:y", -12.0, 0.7).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	tween.tween_property(checkpoint_notice, "modulate", Color(1, 1, 1, 0), 0.7).set_delay(0.15)
	tween.chain().tween_callback(func(): checkpoint_notice.visible = false)

func _show_settings(value: bool) -> void:
	if value:
		panel.visible = false
		overlay.visible = true
		settings_panel.visible = true
		settings_panel.scale = Vector2(0.96, 0.96)
		settings_panel.modulate = Color(1, 1, 1, 0)
		var tween := create_tween().set_parallel(true)
		tween.tween_property(settings_panel, "scale", Vector2.ONE, 0.16).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
		tween.tween_property(settings_panel, "modulate", Color.WHITE, 0.14)
	else:
		var tween := create_tween().set_parallel(true)
		tween.tween_property(settings_panel, "scale", Vector2(0.96, 0.96), 0.12).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
		tween.tween_property(settings_panel, "modulate", Color(1, 1, 1, 0), 0.10)
		tween.chain().tween_callback(func():
			settings_panel.visible = false
			panel.visible = true
			_animate_panel(true)
	)

func _animate_panel(value: bool) -> void:
	if value:
		panel.scale = Vector2(0.94, 0.94)
		panel.modulate = Color(1, 1, 1, 0)
		var tween := create_tween().set_parallel(true)
		tween.tween_property(panel, "scale", Vector2.ONE, 0.18).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)
		tween.tween_property(panel, "modulate", Color.WHITE, 0.16)
	else:
		var tween := create_tween().set_parallel(true)
		tween.tween_property(panel, "scale", Vector2(0.96, 0.96), 0.12).set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_IN)
		tween.tween_property(panel, "modulate", Color(1, 1, 1, 0), 0.10)
		tween.chain().tween_callback(func():
			panel.visible = false
			overlay.visible = false
	)

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

func _on_music_slider(value: float) -> void:
	AudioManager.set_music_volume(value)
	if value > 0.0: music_before_mute = value
	music_value_label.text = _percent(value)
	music_mute_button.text = "Unmute" if is_zero_approx(value) else "Mute"

func _on_sfx_slider(value: float) -> void:
	AudioManager.set_sfx_volume(value)
	if value > 0.0: sfx_before_mute = value
	sfx_value_label.text = _percent(value)
	sfx_mute_button.text = "Unmute" if is_zero_approx(value) else "Mute"

func _toggle_music_mute() -> void:
	if AudioManager.music_volume > 0.0:
		music_before_mute = AudioManager.music_volume
		music_slider.value = 0.0
	else:
		music_slider.value = maxf(music_before_mute, 0.25)

func _toggle_sfx_mute() -> void:
	if AudioManager.sfx_volume > 0.0:
		sfx_before_mute = AudioManager.sfx_volume
		sfx_slider.value = 0.0
	else:
		sfx_slider.value = maxf(sfx_before_mute, 0.25)

func _percent(value: float) -> String:
	return "%d%%" % int(round(value * 100.0))

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
