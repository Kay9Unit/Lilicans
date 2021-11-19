package com.github.kay9.lilicans.entity;

import com.github.kay9.lilicans.Lilicans;
import com.github.kay9.lilicans.Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import javax.annotation.Nullable;
import java.util.EnumSet;
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
        setPathfindingMalus(BlockPathTypes.WATER, 0);

        moveControl = new Mover();
    }

    @Override
    protected PathNavigation createNavigation(Level level)
    {
        return new WaterBoundPathNavigation(this, level)
        {
            @Override
            protected boolean canUpdatePath()
            {
                return true;
            }

            @Override
            protected PathFinder createPathFinder(int i)
            {
                nodeEvaluator = new AmphibiousNodeEvaluator(true);
                nodeEvaluator.setCanPassDoors(true);
                return new PathFinder(nodeEvaluator, i);
            }

            @Override
            public boolean isStableDestination(BlockPos at)
            {
                return !level.getBlockState(at.below()).isAir();
            }
        };
    }

    @Override
    protected void registerGoals()
    {
        goalSelector.addGoal(0, new WaterMovementTasks());
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new FollowOwnerGoal());
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
        InteractionResult result = super.mobInteract(pPlayer, pHand);
        if (result.consumesAction()) return result;
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!isTame() && stack.is(Items.OXEYE_DAISY))
        {
            tame(pPlayer);
            navigation.stop();
            setOrderedToSit(true);
            level.broadcastEntityEvent(this, (byte) 7);
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

    @Nullable
    @Override
    protected SoundEvent getAmbientSound()
    {
        return Sounds.LILICAN_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return Sounds.LILICAN_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound()
    {
        return Sounds.LILICAN_HURT.get();
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.315)
                .add(Attributes.MAX_HEALTH, 8);
    }

    class WaterMovementTasks extends RandomSwimmingGoal
    {
        public WaterMovementTasks()
        {
            super(Lilican.this, 0.5d, 10);
        }

        @Override
        public boolean canUse()
        {
            return isInWater() && super.canUse();
        }

        @Override
        public void tick()
        {
            if (!isPadding())
            {
                if (random.nextDouble() < 0.01) setPadding(true);
            }
            else if (random.nextDouble() < 0.01) setPadding(false);
            else super.tick(); // random movements
        }
    }

    public class FollowOwnerGoal extends Goal
    {
        private int timeToRecalcPath;
        private float oldWaterCost;

        public FollowOwnerGoal()
        {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse()
        {
            LivingEntity owner = getOwner();
            if (owner == null) return false;
            else if (owner.isSpectator()) return false;
            else if (isOrderedToSit()) return false;
            else return !(distanceToSqr(owner) < 80);
        }

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse()
        {
            if (navigation.isDone()) return false;
            else if (isOrderedToSit()) return false;
            LivingEntity owner = getOwner();
            if (owner == null) return false;
            return !(distanceToSqr(owner) <= 10);
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start()
        {
            timeToRecalcPath = 0;
            oldWaterCost = getPathfindingMalus(BlockPathTypes.WATER);
            setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop()
        {
            navigation.stop();
            setPathfindingMalus(BlockPathTypes.WATER, oldWaterCost);
        }

        /**
         * Keep ticking a continuous task that has already been started
         */
        public void tick()
        {
            LivingEntity owner = getOwner();
            getLookControl().setLookAt(owner, 10f, (float) getMaxHeadXRot());
            if (--timeToRecalcPath <= 0)
            {
                timeToRecalcPath = 10;
                if (!isLeashed() && !isPassenger())
                {
                    if (distanceToSqr(owner) >= 144) teleportToOwner(owner);
                    else navigation.moveTo(owner, 1f);
                }
            }
        }

        private void teleportToOwner(LivingEntity owner)
        {
            BlockPos blockpos = owner.blockPosition();

            for (int i = 0; i < 10; ++i)
            {
                int j = randomIntInclusive(-3, 3);
                int k = randomIntInclusive(-1, 1);
                int l = randomIntInclusive(-3, 3);
                boolean flag = maybeTeleportTo(owner, blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
                if (flag)
                {
                    return;
                }
            }

        }

        private boolean maybeTeleportTo(LivingEntity owner, int pX, int pY, int pZ)
        {
            if (Math.abs((double) pX - owner.getX()) < 2.0D && Math.abs((double) pZ - owner.getZ()) < 2.0D)
                return false;
            else if (!canTeleportTo(new BlockPos(pX, pY, pZ))) return false;
            else
            {
                moveTo((double) pX + 0.5D, pY, (double) pZ + 0.5D, getYRot(), getXRot());
                navigation.stop();
                return true;
            }
        }

        private boolean canTeleportTo(BlockPos pPos)
        {
            BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(level, pPos.mutable());
            if (blockpathtypes != BlockPathTypes.WALKABLE)
            {
                return false;
            }
            else
            {
                BlockPos blockpos = pPos.subtract(blockPosition());
                return level.noCollision(Lilican.this, getBoundingBox().move(blockpos));
            }
        }

        private int randomIntInclusive(int pMin, int pMax)
        {
            return getRandom().nextInt(pMax - pMin + 1) + pMin;
        }
    }

    class Mover extends MoveControl
    {
        public Mover()
        {
            super(Lilican.this);
        }

        public void tick()
        {
            if (isPadding() || getNavigation().isDone()) return;

            if (isInWater())
            {
                if (operation == Operation.MOVE_TO)
                {
                    double d0 = wantedX - getX();
                    double d1 = wantedY - getY();
                    double d2 = wantedZ - getZ();
                    double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                    if (d3 < (double) 2.5000003E-7f) setZza(0);
                    else
                    {
                        float f = (float) (Mth.atan2(d2, d0) * (double) (180f / (float) Math.PI)) - 90f;
                        setYRot(rotlerp(getYRot(), f, 90f));
                        yBodyRot = getYRot();
                        yHeadRot = getYRot();
                        float speed = (float) (speedModifier * getAttributeValue(Attributes.MOVEMENT_SPEED));
                        setSpeed(speed * 1.1f);
                        double d4 = Math.sqrt(d0 * d0 + d2 * d2);
                        if (Math.abs(d1) > (double) 1.0E-5F || Math.abs(d4) > (double) 1.0E-5F)
                        {
                            float f2 = -((float) (Mth.atan2(d1, d4) * (double) (180F / (float) Math.PI)));
                            f2 = Mth.clamp(Mth.wrapDegrees(f2), -90f, 90f);
                            setXRot(rotlerp(getXRot(), f2, 5.0F));
                        }

                        float f4 = Mth.cos(getXRot() * ((float) Math.PI / 180F));
                        float f3 = Mth.sin(getXRot() * ((float) Math.PI / 180F));
                        zza = f4 * speed;
                        yya = -f3 * speed;
                    }
                }
            }
            else super.tick();
        }
    }
}