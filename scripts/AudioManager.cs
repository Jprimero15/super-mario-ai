using Godot;

public partial class AudioManager : Node
{
    private const string SettingsPath = "user://settings.cfg";
    private const string MusicBus = "Music";
    private const string SfxBus = "SFX";

    public float MusicVolume { get; private set; } = 0.8f;
    public float SfxVolume { get; private set; } = 0.9f;

    public override void _Ready()
    {
        EnsureBus(MusicBus);
        EnsureBus(SfxBus);
        LoadSettings();
        ApplyMusic();
        ApplySfx();
    }

    public void SetMusicVolume(float value)
    {
        MusicVolume = Mathf.Clamp(value, 0.0f, 1.0f);
        ApplyMusic();
        SaveSettings();
    }

    public void SetSfxVolume(float value)
    {
        SfxVolume = Mathf.Clamp(value, 0.0f, 1.0f);
        ApplySfx();
        SaveSettings();
    }

    // Kept intentionally lightweight so the manager is safe during project startup.
    // Individual gameplay scenes can provide their own AudioStreamPlayer nodes later.
    public void PlaySfx(string type)
    {
        if (string.IsNullOrEmpty(type))
            return;
    }

    private void ApplyMusic()
    {
        SetBusVolume(MusicBus, MusicVolume);
    }

    private void ApplySfx()
    {
        SetBusVolume(SfxBus, SfxVolume);
    }

    private void SetBusVolume(string busName, float value)
    {
        int index = AudioServer.GetBusIndex(busName);
        if (index >= 0)
            AudioServer.SetBusVolumeDb(index, Mathf.LinearToDb(Mathf.Max(value, 0.0001f)));
    }

    private void EnsureBus(string name)
    {
        if (AudioServer.GetBusIndex(name) >= 0)
            return;

        AudioServer.AddBus();
        AudioServer.SetBusName(AudioServer.BusCount - 1, name);
    }

    private void LoadSettings()
    {
        ConfigFile config = new ConfigFile();
        if (config.Load(SettingsPath) != Error.Ok)
            return;

        Variant music = config.GetValue("audio", "music", 0.8f);
        Variant sfx = config.GetValue("audio", "sfx", 0.9f);

        MusicVolume = Mathf.Clamp(music.AsSingle(), 0.0f, 1.0f);
        SfxVolume = Mathf.Clamp(sfx.AsSingle(), 0.0f, 1.0f);
    }

    private void SaveSettings()
    {
        ConfigFile config = new ConfigFile();
        config.SetValue("audio", "music", MusicVolume);
        config.SetValue("audio", "sfx", SfxVolume);

        Error error = config.Save(SettingsPath);
        if (error != Error.Ok)
            GD.PushWarning("Could not save audio settings: " + error);
    }
}
