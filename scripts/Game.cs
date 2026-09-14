using Godot;

[GlobalClass]
public partial class Game : Node2D
{
    private readonly Texture2D _backgrounds = GD.Load<Texture2D>("res://assets/world/backgrounds.svg");
    private const float WorldHeight = 720, GroundY = 560, BackdropWidth = 720;
    private Player? _player;
    private WorldGenerator? _world;
    private Node2D? _enemies;
    private HUD? _hud;
    private int _steps;
    private bool _hitLock;

    private ScoreManager Score => GetNode<ScoreManager>("/root/ScoreManager");
    private AudioManager Audio => GetNode<AudioManager>("/root/AudioManager");

    public override void _Ready()
    {
        ProcessMode = ProcessModeEnum.Pausable;
        if (OS.HasFeature("android")) DisplayServer.ScreenSetOrientation(DisplayServer.ScreenOrientation.SensorLandscape);
        Score.ResetRun();
        _enemies = new Node2D { Name = "Enemies" }; AddChild(_enemies);
        _world = new WorldGenerator { Name = "WorldGenerator" }; AddChild(_world); _world.Setup(_enemies);
        _world.CoinCollected += OnCoin; _world.ShieldCollected += OnShield; _world.HazardHit += OnHazard;
        _player = new Player { Name = "Player", Position = new Vector2(180, GroundY - 35) }; AddChild(_player);
        _hud = new HUD { Name = "HUD" }; _hud.PausePressed += TogglePause; _hud.RestartPressed += Restart; _hud.BackPressed += BackFromOverlay; AddChild(_hud);
        _world.GenerateUntil(_player.Position.X, 0); WireEnemies(); QueueRedraw();
    }

    public override void _PhysicsProcess(double deltaValue)
    {
        if (!IsInstanceValid(_player) || !IsInstanceValid(_world) || !IsInstanceValid(_hud)) return;
        if (Input.IsActionJustPressed("pause")) { TogglePause(); return; }
        _steps = Mathf.Max(_steps, (int)(_player!.Position.X / 32)); Score.UpdateSteps(_steps);
        float speed = DifficultyCurve.SpeedForSteps(_steps);
        bool left = Input.IsActionPressed("move_left"), right = Input.IsActionPressed("move_right"), jumpHeld = Input.IsActionPressed("jump"), jumpPressed = Input.IsActionJustPressed("jump");
        if (jumpPressed) Audio.PlaySfx("jump");
        _player.SetRunLookAhead(speed); _player.Tick(deltaValue, speed, left, right, jumpPressed, jumpHeld);
        _world.GenerateUntil(_player.Position.X, _steps); WireEnemies();
        foreach (Node child in _enemies!.GetChildren()) if (child is Enemy enemy && IsInstanceValid(enemy)) enemy.Tick(deltaValue, _player.Position.X);
        if (_player.Position.Y > WorldHeight + 80) FinishRun();
        _hud.UpdateStats(Score.Score(), Score.Lives, Score.Coins, Score.Multiplier(), DifficultyCurve.TierForSteps(_steps), (int)(_player.Position.X / 32)); QueueRedraw();
    }

    private void WireEnemies()
    {
        if (_enemies == null) return;
        foreach (Node child in _enemies.GetChildren())
        {
            if (child is not Enemy enemy || enemy.HasMeta("maryou_wired")) continue;
            enemy.PlayerContact += OnEnemyContact; enemy.Stomped += OnEnemyStomp; enemy.SetMeta("maryou_wired", true);
        }
    }

    private void OnCoin() => Score.AddCoin();
    private void OnShield() { if (IsInstanceValid(_player)) { _player!.ActivateShield(); Audio.PlaySfx("shield"); } }
    private void OnHazard() => TakeDamage();
    private void OnEnemyContact(Enemy _) => TakeDamage();
    private void OnEnemyStomp(Enemy _) { Score.AddStomp(); if (IsInstanceValid(_player)) _player!.Shake(5, .1f); Juice(.06f, .88f); }

    private void TakeDamage()
    {
        if (_hitLock || Score.Lives <= 0 || !IsInstanceValid(_player) || _player!.Dead) return;
        _hitLock = true;
        if (_player.TakeHit())
        {
            if (Score.Damage()) FinishRun();
            else { _player.Position += new Vector2(110, -60); _player.Velocity = new Vector2(DifficultyCurve.SpeedForSteps(_steps) * .75f, -360); Juice(.08f, .82f); }
        }
        else Juice(.05f, .9f);
        var timer = GetTree().CreateTimer(1.15, true, false, true); timer.Timeout += () => _hitLock = false;
    }

    private void FinishRun()
    {
        if (!IsInstanceValid(_player) || _player!.Dead) return;
        Score.FinishRun(); _player.Kill(); GetTree().Paused = false; _hud?.ShowGameOver(Score.Score(), Score.BestScore);
    }

    private void TogglePause()
    {
        if (!IsInstanceValid(_player) || _player!.Dead) return;
        bool value = !GetTree().Paused; GetTree().Paused = value; _hud?.ShowPause(value, Score.Score(), Score.BestScore);
    }

    private void Restart() { GetTree().Paused = false; GetTree().ReloadCurrentScene(); }
    private void BackFromOverlay() { GetTree().Paused = false; GetTree().Quit(); }

    private void Juice(float duration, float timeScale)
    {
        Engine.TimeScale = timeScale; var timer = GetTree().CreateTimer(duration, true, false, true); timer.Timeout += () => Engine.TimeScale = 1;
    }

    public override void _Draw()
    {
        float camX = IsInstanceValid(_player) ? _player!.Position.X : 640; int biome = Mathf.Min(_steps / 500, 2); float sourceX = biome * 256; float firstX = Mathf.Floor((camX - 1600) / BackdropWidth) * BackdropWidth;
        for (int i = 0; i < 6; i++) { float x = firstX + i * BackdropWidth; DrawTextureRectRegion(_backgrounds, new Rect2(x, -40, BackdropWidth, 720), new Rect2(sourceX, 0, 256, 256)); }
    }
}
