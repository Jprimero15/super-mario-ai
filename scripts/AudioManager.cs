using Godot;
using System.Collections.Generic;

public partial class AudioManager : Node
{
    private const string SettingsPath = "user://settings.cfg";
    private const string MusicBus = "Music";
    private const string SfxBus = "SFX";

    public float MusicVolume { get; private set; } = 0.8f;
    public float SfxVolume { get; private set; } = 0.9f;

    private AudioStreamPlayer _musicPlayer;

    public override void _Ready()
    {
        EnsureBus(MusicBus);
        EnsureBus(SfxBus);
        LoadSettings();
        ApplyMusic();
        ApplySfx();
        StartProceduralMusic();
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
        AudioStreamPlayer player = new AudioStreamPlayer();
        player.Bus = SfxBus;
        player.Stream = CreateTone(type);
        AddChild(player);
        player.Finished += player.QueueFree;
        player.Play();
    }

    private AudioStreamWav CreateTone(string type)
    {
        Dictionary<string, float> frequencies = new Dictionary<string, float>
        {
            { "jump", 520.0f },
            { "coin", 880.0f },
            { "stomp", 220.0f },
            { "hit", 120.0f },
            { "shield", 660.0f },
            { "game_over", 90.0f },
            { "ui", 440.0f }
        };

        float frequency = 440.0f;
        if (frequencies.ContainsKey(type))
            frequency = frequencies[type];

        float length = type == "game_over" ? 0.35f : 0.10f;
        int sampleRate = 22050;
        int sampleCount = Mathf.RoundToInt(length * sampleRate);
        byte[] data = new byte[sampleCount * 2];

        for (int i = 0; i < sampleCount; i++)
        {
            float time = (float)i / sampleRate;
            float envelope = 1.0f - (float)i / sampleCount;
            float value = Mathf.Sin(Mathf.Tau * frequency * time) * envelope * 0.22f;
            int sample = Mathf.Clamp(Mathf.RoundToInt(value * 32767.0f), -32768, 32767);
            data[i * 2] = (byte)(sample & 255);
            data[i * 2 + 1] = (byte)((sample >> 8) & 255);
        }

        AudioStreamWav stream = new AudioStreamWav();
        stream.Format = AudioStreamWav.FormatEnum.Format16Bits;
        stream.MixRate = sampleRate;
        stream.Stereo = false;
        stream.Data = data;
        return stream;
    }

    private void StartProceduralMusic()
    {
        _musicPlayer = new AudioStreamPlayer();
        _musicPlayer.Bus = MusicBus;
        _musicPlayer.Stream = CreateTone("ui");
        _musicPlayer.VolumeDb = -24.0f;
        AddChild(_musicPlayer);
        _musicPlayer.Play();
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

        MusicVolume = Mathf.Clamp(config.GetValue("audio", "music", 0.8).AsSingle(), 0.0f, 1.0f);
        SfxVolume = Mathf.Clamp(config.GetValue("audio", "sfx", 0.9).AsSingle(), 0.0f, 1.0f);
    }

    private void SaveSettings()
    {
        ConfigFile config = new ConfigFile();
        config.SetValue("audio", "music", MusicVolume);
        config.SetValue("audio", "sfx", SfxVolume);

        if (config.Save(SettingsPath) != Error.Ok)
            GD.PushWarning("Could not save audio settings");
    }
}
