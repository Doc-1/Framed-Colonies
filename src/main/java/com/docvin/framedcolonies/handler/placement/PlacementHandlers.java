package com.docvin.framedcolonies.handler.placement;

import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public class PlacementHandlers {

    private static void addHandler(IPlacementHandler handler) {
        com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.add(handler);
    }

    public static void initialiseHandlers(FMLLoadCompleteEvent ignored) {
        //addHandler(new DebugPlacementHandler());
        addHandler(new FrameBlockPlacementHandler());
    }

    private static class DebugPlacementHandler implements IPlacementHandler {
        private static final Logger LOGGER = LogUtils.getLogger();

        @Override
        public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState) {
            LOGGER.debug("data: {}", blockState);
            return false;
        }

        @Override
        public List<ItemStack> getRequiredItems(Level world, BlockPos pos, BlockState blockState, @Nullable CompoundTag tileEntityData, boolean complete) {
            return List.of();
        }

    }
}
