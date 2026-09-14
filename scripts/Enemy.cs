using Godot;

public partial class MaryouEnemy : CharacterBody2D
{
    [Signal] public delegate void PlayerContactEventHandler(MaryouEnemy enemy);
    [Signal] public delegate void StompedEventHandler(MaryouEnemy enemy);
    private readonly SpriteFrames _spriteFrames = GD.Load<SpriteFrames>("res://assets/sprites/enemy_frames.tres");
    private int _kind; private float _speed = 70, _size = 30, _baseY, _phase, _patrolOrigin; private bool _defeated, _shielded;
    private Area2D? _hitArea; private AnimatedSprite2D? _animatedSprite;

    public void Setup(int enemyKind)
    {
        _kind = Mathf.Clamp(enemyKind, 0, 5);
        float[] sizes = { 30, 34, 32, 36, 31, 33 }; _size = sizes[_kind];
        _speed = MaryouDifficultyCurve.EnemySpeed(_kind, MaryouDifficultyCurve.TierForSteps((int)(Position.X / 32)));
        _shielded = _kind == 3; CollisionLayer = 4; CollisionMask = 2;
        AddChild(new CollisionShape2D { Shape = new RectangleShape2D { Size = new Vector2(_size, _size) } });
        _animatedSprite = new AnimatedSprite2D { SpriteFrames = _spriteFrames, Animation = $"kind_{_kind}", Position = new Vector2(0, -2), TextureFilter = CanvasItem.TextureFilterEnum.Nearest };
        AddChild(_animatedSprite);
        _hitArea = new Area2D { CollisionLayer = 0, CollisionMask = 1, Monitoring = true };
        _hitArea.AddChild(new CollisionShape2D { Shape = new RectangleShape2D { Size = new Vector2(_size + 10, _size + 10) } });
        _hitArea.BodyEntered += OnPlayerEntered; AddChild(_hitArea);
        _baseY = Position.Y; _patrolOrigin = Position.X; _phase = Position.X * .02f; ZIndex = 7; _animatedSprite.Play();
    }

    public void Tick(double deltaValue, float playerX)
    {
        float delta = (float)deltaValue;
        if (_defeated) { Velocity += new Vector2(0, 1600 * delta); MoveAndSlide(); return; }
        float direction = playerX < Position.X ? -1 : 1;
        switch (_kind)
        {
            case 0: Velocity = new Vector2(direction * _speed, Velocity.Y + 1500 * delta); break;
            case 1: _phase += delta * 5; Velocity = new Vector2(direction * _speed * .65f, (_baseY - Mathf.Abs(Mathf.Sin(_phase)) * 75 - Position.Y) * 8); break;
            case 2: Velocity = new Vector2(direction * _speed * (Mathf.Abs(playerX - Position.X) < 240 ? 1.79f : 1), Velocity.Y + 1500 * delta); break;
            case 3: Velocity = new Vector2(direction * _speed * .8f, Velocity.Y + 1500 * delta); break;
            case 4: _phase += delta * 4; Velocity = new Vector2(Mathf.Sin(_phase) * _speed + direction * _speed * .45f, (_baseY - 30 + Mathf.Sin(_phase * 1.7f) * 38 - Position.Y) * 7); break;
            case 5: Velocity = new Vector2(direction * _speed * .55f, Velocity.Y + 1500 * delta); if (IsOnFloor() && Mathf.Abs(playerX - Position.X) < 300) Velocity = new Vector2(Velocity.X, -520); break;
        }
        MoveAndSlide(); if (IsInstanceValid(_animatedSprite)) _animatedSprite!.FlipH = direction < 0;
    }

    private void OnPlayerEntered(Node2D body)
    {
        if (_defeated || body is not MaryouPlayer player) return;
        if (player.Velocity.Y > 50 && player.Position.Y < Position.Y - 8)
        {
            if (_shielded) { player.Velocity = new Vector2(player.Velocity.X, -330); player.TakeHit(); return; }
            Defeat(); player.Velocity = new Vector2(player.Velocity.X, -400); EmitSignal(SignalName.Stomped, this);
        }
        else EmitSignal(SignalName.PlayerContact, this);
    }

    private void Defeat()
    {
        if (_defeated) return; _defeated = true; _hitArea?.SetDeferred("monitoring", false); Velocity = new Vector2(Velocity.X * .2f, -330);
        _animatedSprite?.Pause();
    }
}
