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
package dev.technici4n.moderndynamics.attachment;

import dev.technici4n.moderndynamics.attachment.attached.AttachedAttachment;
import dev.technici4n.moderndynamics.attachment.attached.FluidAttachedIo;
import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.network.NodeHost;
import dev.technici4n.moderndynamics.network.item.ItemHost;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class IoAttachmentItem extends AttachmentItem {
    private final IoAttachmentType type;

    public IoAttachmentItem(RenderedAttachment attachment, IoAttachmentType type) {
        super(attachment);
        this.type = type;
    }

    public IoAttachmentType getType() {
        return type;
    }

    @Override
    public AttachedAttachment createAttached(NodeHost host, CompoundTag configTag) {
        if (host instanceof ItemHost) {
            return new ItemAttachedIo(this, configTag, host.getPipe()::setChanged);
        } else {
            return new FluidAttachedIo(this, configTag, host.getPipe()::setChanged);
        }
    }

    public Set<Setting> getSupportedSettings() {
        var result = EnumSet.noneOf(Setting.class);
        result.add(Setting.FILTER_INVERSION);
        result.add(Setting.FILTER_DAMAGE);
        result.add(Setting.FILTER_NBT);
        result.add(Setting.FILTER_MOD);
        result.add(Setting.FILTER_SIMILAR);
        switch (type) {
        case FILTER -> {
            // TODO implement
            // result.add(Setting.OVERSENDING_MODE);
            // result.add(Setting.MAX_ITEMS_IN_INVENTORY);
        }
        case EXTRACTOR -> {
            result.add(Setting.MAX_ITEMS_EXTRACTED);
            result.add(Setting.ROUTING_MODE);
        }
        case ATTRACTOR -> {
            result.add(Setting.MAX_ITEMS_EXTRACTED);
            // TODO implement
            // result.add(Setting.MAX_ITEMS_IN_INVENTORY);
            result.add(Setting.ROUTING_MODE);
        }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents
                .add(Component.translatable("gui.moderndynamics.tooltip.attachment_upgrades").setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        /*
         * if (level != null && level.isClientSide()) {
         * if (MdProxy.INSTANCE.isShiftDown()) {
         * MutableComponent filters = null;
         * for (var setting : getSupportedSettings()) {
         * if (setting.isFilter()) {
         * if (filters == null) {
         * filters = setting.getTooltipName().copy();
         * } else {
         * filters.append(", ").append(setting.getTooltipName());
         * }
         * }
         * }
         * if (filters != null) {
         * filters.withStyle(ChatFormatting.WHITE);
         * tooltipComponents.add(Component.translatable("gui.moderndynamics.tooltip.filters", filters).withStyle(ChatFormatting.GRAY));
         * }
         * } else {
         * var keyText = Component.translatable("gui.moderndynamics.tooltip.more_info_key").withStyle(ChatFormatting.YELLOW,
         * ChatFormatting.ITALIC);
         * tooltipComponents.add(Component.translatable("gui.moderndynamics.tooltip.more_info", keyText).withStyle(ChatFormatting.GRAY));
         * }
         * }
         */
    }
}
