package com.github.kay9.lilicans;

import com.github.kay9.lilicans.client.model.LilicanModel;
import com.github.kay9.lilicans.client.render.LilicanRenderer;
import com.github.kay9.lilicans.entity.Lilican;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiConsumer;

@Mod(Lilicans.MOD_ID)
public class Lilicans
{
    public static final String MOD_ID = "lilicans";
    public static final RegistryObject<EntityType<Lilican>> LILICAN = RegistryObject.of(id("lilican"), ForgeRegistries.ENTITIES);

    public Lilicans()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addGenericListener(EntityType.class, Lilicans::registerEntities);
        bus.addGenericListener(Item.class, Lilicans::registerItems);
        bus.addListener(Lilicans::registerEntityAttributes);

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            bus.addListener(Lilicans::registerRenderers);
            bus.addListener(Lilicans::registerModelDefinitions);
            bus.addListener(Lilicans::registerSpawning);
        }

        Sounds.REGISTRY.register(bus);
    }

    private static void registerEntities(RegistryEvent.Register<EntityType<?>> registry)
    {
        BiConsumer<ResourceLocation, EntityType.Builder<?>> reg = (s, e) -> registry.getRegistry().register(e.build(s.toString()).setRegistryName(s));

        reg.accept(LILICAN.getId(), EntityType.Builder.of(Lilican::new, MobCategory.CREATURE).sized(0.75f, 0.6875f).clientTrackingRange(10));
    }

    private static void registerItems(RegistryEvent.Register<Item> registry)
    {
        BiConsumer<String, Item> reg = (s, i) ->
        {
            s = MOD_ID + ":" + s;
            registry.getRegistry().register(i.setRegistryName(s));
        };

        reg.accept("lilican_spawn_egg", new ForgeSpawnEggItem(LILICAN, 0x62AA27, 0x1F441F, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    }

    private static void registerEntityAttributes(EntityAttributeCreationEvent event)
    {
        event.put(LILICAN.get(), Lilican.createAttributes().build());
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(LILICAN.get(), LilicanRenderer::new);
    }

    private static void registerSpawning(BiomeLoadingEvent event)
    {
        if (event.getCategory() == Biome.BiomeCategory.SWAMP)
        {
            event.getSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(LILICAN.get(), 10, 2, 4));
        }
    }

    private static void registerModelDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(LilicanModel.LAYER_LOCATION, LilicanModel::createBodyLayer);
    }

    public static ResourceLocation id(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }
}
