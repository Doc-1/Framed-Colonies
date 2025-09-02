package com.docvin.framedcolonies.handler.rotation;

import com.ldtteam.structurize.api.RotationMirror;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xfacthd.framedblocks.api.block.FramedProperties;
import xfacthd.framedblocks.api.block.blockentity.FramedBlockEntity;
import xfacthd.framedblocks.api.camo.block.AbstractBlockCamoContainer;
import xfacthd.framedblocks.common.block.slab.FramedAdjustableDoublePanelBlock;
import xfacthd.framedblocks.common.blockentity.doubled.FramedDoubleBlockEntity;

public class RotationMirrorHandlers {


    public static void rotateCamo(BlockPos pos, BlockState state, final CompoundTag compound, Level level, RotationMirror transformBy) {
        if (BlockEntity.loadStatic(pos, state, compound, level.registryAccess()) instanceof FramedBlockEntity framedBlockEntity) {
            framedBlockEntity.setLevel(level);
            AbstractBlockCamoContainer<?> newCamo = (AbstractBlockCamoContainer<?>) framedBlockEntity.getCamo();
            AbstractBlockCamoContainer<?> newCamo2 = null;

            if (framedBlockEntity instanceof FramedDoubleBlockEntity framedDoubleBlockEntity)
                newCamo2 = (AbstractBlockCamoContainer<?>) framedDoubleBlockEntity.getCamoTwo();

            if (newCamo != null && rotate(newCamo, framedBlockEntity, transformBy, level, false).get("camo") instanceof CompoundTag camoTag)
                compound.put("camo", camoTag);
            if (newCamo2 != null && rotate(newCamo2, framedBlockEntity, transformBy, level, true).get("camo_two") instanceof CompoundTag camoTag)
                compound.put("camo_two", camoTag);
        }
    }

    public static BlockState rotateDoublePanel(BlockState blockState, RotationMirror settings) {
        if (blockState.getBlock() instanceof FramedAdjustableDoublePanelBlock block) {

            Direction direction = blockState.getValue(FramedProperties.FACING_HOR);
            direction = settings.rotation().rotate(direction);
            direction = settings.mirror().mirror(direction);
            return blockState.setValue(FramedProperties.FACING_HOR, direction);

        }
        return blockState;
    }

    private static CompoundTag rotate(AbstractBlockCamoContainer<?> newCamo, final FramedBlockEntity framedBlockEntity, RotationMirror transformBy, Level level, boolean secondary) {
        if (newCamo != null && newCamo.canRotateCamo()) {
            BlockState camoR = newCamo.getState().rotate(transformBy.rotation());
            AbstractBlockCamoContainer<?> camoRot = newCamo.copyWithState(camoR.mirror(transformBy.mirror()));
            framedBlockEntity.setCamo(camoRot, secondary);
        }


        return framedBlockEntity.saveCustomOnly(level.registryAccess());
    }
}
