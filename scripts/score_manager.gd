extends Node

var steps := 0
var coins := 0
var best_steps := 0
var run_active := false

func _ready() -> void:
	load_best()

func reset_run() -> void:
	steps = 0
	coins = 0
	run_active = true

func add_coin() -> void:
	coins += 1
	AudioManager.play_sfx("coin")

func add_stomp() -> void:
	AudioManager.play_sfx("stomp")

func finish_run() -> void:
	if not run_active: return
	run_active = false
	best_steps = maxi(best_steps, steps)
	save_best()
	AudioManager.play_sfx("game_over")

func load_best() -> void:
	var cfg := ConfigFile.new()
	if cfg.load("user://records.cfg") != OK:
		best_steps = 0
		return
	best_steps = maxi(0, int(cfg.get_value("records", "best_steps", 0)))

func save_best() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("records", "best_steps", best_steps)
	if cfg.save("user://records.cfg") != OK:
		push_warning("Could not save records.cfg")
