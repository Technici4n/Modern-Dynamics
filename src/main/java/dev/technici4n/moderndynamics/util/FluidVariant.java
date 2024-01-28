package dev.technici4n.moderndynamics.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record FluidVariant(Fluid fluid, @Nullable CompoundTag nbt) {
    public FluidVariant(Fluid fluid) {
        this(fluid, null);
    }

    public static FluidVariant blank() {
        return new FluidVariant(Fluids.EMPTY, null);
    }

    public static FluidVariant fromNbt(CompoundTag filterTag) {
        throw new UnsupportedOperationException();
    }

    public static FluidVariant fromPacket(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    public static FluidVariant of(Fluid fluid, @Nullable CompoundTag nbt) {
        return new FluidVariant(fluid, nbt);
    }

    public static FluidVariant of(Fluid fluid) {
        return of(fluid, null);
    }

    public static FluidVariant of(FluidStack resource) {
        return new FluidVariant(resource.getFluid(), resource.getTag() != null ? resource.getTag().copy() : null);
    }

    public Tag toNbt() {
        throw new UnsupportedOperationException();
    }

    public boolean isBlank() {
        return fluid.isSame(Fluids.EMPTY);
    }

    public boolean matches(FluidStack stack) {
        return fluid == stack.getFluid() && Objects.equals(nbt, stack.getTag());
    }

    public Fluid getFluid() {
        return fluid();
    }

    public FluidStack toStack(int amount) {
        return new FluidStack(fluid, amount, nbt != null ? nbt.copy() : null);
    }

    public List<Component> getTooltip() {
        var tooltip = new ArrayList<Component>();
        tooltip.add(toStack(1).getDisplayName());

        var modId = BuiltInRegistries.FLUID.getKey(fluid).getNamespace();

        // Heuristic: If the last line doesn't include the modname, add it ourselves
        var modName = formatModName(modId);
        if (tooltip.isEmpty() || !tooltip.get(tooltip.size() - 1).getString().equals(modName)) {
            tooltip.add(Component.literal(modName));
        }

        return tooltip;
    }

    private static String formatModName(String modId) {
        return "" + ChatFormatting.BLUE + ChatFormatting.ITALIC + getModName(modId);
    }

    private static String getModName(String modId) {
        return ModList.get().getModContainerById(modId).map(mc -> mc.getModInfo().getDisplayName())
                .orElse(modId);
    }

    public @Nullable CompoundTag getNbt() {
        return nbt != null ? nbt.copy() : null;
    }
}
