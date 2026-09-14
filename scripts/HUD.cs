using Godot;
using System;

public partial class HUD : CanvasLayer
{
    [Signal] public delegate void PausePressedEventHandler();
    [Signal] public delegate void RestartPressedEventHandler();
    [Signal] public delegate void BackPressedEventHandler();
    private Label? _scoreLabel, _statusLabel, _tierLabel, _titleLabel, _infoLabel;
    private ColorRect? _overlay;
    private PanelContainer? _panel, _settingsPanel;
    private HSlider? _musicSlider, _sfxSlider;

    public override void _Ready() { ProcessMode = ProcessModeEnum.Always; Build(); }

    private void Build()
    {
        var root = new Control(); root.SetAnchorsAndOffsetsPreset(Control.LayoutPreset.FullRect); root.MouseFilter = Control.MouseFilterEnum.Ignore; AddChild(root);
        var top = new MarginContainer { OffsetLeft = 20, OffsetTop = 14, OffsetRight = -20, OffsetBottom = 96, MouseFilter = Control.MouseFilterEnum.Ignore }; top.SetAnchorsPreset(Control.LayoutPreset.TopWide); root.AddChild(top);
        var row = new HBoxContainer { Theme = null }; row.AddThemeConstantOverride("separation", 12); top.AddChild(row);
        var stats = new VBoxContainer { SizeFlagsHorizontal = Control.SizeFlags.ExpandFill }; row.AddChild(stats);
        _scoreLabel = Label("SCORE 0", 26); stats.AddChild(_scoreLabel); _statusLabel = Label("LIVES 3   COINS 0   ×1", 17); stats.AddChild(_statusLabel);
        var right = new VBoxContainer { CustomMinimumSize = new Vector2(170, 0) }; row.AddChild(right);
        _tierLabel = Label("TIER 1   0m", 17); _tierLabel.HorizontalAlignment = HorizontalAlignment.Right; right.AddChild(_tierLabel);
        var pause = Button("PAUSE", 46); pause.ProcessMode = ProcessModeEnum.Always; pause.Pressed += () => EmitSignal(SignalName.PausePressed); right.AddChild(pause);
        var controls = new HBoxContainer { OffsetLeft = 18, OffsetTop = -102, OffsetRight = -18, OffsetBottom = -16, MouseFilter = Control.MouseFilterEnum.Stop }; controls.SetAnchorsPreset(Control.LayoutPreset.BottomWide); controls.AddThemeConstantOverride("separation", 10); root.AddChild(controls);
        AddControlButton(controls, "‹", "move_left", 82); AddControlButton(controls, "›", "move_right", 82); controls.AddChild(new Control { SizeFlagsHorizontal = Control.SizeFlags.ExpandFill }); AddControlButton(controls, "JUMP", "jump", 138);
        _overlay = new ColorRect { Color = new Color(.02f, .04f, .08f, .76f), Visible = false, MouseFilter = Control.MouseFilterEnum.Stop }; _overlay.SetAnchorsAndOffsetsPreset(Control.LayoutPreset.FullRect); root.AddChild(_overlay);
        _panel = MakePanel(); root.AddChild(_panel); _settingsPanel = MakeSettings(); root.AddChild(_settingsPanel);
    }

    private Label Label(string text, int size) { var label = new Label { Text = text }; label.AddThemeFontSizeOverride("font_size", size); label.AddThemeColorOverride("font_color", new Color("#f5f7ff")); return label; }
    private Button Button(string text, int height) { var b = new Button { Text = text, CustomMinimumSize = new Vector2(0, height) }; b.AddThemeFontSizeOverride("font_size", 18); b.AddThemeColorOverride("font_color", new Color("#f5f7ff")); b.AddThemeStyleboxOverride("normal", Box(new Color("#1a2740"), 14)); b.AddThemeStyleboxOverride("hover", Box(new Color("#243758"), 14)); b.AddThemeStyleboxOverride("pressed", Box(new Color("#315080"), 14)); return b; }
    private void AddControlButton(HBoxContainer parent, string text, string action, int width) { var b = Button(text, 68); b.CustomMinimumSize = new Vector2(width, 68); b.AddThemeFontSizeOverride("font_size", action == "jump" ? 20 : 28); b.ButtonDown += () => Input.ActionPress(action); b.ButtonUp += () => Input.ActionRelease(action); b.FocusMode = Control.FocusModeEnum.None; parent.AddChild(b); }

    private PanelContainer MakePanel()
    {
        var p = new PanelContainer { OffsetLeft = -210, OffsetTop = -170, OffsetRight = 210, OffsetBottom = 170, Visible = false }; p.SetAnchorsPreset(Control.LayoutPreset.Center); p.AddThemeStyleboxOverride("panel", Box(new Color("#111d31"), 22));
        var box = new VBoxContainer(); box.AddThemeConstantOverride("separation", 12); p.AddChild(box);
        _titleLabel = Label("PAUSED", 32); _titleLabel.HorizontalAlignment = HorizontalAlignment.Center; box.AddChild(_titleLabel); _infoLabel = Label("", 18); _infoLabel.HorizontalAlignment = HorizontalAlignment.Center; box.AddChild(_infoLabel);
        AddButton(box, "Resume", () => EmitSignal(SignalName.PausePressed)); AddButton(box, "Restart", () => EmitSignal(SignalName.RestartPressed)); AddButton(box, "Settings", () => ShowSettings(true)); AddButton(box, "Back", () => EmitSignal(SignalName.BackPressed)); return p;
    }

    private PanelContainer MakeSettings()
    {
        var p = new PanelContainer { OffsetLeft = -220, OffsetTop = -205, OffsetRight = 220, OffsetBottom = 205, Visible = false }; p.SetAnchorsPreset(Control.LayoutPreset.Center); p.AddThemeStyleboxOverride("panel", Box(new Color("#111d31"), 22));
        var box = new VBoxContainer(); box.AddThemeConstantOverride("separation", 10); p.AddChild(box);
        var title = Label("SETTINGS", 30); title.HorizontalAlignment = HorizontalAlignment.Center; box.AddChild(title); box.AddChild(Label("Music volume", 17));
        AudioManager audio = GetNode<AudioManager>("/root/AudioManager");
        _musicSlider = new HSlider { MinValue = 0, MaxValue = 1, Step = .01, Value = audio.MusicVolume }; _musicSlider.ValueChanged += v => audio.SetMusicVolume((float)v); box.AddChild(_musicSlider);
        box.AddChild(Label("SFX volume", 17)); _sfxSlider = new HSlider { MinValue = 0, MaxValue = 1, Step = .01, Value = audio.SfxVolume }; _sfxSlider.ValueChanged += v => audio.SetSfxVolume((float)v); box.AddChild(_sfxSlider);
        box.AddChild(Label("Touch controls are intentionally large for phones.", 14)); AddButton(box, "Close", () => ShowSettings(false)); return p;
    }

    private void AddButton(VBoxContainer parent, string text, Action callback) { var button = Button(text, 50); button.Pressed += callback; parent.AddChild(button); }
    public void ShowPause(bool value, int score, int best) { if (_settingsPanel != null) _settingsPanel.Visible = false; _overlay!.Visible = value; _panel!.Visible = value; _titleLabel!.Text = "PAUSED"; _infoLabel!.Text = $"Score {score}   Best {best}"; AnimatePanel(value); }
    public void ShowGameOver(int score, int best) { _overlay!.Visible = true; _panel!.Visible = true; _titleLabel!.Text = "RUN COMPLETE"; _infoLabel!.Text = $"Score {score}   Best {best}"; AnimatePanel(true); }
    public void UpdateStats(int score, int lives, int coins, int combo, int tier, int meters) { _scoreLabel!.Text = $"SCORE {score}"; _statusLabel!.Text = $"LIVES {lives}   COINS {coins}   ×{Mathf.Max(1, combo)}"; _tierLabel!.Text = $"TIER {tier}   {meters}m"; }
    private void ShowSettings(bool value) { _settingsPanel!.Visible = value; if (value) _panel!.Visible = false; _overlay!.Visible = value; if (!value) _panel!.Visible = true; }
    private void AnimatePanel(bool value) { if (!value) return; _panel!.Scale = new Vector2(.94f, .94f); _panel.Modulate = new Color(1, 1, 1, 0); var tween = CreateTween().SetParallel(); tween.TweenProperty(_panel, "scale", Vector2.One, .18).SetTrans(Tween.TransitionType.Back).SetEase(Tween.EaseType.Out); tween.TweenProperty(_panel, "modulate", Colors.White, .16); }
    private static StyleBoxFlat Box(Color color, int radius) { return new StyleBoxFlat { BgColor = color, CornerRadiusTopLeft = radius, CornerRadiusTopRight = radius, CornerRadiusBottomLeft = radius, CornerRadiusBottomRight = radius, ContentMarginLeft = 16, ContentMarginRight = 16, ContentMarginTop = 8, ContentMarginBottom = 8 }; }
}
