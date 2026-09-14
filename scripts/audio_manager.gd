extends Node

const MUSIC_BUS := "Music"
const SFX_BUS := "SFX"
const SETTINGS_PATH := "user://settings.cfg"
var music_volume := 0.8
var sfx_volume := 0.9

func _ready() -> void:
	_ensure_bus(MUSIC_BUS)
	_ensure_bus(SFX_BUS)
	_load_settings()
	_apply_music()
	_apply_sfx()

func set_music_volume(value: float) -> void:
	music_volume = clampf(value, 0.0, 1.0)
	_apply_music()
	_save_settings()

func set_sfx_volume(value: float) -> void:
	sfx_volume = clampf(value, 0.0, 1.0)
	_apply_sfx()
	_save_settings()

func _apply_music() -> void:
	_set_bus_volume(MUSIC_BUS, music_volume)

func _apply_sfx() -> void:
	_set_bus_volume(SFX_BUS, sfx_volume)

func _set_bus_volume(bus_name: String, value: float) -> void:
	var index := AudioServer.get_bus_index(bus_name)
	if index >= 0:
		AudioServer.set_bus_volume_db(index, linear_to_db(maxf(value, 0.0001)))

func _ensure_bus(name: String) -> void:
	if AudioServer.get_bus_index(name) != -1:
		return
	AudioServer.add_bus()
	AudioServer.set_bus_name(AudioServer.bus_count - 1, name)

func _load_settings() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(SETTINGS_PATH) != OK:
		return
	music_volume = clampf(float(cfg.get_value("audio", "music", 0.8)), 0.0, 1.0)
	sfx_volume = clampf(float(cfg.get_value("audio", "sfx", 0.9)), 0.0, 1.0)

func _save_settings() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("audio", "music", music_volume)
	cfg.set_value("audio", "sfx", sfx_volume)
	var result := cfg.save(SETTINGS_PATH)
	if result != OK:
		push_warning("Could not save audio settings: %s" % result)
