package com.outurnate.railbridges;

import java.util.function.Predicate;
import java.util.function.Function;

import net.minecraft.world.level.ChunkPos;

public final class TrackBridgeMap {
  public enum Piece {
    EW,
    NS,
    N,
    S,
    E,
    W,
    X
  }

  private enum Direction {
    NS,
    EW
  }

  public record Vec2i(int x, int z)
  {
    public Vec2i InChunks() {
      return new Vec2i(x / 16, z / 16);
    }
  }

  public record PieceWithOffset(Piece piece, int offsetX, int offsetZ) { }

  private final ImprovedNoise noise = new ImprovedNoise();

  int chunkSeparation; // must be >0
  double damageChance; // between 0 and 1
  double infiniteChance; // between 0 and 1

  public TrackBridgeMap(int chunkSeparation, double damageChance, double infiniteChance) {
    this.chunkSeparation = chunkSeparation;
    this.damageChance = damageChance;
    this.infiniteChance = infiniteChance;
  }

  private boolean isOnRawGrid(long seed, ChunkPos pos, Direction direction, Predicate<ChunkPos> biomeTester) {
    double x = direction == Direction.NS ? pos.x : pos.z;
    double z = direction == Direction.NS ? pos.z : pos.x;
    // early return if we're too close
    if (x % (chunkSeparation + 1) != 0)
      return false;
    
    // early return if we're in a banned biome
    if (!biomeTester.test(pos)) {
      return false;
    }
    // x is the "primary" factor - should change with each invocation
    // y describes the line we are part of
    // z is some factor that's unique in either direction
    // this keeps the NS lines from correlating with EW lines too much
    double factor = noise.noiseNormalized(x / 16.0, z / 8.0, ((seed % 64) / 8.0) * (direction == Direction.NS ? -1.0 : 1.0));
    if (factor <= this.infiniteChance) {
      // this is part of an undamaged, infinite track
      return true;
    }

    // otherwise, it's a track fragment
    double cutoff = 1.0 - damageChance;
    return factor > -cutoff && factor < cutoff;
  }

  private Piece getPieceAt(long seed, ChunkPos pos, Predicate<ChunkPos> biomeTester) {
    boolean isOnNS = isOnRawGrid(seed, pos, Direction.NS, biomeTester);
    boolean isOnEW = isOnRawGrid(seed, pos, Direction.EW, biomeTester);
    if (isOnEW && isOnNS)
      return Piece.X;
  
    ChunkPos north = new ChunkPos(pos.x, pos.z - 1);
    ChunkPos south = new ChunkPos(pos.x, pos.z + 1);
    boolean isNonNS = isOnRawGrid(seed, north, Direction.NS, biomeTester);
    boolean isSonNS = isOnRawGrid(seed, south, Direction.NS, biomeTester);
    boolean isNonEW = isOnRawGrid(seed, north, Direction.EW, biomeTester);
    boolean isSonEW = isOnRawGrid(seed, south, Direction.EW, biomeTester);
    if (isOnNS) {
      if ((isNonNS && isSonNS) || (isNonEW && isNonNS) || (isSonEW && isSonNS))
        return Piece.NS;
      if (isNonNS)
        return Piece.S;
      if (isSonNS)
        return Piece.N;
    }
    ChunkPos west = new ChunkPos(pos.x - 1, pos.z);
    ChunkPos east = new ChunkPos(pos.x + 1, pos.z);
    boolean isWonEW = isOnRawGrid(seed, west, Direction.EW, biomeTester);
    boolean isEonEW = isOnRawGrid(seed, east, Direction.EW, biomeTester);
    boolean isWonNS = isOnRawGrid(seed, west, Direction.NS, biomeTester);
    boolean isEonNS = isOnRawGrid(seed, east, Direction.NS, biomeTester);
    if (isOnEW) {
      if ((isWonEW && isEonEW) || (isWonEW && isWonNS) || (isEonEW && isEonNS))
        return Piece.EW;
      if (isWonEW)
        return Piece.E;
      if (isEonEW)
        return Piece.W;
    }
    // X pieces need a track in all four directions
    // if we got here, and we're adjacent, add a quick
    // cap piece
    if (isNonEW && isNonNS) {
      return Piece.S;
    }
    if (isSonEW && isSonNS) {
      return Piece.N;
    }
    if (isWonEW && isWonNS) {
      return Piece.E;
    }
    if (isEonEW && isEonNS) {
      return Piece.W;
    }
    return null;
  }

  public Vec2i[] getOverlayTestCheckPos(int width, int height) {
    Vec2i[] positions = new Vec2i[width * height];
    for (int x = 0; x < width; ++x)
      for (int z = 0; z < height; ++z)
        positions[(z * width) + x] = new Vec2i(x - (width / 2), z - (height / 2));
    return positions;
  }

  public PieceWithOffset getPiece(long seed, ChunkPos pos, Predicate<ChunkPos> biomeTester, Function<Piece, Vec2i> sizeFinder) {
    Vec2i pieceSize;

    pieceSize = sizeFinder.apply(Piece.X).InChunks();
    for (Vec2i offset : getOverlayTestCheckPos(pieceSize.x, pieceSize.z))
      if (getPieceAt(seed, new ChunkPos(pos.x + offset.x, pos.z + offset.z), biomeTester) == Piece.X)
        return new PieceWithOffset(Piece.X, (16 * offset.x) - (16 * (pieceSize.x / 2)), (16 * offset.z) - (16 * (pieceSize.z / 2)));

    pieceSize = sizeFinder.apply(Piece.N).InChunks();
    for (Vec2i offset : getOverlayTestCheckPos(pieceSize.x, pieceSize.z))
      if (getPieceAt(seed, new ChunkPos(pos.x + offset.x, pos.z + offset.z), biomeTester) == Piece.N)
        return new PieceWithOffset(Piece.N, (16 * offset.x) - (16 * (pieceSize.x / 2)), 16 * offset.z);

    pieceSize = sizeFinder.apply(Piece.S).InChunks();
    for (Vec2i offset : getOverlayTestCheckPos(pieceSize.x, pieceSize.z))
      if (getPieceAt(seed, new ChunkPos(pos.x + offset.x, pos.z + offset.z), biomeTester) == Piece.S)
        return new PieceWithOffset(Piece.S, (16 * offset.x) - (16 * (pieceSize.x / 2)), 16 * offset.z);

    pieceSize = sizeFinder.apply(Piece.E).InChunks();
    for (Vec2i offset : getOverlayTestCheckPos(pieceSize.x, pieceSize.z))
      if (getPieceAt(seed, new ChunkPos(pos.x + offset.x, pos.z + offset.z), biomeTester) == Piece.E)
        return new PieceWithOffset(Piece.E, 16 * offset.x, (16 * offset.z) - (16 * (pieceSize.z / 2)));

    pieceSize = sizeFinder.apply(Piece.W).InChunks();
    for (Vec2i offset : getOverlayTestCheckPos(pieceSize.x, pieceSize.z))
      if (getPieceAt(seed, new ChunkPos(pos.x + offset.x, pos.z + offset.z), biomeTester) == Piece.W)
        return new PieceWithOffset(Piece.W, 16 * offset.x, (16 * offset.z) - (16 * (pieceSize.z / 2)));

    Piece fallthrough = getPieceAt(seed, pos, biomeTester);
    if (fallthrough != null) {
      return new PieceWithOffset(fallthrough, 0, 0);
    }
    return null;
  }
}
