extends Resource
class_name MaryouDifficultyCurve

const MAX_SPEED := 520.0

static func tier_for_steps(distance_steps: int) -> int:
	if distance_steps < 300:
		return 1
	if distance_steps < 750:
		return 2
	if distance_steps < 1400:
		return 3
	return 4

static func speed_for_steps(distance_steps: int) -> float:
	var t := clampf(float(distance_steps) / 1800.0, 0.0, 1.0)
	return lerpf(300.0, MAX_SPEED, pow(t, 0.78))

static func hole_chance(distance_steps: int) -> float:
	return lerpf(0.045, 0.14, clampf(float(distance_steps) / 1800.0, 0.0, 1.0))

static func pipe_chance(distance_steps: int) -> float:
	return lerpf(0.10, 0.34, clampf(float(distance_steps) / 1800.0, 0.0, 1.0))

static func enemy_count(distance_steps: int) -> int:
	return clampi(1 + int(distance_steps / 420.0), 0, 4)

static func enemy_kind(distance_steps: int, index: int) -> int:
	var tier := tier_for_steps(distance_steps)
	if tier <= 1:
		return 0
	if tier == 2:
		return 0 if index % 3 else 1
	return index % 3
