package com.docvin.framedcolonies.handler.placement;

import com.docvin.framedcolonies.handler.rotation.RotationMirrorHandlers;
import com.ldtteam.common.util.BlockToItemHelper;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.handlers.placement.IPlacementHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.api.block.blockentity.FramedBlockEntity;
import xfacthd.framedblocks.api.util.ConfigView;
import xfacthd.framedblocks.common.block.FramedBlock;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.api.constants.Constants.UPDATE_FLAG;
import static com.ldtteam.structurize.placement.handlers.placement.PlacementHandlers.handleTileEntityPlacement;

public class FrameBlockPlacementHandler implements IPlacementHandler {

    @Override
    public boolean canHandle(Level level, BlockPos blockPos, BlockState blockState) {
        return blockState.getBlock() instanceof FramedBlock;
    }

    @Override
    public ActionProcessingResult handle(Blueprint blueprint, Level level, BlockPos pos, BlockState blockState, @Nullable CompoundTag tileEntityData, boolean complete, BlockPos centerPos, RotationMirror settings) {

        blockState = RotationMirrorHandlers.rotateDoublePanel(blockState, settings);

        if (level.getBlockState(pos).equals(blockState)) {
            level.removeBlock(pos, false);
            level.setBlock(pos, blockState, UPDATE_FLAG);
            if (tileEntityData != null) {
                RotationMirrorHandlers.rotateCamo(pos, blockState, tileEntityData, level, settings);
                handleTileEntityPlacement(tileEntityData, level, pos, settings);
            }
            return ActionProcessingResult.PASS;
        }

        if (!level.setBlock(pos, blockState, UPDATE_FLAG)) {
            return ActionProcessingResult.DENY;
        }

        if (tileEntityData != null) {
            RotationMirrorHandlers.rotateCamo(pos, blockState, tileEntityData, level, settings);
            handleTileEntityPlacement(tileEntityData, level, pos, settings);
        }

        return ActionProcessingResult.SUCCESS;
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
