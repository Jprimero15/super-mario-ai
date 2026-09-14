extends SceneTree

func _init() -> void:
    var scene: PackedScene = load("res://scenes/main.tscn") as PackedScene
    if scene == null:
        push_error("Maryou smoke test: main scene could not be loaded")
        quit(1)
        return

    var instance: Node = scene.instantiate()
    if instance == null:
        push_error("Maryou smoke test: main scene could not be instantiated")
        quit(1)
        return

    if not instance.get_script():
        push_error("Maryou smoke test: main scene root has no Game.cs script")
        instance.free()
        quit(1)
        return

    instance.free()
    print("Maryou smoke test: main scene parsed and instantiated successfully")
    quit(0)
