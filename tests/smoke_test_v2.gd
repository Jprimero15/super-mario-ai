extends SceneTree

func _initialize() -> void:
	var failures: Array[String] = []
	if MaryouDifficultyCurve.tier_for_steps(0) != 1:
		failures.append("tier at zero")
	if MaryouDifficultyCurve.tier_for_steps(1500) != 4:
		failures.append("tier at 1500")
	if MaryouDifficultyCurve.speed_for_steps(900) <= MaryouDifficultyCurve.speed_for_steps(0):
		failures.append("speed curve")
	if MaryouDifficultyCurve.hole_chance(1800) <= MaryouDifficultyCurve.hole_chance(0):
		failures.append("hole curve")
	var player := MaryouPlayer.new()
	if player == null:
		failures.append("player creation")
	if failures.is_empty():
		print("MARYOU SMOKE TEST: PASS")
		quit(0)
	else:
		for failure in failures:
			printerr("FAIL: %s" % failure)
		printerr("MARYOU SMOKE TEST: FAILED")
		quit(1)
