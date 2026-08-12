package ru.codex.kenmod.block.entity;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import ru.codex.kenmod.KenMod;
import ru.codex.kenmod.block.NavigationWorkbenchBlock;
import ru.codex.kenmod.menu.NavigationWorkbenchMenu;

public class NavigationWorkbenchBlockEntity extends BlockEntity implements MenuProvider {
    private final Map<Direction, Integer> signalStrengthCache = new EnumMap<>(Direction.class);
    private int targetX;
    private int targetY;
    private int targetZ;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> targetX;
                case 1 -> targetY;
                case 2 -> targetZ;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> targetX = value;
                case 1 -> targetY = value;
                case 2 -> targetZ = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public NavigationWorkbenchBlockEntity(BlockPos pos, BlockState blockState) {
        super(KenMod.NAVIGATION_WORKBENCH_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.create_autonavigation.navigation_workbench");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new NavigationWorkbenchMenu(containerId, inventory, this, containerData);
    }

    public BlockPos getTargetPos() {
        return new BlockPos(targetX, targetY, targetZ);
    }

    public void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        boolean changed = false;
        for (Direction direction : Direction.values()) {
            int oldStrength = signalStrengthCache.getOrDefault(direction, 0);
            int newStrength = getRedstoneStrength(direction);
            if (oldStrength != newStrength) {
                signalStrengthCache.put(direction, newStrength);
                changed = true;
            }
        }

        if (changed) {
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(worldPosition.relative(direction), getBlockState().getBlock());
            }
        }
    }

    public void setTarget(int x, int y, int z) {
        targetX = x;
        targetY = y;
        targetZ = z;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public int getRedstoneStrength(Direction side) {
        if (level == null) {
            return 0;
        }

        Direction facing = getBlockState().getValue(NavigationWorkbenchBlock.FACING);
        if (side.getAxis() == facing.getAxis()) {
            return 0;
        }

        Vec3 target = getTargetPosition();
        if (target == null) {
            return 0;
        }

        Vec3 projectedSelf = getProjectedSelfPos();
        Vec3 delta = target.subtract(projectedSelf);
        Vec3 planeProjected = getPlaneProjectedPos(rotate(rotate(delta, getSublevelRot()), facing.getRotation()), facing.getNormal());
        double length = planeProjected.length();
        if (length < 1.9999D) {
            return 0;
        }

        double normalized = -planeProjected.dot(Vec3.atLowerCornerOf(side.getNormal())) / length;
        return (int) (Math.asin(normalized) / Math.PI * 30.0D + 0.5D);
    }

    public float getPointerAngleDegrees() {
        if (level == null) {
            return 0.0F;
        }

        Vec3 target = getTargetPosition();
        if (target == null) {
            return 0.0F;
        }

        Vec3 self = getProjectedSelfPos();
        Vec3 direction = target.subtract(self).normalize();
        direction = rotate(direction, getSublevelRot());
        direction = rotate(direction, getBlockState().getValue(NavigationWorkbenchBlock.FACING).getRotation());
        direction = new Vec3(direction.x, 0.0D, direction.z);

        return (float) ((360.0D + Math.toDegrees(Math.atan2(direction.z, direction.x))) % 360.0D);
    }

    public Vec3 getProjectedSelfPos() {
        Vec3 position = Vec3.atCenterOf(worldPosition);
        Object subLevel = getContainingSubLevel();
        if (subLevel == null) {
            return position;
        }

        try {
            Object pose = subLevel.getClass().getMethod("logicalPose").invoke(subLevel);
            return (Vec3) pose.getClass().getMethod("transformPosition", Vec3.class).invoke(pose, position);
        } catch (ReflectiveOperationException ignored) {
            return position;
        }
    }

    private Vec3 getTargetPosition() {
        return Vec3.atCenterOf(getTargetPos());
    }

    private Quaterniond getSublevelRot() {
        Object subLevel = getContainingSubLevel();
        if (subLevel == null) {
            return new Quaterniond();
        }

        try {
            Object pose = subLevel.getClass().getMethod("logicalPose").invoke(subLevel);
            Object orientation = pose.getClass().getMethod("orientation").invoke(pose);
            if (orientation instanceof Quaterniond quaternion) {
                return new Quaterniond(quaternion);
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return new Quaterniond();
    }

    private Object getContainingSubLevel() {
        if (level == null) {
            return null;
        }

        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            Object helper = sableClass.getField("HELPER").get(null);
            Method getContaining = helper.getClass().getMethod("getContaining", BlockEntity.class);
            return getContaining.invoke(helper, this);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Vec3 getPlaneProjectedPos(Vec3 vec, net.minecraft.core.Vec3i normal) {
        double dot = vec.dot(Vec3.atLowerCornerOf(normal));
        return vec.subtract(Vec3.atLowerCornerOf(normal).scale(dot));
    }

    private static Vec3 rotate(Vec3 vec, Quaterniond quaternion) {
        Quaterniond input = new Quaterniond((float) vec.x, (float) vec.y, (float) vec.z, 0.0D);
        Quaterniond rotation = new Quaterniond(quaternion);
        input.mul(rotation);
        rotation.conjugate();
        rotation.mul(input);
        return new Vec3(rotation.x(), rotation.y(), rotation.z());
    }

    private static Vec3 rotate(Vec3 vec, Quaternionf quaternion) {
        Quaternionf input = new Quaternionf((float) vec.x, (float) vec.y, (float) vec.z, 0.0F);
        Quaternionf rotation = new Quaternionf(quaternion);
        input.mul(rotation);
        rotation.conjugate();
        rotation.mul(input);
        return new Vec3(rotation.x(), rotation.y(), rotation.z());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("TargetX", targetX);
        tag.putInt("TargetY", targetY);
        tag.putInt("TargetZ", targetZ);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        targetX = tag.getInt("TargetX");
        targetY = tag.getInt("TargetY");
        targetZ = tag.getInt("TargetZ");
    }
}
