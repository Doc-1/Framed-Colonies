package com.docvin.framedcolonies.handler.rotation;

import com.ldtteam.structurize.api.RotationMirror;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import xfacthd.framedblocks.api.block.FramedProperties;
import xfacthd.framedblocks.common.block.slab.FramedAdjustableDoubleBlock;

public class FramedAdjustableDoubleBlockRotationHandler implements IRotationMirrorHandler {


    @Override
    public boolean canHandle(BlockState blockState) {
        return blockState.getBlock() instanceof FramedAdjustableDoubleBlock;
    }

    @Override
    public BlockState handleRotationMirror(BlockState blockState, RotationMirror settings) {
        FramedAdjustableDoubleBlock block = (FramedAdjustableDoubleBlock) blockState.getBlock();
        Direction direction = blockState.getValue(FramedProperties.FACING_HOR);
        direction = settings.rotation().rotate(direction);
        System.out.println(block.getFacing(blockState) + " " + settings);
        return blockState.setValue(FramedProperties.FACING_HOR, direction);
    }
}
