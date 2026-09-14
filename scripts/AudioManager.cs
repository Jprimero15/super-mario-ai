using Godot;

public partial class AudioManager : Node
{
    private const string SettingsPath = "user://settings.cfg";
    private const string MusicBus = "Music";
    private const string SfxBus = "SFX";
    private const int SfxPlayerCount = 8;

    public float MusicVolume { get; private set; } = 0.8f;
    public float SfxVolume { get; private set; } = 0.9f;

    private readonly AudioStreamPlayer[] _sfxPlayers = new AudioStreamPlayer[SfxPlayerCount];
    private int _nextSfxPlayer;

    public override void _Ready()
    {
        EnsureBus(MusicBus);
        EnsureBus(SfxBus);
        CreateSfxPlayers();
        LoadSettings();
        ApplyVolumes();
    }

    public void SetMusicVolume(float value)
    {
        MusicVolume = Mathf.Clamp(value, 0.0f, 1.0f);
        SetBusVolume(MusicBus, MusicVolume);
        SaveSettings();
    }

    public void SetSfxVolume(float value)
    {
        SfxVolume = Mathf.Clamp(value, 0.0f, 1.0f);
        SetBusVolume(SfxBus, SfxVolume);
        SaveSettings();
    }

    public void PlaySfx(string type)
    {
        if (string.IsNullOrEmpty(type) || SfxVolume <= 0.0f)
            return;

        AudioStream stream = LoadSfx(type);
        if (stream == null)
            return;

        AudioStreamPlayer player = _sfxPlayers[_nextSfxPlayer];
        _nextSfxPlayer = (_nextSfxPlayer + 1) % SfxPlayerCount;
        player.Stream = stream;
        player.Play();
    }

    private AudioStream LoadSfx(string type)
    {
        string oggPath = "res://audio/sfx/" + type + ".ogg";
        if (ResourceLoader.Exists(oggPath))
        {
            AudioStream stream = ResourceLoader.Load<AudioStream>(oggPath);
            if (stream != null)
                return stream;
        }

        string wavPath = "res://audio/sfx/" + type + ".wav";
        if (ResourceLoader.Exists(wavPath))
        {
            AudioStream stream = ResourceLoader.Load<AudioStream>(wavPath);
            if (stream != null)
                return stream;
        }

        return null;
    }

    private void CreateSfxPlayers()
    {
        for (int i = 0; i < SfxPlayerCount; i++)
        {
            AudioStreamPlayer player = new AudioStreamPlayer
            {
                Name = "SfxPlayer" + i,
                Bus = SfxBus,
                ProcessMode = ProcessModeEnum.Always
            };
            AddChild(player);
            _sfxPlayers[i] = player;
        }
    }

    private void ApplyVolumes()
    {
        SetBusVolume(MusicBus, MusicVolume);
        SetBusVolume(SfxBus, SfxVolume);
    }

    private void SetBusVolume(string busName, float value)
    {
        int busIndex = AudioServer.GetBusIndex(busName);
        if (busIndex < 0)
            return;

        AudioServer.SetBusMute(busIndex, value <= 0.0f);
        if (value > 0.0f)
            AudioServer.SetBusVolumeDb(busIndex, Mathf.LinearToDb(value));
    }

    private void EnsureBus(string busName)
    {
        if (AudioServer.GetBusIndex(busName) >= 0)
            return;

        AudioServer.AddBus();
        AudioServer.SetBusName(AudioServer.BusCount - 1, busName);
    }

    private void LoadSettings()
    {
        ConfigFile config = new ConfigFile();
        if (config.Load(SettingsPath) != Error.Ok)
            return;

        MusicVolume = Mathf.Clamp(config.GetValue("audio", "music", 0.8f).AsSingle(), 0.0f, 1.0f);
        SfxVolume = Mathf.Clamp(config.GetValue("audio", "sfx", 0.9f).AsSingle(), 0.0f, 1.0f);
    }

    private void SaveSettings()
    {
        ConfigFile config = new ConfigFile();
        config.SetValue("audio", "music", MusicVolume);
        config.SetValue("audio", "sfx", SfxVolume);

        Error error = config.Save(SettingsPath);
        if (error != Error.Ok)
            GD.PushWarning("Could not save settings.cfg: " + error);
    }
}
