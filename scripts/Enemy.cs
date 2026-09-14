using Godot;

public partial class Enemy : CharacterBody2D
{
    [Signal] public delegate void PlayerContactEventHandler(Enemy enemy);
    [Signal] public delegate void StompedEventHandler(Enemy enemy);
    private readonly SpriteFrames _spriteFrames = GD.Load<SpriteFrames>("res://assets/sprites/enemy_frames.tres");
    private const float Gravity = Player.SharedGravity;
    private int _kind;
    private float _speed = 70, _size = 30, _baseY, _phase;
    private bool _defeated, _shielded;
    private Area2D? _hitArea, _stompArea;
    private AnimatedSprite2D? _animatedSprite;
    private enum HopperState { Idle, Windup, Hop, Cooldown }
    private HopperState _hopperState = HopperState.Idle;
    private float _hopperTimer;

    public void Setup(int enemyKind)
    {
        _kind = Mathf.Clamp(enemyKind, 0, 5);
        float[] sizes = { 30, 34, 32, 36, 31, 33 };
        _size = sizes[_kind];
        _speed = DifficultyCurve.EnemySpeed(_kind, DifficultyCurve.TierForSteps((int)(Position.X / 32)));
        _shielded = _kind == 3; CollisionLayer = 4; CollisionMask = 2;
        AddChild(new CollisionShape2D { Shape = new RectangleShape2D { Size = new Vector2(_size, _size) } });
        _animatedSprite = new AnimatedSprite2D { SpriteFrames = _spriteFrames, Animation = $"kind_{_kind}", Position = new Vector2(0, -2), TextureFilter = CanvasItem.TextureFilterEnum.Nearest };
        AddChild(_animatedSprite);
        _hitArea = new Area2D { CollisionLayer = 0, CollisionMask = 1, Monitoring = true, Position = new Vector2(0, 5) };
        _hitArea.AddChild(new CollisionShape2D { Shape = new RectangleShape2D { Size = new Vector2(_size + 12, Mathf.Max(12, _size * .55f)) } });
        _hitArea.BodyEntered += OnPlayerEntered; AddChild(_hitArea);
        _stompArea = new Area2D { CollisionLayer = 0, CollisionMask = 1, Monitoring = true, Position = new Vector2(0, -_size * .55f) };
        _stompArea.AddChild(new CollisionShape2D { Shape = new RectangleShape2D { Size = new Vector2(_size + 8, 12) } });
        _stompArea.BodyEntered += OnStompAreaEntered; AddChild(_stompArea);
        _baseY = Position.Y; _phase = Position.X * .02f; _hopperState = HopperState.Idle; _hopperTimer = 0; ZIndex = 7; _animatedSprite.Play();
    }

    public void Tick(double deltaValue, float playerX)
    {
        float delta = (float)deltaValue;
        if (_defeated) { Velocity += new Vector2(0, Gravity * delta); MoveAndSlide(); return; }
        float direction = playerX < Position.X ? -1 : 1;
        switch (_kind)
        {
            case 0: Velocity = new Vector2(direction * _speed, Velocity.Y + Gravity * delta); break;
            case 1: _phase += delta * 5; Velocity = new Vector2(direction * _speed * .65f, (_baseY - Mathf.Abs(Mathf.Sin(_phase)) * 75 - Position.Y) * 8); break;
            case 2: Velocity = new Vector2(direction * _speed * (Mathf.Abs(playerX - Position.X) < 240 ? 1.79f : 1), Velocity.Y + Gravity * delta); break;
            case 3: Velocity = new Vector2(direction * _speed * .8f, Velocity.Y + Gravity * delta); break;
            case 4: _phase += delta * 4; Velocity = new Vector2(Mathf.Sin(_phase) * _speed + direction * _speed * .45f, (_baseY - 30 + Mathf.Sin(_phase * 1.7f) * 38 - Position.Y) * 7); break;
            case 5: TickHopper(delta, playerX, direction); break;
        }
        MoveAndSlide(); if (IsInstanceValid(_animatedSprite)) _animatedSprite!.FlipH = direction < 0;
    }

    private void TickHopper(float delta, float playerX, float direction)
    {
        float distance = Mathf.Abs(playerX - Position.X); _hopperTimer = Mathf.Max(0, _hopperTimer - delta);
        switch (_hopperState)
        {
            case HopperState.Idle:
                Velocity = new Vector2(direction * _speed * .55f, Velocity.Y + Gravity * delta);
                if (IsOnFloor() && distance < 300 && _hopperTimer <= 0) { _hopperState = HopperState.Windup; _hopperTimer = .16f; Velocity = new Vector2(Velocity.X, 0); }
                break;
            case HopperState.Windup:
                Velocity = new Vector2(direction * _speed * .35f, 0);
                if (_hopperTimer <= 0) { Velocity = new Vector2(Velocity.X, -520); _hopperState = HopperState.Hop; }
                break;
            case HopperState.Hop:
                Velocity = new Vector2(direction * _speed * .55f, Velocity.Y + Gravity * delta);
                if (IsOnFloor() && Velocity.Y >= 0) { _hopperState = HopperState.Cooldown; _hopperTimer = .42f; }
                break;
            case HopperState.Cooldown:
                Velocity = new Vector2(direction * _speed * .55f, Velocity.Y + Gravity * delta);
                if (_hopperTimer <= 0) _hopperState = HopperState.Idle;
                break;
        }
    }

    private void OnStompAreaEntered(Node2D body)
    {
        if (_defeated || body is not Player player || player.Velocity.Y <= 50) return;
        if (_shielded) { player.Velocity = new Vector2(player.Velocity.X, -330); player.TakeHit(); return; }
        Defeat(); player.Velocity = new Vector2(player.Velocity.X, -400); EmitSignal(SignalName.Stomped, this);
    }

    private void OnPlayerEntered(Node2D body)
    {
        if (_defeated || body is not Player) return;
        EmitSignal(SignalName.PlayerContact, this);
    }

    private void Defeat()
    {
        if (_defeated) return;
        _defeated = true; _hitArea?.SetDeferred("monitoring", false); _stompArea?.SetDeferred("monitoring", false); Velocity = new Vector2(Velocity.X * .2f, -330); _animatedSprite?.Pause();
    }
}
