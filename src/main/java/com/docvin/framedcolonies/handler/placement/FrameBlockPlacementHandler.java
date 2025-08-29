package com.docvin.framedcolonies.handler.placement;

import com.ldtteam.common.util.BlockToItemHelper;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.api.block.blockentity.FramedBlockEntity;
import xfacthd.framedblocks.api.util.ConfigView;

import java.util.ArrayList;
import java.util.List;

public class FrameBlockPlacementHandler implements IPlacementHandler {

    @Override
    public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState) {
        return level.getBlockEntity(blockPos) instanceof FramedBlockEntity;
    }

    @Override
    public List<ItemStack> getRequiredItems(Level level, BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag, boolean b) {
        final List<ItemStack> itemList = new ArrayList<>();
        itemList.add(new ItemStack(BlockToItemHelper.getItem(blockState)));

        if (level.getBlockEntity(blockPos) instanceof FramedBlockEntity be)
            be.addAdditionalDrops(itemList, ConfigView.Server.INSTANCE.shouldConsumeCamoItem());

        return itemList;
    }
}
