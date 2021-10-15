package com.github.kay9.lilicans.entity;

import com.github.kay9.lilicans.Lilicans;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class Lilican extends TamableAnimal
{
    private static final EntityDataAccessor<Boolean> DATA_PADDING = SynchedEntityData.defineId(Lilican.class, EntityDataSerializers.BOOLEAN);
    private static final String PADDING_TAG = "Padding";

    public Lilican(EntityType<? extends TamableAnimal> type, Level level)
    {
        super(type, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(PADDING_TAG, isPadding());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        setPadding(tag.getBoolean(PADDING_TAG));
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(DATA_PADDING, false);
    }

    public void setPadding(boolean padding)
    {
        entityData.set(DATA_PADDING, padding);
    }

    public boolean isPadding()
    {
        return entityData.get(DATA_PADDING);
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return isPadding();
    }

    @Nullable
    @Override
    public Lilican getBreedOffspring(ServerLevel level, AgeableMob parent)
    {
        Lilican child = Lilicans.LILICAN.get().create(level);
        UUID uuid = getOwnerUUID();
        if (uuid != null)
        {
            child.setOwnerUUID(uuid);
            child.setTame(true);
        }

        return child;
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.MAX_HEALTH, 6);
    }
}
