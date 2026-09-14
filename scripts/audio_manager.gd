extends Node

const SETTINGS_PATH := "user://settings.cfg"
const MUSIC_BUS := "Music"
const SFX_BUS := "SFX"
const MUSIC_RATE := 22050
var music_volume := 0.8
var sfx_volume := 0.9
var music_player: AudioStreamPlayer

func _ready() -> void:
	_ensure_bus(MUSIC_BUS)
	_ensure_bus(SFX_BUS)
	_load_settings()
	_apply_music()
	_apply_sfx()
	_start_procedural_music()

func set_music_volume(value: float) -> void:
	music_volume = clampf(value, 0.0, 1.0)
	_apply_music()
	_save_settings()

func set_sfx_volume(value: float) -> void:
	sfx_volume = clampf(value, 0.0, 1.0)
	_apply_sfx()
	_save_settings()

func play_sfx(type: String) -> void:
	var player := AudioStreamPlayer.new()
	player.bus = SFX_BUS
	player.stream = _tone_stream(type)
	add_child(player)
	player.finished.connect(player.queue_free)
	player.play()

func _tone_stream(type: String) -> AudioStreamWAV:
	var frequencies := {"jump": 520.0, "coin": 880.0, "stomp": 220.0, "hit": 120.0, "shield": 660.0, "game_over": 90.0, "ui": 440.0}
	var frequency: float = frequencies.get(type, 440.0)
	var length := 0.10 if type != "game_over" else 0.35
	var samples := int(length * MUSIC_RATE)
	var data := PackedByteArray()
	data.resize(samples * 2)
	for i in range(samples):
		var t := float(i) / float(MUSIC_RATE)
		var envelope := 1.0 - (float(i) / float(samples))
		var value := sin(TAU * frequency * t) * envelope * 0.22
		var sample := clampi(int(value * 32767.0), -32768, 32767)
		data[i * 2] = sample & 255
		data[i * 2 + 1] = (sample >> 8) & 255
	var wav := AudioStreamWAV.new()
	wav.format = AudioStreamWAV.FORMAT_16_BITS
	wav.mix_rate = MUSIC_RATE
	wav.stereo = false
	wav.data = data
	return wav

func _start_procedural_music() -> void:
	music_player = AudioStreamPlayer.new()
	music_player.bus = MUSIC_BUS
	music_player.stream = _build_music_stream()
	add_child(music_player)
	music_player.play()

func _build_music_stream() -> AudioStreamWAV:
	# A light 8-second arcade loop: bass pulse, chord bed and a simple melody.
	const LENGTH := 8.0
	const BEAT := 0.5
	var samples := int(LENGTH * MUSIC_RATE)
	var data := PackedByteArray()
	data.resize(samples * 2)
	var melody := [523.25, 659.25, 783.99, 659.25, 587.33, 698.46, 880.0, 698.46,
		523.25, 659.25, 783.99, 987.77, 880.0, 783.99, 659.25, 523.25]
	var chords := [261.63, 329.63, 392.0, 329.63]
	for i in range(samples):
		var t := float(i) / float(MUSIC_RATE)
		var beat_index := int(floor(t / BEAT))
		var note_index := posmod(beat_index, melody.size())
		var chord_index := posmod(int(floor(t / 2.0)), chords.size())
		var note_t := fmod(t, BEAT)
		var melody_env := exp(-note_t * 5.0)
		var bass_env := 0.35 + 0.25 * sin(TAU * fmod(t, 1.0))
		var melody_value := sin(TAU * melody[note_index] * t) * 0.16 * melody_env
		var bass_value := sin(TAU * (chords[chord_index] / 2.0) * t) * 0.10 * bass_env
		var shimmer := sin(TAU * (melody[note_index] * 2.0) * t) * 0.025 * melody_env
		var value := clampf(melody_value + bass_value + shimmer, -0.65, 0.65)
		var sample := clampi(int(value * 32767.0), -32768, 32767)
		data[i * 2] = sample & 255
		data[i * 2 + 1] = (sample >> 8) & 255
	var wav := AudioStreamWAV.new()
	wav.format = AudioStreamWAV.FORMAT_16_BITS
	wav.mix_rate = MUSIC_RATE
	wav.stereo = false
	wav.data = data
	wav.loop_mode = AudioStreamWAV.LOOP_FORWARD
	wav.loop_begin = 0
	wav.loop_end = samples
	return wav

func _apply_music() -> void: _set_bus_volume(MUSIC_BUS, music_volume)
func _apply_sfx() -> void: _set_bus_volume(SFX_BUS, sfx_volume)

func _set_bus_volume(bus_name: String, value: float) -> void:
	var index := AudioServer.get_bus_index(bus_name)
	if index >= 0: AudioServer.set_bus_volume_db(index, linear_to_db(maxf(value, 0.0001)))

func _ensure_bus(name: String) -> void:
	if AudioServer.get_bus_index(name) != -1: return
	AudioServer.add_bus()
	AudioServer.set_bus_name(AudioServer.bus_count - 1, name)

func _load_settings() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(SETTINGS_PATH) != OK: return
	music_volume = clampf(float(cfg.get_value("audio", "music", 0.8)), 0.0, 1.0)
	sfx_volume = clampf(float(cfg.get_value("audio", "sfx", 0.9)), 0.0, 1.0)

func _save_settings() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("audio", "music", music_volume)
	cfg.set_value("audio", "sfx", sfx_volume)
	if cfg.save(SETTINGS_PATH) != OK: push_warning("Could not save audio settings")
