extends Node

const MUSIC_BUS := "Music"
const SFX_BUS := "SFX"

func _ready() -> void:
	_ensure_bus(MUSIC_BUS)
	_ensure_bus(SFX_BUS)

func set_music_volume(value: float) -> void:
	AudioServer.set_bus_volume_db(AudioServer.get_bus_index(MUSIC_BUS), linear_to_db(clampf(value, 0.0, 1.0)))

func set_sfx_volume(value: float) -> void:
	AudioServer.set_bus_volume_db(AudioServer.get_bus_index(SFX_BUS), linear_to_db(clampf(value, 0.0, 1.0)))

func _ensure_bus(name: String) -> void:
	var index := AudioServer.get_bus_index(name)
	if index == -1:
		AudioServer.add_bus()
		index = AudioServer.bus_count - 1
		AudioServer.set_bus_name(index, name)
