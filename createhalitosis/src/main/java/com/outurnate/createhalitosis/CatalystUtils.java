package com.outurnate.createhalitosis;

import java.util.Optional;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CatalystUtils {
  public static boolean TestFan(Level level, BlockPos pos, Direction facing) {
    BlockEntity fanEntity = level.getBlockEntity(pos.relative(facing.getOpposite()));
    if (fanEntity instanceof IAirCurrentSource) {
      AirCurrent airCurrentSource = ((IAirCurrentSource)fanEntity).getAirCurrent();
      if (airCurrentSource != null) {
        return airCurrentSource.direction == facing && (int)airCurrentSource.maxDistance != 0;
      }
    }
    return false;
  }

  public static Optional<Direction> GetDirection(BlockState blockState) {
    Optional<Direction> facingFromBlockState = blockState.getOptionalValue(WallSkullBlock.FACING)
      .filter((facing) -> facing == Direction.NORTH || facing == Direction.EAST || facing == Direction.SOUTH || facing == Direction.WEST);
    if (facingFromBlockState.isPresent())
      return facingFromBlockState;
    else
      return blockState.getOptionalValue(SkullBlock.ROTATION)
        .flatMap((rotation) -> switch (rotation) {
          case 0 -> Optional.of(Direction.NORTH);
          case 4 -> Optional.of(Direction.EAST);
          case 8 -> Optional.of(Direction.SOUTH);
          case 12 -> Optional.of(Direction.WEST);
          default -> Optional.empty();
        });
  }
}
