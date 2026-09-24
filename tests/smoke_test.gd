extends SceneTree

func _initialize() -> void:
	var failures: Array[String] = []
	if MaryouDifficultyCurve.tier_for_steps(0) != 1:
		failures.append("tier at zero")
	if MaryouDifficultyCurve.tier_for_steps(1500) != 4:
		failures.append("tier at 1500")
	var speed_a := MaryouDifficultyCurve.speed_for_steps(0)
	var speed_b := MaryouDifficultyCurve.speed_for_steps(900)
	if speed_b <= speed_a:
		failures.append("speed curve is not increasing")
	if MaryouDifficultyCurve.hole_chance(1800) <= MaryouDifficultyCurve.hole_chance(0):
		failures.append("hole curve is not increasing")
	var rng := RandomNumberGenerator.new()
	rng.seed = 12345
	var first_kind := MaryouDifficultyCurve.enemy_kind(1500, 0, rng)
	if first_kind < 0 or first_kind > 5:
		failures.append("enemy kind range")

	var player := MaryouPlayer.new()
	if player == null:
		failures.append("player creation")
	else:
		if not player.take_damage(1):
			failures.append("single player damage must register")

	var enemies := Node2D.new()
	var world := MaryouWorldGenerator.new()
	world.setup(enemies)
	world.run_seed = 12345
	world._generate_chunk(1, 0)
	if not world.active_chunks.has(1):
		failures.append("chunk generation")
	else:
		var chunk: MaryouChunk = world.active_chunks[1]
		for child in chunk.get_children():
			if child is Area2D and child.name == "Coin":
				if child.position.x < world.CHUNK_WIDTH or child.position.x >= world.CHUNK_WIDTH * 2.0:
					failures.append("coin spawned outside chunk bounds")
					break

	# Milestone logic is tested directly so the smoke test does not construct a full game scene.
	var game_script := load("res://scripts/game.gd")
	if game_script == null:
		failures.append("game script load")
	else:
		var game := game_script.new()
		root.add_child(game)
		game.steps = 499
		game._check_milestone()
		if game.last_milestone != 0:
			failures.append("milestone fired before 500")
		game.steps = 500
		game._check_milestone()
		if game.last_milestone != 500:
			failures.append("milestone did not fire at 500")

	if failures.is_empty():
		print("MARYOU SMOKE TEST: PASS")
		quit(0)
	else:
		for failure in failures:
			printerr("FAIL: %s" % failure)
		printerr("MARYOU SMOKE TEST: FAILED")
		quit(1)
