extends SceneTree

func _initialize() -> void:
	var failures: Array[String] = []
	if MaryouDifficultyCurve.tier_for_steps(0) != 1:
		failures.append("tier at zero")
	if MaryouDifficultyCurve.tier_for_steps(299) != 1:
		failures.append("tier at 299")
	if MaryouDifficultyCurve.tier_for_steps(300) != 2:
		failures.append("tier at 300")
	if MaryouDifficultyCurve.tier_for_steps(599) != 2:
		failures.append("tier at 599")
	if MaryouDifficultyCurve.tier_for_steps(600) != 3:
		failures.append("tier at 600")
	if MaryouDifficultyCurve.tier_for_steps(899) != 3:
		failures.append("tier at 899")
	if MaryouDifficultyCurve.tier_for_steps(900) != 4:
		failures.append("tier at 900")
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

	var enemies: Node2D = Node2D.new()
	var world: MaryouWorldGenerator = MaryouWorldGenerator.new()
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
	var game_script: GDScript = load("res://scripts/game.gd") as GDScript
	if game_script == null:
		failures.append("game script load")
	else:
		var game: MaryouGame = game_script.new() as MaryouGame
		if game == null:
			failures.append("game creation")
		else:
			# Keep the smoke test out of the active scene tree so game._ready()
			# does not start a full gameplay run or require runtime autoload nodes.
			game.steps = 299
			game._check_milestone()
			if game.last_milestone != 0:
				failures.append("milestone fired before 500")
			game.steps = 300
			game._check_milestone()
			if game.last_milestone != 500:
				failures.append("milestone did not fire at 500")
			game.free()

	# Explicitly release every runtime object created by the smoke test before
	# SceneTree cleanup, preventing physics/rendering RID leaks from generated chunks.
	if is_instance_valid(world):
		world.free()
	if is_instance_valid(enemies):
		enemies.free()
	if is_instance_valid(player):
		player.free()

	if failures.is_empty():
		print("MARYOU SMOKE TEST: PASS")
		quit(0)
	else:
		for failure in failures:
			printerr("FAIL: %s" % failure)
		printerr("MARYOU SMOKE TEST: FAILED")
		quit(1)
