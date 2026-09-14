using Godot;
using System.Collections.Generic;

public partial class MaryouWorldGenerator : Node2D
{
    [Signal] public delegate void CoinCollectedEventHandler();
    [Signal] public delegate void ShieldCollectedEventHandler();
    [Signal] public delegate void HazardHitEventHandler();
    private const float ChunkWidth = 640, Tile = 32, GroundY = 560;
    private const int Seed = 20260914;
    private readonly Texture2D _pipeTexture = GD.Load<Texture2D>("res://assets/sprites/pipe.svg");
    private readonly Texture2D _environment = GD.Load<Texture2D>("res://assets/world/environment.svg");
    private readonly Texture2D _obstacles = GD.Load<Texture2D>("res://assets/world/obstacles.svg");
    private int _generatedTo = -1;
    private readonly Dictionary<int, MaryouChunk> _activeChunks = new();
    private Node2D? _enemyRoot;

    public void Setup(Node2D enemies) => _enemyRoot = enemies;

    public void GenerateUntil(float playerX, int distanceSteps)
    {
        int target = (int)Mathf.Floor(playerX / ChunkWidth) + 3;
        while (_generatedTo < target) { _generatedTo++; GenerateChunk(_generatedTo, distanceSteps); }
        Prune(playerX);
    }

    private void GenerateChunk(int chunkIndex, int distanceSteps)
    {
        if (_activeChunks.ContainsKey(chunkIndex)) return;
        var root = new MaryouChunk { Name = $"Chunk_{chunkIndex}" }; AddChild(root); _activeChunks[chunkIndex] = root;
        var rng = new RandomNumberGenerator { Seed = (ulong)(Seed + chunkIndex * 7919) };
        float startX = chunkIndex * ChunkWidth;
        var occupied = new List<Rect2>(); var holes = new List<Rect2>();
        float holeChance = MaryouDifficultyCurve.HoleChance(distanceSteps), safeGapUntil = startX + 300;
        for (int i = 0; i < 20; i++)
        {
            float x = startX + i * Tile;
            if (x < safeGapUntil) continue;
            if (i > 7 && i < 19 && rng.Randf() < holeChance)
            {
                float width = rng.RandiRange(32, 64); var hole = new Rect2(x, GroundY, width, Tile); holes.Add(hole); safeGapUntil = hole.End.X + 96;
            }
        }
        for (int i = 0; i < 20; i++)
        {
            var tile = new Rect2(startX + i * Tile, GroundY, Tile, Tile); bool blocked = false;
            foreach (var hole in holes) if (tile.Intersects(hole)) { blocked = true; break; }
            if (!blocked) { AddSolid(root, tile); root.Solids.Add(tile); }
        }
        root.Holes.AddRange(holes); foreach (var hole in holes) AddHoleWarning(root, hole);

        if (chunkIndex > 0)
        {
            int decorCount = rng.RandiRange(2, 4);
            for (int i = 0; i < decorCount; i++) AddEnvironment(root, startX + rng.RandiRange(2, 19) * Tile, rng.Randf() < .58f ? 0 : 160);
        }

        int pipeAttempts = 1 + distanceSteps / 500, pipesPlaced = 0;
        for (int i = 0; i < pipeAttempts; i++)
        {
            if (rng.Randf() > MaryouDifficultyCurve.PipeChance(distanceSteps)) continue;
            var pipe = new Rect2(startX + rng.RandiRange(9, 18) * Tile, GroundY - rng.RandiRange(2, 3) * Tile, Tile, rng.RandiRange(2, 3) * Tile);
            if (pipe.Position.X < safeGapUntil || !Safe(pipe, occupied, holes, 42)) continue;
            occupied.Add(pipe); root.Pipes.Add(pipe); AddSolid(root, pipe); AddHazard(root, pipe); pipesPlaced++;
        }
        if (chunkIndex > 0 && pipesPlaced == 0 && rng.Randf() < .78f)
        {
            var fallback = new Rect2(startX + rng.RandiRange(13, 17) * Tile, GroundY - 64, Tile, 64);
            if (Safe(fallback, occupied, holes, 42)) { occupied.Add(fallback); root.Pipes.Add(fallback); AddSolid(root, fallback); AddHazard(root, fallback); }
        }

        int coinCount = rng.RandiRange(2, 4);
        for (int i = 0; i < coinCount; i++)
        {
            var pos = new Vector2(startX + rng.RandiRange(7, 18) * Tile, GroundY - rng.RandiRange(92, 190));
            if (Safe(new Rect2(pos - new Vector2(15, 15), new Vector2(30, 30)), occupied, holes, 10)) AddCollectible(root, pos, "coin");
        }
        if (distanceSteps >= 650 && rng.Randf() < .22f)
        {
            var pos = new Vector2(startX + rng.RandiRange(14, 19) * Tile, GroundY - 120);
            if (Safe(new Rect2(pos - new Vector2(15, 15), new Vector2(30, 30)), occupied, holes, 10)) AddCollectible(root, pos, "shield");
        }

        int count = MaryouDifficultyCurve.EnemyCount(distanceSteps); if (chunkIndex == 0) count = 0;
        for (int i = 0; i < count; i++)
            for (int attempt = 0; attempt < 8; attempt++)
            {
                float enemyX = startX + rng.RandiRange(11, 18) * Tile; var enemyRect = new Rect2(enemyX - 18, GroundY - 58, 36, 58);
                if (!Safe(enemyRect, occupied, holes, 48)) continue;
                var enemy = new MaryouEnemy { Position = new Vector2(enemyX, GroundY - 40) }; enemy.Setup(MaryouDifficultyCurve.EnemyKind(distanceSteps, i)); _enemyRoot!.AddChild(enemy); occupied.Add(enemyRect); break;
            }
    }

    private void AddSolid(Node2D parent, Rect2 rect)
    {
        var body = new StaticBody2D { CollisionLayer = 2, CollisionMask = 1, Position = rect.Position + rect.Size * .5f };
        body.AddChild(new CollisionShape2D { Shape = new RectangleShape2D { Size = rect.Size } });
        if (rect.Position.Y < GroundY && rect.Size.Y > Tile)
        {
            var sprite = new Sprite2D { Texture = _pipeTexture, TextureFilter = CanvasItem.TextureFilterEnum.Nearest, Scale = new Vector2(rect.Size.X / 32, rect.Size.Y / 96) }; body.AddChild(sprite);
        }
        parent.AddChild(body);
    }

    private void AddHoleWarning(Node2D parent, Rect2 hole)
    {
        var sprite = new Sprite2D { Texture = _obstacles, RegionEnabled = true, RegionRect = new Rect2(200, 8, 58, 60), TextureFilter = CanvasItem.TextureFilterEnum.Nearest, Position = new Vector2(hole.Position.X + hole.Size.X * .5f, GroundY - 18), Scale = new Vector2(hole.Size.X / 58, .72f) };
        parent.AddChild(sprite);
    }

    private void AddEnvironment(Node2D parent, float x, float sourceY)
    {
        var sprite = new Sprite2D { Texture = _environment, RegionEnabled = true, TextureFilter = CanvasItem.TextureFilterEnum.Nearest, Scale = new Vector2(.78f, .78f), ZIndex = 1 };
        if (sourceY == 0) sprite.RegionRect = (int)(x / Tile) % 3 == 0 ? new Rect2(0, 0, 88, 142) : new Rect2(160, 0, 96, 126);
        else sprite.RegionRect = new Rect2(256, 0, 104, 126);
        sprite.Position = new Vector2(x, GroundY - 63); parent.AddChild(sprite);
    }

    private void AddHazard(Node2D parent, Rect2 pipe)
    {
        var area = new MaryouHazard(); float height = Mathf.Max(10, pipe.Size.Y - 20); area.Position = pipe.Position + new Vector2(pipe.Size.X * .5f, pipe.Size.Y * .5f + 10);
        area.Setup(new Vector2(pipe.Size.X + 8, height)); area.HitPlayer += () => EmitSignal(SignalName.HazardHit); parent.AddChild(area);
    }

    private void AddCollectible(Node2D parent, Vector2 position, string kind)
    {
        var item = new MaryouCollectible { Position = position }; item.Setup(kind); item.Collected += OnCollectible; parent.AddChild(item);
    }

    private void OnCollectible(string kind) { if (kind == "shield") EmitSignal(SignalName.ShieldCollected); else EmitSignal(SignalName.CoinCollected); }

    private static bool Safe(Rect2 rect, List<Rect2> occupied, List<Rect2> holes, float padding)
    {
        var expanded = rect.Grow(padding); foreach (var hole in holes) if (expanded.Intersects(hole)) return false; foreach (var other in occupied) if (expanded.Intersects(other)) return false; return true;
    }

    private void Prune(float playerX)
    {
        float pruneBefore = playerX - ChunkWidth * 5;
        foreach (int key in new List<int>(_activeChunks.Keys)) if (IsInstanceValid(_activeChunks[key]) && key * ChunkWidth + ChunkWidth < pruneBefore) { _activeChunks[key].QueueFree(); _activeChunks.Remove(key); }
        if (_enemyRoot == null) return;
        foreach (Node child in _enemyRoot.GetChildren()) if (IsInstanceValid(child) && child is Node2D node && node.Position.X < pruneBefore) node.QueueFree();
    }
}
