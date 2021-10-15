package com.github.kay9.lilicans.entity;

import com.github.kay9.lilicans.Lilicans;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class Lilican extends TamableAnimal
{
    private static final EntityDataAccessor<Boolean> DATA_PADDING = SynchedEntityData.defineId(Lilican.class, EntityDataSerializers.BOOLEAN);
    private static final String PADDING_TAG = "Padding";

    private TemptGoal temptGoal;
    private AvoidEntityGoal<?> avoidEntityGoal;

    public Lilican(EntityType<? extends TamableAnimal> type, Level level)
    {
        super(type, level);
    }

    @Override
    protected void registerGoals()
    {
//        goalSelector.addGoal(0, ); padding goal
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new FollowOwnerGoal(this, 1, 10f, 5f, false));
        goalSelector.addGoal(5, new BreedGoal(this, 0.8));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 13f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    protected void reassessTameGoals()
    {
        if (temptGoal == null) temptGoal = new TemptGoal(this, 0.6, Ingredient.of(Items.OXEYE_DAISY), true);
        if (avoidEntityGoal == null) avoidEntityGoal = new AvoidEntityGoal<>(this, Player.class, 16f, 0.8, 1.75);

        if (isTame())
        {
            goalSelector.removeGoal(temptGoal);
            goalSelector.removeGoal(avoidEntityGoal);
        }
        else
        {
            goalSelector.addGoal(3, temptGoal);
            goalSelector.addGoal(4, avoidEntityGoal);
        }
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

    @Override
    public boolean canBreatheUnderwater()
    {
        return true;
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize)
    {
        return pSize.height * 0.61f;
    }

    @Override
    protected void dropEquipment()
    {
        super.dropEquipment();
        if (isTame()) spawnAtLocation(Items.OXEYE_DAISY);
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

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand)
    {
        refreshDimensions();

        InteractionResult result = super.mobInteract(pPlayer, pHand);
        if (result.consumesAction()) return result;
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!isTame() && stack.is(Items.OXEYE_DAISY))
        {
            tame(pPlayer);
            navigation.stop();
            setOrderedToSit(true);
            level.broadcastEntityEvent(this, (byte)7);
            usePlayerItem(pPlayer, pHand, stack);
            playSound(SoundEvents.BONE_MEAL_USE, 0.75f, 1f);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (isOwnedBy(pPlayer))
        {
            setOrderedToSit(!isOrderedToSit());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.315)
                .add(Attributes.MAX_HEALTH, 8);
    }
}
