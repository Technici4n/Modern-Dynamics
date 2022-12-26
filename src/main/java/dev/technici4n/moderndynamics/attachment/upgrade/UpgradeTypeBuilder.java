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
package dev.technici4n.moderndynamics.attachment.upgrade;

import com.google.gson.JsonObject;
import java.lang.reflect.Modifier;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class UpgradeTypeBuilder {
    private static final UpgradeTypeBuilder DUMMY = new UpgradeTypeBuilder(Items.AIR, 0);

    public transient final ConditionJsonProvider[] conditions;
    public transient final String path;

    private final String item;
    private final int slotLimit;
    private boolean enableAdvancedBehavior = false;
    private int addFilterSlots = 0;
    private int addItemCount = 0;
    private int addItemSpeed = 0;
    private int addItemTransferFrequency = 0;
    private int addFluidTransfer = 0;
    private int multiplyFluidTransfer = 0;

    public UpgradeTypeBuilder(String requiredMod, String itemId, int slotLimit) {
        this.conditions = new ConditionJsonProvider[] { DefaultResourceConditions.allModsLoaded(requiredMod) };
        this.item = requiredMod + ":" + itemId;
        this.path = requiredMod + "/" + itemId;
        this.slotLimit = slotLimit;
    }

    public UpgradeTypeBuilder(ItemLike upgrade, int slotLimit) {
        var itemId = BuiltInRegistries.ITEM.getKey(upgrade.asItem());

        this.conditions = null;
        this.item = itemId.toString();
        this.path = itemId.getPath();
        this.slotLimit = slotLimit;
    }

    public UpgradeTypeBuilder advancedBehavior() {
        this.enableAdvancedBehavior = true;
        return this;
    }

    public UpgradeTypeBuilder filterSlots(int slots) {
        this.addFilterSlots = slots;
        return this;
    }

    public UpgradeTypeBuilder itemCount(int itemCount) {
        this.addItemCount = itemCount;
        return this;
    }

    public UpgradeTypeBuilder itemSpeed(int itemSpeed) {
        this.addItemSpeed = itemSpeed;
        return this;
    }

    public UpgradeTypeBuilder itemTransferFrequency(int frequency) {
        this.addItemTransferFrequency = frequency;
        return this;
    }

    public UpgradeTypeBuilder fluidTransfer(int add, int multiply) {
        this.addFluidTransfer = add;
        this.multiplyFluidTransfer = multiply;
        return this;
    }

    public String getFileName() {
        return path + ".json";
    }

    public JsonObject toJson() {
        var obj = new JsonObject();

        for (var field : UpgradeTypeBuilder.class.getDeclaredFields()) {
            if ((field.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) > 0) {
                // skip transient or static
                continue;
            }

            try {
                if (field.get(this).equals(field.get(DUMMY))) {
                    // skip field if it has the default value
                    continue;
                }

                obj.add(field.getName(), AttachmentUpgradesLoader.GSON.toJsonTree(field.get(this)));
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
        }

        ConditionJsonProvider.write(obj, conditions);
        return obj;
    }
}
