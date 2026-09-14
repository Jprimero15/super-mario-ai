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
	if has_node("/root/AudioManager"): AudioManager.play_sfx("coin")

func add_stomp() -> void:
	combo += 1
	if has_node("/root/AudioManager"): AudioManager.play_sfx("stomp")

func damage() -> bool:
	if lives <= 0: return false
	lives -= 1
	combo = 0
	if has_node("/root/AudioManager"): AudioManager.play_sfx("hit")
	return lives <= 0

func multiplier() -> int:
	return clampi(1 + int(combo / 5), 1, 9)

func score() -> int:
	return steps * 5 + coins * 25 * multiplier() + combo * 10

func finish_run() -> void:
	if not run_active: return
	run_active = false
	best_score = maxi(best_score, score())
	save_best()
	if has_node("/root/AudioManager"): AudioManager.play_sfx("game_over")

func load_best() -> void:
	var cfg := ConfigFile.new()
	if cfg.load("user://records.cfg") != OK:
		best_score = 0
		return
	best_score = maxi(0, int(cfg.get_value("records", "best_score", 0)))

func save_best() -> void:
	var cfg := ConfigFile.new()
	cfg.set_value("records", "best_score", best_score)
	if cfg.save("user://records.cfg") != OK: push_warning("Could not save records.cfg")
