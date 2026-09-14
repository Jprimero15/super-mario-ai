extends Resource
class_name MaryouDifficultyCurve

const MAX_SPEED: float = 520.0
const MAX_DISTANCE: float = 1800.0

static func tier_for_steps(distance_steps: int) -> int:
	if distance_steps < 300: return 1
	if distance_steps < 750: return 2
	if distance_steps < 1400: return 3
	return 4

static func progress(distance_steps: int) -> float:
	return clampf(float(maxi(distance_steps, 0)) / MAX_DISTANCE, 0.0, 1.0)

static func speed_for_steps(distance_steps: int) -> float:
	return lerpf(300.0, MAX_SPEED, pow(progress(distance_steps), 0.78))

static func hole_chance(distance_steps: int) -> float:
	return lerpf(0.045, 0.14, progress(distance_steps))

static func enemy_count(distance_steps: int) -> int:
	return clampi(1 + int(distance_steps / 420.0), 0, 4)

static func enemy_kind(distance_steps: int, index: int, rng: RandomNumberGenerator = null) -> int:
	var tier: int = tier_for_steps(distance_steps)
	if tier <= 1: return 0
	if rng == null:
		return index % 4
	var roll := rng.randf()
	if tier == 2:
		if roll < 0.45: return 0
		if roll < 0.70: return 1
		if roll < 0.88: return 2
		return 3
	if tier == 3:
		if roll < 0.30: return 0
		if roll < 0.55: return 1
		if roll < 0.78: return 2
		return 3
	if roll < 0.25: return 0
	if roll < 0.50: return 1
	if roll < 0.75: return 2
	return 3

static func enemy_speed(kind: int, tier: int) -> float:
	var speeds: Array[float] = [68.0, 82.0, 108.0, 76.0]
	var base: float = speeds[clampi(kind, 0, speeds.size() - 1)]
	return base + float(maxi(tier - 1, 0)) * 8.0
