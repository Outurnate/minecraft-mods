package com.outurnate.railbridges;

import javax.annotation.Nonnull;

import java.util.Optional;

import java.lang.IllegalStateException;

import com.outurnate.railbridges.TrackBridgeMap.Piece;
import com.outurnate.railbridges.TrackBridgeMap.PieceWithOffset;
import com.outurnate.railbridges.TrackBridgeMap.Vec2i;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class TrackBridgeFeature extends Feature<TrackBridgeFeatureConfiguration> {
  public TrackBridgeFeature() {
    super(TrackBridgeFeatureConfiguration.CODEC);
  }

  @Override
  public boolean place(@Nonnull FeaturePlaceContext<TrackBridgeFeatureConfiguration> context) {
    WorldGenLevel level = context.level();
    if (level instanceof WorldGenRegion) {
      WorldGenRegion region = (WorldGenRegion) level;
      MinecraftServer server = region.getServer();
      if (server != null) {
        TrackBridgeMap map = new TrackBridgeMap(context.config().chunkSeparation(), context.config().damageChance(), context.config().infiniteChance());
        return placeTrackPiece(server, region, map, context.config().yLevelBase(), context.config().yLevelLower(), context.config().numEndVariants(), context.config().allowedBiomesTag());
      }
    }
    return false;
  }

  private StructureTemplate getPieceStructure(MinecraftServer server, Piece piece, boolean isSupport, RandomSource random, int maxVariant) {
    StructureTemplateManager manager = server.getStructureManager();
    if (!isSupport)
      return manager.get(new ResourceLocation(RailBridgesMod.MODID, switch (piece) {
        case EW -> "track_ew";
        case NS -> "track_ns";
        case N -> random == null ? "track_end_n0" : String.format("track_end_n%s", random.nextInt(0, maxVariant));
        case S -> random == null ? "track_end_s0" : String.format("track_end_s%s", random.nextInt(0, maxVariant));
        case E -> random == null ? "track_end_e0" : String.format("track_end_e%s", random.nextInt(0, maxVariant));
        case W -> random == null ? "track_end_w0" : String.format("track_end_w%s", random.nextInt(0, maxVariant));
        default -> "track_x";
      })).get();
    else
    {
      Optional<StructureTemplate> specific = manager.get(new ResourceLocation(RailBridgesMod.MODID, switch (piece) {
        case EW -> "track_support_ew";
        case NS -> "track_support_ns";
        case N -> "track_support_n";
        case S -> "track_support_s";
        case E -> "track_support_e";
        case W -> "track_support_w";
        default -> "track_support_x";
      }));
      if (specific.isPresent())
        return specific.get();
      return manager.get(new ResourceLocation(RailBridgesMod.MODID, switch (piece) {
        case EW -> "track_support_ew";
        case NS -> "track_support_ns";
        case N -> "track_support_ns";
        case S -> "track_support_ns";
        case E -> "track_support_ew";
        case W -> "track_support_ew";
        default -> "track_support_x";
      })).get();
    }
  }

  private StructureTemplate getPieceStructure(MinecraftServer server, Piece piece, boolean isSupport) {
    return getPieceStructure(server, piece, isSupport, null, 0);
  }

  private Vec2i getPieceSize(MinecraftServer server, Piece piece) {
    Vec3i size = getPieceStructure(server, piece, false).getSize();
    Vec2i size2d = new Vec2i(size.getX(), size.getZ());
    if (size2d.x() % 16 != 0 || size2d.z() % 16 != 0)
      throw new IllegalStateException(String.format("Piece %s must have X/Z sizes aligned to chunk boundaries (given %n, %n)", piece, size2d.x(), size2d.z()));
    return size2d;
  }

  private void place(WorldGenRegion region, BlockPos placement, StructureTemplate template, StructurePlaceSettings settings) {
    template.placeInWorld(region, placement, placement, settings, region.getRandom(), 0);
  }

  private boolean placeTrackPiece(MinecraftServer server, WorldGenRegion region, TrackBridgeMap map, int yLevelBase, int yLevelLower, int numEndVariants, TagKey<Biome> biomeTag) {
    ChunkPos center = region.getCenter();
    RandomSource random = region.getRandom();
    PieceWithOffset piece = map.getPiece(region.getSeed(), center, pos -> region.getBiomeManager().getBiome(pos.getMiddleBlockPosition(yLevelBase)).is(biomeTag), x -> getPieceSize(server, x));
    if (piece != null) {
      StructurePlaceSettings settings = new StructurePlaceSettings();
      settings.setBoundingBox(new BoundingBox(region.getCenter().getMinBlockX(), -2048, region.getCenter().getMinBlockZ(), region.getCenter().getMaxBlockX(), 2048, region.getCenter().getMaxBlockZ()));

      StructureTemplate pieceTemplate = getPieceStructure(server, piece.piece(), false, random, numEndVariants);
      BlockPos placement = center.getWorldPosition().atY(yLevelBase).offset(piece.offsetX(), 0, piece.offsetZ());
      place(region, placement, pieceTemplate, settings);

      StructureTemplate supportTemplate = getPieceStructure(server, piece.piece(), true);

      for (int i = yLevelLower; i < yLevelBase; i += supportTemplate.getSize().getY()) {
        placement = placement.atY(i);
        place(region, placement, supportTemplate, settings);
      }

      return true;
    }
    return false;
  }
}
