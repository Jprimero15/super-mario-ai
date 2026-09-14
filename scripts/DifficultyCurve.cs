using Godot;

public static class MaryouDifficultyCurve
{
    private const float MaxSpeed = 520.0f;
    private const float MaxDistance = 1800.0f;

    public static int TierForSteps(int distanceSteps)
    {
        if (distanceSteps < 300) return 1;
        if (distanceSteps < 750) return 2;
        if (distanceSteps < 1400) return 3;
        return 4;
    }

    public static float Progress(int distanceSteps) => Mathf.Clamp((float)Mathf.Max(distanceSteps, 0) / MaxDistance, 0.0f, 1.0f);
    public static float SpeedForSteps(int distanceSteps) => Mathf.Lerp(300.0f, MaxSpeed, Mathf.Pow(Progress(distanceSteps), 0.78f));
    public static float HoleChance(int distanceSteps) => Mathf.Lerp(0.045f, 0.14f, Progress(distanceSteps));
    public static float PipeChance(int distanceSteps) => Mathf.Lerp(0.10f, 0.34f, Progress(distanceSteps));
    public static int EnemyCount(int distanceSteps) => Mathf.Clamp(1 + (int)(distanceSteps / 420.0f), 0, 4);

    public static int EnemyKind(int distanceSteps, int index)
    {
        int tier = TierForSteps(distanceSteps);
        if (tier <= 1) return 0;
        if (tier == 2) return new[] { 0, 1, 0, 2 }[index % 4];
        if (tier == 3) return new[] { 0, 1, 2, 3, 0, 4 }[index % 6];
        return new[] { 0, 1, 2, 3, 4, 5 }[index % 6];
    }

    public static float EnemySpeed(int kind, int tier)
    {
        float[] speeds = { 68.0f, 82.0f, 108.0f, 76.0f, 92.0f, 70.0f };
        float baseSpeed = speeds[Mathf.Clamp(kind, 0, speeds.Length - 1)];
        return baseSpeed + Mathf.Max(tier - 1, 0) * 8.0f;
    }
}
