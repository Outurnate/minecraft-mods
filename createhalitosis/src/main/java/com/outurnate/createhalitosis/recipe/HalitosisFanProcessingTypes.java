package com.outurnate.createhalitosis.recipe;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.util.Color;
import com.outurnate.createhalitosis.CatalystUtils;
import com.outurnate.createhalitosis.HalitosisMod;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.simibubi.create.foundation.recipe.RecipeApplier;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class HalitosisFanProcessingTypes extends AllFanProcessingTypes {
  public static final HalitosisType HALITOSIS = register("halitosis", new HalitosisType());

  private static <T extends FanProcessingType> T register(String id, T type) {
    FanProcessingTypeRegistry.register(HalitosisMod.asResource(id), type);
    return type;
  }

  public static void register() {
    // I have no idea why the hell this is here
    // but if i remove it
    // the recipe doesn't register
    // I THINK this class is lazy loaded
    // and this method call triggers it?
    // i dunno i blame java
  }

  public static class HalitosisType implements FanProcessingType {
    private static final RecipeWrapper RECIPE_WRAPPER = new HalitosisRecipe.Wrapper();

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
      BlockState block = level.getBlockState(pos);
      if (block.is(HalitosisMod.FAN_PROCESSING_CATALYSTS_HALITOSIS)) {
        if (block.is(Blocks.DRAGON_WALL_HEAD) || block.is(Blocks.DRAGON_HEAD)) {
          Optional<Direction> facing = CatalystUtils.GetDirection(block);
          return facing.isPresent() && level.getBlockState(pos.relative(facing.get().getOpposite())).is(AllBlocks.ENCASED_FAN.get());
        }
        return true;
      }
      return false;
    }

    @Override
    public int getPriority() {
      return 500;
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
      RECIPE_WRAPPER.setItem(0, stack);
      return HalitosisRecipeTypes.HALITOSIS.find(RECIPE_WRAPPER, level).isPresent();
    }

    @Override
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
      RECIPE_WRAPPER.setItem(0, stack);
      Optional<Recipe<RecipeWrapper>> recipe = HalitosisRecipeTypes.HALITOSIS.find(RECIPE_WRAPPER, level);
      if (recipe.isPresent())
        return RecipeApplier.applyRecipeOn(level, stack, recipe.get());
      return null;
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
      if (level.random.nextInt(4) != 0)
        return;
      pos = pos.add(VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1).multiply(1, 0.05f, 1).normalize().scale(0.15f));
      level.addParticle(new DustParticleOptions(new Color(0xB180E5).asVectorF(), 1), pos.x + (level.random.nextFloat() - .5f) * .5f, pos.y + .5f, pos.z + (level.random.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
      particleAccess.setColor(Color.mixColors(0xB180E5, 0x7353BA, random.nextFloat()));
      particleAccess.setAlpha(.5f);
      if (random.nextFloat() < 1 / 16f)
        particleAccess.spawnExtraParticle(ParticleTypes.WITCH, .5f);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
      if (level.isClientSide)
        return;

      entity.hurt(level.damageSources().dragonBreath(), 4.0f);
    }
  }
}
