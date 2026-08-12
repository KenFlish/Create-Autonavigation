package ru.codex.kenmod.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import ru.codex.kenmod.KenMod;
import ru.codex.kenmod.block.entity.NavigationWorkbenchBlockEntity;

public class NavigationWorkbenchMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;

    public NavigationWorkbenchMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, new SimpleContainerData(3), buffer.readBlockPos(), ContainerLevelAccess.NULL);
    }

    public NavigationWorkbenchMenu(int containerId, Inventory inventory, NavigationWorkbenchBlockEntity blockEntity, ContainerData data) {
        this(containerId, data, blockEntity.getBlockPos(),
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private NavigationWorkbenchMenu(int containerId, ContainerData data, BlockPos blockPos, ContainerLevelAccess access) {
        super(KenMod.NAVIGATION_WORKBENCH_MENU.get(), containerId);
        checkContainerDataCount(data, 3);
        this.data = data;
        this.blockPos = blockPos;
        this.access = access;
        addDataSlots(data);
    }

    public int getTargetX() {
        return data.get(0);
    }

    public int getTargetY() {
        return data.get(1);
    }

    public int getTargetZ() {
        return data.get(2);
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, KenMod.NAVIGATION_WORKBENCH.get());
    }
}
