package com.outurnate.createhalitosis.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.outurnate.createhalitosis.CatalystUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(SkullBlockEntity.class)
public abstract class SkullBlockEntityMixin extends BlockEntity {
  public SkullBlockEntityMixin(BlockPos p_155731_, BlockState p_155732_) {
    super(BlockEntityType.SKULL, p_155731_, p_155732_);
  }

  @Inject(method = "animation", at = @At("HEAD"), cancellable = true)
  private static void onAnimation(Level level, BlockPos pos, BlockState block, SkullBlockEntity entity, CallbackInfo info) {
    if (block.is(Blocks.DRAGON_WALL_HEAD) || block.is(Blocks.DRAGON_HEAD)) {
      Optional<Direction> facing = CatalystUtils.GetDirection(block);
      if (facing.isPresent() && CatalystUtils.TestFan(level, pos, facing.get())) {
        SkullBlockEntityAccessor animationAccessor = (SkullBlockEntityAccessor)entity;
        animationAccessor.setIsAnimating(true);
        animationAccessor.setAnimationTickCount(animationAccessor.getAnimationTickCount() + 1);
        info.cancel();
      }
    }
  }
}
