using Godot;

[GlobalClass]
public partial class AudioManager : Node
{
    private const string SettingsPath = "user://settings.cfg";
    private const string MusicBus = "Music";
    private const string SfxBus = "SFX";
    private const int SfxPlayerCount = 8;

    public float MusicVolume { get; private set; } = 0.8f;
    public float SfxVolume { get; private set; } = 0.9f;

    private AudioStreamPlayer[] _sfxPlayers = System.Array.Empty<AudioStreamPlayer>();
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
        if (string.IsNullOrEmpty(type) || SfxVolume <= 0.0f || _sfxPlayers.Length == 0)
            return;

        AudioStream? stream = LoadSfx(type);
        if (stream == null)
            return;

        AudioStreamPlayer player = _sfxPlayers[_nextSfxPlayer];
        _nextSfxPlayer = (_nextSfxPlayer + 1) % _sfxPlayers.Length;
        player.Stream = stream;
        player.Play();
    }

    private AudioStream? LoadSfx(string type)
    {
        string oggPath = "res://audio/sfx/" + type + ".ogg";
        if (ResourceLoader.Exists(oggPath))
        {
            AudioStream? ogg = ResourceLoader.Load<AudioStream>(oggPath);
            if (ogg != null)
                return ogg;
        }

        string wavPath = "res://audio/sfx/" + type + ".wav";
        if (ResourceLoader.Exists(wavPath))
        {
            AudioStream? wav = ResourceLoader.Load<AudioStream>(wavPath);
            if (wav != null)
                return wav;
        }

        return null;
    }

    private void CreateSfxPlayers()
    {
        _sfxPlayers = new AudioStreamPlayer[SfxPlayerCount];

        for (int i = 0; i < _sfxPlayers.Length; i++)
        {
            AudioStreamPlayer player = new AudioStreamPlayer();
            player.Name = "SfxPlayer" + i;
            player.Bus = SfxBus;
            player.ProcessMode = ProcessModeEnum.Always;
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

        if (value <= 0.0f)
        {
            AudioServer.SetBusMute(busIndex, true);
            return;
        }

        AudioServer.SetBusMute(busIndex, false);
        AudioServer.SetBusVolumeDb(busIndex, Mathf.LinearToDb(value));
    }

    private void EnsureBus(string busName)
    {
        if (AudioServer.GetBusIndex(busName) >= 0)
            return;

        AudioServer.AddBus();
        int index = AudioServer.BusCount - 1;
        AudioServer.SetBusName(index, busName);
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
            GD.PushWarning("Could not save settings.cfg: " + error);
    }
}
