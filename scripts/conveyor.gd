extends StaticBody2D
class_name MaryouConveyor

@export var belt_speed := 95.0
@export_enum("Right", "Left") var direction := 0

var players: Array[MaryouPlayer] = []

func _ready() -> void:
	$PushArea.body_entered.connect(_on_body_entered)
	$PushArea.body_exited.connect(_on_body_exited)

func _physics_process(delta: float) -> void:
	var force := belt_speed * (-1.0 if direction == 1 else 1.0)
	for player in players.duplicate():
		if not is_instance_valid(player):
			players.erase(player)
			continue
		player.velocity.x += force * delta

func _on_body_entered(body: Node2D) -> void:
	if body is MaryouPlayer and not players.has(body):
		players.append(body as MaryouPlayer)

func _on_body_exited(body: Node2D) -> void:
	if body is MaryouPlayer:
		players.erase(body as MaryouPlayer)
