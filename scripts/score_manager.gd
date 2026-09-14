extends Node

var steps := 0
var coins := 0
var combo := 0
var lives := 3
var best_score := 0
var run_active := false

func _ready() -> void:
	load_best()

func reset_run() -> void:
	steps = 0
	coins = 0
	combo = 0
	lives = 3
	run_active = true

func add_coin() -> void:
	coins += 1
	combo += 1

func add_stomp() -> void:
	combo += 1

func damage() -> bool:
	if lives <= 0:
		return false
	lives -= 1
	combo = 0
	return lives <= 0

func score() -> int:
	return steps * 5 + coins * 25 + combo * 10

func finish_run() -> void:
	run_active = false
	best_score = maxi(best_score, score())
	save_best()

func load_best() -> void:
	var cfg := ConfigFile.new()
	if cfg.load("user://records.cfg") != OK:
		best_score = 0
		return
	best_score = maxi(0, int(cfg.get_value("records", "best_score", 0)))

func save_best() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("records", "best_score", best_score)
	var result := cfg.save("user://records.cfg")
	if result != OK:
		push_warning("Could not save records.cfg: %s" % result)
