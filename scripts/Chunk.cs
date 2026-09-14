using Godot;
using System.Collections.Generic;

public partial class MaryouChunk : Node2D
{
    public readonly List<Rect2> Solids = new();
    public readonly List<Rect2> Holes = new();
    public readonly List<Rect2> Pipes = new();

    public override void _Draw()
    {
        foreach (var rect in Solids)
        {
            DrawRect(rect, new Color("#5b3f2b"));
            DrawRect(new Rect2(rect.Position, new Vector2(rect.Size.X, 9)), new Color("#78ad52"));
            DrawLine(rect.Position, new Vector2(rect.End.X, rect.Position.Y), new Color("#9bca6b"), 2);
        }
        foreach (var rect in Holes)
        {
            DrawRect(rect, new Color("#25402d"));
            DrawLine(new Vector2(rect.Position.X, rect.Position.Y), new Vector2(rect.End.X, rect.Position.Y), new Color("#6b9b4d"), 3);
        }
        foreach (var rect in Pipes)
        {
            DrawStyleBox(Box(new Color("#3aaf65"), 8), rect);
            DrawRect(new Rect2(rect.Position.X - 4, rect.Position.Y, rect.Size.X + 8, 12), new Color("#70d487"));
            DrawLine(new Vector2(rect.Position.X + 6, rect.Position.Y + 15), new Vector2(rect.Position.X + 6, rect.End.Y), new Color("#2b814d"), 3);
        }
    }

    private static StyleBoxFlat Box(Color color, int radius)
    {
        var box = new StyleBoxFlat { BgColor = color, CornerRadiusTopLeft = radius, CornerRadiusTopRight = radius, CornerRadiusBottomLeft = radius, CornerRadiusBottomRight = radius };
        return box;
    }
}
