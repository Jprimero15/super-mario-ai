using Godot;

public partial class ScoreManager : Node
{
    public int Steps { get; set; }
    public int Coins { get; private set; }
    public int Combo { get; private set; }
    public int Lives { get; private set; } = 3;
    public int BestScore { get; private set; }
    public bool RunActive { get; private set; }

    public override void _Ready() => LoadBest();

    public void ResetRun()
    {
        Steps = 0; Coins = 0; Combo = 0; Lives = 3; RunActive = true;
    }

    public void AddCoin()
    {
        Coins++; Combo++; GetAudio()?.PlaySfx("coin");
    }

    public void AddStomp()
    {
        Combo++; GetAudio()?.PlaySfx("stomp");
    }

    public bool Damage()
    {
        if (Lives <= 0) return false;
        Lives--; Combo = 0; GetAudio()?.PlaySfx("hit");
        return Lives <= 0;
    }

    public int Multiplier() => Mathf.Clamp(1 + Combo / 5, 1, 9);
    public int Score() => Steps * 5 + Coins * 25 * Multiplier() + Combo * 10;

    public void FinishRun()
    {
        if (!RunActive) return;
        RunActive = false;
        BestScore = Mathf.Max(BestScore, Score());
        SaveBest();
        GetAudio()?.PlaySfx("game_over");
    }

    private AudioManager? GetAudio() => GetNodeOrNull<AudioManager>("/root/AudioManager");

    private void LoadBest()
    {
        var cfg = new ConfigFile();
        if (cfg.Load("user://records.cfg") != Error.Ok) { BestScore = 0; return; }
        BestScore = Mathf.Max(0, (int)cfg.GetValue("records", "best_score", 0));
    }

    private void SaveBest()
    {
        var cfg = new ConfigFile();
        cfg.SetValue("records", "best_score", BestScore);
        if (cfg.Save("user://records.cfg") != Error.Ok) GD.PushWarning("Could not save records.cfg");
    }
}
