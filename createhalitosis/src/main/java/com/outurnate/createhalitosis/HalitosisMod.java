package com.outurnate.createhalitosis;

import com.jozufozu.flywheel.core.PartialModel;
import com.outurnate.createhalitosis.datagen.DataGen;
import com.outurnate.createhalitosis.recipe.HalitosisFanProcessingTypes;
import com.outurnate.createhalitosis.recipe.HalitosisRecipeTypes;
import com.simibubi.create.foundation.data.CreateRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HalitosisMod.MODID)
public class HalitosisMod
{
  public static final String MODID = "createhalitosis";
  public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);
  public static PartialModel DRAGON_MODEL;
  public static final TagKey<Block> FAN_PROCESSING_CATALYSTS_HALITOSIS = BlockTags.create(new ResourceLocation(MODID, "fan_processing_catalysts/halitosis"));

  public HalitosisMod()
  {
    IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

    HalitosisRecipeTypes.register(modEventBus);
    HalitosisFanProcessingTypes.register();

    REGISTRATE.registerEventListeners(modEventBus);
    modEventBus.addListener(EventPriority.LOWEST, DataGen::gatherData);

    DRAGON_MODEL = new PartialModel(new ResourceLocation(HalitosisMod.MODID, "dragon_head_export"));
  }

  public static ResourceLocation asResource(String id) {
    return new ResourceLocation(MODID, id);
  }
}
