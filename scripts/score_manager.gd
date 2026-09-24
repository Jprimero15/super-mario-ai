extends Node

var steps := 0
var coins := 0
var best_steps := 0
var total_coins := 0
var total_runs := 0
var run_active := false
var new_best := false

func _ready() -> void:
	load_records()

func reset_run() -> void:
	steps = 0
	coins = 0
	new_best = false
	run_active = true

func add_coin() -> void:
	coins += 1
	total_coins += 1
	get_node("/root/AudioManager").play_sfx("coin")

func add_stomp() -> void:
	coins += 1
	total_coins += 1
	get_node("/root/AudioManager").play_sfx("stomp")

func finish_run() -> void:
	if not run_active: return
	run_active = false
	total_runs += 1
	new_best = steps > best_steps
	best_steps = maxi(best_steps, steps)
	save_records()
	get_node("/root/AudioManager").play_sfx("game_over")

func load_records() -> void:
	var cfg := ConfigFile.new()
	if cfg.load("user://records.cfg") != OK:
		best_steps = 0
		total_coins = 0
		total_runs = 0
		return
	best_steps = maxi(0, int(cfg.get_value("records", "best_steps", 0)))
	total_coins = maxi(0, int(cfg.get_value("records", "total_coins", 0)))
	total_runs = maxi(0, int(cfg.get_value("records", "total_runs", 0)))

func save_records() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("records", "best_steps", best_steps)
	cfg.set_value("records", "total_coins", total_coins)
	cfg.set_value("records", "total_runs", total_runs)
	if cfg.save("user://records.cfg") != OK:
		push_warning("Could not save records.cfg")
