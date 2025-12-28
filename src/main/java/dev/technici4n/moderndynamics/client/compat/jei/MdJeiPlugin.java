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
package dev.technici4n.moderndynamics.client.compat.jei;

import dev.technici4n.moderndynamics.attachment.upgrade.LoadedUpgrades;
import dev.technici4n.moderndynamics.client.compat.RecipeViewer;
import dev.technici4n.moderndynamics.client.screen.AttachedIoScreen;
import dev.technici4n.moderndynamics.gui.menu.FluidConfigSlot;
import dev.technici4n.moderndynamics.init.MdItems;
import dev.technici4n.moderndynamics.util.MdId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class MdJeiPlugin implements IModPlugin {
    @Override
    public Identifier getPluginUid() {
        return MdId.of("jei");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        RecipeViewer.setCurrent(new JeiRecipeViewer(jeiRuntime));
    }

    @Override
    public void onRuntimeUnavailable() {
        RecipeViewer.setCurrent(null);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new UpgradeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (var workstation : List.of(MdItems.ATTRACTOR, MdItems.EXTRACTOR, MdItems.FILTER)) {
            registration.addCraftingStation(UpgradeCategory.TYPE, new ItemStack(workstation.get()));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                UpgradeCategory.TYPE,
                LoadedUpgrades.get().list.stream()
                        .map(u -> new UpgradeDisplay(u, LoadedUpgrades.getType(u)))
                        .toList());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(AttachedIoScreen.class, new IGuiContainerHandler<AttachedIoScreen<?>>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(AttachedIoScreen<?> screen) {
                List<Rect2i> rectangles = new ArrayList<>();
                screen.appendExclusionZones(r -> rectangles.add(new Rect2i(r.getX(), r.getY(), r.getWidth(), r.getHeight())));
                return rectangles;
            }

            @Override
            public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder,
                    AttachedIoScreen<?> screen, double mouseX, double mouseY) {
                // Ensures that users can press R, U, etc... on fluid config slots.
                if (screen.getHoveredSlot() instanceof FluidConfigSlot fluidConfig) {
                    var resource = fluidConfig.getFilter();
                    if (!resource.isEmpty()) {
                        var slotArea = new Rect2i(screen.getLeftPos() + fluidConfig.x, screen.getTopPos() + fluidConfig.y, 16, 16);
                        return builder.createBuilder(NeoForgeTypes.FLUID_STACK, resource.toStack(1))
                                .buildWithArea(slotArea);
                    }
                }

                return Optional.empty();
            }
        });

        // Allows slots to be locked by dragging from REI
        registration.addGhostIngredientHandler(AttachedIoScreen.class, new GhostIngredientHandler());
    }

}
