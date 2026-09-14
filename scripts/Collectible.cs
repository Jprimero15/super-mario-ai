using Godot;

public partial class MaryouCollectible : Area2D
{
    [Signal] public delegate void CollectedEventHandler(string kind);
    private readonly SpriteFrames _coinFrames = GD.Load<SpriteFrames>("res://assets/sprites/coin_frames.tres");
    private readonly Texture2D _collectibles = GD.Load<Texture2D>("res://assets/world/collectibles.svg");
    private string _kind = "coin"; private bool _active = true; private AnimatedSprite2D? _animatedSprite;

    public void Setup(string collectibleKind)
    {
        _kind = collectibleKind; CollisionLayer = 0; CollisionMask = 1; Monitoring = true;
        AddChild(new CollisionShape2D { Shape = new CircleShape2D { Radius = 15 } });
        if (_kind == "coin")
        {
            _animatedSprite = new AnimatedSprite2D { SpriteFrames = _coinFrames, Animation = "spin", TextureFilter = CanvasItem.TextureFilterEnum.Nearest, Scale = new Vector2(.55f, .55f) };
            AddChild(_animatedSprite);
        }
        BodyEntered += OnBodyEntered; QueueRedraw();
    }

    private void OnBodyEntered(Node2D body)
    {
        if (!_active || body is not MaryouPlayer) return;
        _active = false; EmitSignal(SignalName.Collected, _kind); QueueFree();
    }

    public override void _Draw()
    {
        if (_kind == "shield") DrawTextureRectRegion(_collectibles, new Rect2(-22, -22, 44, 44), new Rect2(448, 0, 64, 64));
    }
}
