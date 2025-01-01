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
package dev.technici4n.moderndynamics.compat.jade;

import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.network.item.ItemHost;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import dev.technici4n.moderndynamics.util.ItemVariant;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

public enum ItemPipeServerProvider implements IServerExtensionProvider<ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return MdId.of("item_pipe");
    }

    @Override
    public @Nullable List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
        if (!(accessor.getTarget() instanceof PipeBlockEntity pipe)) {
            return null;
        }

        // Send attachment data here
        var itemHost = pipe.findHost(ItemHost.class);
        if (itemHost == null) {
            return null;
        }

        List<ViewGroup<ItemStack>> groups = new ArrayList<>();

        for (var side : Direction.values()) {
            if (pipe.getAttachment(side) instanceof ItemAttachedIo io) {
                if (io.isStuffed()) {
                    var group = new ViewGroup<ItemStack>(new ArrayList<>());
                    group.views.addAll(variantMapToStacks(io.getStuffedItems()));
                    group.id = "stuffed_" + side.getName();
                    groups.add(group);
                }
            }
        }

        // Always add a dummy group.
        // We need to run on the client side to add client traveling items on the client side,
        // even if the server doesn't provide any stack.
        var dummyGroup = new ViewGroup<ItemStack>(new ArrayList<>());
        dummyGroup.views.add(MdItems.WRENCH.getDefaultInstance());
        dummyGroup.id = "dummy";
        groups.add(dummyGroup);

        return groups;
    }

    private static Collection<ItemStack> variantMapToStacks(Map<ItemVariant, Integer> map) {
        List<ItemStack> stacks = new ArrayList<>();
        for (var entry : map.entrySet()) {
            stacks.add(entry.getKey().toStack(entry.getValue()));
        }
        stacks.sort(Comparator.comparingInt(ItemStack::getCount).reversed());
        return stacks;
    }

    @Override
    public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> serverGroups) {
        if (!(accessor.getTarget() instanceof PipeBlockEntity pipe)) {
            return List.of();
        }

        List<ClientViewGroup<ItemView>> clientGroups = new ArrayList<>();

        // Stuffed items
        var posInBlock = pipe.getPosInBlock(accessor.getHitResult());
        var hitAttachmentSide = pipe.hitTestAttachments(posInBlock);
        if (hitAttachmentSide != null) {
            var attachmentItem = pipe.getAttachmentItem(hitAttachmentSide);
            if (attachmentItem != null) {
                // Find corresponding server group
                serverGroups.stream()
                        .filter(vg -> vg.id != null && vg.id.equals("stuffed_" + hitAttachmentSide.getName()))
                        .findFirst()
                        .ifPresent(group -> {
                            var clientGroup = new ClientViewGroup<ItemView>(new ArrayList<>());
                            for (var stack : group.views) {
                                clientGroup.views.add(new ItemView(stack));
                            }
                            clientGroup.title = Component.translatable("gui.moderndynamics.tooltip.stuffed", attachmentItem.getDescription())
                                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true));
                            clientGroups.add(clientGroup);
                        });
            }
        }

        // Pipe contents, only if nothing is stuffed
        if (!clientGroups.isEmpty()) {
            return clientGroups;
        }
        for (var host : pipe.getHosts()) {
            if (host instanceof ItemHost itemHost) {
                Map<ItemVariant, Integer> items = new HashMap<>();
                for (var item : itemHost.getClientTravelingItems()) {
                    items.merge(item.variant(), item.amount(), Integer::sum);
                }

                var clientGroup = new ClientViewGroup<ItemView>(new ArrayList<>());
                for (var stack : variantMapToStacks(items)) {
                    clientGroup.views.add(new ItemView(stack));
                }
                if (!clientGroup.views.isEmpty()) {
                    clientGroups.add(clientGroup);
                }
            }
        }

        return clientGroups;
    }
}
