using Godot;

public partial class MaryouPlayer : CharacterBody2D
{
    public const float SharedGravity = 1850.0f;

    [ExportCategory("Movement")]
    [Export] public float Gravity { get; set; } = SharedGravity;
    [Export] public float JumpVelocity { get; set; } = -720.0f;
    [Export] public float SideAccel { get; set; } = 2100.0f;
    [Export] public float MaxSideSpeed { get; set; } = 190.0f;
    [Export] public float CoyoteTime { get; set; } = 0.12f;
    [Export] public float JumpBufferTime { get; set; } = 0.14f;

    private const float Width = 38.0f, Height = 54.0f, HitInvulnerability = 1.15f;
    private readonly SpriteFrames _spriteFrames = GD.Load<SpriteFrames>("res://assets/sprites/maryou_frames.tres");
    public bool Dead { get; private set; }
    public bool Shielded { get; private set; }
    private float _shieldTime, _hitInvulnerability, _coyoteTimer, _jumpBufferTimer;
    private float _squash = 1, _stretch = 1, _shakeTime, _shakeStrength;
    private Camera2D? _camera;
    private AnimatedSprite2D? _animatedSprite;
    private string _lastAnimation = "";

    public override void _Ready()
    {
        ZIndex = 10;
        CollisionLayer = 1;
        CollisionMask = 2;
        AddChild(new CollisionShape2D { Shape = new RectangleShape2D { Size = new Vector2(Width, Height) } });

        _animatedSprite = new AnimatedSprite2D
        {
            SpriteFrames = _spriteFrames,
            Animation = "idle",
            Position = new Vector2(0, -2),
            TextureFilter = CanvasItem.TextureFilterEnum.Nearest
        };
        AddChild(_animatedSprite);

        _camera = new Camera2D
        {
            Position = new Vector2(250, -30),
            Enabled = true,
            PositionSmoothingEnabled = true,
            PositionSmoothingSpeed = 7.0f
        };
        AddChild(_camera);
    }

    public void SetRunLookAhead(float targetSpeed)
    {
        if (!IsInstanceValid(_camera))
            return;

        float extra = Mathf.Clamp((targetSpeed - 300.0f) * 0.35f, 0.0f, 90.0f);
        _camera!.Position = new Vector2(250.0f + extra, -30.0f);
    }

    public void Tick(double deltaValue, float targetSpeed, bool left, bool right, bool jumpPressed, bool jumpHeld)
    {
        float delta = (float)deltaValue;
        _hitInvulnerability = Mathf.Max(0, _hitInvulnerability - delta);

        if (_shieldTime > 0)
        {
            _shieldTime = Mathf.Max(0, _shieldTime - delta);
            if (_shieldTime == 0)
                Shielded = false;
        }

        if (_shakeTime > 0)
        {
            _shakeTime = Mathf.Max(0, _shakeTime - delta);
            if (IsInstanceValid(_camera))
                _camera!.Offset = new Vector2((float)GD.RandRange(-_shakeStrength, _shakeStrength), (float)GD.RandRange(-_shakeStrength, _shakeStrength));
        }
        else if (IsInstanceValid(_camera))
        {
            _camera!.Offset = _camera.Offset.Lerp(Vector2.Zero, Mathf.Min(delta * 14, 1));
        }

        if (Dead)
        {
            Velocity += new Vector2(0, Gravity * delta);
            MoveAndSlide();
            SetAnimation("dead");
            ApplySpriteJuice(delta);
            QueueRedraw();
            return;
        }

        if (jumpPressed)
            _jumpBufferTimer = JumpBufferTime;
        else
            _jumpBufferTimer = Mathf.Max(0, _jumpBufferTimer - delta);

        _coyoteTimer = IsOnFloor() ? CoyoteTime : Mathf.Max(0, _coyoteTimer - delta);

        float direction = 0;
        if (left) direction -= 1;
        if (right) direction += 1;

        Velocity = new Vector2(targetSpeed, Velocity.Y);
        if (direction != 0)
        {
            Velocity = new Vector2(
                Mathf.Clamp(Velocity.X + direction * SideAccel * delta, targetSpeed - MaxSideSpeed, targetSpeed + MaxSideSpeed),
                Velocity.Y);
        }
        else
        {
            Velocity = new Vector2(Mathf.MoveToward(Velocity.X, targetSpeed, SideAccel * delta), Velocity.Y);
        }

        if (_jumpBufferTimer > 0 && _coyoteTimer > 0)
        {
            Velocity = new Vector2(Velocity.X, JumpVelocity);
            _jumpBufferTimer = 0;
            _coyoteTimer = 0;
            _stretch = 1.18f;
            _squash = 1.0f;
        }

        if (!jumpHeld && Velocity.Y < -260)
            Velocity = new Vector2(Velocity.X, -260);

        Velocity += new Vector2(0, Gravity * delta);
        MoveAndSlide();

        if (IsOnFloor())
            _squash = Mathf.MoveToward(_squash, 1, delta * 8);
        _stretch = Mathf.MoveToward(_stretch, 1, delta * 6);

        if (_hitInvulnerability > 0)
            SetAnimation("hurt");
        else if (!IsOnFloor())
            SetAnimation(Velocity.Y < 0 ? "jump" : "fall");
        else if (Mathf.Abs(Velocity.X) > targetSpeed + 25)
            SetAnimation("run");
        else
            SetAnimation("idle");

        if (IsInstanceValid(_animatedSprite) && Mathf.Abs(direction) > 0.01f)
            _animatedSprite!.FlipH = direction < 0;

        ApplySpriteJuice(delta);
        QueueRedraw();
    }

    private void ApplySpriteJuice(float delta)
    {
        if (!IsInstanceValid(_animatedSprite))
            return;

        float vertical = Mathf.Max(0.65f, _squash * _stretch);
        float horizontal = Mathf.Clamp(1.0f + (1.0f - vertical) * 0.65f, 0.78f, 1.16f);
        Vector2 target = new Vector2(horizontal, vertical);
        _animatedSprite!.Scale = _animatedSprite.Scale.Lerp(target, Mathf.Min(delta * 18.0f, 1.0f));
    }

    private void SetAnimation(string name)
    {
        if (!IsInstanceValid(_animatedSprite) || _lastAnimation == name)
            return;
        _lastAnimation = name;
        _animatedSprite!.Play(name);
    }

    public bool TakeHit()
    {
        if (Dead || _hitInvulnerability > 0)
            return false;

        if (Shielded)
        {
            Shielded = false;
            _shieldTime = 0;
            _hitInvulnerability = HitInvulnerability;
            Velocity = new Vector2(Velocity.X, -300);
            _squash = .82f;
            Shake(4, .12f);
            SetAnimation("hurt");
            QueueRedraw();
            return false;
        }

        _hitInvulnerability = HitInvulnerability;
        _squash = .82f;
        Velocity = new Vector2(Velocity.X, Mathf.Min(Velocity.Y, -240));
        Shake(8, .16f);
        SetAnimation("hurt");
        QueueRedraw();
        return true;
    }

    public void ActivateShield()
    {
        Shielded = true;
        _shieldTime = 8;
        QueueRedraw();
    }

    public void Shake(float strength, float duration)
    {
        _shakeStrength = Mathf.Max(_shakeStrength, strength);
        _shakeTime = Mathf.Max(_shakeTime, duration);
    }

    public void Kill()
    {
        if (Dead)
            return;
        Dead = true;
        Velocity = new Vector2(Velocity.X * .35f, -420);
        _squash = .72f;
        Shake(10, .2f);
        SetAnimation("dead");
        QueueRedraw();
    }

    public override void _Draw()
    {
        if (Shielded)
            DrawArc(Vector2.Zero, 34, 0, Mathf.Tau, 32, new Color(.35f, .9f, .85f, .75f), 3);
    }
}
