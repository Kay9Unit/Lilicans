package com.github.kay9.lilicans;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Sounds
{
    protected static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Lilicans.MOD_ID);

    public static final RegistryObject<SoundEvent> LILICAN_AMBIENT = register("entity.lilican.ambient");
    public static final RegistryObject<SoundEvent> LILICAN_HURT = register("entity.lilican.hurt");

    private static RegistryObject<SoundEvent> register(String name)
    {
        return REGISTRY.register(name, () -> new SoundEvent(Lilicans.id(name)));
    }
}
