using Godot;
using System;
using System.Collections.Generic;

public partial class AudioManager : Node
{
    private const string SettingsPath = "user://settings.cfg";
    private const string MusicBus = "Music";
    private const string SfxBus = "SFX";
    public float MusicVolume { get; private set; } = 0.8f;
    public float SfxVolume { get; private set; } = 0.9f;
    private AudioStreamPlayer? _musicPlayer;

    public override void _Ready()
    {
        EnsureBus(MusicBus); EnsureBus(SfxBus); LoadSettings(); ApplyMusic(); ApplySfx(); StartProceduralMusic();
    }

    public void SetMusicVolume(float value) { MusicVolume = Mathf.Clamp(value, 0, 1); ApplyMusic(); SaveSettings(); }
    public void SetSfxVolume(float value) { SfxVolume = Mathf.Clamp(value, 0, 1); ApplySfx(); SaveSettings(); }

    public void PlaySfx(string type)
    {
        var player = new AudioStreamPlayer { Bus = SfxBus, Stream = ToneStream(type) };
        AddChild(player); player.Finished += player.QueueFree; player.Play();
    }

    private AudioStreamWav ToneStream(string type)
    {
        var frequencies = new Dictionary<string, float> { ["jump"] = 520, ["coin"] = 880, ["stomp"] = 220, ["hit"] = 120, ["shield"] = 660, ["game_over"] = 90, ["ui"] = 440 };
        float frequency = frequencies.TryGetValue(type, out var f) ? f : 440;
        float length = type == "game_over" ? 0.35f : 0.10f;
        int rate = 22050, samples = (int)(length * rate);
        var data = new byte[samples * 2];
        for (int i = 0; i < samples; i++)
        {
            float t = (float)i / rate;
            float envelope = 1.0f - (float)i / samples;
            float value = Mathf.Sin(Mathf.Tau * frequency * t) * envelope * 0.22f;
            int sample = Mathf.Clamp((int)(value * 32767), -32768, 32767);
            data[i * 2] = (byte)(sample & 255);
            data[i * 2 + 1] = (byte)((sample >> 8) & 255);
        }
        return new AudioStreamWav { Format = AudioStreamWav.FormatEnum.Format16Bits, MixRate = rate, Stereo = false, Data = data };
    }

    private void StartProceduralMusic()
    {
        _musicPlayer = new AudioStreamPlayer { Bus = MusicBus, Stream = ToneStream("ui"), VolumeDb = -24.0f };
        AddChild(_musicPlayer); _musicPlayer.Play();
    }

    private void ApplyMusic() => SetBusVolume(MusicBus, MusicVolume);
    private void ApplySfx() => SetBusVolume(SfxBus, SfxVolume);
    private void SetBusVolume(string busName, float value)
    {
        int index = AudioServer.GetBusIndex(busName);
        if (index >= 0) AudioServer.SetBusVolumeDb(index, Mathf.LinearToDb(Mathf.Max(value, 0.0001f)));
    }

    private void EnsureBus(string name)
    {
        if (AudioServer.GetBusIndex(name) != -1) return;
        AudioServer.AddBus(); AudioServer.SetBusName(AudioServer.BusCount - 1, name);
    }

    private void LoadSettings()
    {
        var cfg = new ConfigFile();
        if (cfg.Load(SettingsPath) != Error.Ok) return;
        MusicVolume = Mathf.Clamp((float)cfg.GetValue("audio", "music", 0.8), 0, 1);
        SfxVolume = Mathf.Clamp((float)cfg.GetValue("audio", "sfx", 0.9), 0, 1);
    }

    private void SaveSettings()
    {
        var cfg = new ConfigFile(); cfg.SetValue("audio", "music", MusicVolume); cfg.SetValue("audio", "sfx", SfxVolume);
        if (cfg.Save(SettingsPath) != Error.Ok) GD.PushWarning("Could not save audio settings");
    }
}
