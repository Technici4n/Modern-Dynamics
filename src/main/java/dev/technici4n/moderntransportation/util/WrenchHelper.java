package dev.technici4n.moderntransportation.util;

import dev.technici4n.moderntransportation.block.PipeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper to detect if items are wrenches, and to make wrench shift-clicking dismantle MT pipes.
 */
public class WrenchHelper {
    private static final Set<Identifier> MODDED_WRENCHES;
    private static final Tag<Item> WRENCH_TAG = ForgeTagHandler.createOptionalTag(ForgeRegistries.ITEMS, new Identifier("forge:tools/wrench"));

    static {
        MODDED_WRENCHES = new HashSet<>();
        MODDED_WRENCHES.add(new Identifier("immersiveengineering:hammer"));
    }

    public static boolean isWrench(ItemStack stack) {
        Item item = stack.getItem();

        return WRENCH_TAG.contains(item) || MODDED_WRENCHES.contains(item.getRegistryName());
    }

    public static void registerEvents() {
        MinecraftForge.EVENT_BUS.addListener(WrenchHelper::onPlayerInteract);
    }

    /**
     * Dismantle target pipe on shift-click.
     */
    private static void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().isSneaking()) {
            World world = event.getWorld();
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof PipeBlock && isWrench(event.getItemStack())) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(state.getBlock().asItem()));
                // TODO: play a cool sound
                event.setCanceled(true);
                event.setCancellationResult(ActionResult.success(world.isClient()));
            }
        }
    }
}
