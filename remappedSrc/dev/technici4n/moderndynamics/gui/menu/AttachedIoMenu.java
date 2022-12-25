/*
 * Modern Dynamics
 * Copyright (C) 2021 shartte & Technici4n
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package dev.technici4n.moderndynamics.gui.menu;

import dev.technici4n.moderndynamics.attachment.Setting;
import dev.technici4n.moderndynamics.attachment.attached.AttachedIo;
import dev.technici4n.moderndynamics.attachment.settings.FilterInversionMode;
import dev.technici4n.moderndynamics.attachment.settings.RedstoneMode;
import dev.technici4n.moderndynamics.gui.MdPackets;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import java.util.function.BiConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

// TODO: need to sync item and fluid filter changes done by other players
public class AttachedIoMenu<A extends AttachedIo> extends ScreenHandler {
    public final PipeBlockEntity pipe;
    public final Direction side;
    public final A attachment;
    protected final PlayerEntity player;

    public AttachedIoMenu(ScreenHandlerType<?> menuType, int syncId, PlayerInventory playerInventory, PipeBlockEntity pipe, Direction side, A attachment) {
        super(menuType, syncId);
        this.pipe = pipe;
        this.side = side;
        this.attachment = attachment;
        this.player = playerInventory.player;

        // Player inventory slots
        int i;
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 123 + i * 18));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 181));
        }

        // Upgrade slots
        for (int k = 0; k < AttachedIo.UPGRADE_SLOTS; ++k) {
            this.addSlot(new UpgradeSlot(attachment, k, UpgradePanel.FIRST_SLOT_LEFT, UpgradePanel.FIRST_SLOT_TOP + k * 18));
        }

        syncEnum(FilterInversionMode.class, this::getFilterMode, this::setFilterMode);
        syncEnum(RedstoneMode.class, this::getRedstoneMode, this::setRedstoneMode);
    }

    protected <T extends Enum<T>> void syncEnum(Class<T> enumClass, Supplier<T> getter, BiConsumer<T, Boolean> setter) {
        addProperty(new Property() {
            @Override
            public int get() {
                return getter.get().ordinal();
            }

            @Override
            public void set(int value) {
                setter.accept(enumClass.getEnumConstants()[value], false);
            }
        });
    }

    protected void syncShort(IntSupplier getter, BiConsumer<Integer, Boolean> setter) {
        addProperty(new Property() {
            @Override
            public int get() {
                return getter.getAsInt();
            }

            @Override
            public void set(int value) {
                setter.accept(value, false);
            }
        });
    }

    protected boolean trySetFilterOnShiftClick(int clickedSlot) {
        return false;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = slots.get(index);
        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (index < 36) {
            // Click inside player inventory. Try to move to upgrades.
            // Don't try to use moveItemStackTo, it's missing some checks.
            for (var otherSlot : slots) {
                if (otherSlot instanceof UpgradeSlot && otherSlot.canInsert(stack)) {
                    if (ItemStack.canCombine(otherSlot.getStack(), stack)) {
                        int inserted = Math.min(stack.getCount(), otherSlot.getMaxItemCount() - otherSlot.getStack().getCount());
                        if (inserted > 0) {
                            stack.decrement(inserted);
                            otherSlot.getStack().increment(inserted);
                            otherSlot.markDirty();
                            return ItemStack.EMPTY;
                        }
                    } else if (otherSlot.getStack().isEmpty()) {
                        int inserted = Math.min(stack.getCount(), otherSlot.getMaxItemCount(stack));
                        if (inserted > 0) {
                            otherSlot.setStack(stack.split(inserted));
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            // Try to set filter
            if (trySetFilterOnShiftClick(index)) {
                return ItemStack.EMPTY;
            }

            if (index < 27) { // Main inventory to hotbar
                if (insertItem(stack, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else { // Hotbar to main inventory
                if (insertItem(stack, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (slot instanceof UpgradeSlot) {
            // Move to player inventory
            if (insertItem(stack, 0, 36, true)) {
                // Send change notification because vanilla doesn't send it.
                slot.markDirty();
                return ItemStack.EMPTY;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        if (isClientSide())
            return true;

        var pos = pipe.getPos();
        if (player.getWorld().getBlockEntity(pos) != pipe) {
            return false;
        }
        if (player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64.0D) {
            return false;
        }
        return pipe.getAttachment(side) == attachment;
    }

    public FilterInversionMode getFilterMode() {
        return attachment.getFilterInversion();
    }

    public void setFilterMode(FilterInversionMode value, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetFilterMode(syncId, value);
        }
        attachment.setFilterInversion(value);
        // TODO: clear all these pipe.setChanged() calls and use the setChangedCallback in the attachment instead.
        pipe.markDirty();
    }

    public RedstoneMode getRedstoneMode() {
        return attachment.getRedstoneMode();
    }

    public void setRedstoneMode(RedstoneMode redstoneMode, boolean sendPacket) {
        if (isClientSide() && sendPacket) {
            MdPackets.sendSetRedstoneMode(syncId, redstoneMode);
        }
        attachment.setRedstoneMode(redstoneMode);
        pipe.markDirty();
    }

    public boolean isSettingSupported(Setting setting) {
        return attachment.getSupportedSettings().contains(setting);
    }

    public boolean isClientSide() {
        return player.getEntityWorld().isClient();
    }

    /**
     * Convenience method to get the player owning this menu.
     */
    public PlayerEntity getPlayer() {
        return player;
    }

    public boolean isEnabledViaRedstone() {
        return attachment.isEnabledViaRedstone(pipe);
    }

    public boolean isAdvancedBehaviorAllowed() {
        return attachment.isAdvancedBehaviorAllowed();
    }
}
