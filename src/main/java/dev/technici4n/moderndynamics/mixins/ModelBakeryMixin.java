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
package dev.technici4n.moderndynamics.mixins;

import dev.technici4n.moderndynamics.client.model.BuiltInModelHooks;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replicates the part of the Fabric API for adding built-in models that we actually use.
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {
    @Inject(at = @At("HEAD"), method = "loadModel", cancellable = true)
    private void loadModelHook(ResourceLocation id, CallbackInfo ci) {
        var model = BuiltInModelHooks.getBuiltInModel(id);

        if (model != null) {
            cacheAndQueueDependencies(id, model);
            ci.cancel();
        }
    }

    @Shadow
    protected void cacheAndQueueDependencies(ResourceLocation id, UnbakedModel unbakedModel) {
    }
}
