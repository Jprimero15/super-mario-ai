using Godot;

public partial class Hazard : Area2D
{
    [Signal] public delegate void HitPlayerEventHandler();

    public void Setup(Vector2 size)
    {
        CollisionLayer = 0; CollisionMask = 1; Monitoring = true;
        AddChild(new CollisionShape2D { Shape = new RectangleShape2D { Size = size } });
        BodyEntered += OnBodyEntered;
    }

    private void OnBodyEntered(Node2D body)
    {
        if (body is Player) EmitSignal(SignalName.HitPlayer);
    }
}
