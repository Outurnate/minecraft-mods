package com.outurnate.createhalitosis.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import com.outurnate.createhalitosis.HalitosisMod;
import com.outurnate.createhalitosis.recipe.HalitosisRecipe;
import com.outurnate.createhalitosis.recipe.HalitosisRecipeTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.Components;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class HalitosisJEI implements IModPlugin {
  private class FanHalitosisCategory extends ProcessingViaFanCategory<HalitosisRecipe> {
    public FanHalitosisCategory(CreateRecipeCategory.Info<HalitosisRecipe> info) {
      super(info);
    }

    @Override
    protected AllGuiTextures getBlockShadow() {
      return AllGuiTextures.JEI_LIGHT;
    }

    @Override
    protected void renderAttachedBlock(GuiGraphics graphics) {
      GuiGameElement.of(HalitosisMod.DRAGON_MODEL)
        .scale(SCALE * 0.8)
        .rotate(0, 180, 0)
        .atLocal(1.1, -0.1, 2)
        .lighting(AnimatedKinetics.DEFAULT_LIGHTING)
        .render(graphics);
    }
  }

  private static final ResourceLocation ID = HalitosisMod.asResource("jei_plugin");
  private FanHalitosisCategory category;

  @Override
  public ResourceLocation getPluginUid() {
    return ID;
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    String recipeBaseKey = HalitosisMod.MODID + ".recipe.fan_halitosis";
    ClientPacketListener connection = Minecraft.getInstance().getConnection();
    if (connection != null) {
      Supplier<List<HalitosisRecipe>> recipesSupplier = () -> connection.getRecipeManager().getAllRecipesFor(HalitosisRecipeTypes.HALITOSIS.getType());
      IDrawable background = new EmptyBackground(178, 72);
      IDrawable icon = new DoubleItemIcon(() -> new ItemStack(AllItems.PROPELLER.get()), () -> new ItemStack(Items.DRAGON_HEAD));
      List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();
      catalysts.add(() -> AllBlocks.ENCASED_FAN.asStack().setHoverName(Components.translatable(recipeBaseKey + ".fan").withStyle(style -> style.withItalic(false))));
      
      var jeiRecipeType = new mezz.jei.api.recipe.RecipeType<HalitosisRecipe>(HalitosisRecipeTypes.HALITOSIS.getId(), HalitosisRecipe.class);
      CreateRecipeCategory.Info<HalitosisRecipe> info = new CreateRecipeCategory.Info<HalitosisRecipe>(jeiRecipeType, Components.translatable(recipeBaseKey), background, icon, recipesSupplier, catalysts);
      
      this.category = new FanHalitosisCategory(info);
      registration.addRecipeCategories(category);
    }
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    this.category.registerRecipes(registration);
  }

  @Override
  public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    this.category.registerCatalysts(registration);
  }
}
