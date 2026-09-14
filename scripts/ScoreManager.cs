using Godot;

public partial class ScoreManager : Node
{
    private const string RecordsPath = "user://records.cfg";

    public int Steps { get; private set; }
    public int Coins { get; private set; }
    public int Combo { get; private set; }
    public int Lives { get; private set; } = 3;
    public int BestScore { get; private set; }
    public bool RunActive { get; private set; }

    public override void _Ready()
    {
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

    public void UpdateSteps(int steps)
    {
        Steps = Mathf.Max(Steps, steps);
    }

    public void AddCoin()
    {
        Coins++;
        Combo++;
        PlaySound("coin");
    }

    public void AddStomp()
    {
        Combo++;
        PlaySound("stomp");
    }

    public bool Damage()
    {
        if (Lives <= 0)
            return false;

        Lives--;
        Combo = 0;
        PlaySound("hit");
        return Lives == 0;
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
        if (score > BestScore)
            BestScore = score;

        SaveBest();
        PlaySound("game_over");
    }

    private void PlaySound(string type)
    {
        Node node = GetNodeOrNull("/root/AudioManager");
        if (node is AudioManager audio)
            audio.PlaySfx(type);
    }

    private void LoadBest()
    {
        ConfigFile config = new ConfigFile();
        if (config.Load(RecordsPath) != Error.Ok)
        {
            BestScore = 0;
            return;
        }

        BestScore = Mathf.Max(0, config.GetValue("records", "best_score", 0).AsInt32());
    }

    private void SaveBest()
    {
        ConfigFile config = new ConfigFile();
        config.SetValue("records", "best_score", BestScore);
        Error error = config.Save(RecordsPath);
        if (error != Error.Ok)
            GD.PushWarning("Could not save records.cfg: " + error);
    }
}
