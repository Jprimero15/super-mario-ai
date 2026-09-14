using Godot;

public partial class AudioManager : Node
{
    private const string SettingsPath = "user://settings.cfg";
    private const string MusicBus = "Music";
    private const string SfxBus = "SFX";
    private const int SfxPlayerCount = 8;

    private static readonly string[] SfxTypes =
    {
        "jump", "coin", "stomp", "hit", "shield", "game_over"
    };

    public float MusicVolume { get; private set; } = 0.8f;
    public float SfxVolume { get; private set; } = 0.9f;

    private AudioStreamPlayer[] _sfxPlayers = System.Array.Empty<AudioStreamPlayer>();
    private int _nextSfxPlayer;

    public override void _Ready()
    {
        EnsureBus(MusicBus);
        EnsureBus(SfxBus);
        BuildSfxPlayers();
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

    public void PlaySfx(string type)
    {
        if (string.IsNullOrEmpty(type) || SfxVolume <= 0.0f || _sfxPlayers.Length == 0)
            return;

        string path = $"res://audio/sfx/{type}.ogg";
        if (!ResourceLoader.Exists(path))
            path = $"res://audio/sfx/{type}.wav";
        if (!ResourceLoader.Exists(path))
            return;

        AudioStream stream = ResourceLoader.Load<AudioStream>(path);
        if (stream == null)
            return;

        AudioStreamPlayer player = _sfxPlayers[_nextSfxPlayer];
        _nextSfxPlayer = (_nextSfxPlayer + 1) % _sfxPlayers.Length;
        player.Stream = stream;
        player.Play();
    }

    private void BuildSfxPlayers()
    {
        _sfxPlayers = new AudioStreamPlayer[SfxPlayerCount];
        for (int i = 0; i < _sfxPlayers.Length; i++)
        {
            var player = new AudioStreamPlayer
            {
                Name = $"SfxPlayer{i}",
                Bus = SfxBus,
                ProcessMode = ProcessModeEnum.Always
            };
            AddChild(player);
            _sfxPlayers[i] = player;
        }
    }

    private void ApplyMusic() => SetBusVolume(MusicBus, MusicVolume);
    private void ApplySfx() => SetBusVolume(SfxBus, SfxVolume);

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
