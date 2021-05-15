package dev.technici4n.moderntransportation.network.item;

import dev.technici4n.moderntransportation.block.PipeBlockEntity;
import dev.technici4n.moderntransportation.network.NetworkManager;
import dev.technici4n.moderntransportation.network.NetworkNode;
import dev.technici4n.moderntransportation.network.NodeHost;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemHost extends NodeHost {
    private static final NetworkManager<ItemHost, ItemCache> MANAGER = NetworkManager.get(ItemCache.class);

    public final ItemPipeTier tier;

    public ItemHost(PipeBlockEntity pipe, ItemPipeTier tier) {
        super(pipe);

        this.tier = tier;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public NetworkManager getManager() {
        return MANAGER;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        return LazyOptional.empty();
    }

    @Override
    public void invalidateCapabilities() {
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void doUpdate() {
        updateConnections();

        if (hasInventoryConnections()) {
            NetworkNode<ItemHost, ItemCache> node = findNode();
            node.getNetworkCache().addInventoryConnectionHost(this);
        }
    }

    @Override
    public boolean hasInventoryConnections() {
        return inventoryConnections != 0;
    }

    public void gatherCapabilities(@Nullable List<OfferedItemHandler> out) {
        int oldConnections = inventoryConnections;

        for (int i = 0; i < 6; ++i) {
            if ((inventoryConnections & (1 << i)) > 0 && (pipeConnections & (1 << i)) == 0) {
                Direction dir = Direction.byId(i);
                BlockPos adjacentPos = pipe.getPos().offset(dir);
                @SuppressWarnings("ConstantConditions")
                BlockEntity adjacentBe = pipe.getWorld().getBlockEntity(adjacentPos);
                IItemHandler adjacentCap = null;

                if (adjacentBe != null) {
                    adjacentCap = adjacentBe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()).orElse(null);
                }

                if (adjacentCap != null) {
                    if (out != null) {
                        out.add(new OfferedItemHandler(adjacentCap, adjacentPos, dir.getOpposite()));
                    }
                } else {
                    // Remove the direction from the bitmask
                    inventoryConnections ^= 1 << i;
                }
            }
        }

        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    public void updateConnections() {
        // Store old connections
        int oldConnections = inventoryConnections;

        // Compute new connections (excluding existing adjacent pipe connections, and the blacklist)
        inventoryConnections = (1 << 6) - 1 - (pipeConnections | pipe.connectionBlacklist);
        gatherCapabilities(null);

        // Update render
        if (oldConnections != inventoryConnections) {
            pipe.sync();
        }
    }

    @Override
    public void writeNbt(CompoundTag tag) {
    }

    @Override
    public void readNbt(CompoundTag tag) {
    }
}
