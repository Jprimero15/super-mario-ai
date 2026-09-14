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
	add_child(root)

	score_label = _label("SCORE 0", 24)
	score_label.position = Vector2(28, 22)
	root.add_child(score_label)
	status_label = _label("LIVES 3   COINS 0   ×1", 20)
	status_label.position = Vector2(28, 55)
	root.add_child(status_label)
	tier_label = _label("TIER 1   0m", 20)
	tier_label.anchor_left = 1.0
	tier_label.anchor_right = 1.0
	tier_label.offset_left = -250
	tier_label.offset_right = -85
	tier_label.position.y = 28
	root.add_child(tier_label)

	var pause := Button.new()
	pause.text = "PAUSE"
	pause.position = Vector2(1170, 18)
	pause.size = Vector2(85, 48)
	pause.process_mode = Node.PROCESS_MODE_ALWAYS
	pause.pressed.connect(func(): pause_pressed.emit())
	root.add_child(pause)

	var controls := _label("◀   MOVE      ▶                         JUMP", 18)
	controls.anchor_top = 1.0
	controls.anchor_bottom = 1.0
	controls.offset_left = 80
	controls.offset_top = -62
	controls.offset_right = 1200
	controls.offset_bottom = -22
	controls.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	root.add_child(controls)

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

func _make_panel(title: String) -> PanelContainer:
	var p := PanelContainer.new()
	p.position = Vector2(430, 185)
	p.size = Vector2(420, 350)
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
	p.position = Vector2(430, 170)
	p.size = Vector2(420, 380)
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
	music_slider.value = 0.8
	music_slider.value_changed.connect(func(v): if has_node("/root/AudioManager"): AudioManager.set_music_volume(v))
	box.add_child(music_slider)
	box.add_child(_label("SFX volume", 18))
	sfx_slider = HSlider.new()
	sfx_slider.min_value = 0.0
	sfx_slider.max_value = 1.0
	sfx_slider.value = 0.9
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
	overlay.visible = value
	panel.visible = value
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
