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
package dev.technici4n.moderndynamics.hooks.mixin;

import dev.technici4n.moderndynamics.hooks.ResourceReloadFinished;
import java.util.Collection;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Dynamic
    @Inject(method = "method_29440", at = @At(value = "INVOKE", target = "net/minecraft/server/ReloadableServerResources.updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V"))
    private void onResourceReloadFinished(Collection<String> selectedIds, MinecraftServer.ReloadableResources resources, CallbackInfo ci) {
        ResourceReloadFinished.EVENT.invoker().onResourceReloadFinished((MinecraftServer) (Object) this, resources.resourceManager());
    }
}
