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
package dev.technici4n.moderndynamics.data;

import dev.technici4n.moderndynamics.attachment.upgrade.AttachmentUpgradesLoader;
import dev.technici4n.moderndynamics.attachment.upgrade.UpgradeTypeBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class AttachmentUpgradesProvider implements DataProvider {
    private final FabricDataGenerator gen;
    private final List<UpgradeTypeBuilder> builders = new ArrayList<>();

    public AttachmentUpgradesProvider(FabricDataGenerator gen) {
        this.gen = gen;
    }

    private void genUpgrades() {
        vanillaUpgrade(Items.STICKY_PISTON, 1).itemSpeed(1);
        vanillaUpgrade(Items.HOPPER, 6).filterSlots(2).itemCount(5);
        vanillaUpgrade(Items.REPEATER, 3).itemTransferFrequency(1);

        modUpgrade("ae2", "speed_card", 3).itemSpeed(1).itemTransferFrequency(1).fluidTransfer(0, 1);
        modUpgrade("ae2", "capacity_card", 6).filterSlots(2).itemCount(10).fluidTransfer(1, 0);

        modUpgrade("modern_industrialization", "motor", 5).itemSpeed(1).itemTransferFrequency(1);
        modUpgrade("modern_industrialization", "robot_arm", 10).itemCount(16);
        modUpgrade("modern_industrialization", "pump", 9).fluidTransfer(1, 1);
        modUpgrade("modern_industrialization", "analog_circuit", 6).filterSlots(2);

        modUpgrade("techreborn", "overclocker_upgrade", 3).itemSpeed(1).itemTransferFrequency(1).fluidTransfer(0, 1);
        modUpgrade("techreborn", "electronic_circuit", 6).filterSlots(2).itemCount(10);
        modUpgrade("techreborn", "advanced_circuit", 6).filterSlots(2).itemCount(20).fluidTransfer(1, 0);

        modUpgrade("indrev", "speed_enhancer", 3).itemSpeed(1).itemTransferFrequency(1).fluidTransfer(0, 1);
        modUpgrade("indrev", "circuit_mk1", 6).filterSlots(2).itemCount(10);
        modUpgrade("indrev", "circuit_mk2", 6).filterSlots(2).itemCount(20).fluidTransfer(1, 0);
    }

    private UpgradeTypeBuilder modUpgrade(String modid, String item, int slotLimit) {
        UpgradeTypeBuilder builder = new UpgradeTypeBuilder(modid, item, slotLimit);
        builders.add(builder);
        return builder;
    }

    private UpgradeTypeBuilder vanillaUpgrade(ItemLike item, int slotLimit) {
        UpgradeTypeBuilder builder = new UpgradeTypeBuilder(item, slotLimit);
        builders.add(builder);
        return builder;
    }

    @Override
    public void run(HashCache cache) throws IOException {
        genUpgrades();

        for (var builder : builders) {
            var path = gen.getOutputFolder().resolve("data/%s/attachment_upgrades/%s".formatted(gen.getModId(), builder.getFileName()));
            DataProvider.save(AttachmentUpgradesLoader.GSON, cache, builder.toJson(), path);
        }
    }

    @Override
    public String getName() {
        return "Upgrades";
    }

}
