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
var restart_button: Button
var resume_button: Button
var restart_confirmed := false

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	_build()

func _build() -> void:
	var viewport_size: Vector2 = get_viewport().get_visible_rect().size
	var compact := viewport_size.x < 900.0 or viewport_size.y < 620.0
	var root := Control.new()
	root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	add_child(root)

	var top := MarginContainer.new()
	top.set_anchors_preset(Control.PRESET_TOP_WIDE)
	top.offset_left = 16.0
	top.offset_top = 10.0
	top.offset_right = -16.0
	top.offset_bottom = 92.0
	root.add_child(top)
	var row := HBoxContainer.new()
	row.add_theme_constant_override("separation", 10)
	top.add_child(row)
	var stats := VBoxContainer.new()
	stats.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	row.add_child(stats)
	steps_label = _label("STEPS 0", 28 if not compact else 22)
	stats.add_child(steps_label)
	status_label = _label("COINS 0  •  HP 1", 17 if not compact else 14)
	stats.add_child(status_label)
	var right := VBoxContainer.new()
	right.custom_minimum_size = Vector2(150 if not compact else 128, 0)
	row.add_child(right)
	tier_label = _label("TIER 1", 17 if not compact else 15)
	tier_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
	right.add_child(tier_label)
	var pause := _button("PAUSE", 44 if not compact else 40)
	pause.pressed.connect(func(): pause_pressed.emit())
	right.add_child(pause)

	var controls := HBoxContainer.new()
	controls.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
	controls.offset_left = 14.0
	controls.offset_top = -82.0
	controls.offset_right = -14.0
	controls.offset_bottom = -12.0
	controls.add_theme_constant_override("separation", 8)
	root.add_child(controls)
	_add_control_button(controls, "‹", "move_left", 82 if not compact else 72, 68 if not compact else 56)
	_add_control_button(controls, "›", "move_right", 82 if not compact else 72, 68 if not compact else 56)
	var spacer := Control.new()
	spacer.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	controls.add_child(spacer)
	_add_control_button(controls, "JUMP", "jump", 138 if not compact else 112, 68 if not compact else 56)

	overlay = ColorRect.new()
	overlay.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	overlay.color = Color(0.02, 0.04, 0.08, 0.78)
	overlay.visible = false
	root.add_child(overlay)

	checkpoint_notice = _label("CHECKPOINT SAVED", 20 if not compact else 17)
	checkpoint_notice.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	checkpoint_notice.set_anchors_preset(Control.PRESET_CENTER_TOP)
	checkpoint_notice.offset_left = -160.0
	checkpoint_notice.offset_top = 110.0
	checkpoint_notice.offset_right = 160.0
	checkpoint_notice.offset_bottom = 146.0
	checkpoint_notice.visible = false
	root.add_child(checkpoint_notice)

	panel = _make_panel(viewport_size, compact)
	root.add_child(panel)

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
	button.add_theme_font_size_override("font_size", 18 if height >= 44 else 16)
	button.add_theme_color_override("font_color", Color("#f5f7ff"))
	button.add_theme_stylebox_override("normal", _box(Color("#1a2740"), 14))
	button.add_theme_stylebox_override("hover", _box(Color("#243758"), 14))
	button.add_theme_stylebox_override("pressed", _box(Color("#315080"), 14))
	return button

func _add_control_button(parent: HBoxContainer, text: String, action: String, width: int, height: int) -> void:
	var button := _button(text, height)
	button.custom_minimum_size = Vector2(width, height)
	button.add_theme_font_size_override("font_size", 28 if action != "jump" else 20)
	button.button_down.connect(func(): Input.action_press(action))
	button.button_up.connect(func(): Input.action_release(action))
	button.focus_mode = Control.FOCUS_NONE
	parent.add_child(button)

func _make_panel(viewport_size: Vector2, compact: bool) -> PanelContainer:
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
	title_label = _label("PAUSED", 32 if not compact else 27)
	title_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(title_label)
	info_label = _label("", 18 if not compact else 15)
	info_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	box.add_child(info_label)
	resume_button = _panel_button(box, "Resume", func(): pause_pressed.emit())
	restart_button = _panel_button(box, "Restart", func(): _request_restart())
	_panel_button(box, "Back", func(): back_pressed.emit())
	return p

func _panel_button(parent: VBoxContainer, text: String, callback: Callable) -> Button:
	var button := _button(text, 50)
	button.pressed.connect(callback)
	parent.add_child(button)
	return button

func show_pause(value: bool, steps: int, best_steps: int) -> void:
	if value:
		resume_button.visible = true
		title_label.text = "PAUSED"
		info_label.text = "Steps %d   Best %d" % [steps, best_steps]
		overlay.visible = true
		panel.visible = true
	else:
		panel.visible = false
		overlay.visible = false

func show_game_over(steps: int, best_steps: int) -> void:
	resume_button.visible = false
	_restart_button_reset()
	title_label.text = "GAME OVER"
	info_label.text = "Steps %d   Best %d" % [steps, best_steps]
	overlay.visible = true
	panel.visible = true

func update_stats(steps: int, coins: int, tier: int, shielded: bool = false) -> void:
	steps_label.text = "STEPS %d" % steps
	status_label.text = "COINS %d  •  SHIELD" % coins if shielded else "COINS %d  •  HP 1" % coins
	tier_label.text = "TIER %d" % tier

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
