package dev.technici4n.moderndynamics.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class ItemVariant {
    private static final Logger LOG = LoggerFactory.getLogger(ItemVariant.class);
    private final Item item;
    private final @Nullable CompoundTag nbt;
    private final @Nullable CompoundTag attachments;
    private final int hashCode;

    public ItemVariant(Item item, @Nullable CompoundTag nbt, @Nullable CompoundTag attachmentsNbt) {
        this.item = item;
        this.nbt = nbt == null ? null : nbt.copy(); // defensive copy
        this.attachments = attachmentsNbt == null ? null : attachmentsNbt.copy(); // defensive copy
        this.hashCode = Objects.hash(item, nbt, attachmentsNbt);
    }

    public static ItemVariant blank() {
        return of(Items.AIR);
    }

    public static ItemVariant of(ItemStack stack) {
        return of(
                stack.getItem(),
                stack.getTag(),
                stack.serializeAttachments()
        );
    }

    public static ItemVariant of(ItemLike item, @Nullable CompoundTag nbt, @Nullable CompoundTag attachments) {
        return new ItemVariant(item.asItem(), nbt, attachments);
    }

    public static ItemVariant of(ItemLike item, @Nullable CompoundTag nbt) {
        return new ItemVariant(item.asItem(), nbt, null);
    }

    public static ItemVariant of(ItemLike item) {
        return new ItemVariant(item.asItem(), null, null);
    }

    public CompoundTag toNbt() {
        CompoundTag result = new CompoundTag();
        result.putString("item", BuiltInRegistries.ITEM.getKey(item).toString());

        if (nbt != null) {
            result.put("tag", nbt.copy());
        }

        if (attachments != null) {
            result.put("attachments", attachments.copy());
        }

        return result;
    }

    public static ItemVariant fromNbt(CompoundTag tag) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("item")));
            CompoundTag aTag = tag.contains("tag") ? tag.getCompound("tag") : null;
            CompoundTag attachmentsTag = tag.contains("attachments") ? tag.getCompound("attachments") : null;
            return of(item, aTag, attachmentsTag);
        } catch (RuntimeException runtimeException) {
            LOG.debug("Tried to load an invalid ItemVariant from NBT: {}", tag, runtimeException);
            return ItemVariant.blank();
        }
    }

    public void toPacket(FriendlyByteBuf buf) {
        if (isBlank()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeVarInt(Item.getId(item));
            buf.writeNbt(nbt);
            buf.writeNbt(attachments);
        }
    }

    public static ItemVariant fromPacket(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemVariant.blank();
        } else {
            Item item = Item.byId(buf.readVarInt());
            CompoundTag nbt = buf.readNbt();
            CompoundTag attachments = buf.readNbt();
            return of(item, nbt, attachments);
        }
    }

    public boolean isBlank() {
        return item.equals(Items.AIR);
    }

    public Item getItem() {
        return item();
    }

    public ItemStack toStack(int amount) {
        return new ItemStack(item, amount, nbt != null ? nbt.copy() : null);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public boolean matches(ItemStack stack) {
        return this.item == stack.getItem() && Objects.equals(this.nbt, stack.getTag());
    }

    @Override
    public boolean equals(Object o) {
        // succeed fast with == check
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ItemVariant variant = (ItemVariant) o;
        // fail fast with hash code
        return hashCode == variant.hashCode && item == variant.item && Objects.equals(nbt, variant.nbt)
               && Objects.equals(attachments, variant.attachments);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public Item item() {
        return item;
    }

    public @Nullable CompoundTag nbt() {
        return nbt;
    }

    public @Nullable CompoundTag attachments() {
        return attachments;
    }

    @Override
    public String toString() {
        return "ItemVariant[" +
               "item=" + item + ", " +
               "nbt=" + nbt + ", " +
               "attachments=" + attachments + ']';
    }

}
