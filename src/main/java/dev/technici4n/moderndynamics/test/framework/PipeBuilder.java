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
package dev.technici4n.moderndynamics.test.framework;

import dev.technici4n.moderndynamics.attachment.AttachmentItem;
import dev.technici4n.moderndynamics.attachment.attached.FluidAttachedIo;
import dev.technici4n.moderndynamics.attachment.attached.ItemAttachedIo;
import dev.technici4n.moderndynamics.pipe.PipeBlockEntity;
import java.util.function.Consumer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class PipeBuilder {
    private final MdGameTestHelper helper;
    private final PipeBlockEntity pipe;

    public PipeBuilder(MdGameTestHelper helper, PipeBlockEntity pipe) {
        this.helper = helper;
        this.pipe = pipe;
    }

    public PipeBuilder attachment(Direction direction, AttachmentItem attachment) {
        var stack = attachment.getDefaultInstance();

        for (var host : pipe.getHosts()) {
            if (host.acceptsAttachment(attachment, stack)) {
                host.setAttachment(direction, attachment, new CompoundTag(), pipe.getLevel().registryAccess());
                helper.getLevel().blockUpdated(pipe.getBlockPos(), pipe.getBlockState().getBlock());
                pipe.refreshHosts();
                pipe.scheduleHostUpdates();
                pipe.setChanged();
                pipe.sync();
                return this;
            }
        }

        helper.fail("Failed to add attachment " + attachment + " to pipe", pipe.getBlockPos());
        throw new UnsupportedOperationException();
    }

    public PipeBuilder configureFluidIo(Direction direction, Consumer<FluidAttachedIo> config) {
        if (pipe.getAttachment(direction) instanceof FluidAttachedIo fluidIo) {
            config.accept(fluidIo);
            return this;
        }

        helper.fail("Failed to find item io from pipe", pipe.getBlockPos());
        throw new UnsupportedOperationException();
    }

    public PipeBuilder configureItemIo(Direction direction, Consumer<ItemAttachedIo> config) {
        if (pipe.getAttachment(direction) instanceof ItemAttachedIo itemIo) {
            config.accept(itemIo);
            return this;
        }

        helper.fail("Failed to find item io from pipe", pipe.getBlockPos());
        throw new UnsupportedOperationException();
    }
}
