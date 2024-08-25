package com.outurnate.createhalitosis.datagen;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import com.outurnate.createhalitosis.HalitosisMod;
import com.simibubi.create.AllTags.AllItemTags;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class DataGen {
  public static void gatherData(GatherDataEvent event) {
    HalitosisMod.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, DataGen::initBlockTags);
    HalitosisMod.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, DataGen::initItemTags);

    DataGenerator dataGenerator = event.getGenerator();
    PackOutput output = dataGenerator.getPackOutput();
    if (event.includeServer()) {
      dataGenerator.addProvider(true, new DataProvider() {
        @Override
        public String getName() {
          return "Create Halitosis' Processing Recipes";
        }

        @Override
        public CompletableFuture<?> run(@Nonnull CachedOutput dc) {
          return CompletableFuture.supplyAsync(() -> new HalitosisRecipeGen(output).run(dc));
        }
      });
    }
  }

  private static void initBlockTags(RegistrateTagsProvider<Block> provider) {
    provider.addTag(HalitosisMod.FAN_PROCESSING_CATALYSTS_HALITOSIS)
      .add(ForgeRegistries.BLOCKS.getResourceKey(Blocks.DRAGON_HEAD).get())
      .add(ForgeRegistries.BLOCKS.getResourceKey(Blocks.DRAGON_WALL_HEAD).get());
  }

  private static void initItemTags(RegistrateTagsProvider<Item> provider) {
    provider.addTag(AllItemTags.UPRIGHT_ON_BELT.tag)
      .add(ForgeRegistries.ITEMS.getResourceKey(Items.DRAGON_BREATH).get());
  }
}
