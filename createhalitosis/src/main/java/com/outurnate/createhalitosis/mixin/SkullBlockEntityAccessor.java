package com.outurnate.createhalitosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.entity.SkullBlockEntity;

@Mixin(SkullBlockEntity.class)
public interface SkullBlockEntityAccessor {
  @Accessor("isAnimating")
  public void setIsAnimating(boolean isAnimating);

  @Accessor("animationTickCount")
  public void setAnimationTickCount(int animationTickCount);

  @Accessor("animationTickCount")
  public int getAnimationTickCount();
}
