package com.outurnate.createhalitosis.recipe;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeFactory;
import com.outurnate.createhalitosis.HalitosisMod;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public enum HalitosisRecipeTypes implements IRecipeTypeInfo {
  HALITOSIS(HalitosisRecipe::new);

  private static class Registers {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, HalitosisMod.MODID);
    private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, HalitosisMod.MODID);
  }

  private final ResourceLocation id;
  private final RegistryObject<RecipeSerializer<?>> serializerObject;
  private final Supplier<RecipeType<?>> type;

  HalitosisRecipeTypes(ProcessingRecipeFactory<?> processingFactory) {
    String name = Lang.asId(name());
    id = HalitosisMod.asResource(name);
    serializerObject = Registers.SERIALIZER_REGISTER.register(name, () -> new ProcessingRecipeSerializer<>(processingFactory));
    @Nullable RegistryObject<RecipeType<?>> typeObject = Registers.TYPE_REGISTER.register(name, () -> RecipeType.simple(id));
    type = typeObject;
  }
  
  public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level world) {
    return world.getRecipeManager().getRecipeFor(getType(), inv, world);
  }

  @Override
  public ResourceLocation getId() {
    return id;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends RecipeSerializer<?>> T getSerializer() {
    return (T)serializerObject.get();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends RecipeType<?>> T getType() {
    return (T)type.get();
  }

  public static void register(IEventBus modEventBus) {
    ShapedRecipe.setCraftingSize(9, 9);
    Registers.SERIALIZER_REGISTER.register(modEventBus);
    Registers.TYPE_REGISTER.register(modEventBus);
  }
}
