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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class IoAttachmentItem extends AttachmentItem {
    private final AttachmentTier tier;
    private final IoAttachmentType type;

    public IoAttachmentItem(RenderedAttachment attachment, AttachmentTier tier, IoAttachmentType type) {
        super(attachment, tier.rarity);
        this.tier = tier;
        this.type = type;
    }

    public AttachmentTier getTier() {
        return tier;
    }

    public IoAttachmentType getType() {
        return type;
    }

    @Override
    public AttachedAttachment createAttached(NodeHost host, CompoundTag configTag) {
        if (host instanceof ItemHost) {
            return new ItemAttachedIo(this, configTag);
        } else {
            return new FluidAttachedIo(this, configTag);
        }
    }

    public Set<Setting> getSupportedSettings() {
        var result = EnumSet.noneOf(Setting.class);
        result.add(Setting.FILTER_INVERSION);
        if (tier.compareTo(AttachmentTier.GOLD) >= 0) {
            result.add(Setting.FILTER_DAMAGE);
            result.add(Setting.FILTER_NBT);
            result.add(Setting.FILTER_MOD);
            result.add(Setting.FILTER_SIMILAR);
        }
        switch (type) {
        case FILTER -> {
            result.add(Setting.OVERSENDING_MODE);
            if (tier.compareTo(AttachmentTier.GOLD) >= 0) {
                result.add(Setting.MAX_ITEMS_IN_INVENTORY);
            }
        }
        case SERVO -> {
            result.add(Setting.MAX_ITEMS_EXTRACTED);
            if (tier.compareTo(AttachmentTier.GOLD) >= 0) {
                result.add(Setting.ROUTING_MODE);
            }
        }
        case RETRIEVER -> {
            result.add(Setting.MAX_ITEMS_EXTRACTED);
            if (tier.compareTo(AttachmentTier.GOLD) >= 0) {
                result.add(Setting.MAX_ITEMS_IN_INVENTORY);
                result.add(Setting.ROUTING_MODE);
            }
        }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        if (level != null && level.isClientSide()) {
            if (showExtraTooltipInfo()) {
                MutableComponent filters = null;
                for (var setting : getSupportedSettings()) {
                    if (setting.isFilter()) {
                        if (filters == null) {
                            filters = setting.getTooltipName().copy();
                        } else {
                            filters.append(", ").append(setting.getTooltipName());
                        }
                    }
                }
                if (filters != null) {
                    filters.withStyle(ChatFormatting.WHITE);
                    tooltipComponents.add(new TranslatableComponent("gui.moderndynamics.tooltip.filters", filters).withStyle(ChatFormatting.GRAY));
                }
            } else {
                var keyText = new TranslatableComponent("gui.moderndynamics.tooltip.more_info_key").withStyle(ChatFormatting.YELLOW,
                        ChatFormatting.ITALIC);
                tooltipComponents.add(new TranslatableComponent("gui.moderndynamics.tooltip.more_info", keyText).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static boolean showExtraTooltipInfo() {
        return Screen.hasShiftDown();
    }
}
