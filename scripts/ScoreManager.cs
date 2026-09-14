using Godot;

public partial class ScoreManager : Node
{
    public int Steps { get; set; }
    public int Coins { get; private set; }
    public int Combo { get; private set; }
    public int Lives { get; private set; }
    public int BestScore { get; private set; }
    public bool RunActive { get; private set; }

    public override void _Ready()
    {
        Lives = 3;
        LoadBest();
    }

    public void ResetRun()
    {
        Steps = 0;
        Coins = 0;
        Combo = 0;
        Lives = 3;
        RunActive = true;
    }

    public void AddCoin()
    {
        Coins += 1;
        Combo += 1;
        PlaySound("coin");
    }

    public void AddStomp()
    {
        Combo += 1;
        PlaySound("stomp");
    }

    public bool Damage()
    {
        if (Lives <= 0)
            return false;

        Lives -= 1;
        Combo = 0;
        PlaySound("hit");
        return Lives <= 0;
    }

    public int Multiplier()
    {
        return Mathf.Clamp(1 + Combo / 5, 1, 9);
    }

    public int Score()
    {
        return Steps * 5 + Coins * 25 * Multiplier() + Combo * 10;
    }

    public void FinishRun()
    {
        if (!RunActive)
            return;

        RunActive = false;
        int score = Score();
        BestScore = Mathf.Max(BestScore, score);
        SaveBest();
        PlaySound("game_over");
    }

    private void PlaySound(string type)
    {
        AudioManager audio = GetNodeOrNull<AudioManager>("/root/AudioManager");
        if (audio != null)
            audio.PlaySfx(type);
    }

    private void LoadBest()
    {
        ConfigFile config = new ConfigFile();
        if (config.Load("user://records.cfg") != Error.Ok)
        {
            BestScore = 0;
            return;
        }

        Variant value = config.GetValue("records", "best_score", 0);
        BestScore = Mathf.Max(0, value.AsInt32());
    }

    private void SaveBest()
    {
        ConfigFile config = new ConfigFile();
        config.SetValue("records", "best_score", BestScore);

        if (config.Save("user://records.cfg") != Error.Ok)
            GD.PushWarning("Could not save records.cfg");
    }
}
