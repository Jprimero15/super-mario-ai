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
	if MaryouDifficultyCurve.hole_chance(0) >= MaryouDifficultyCurve.hole_chance(1800):
		pass
	else:
		failures.append("hole curve is not increasing")
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
