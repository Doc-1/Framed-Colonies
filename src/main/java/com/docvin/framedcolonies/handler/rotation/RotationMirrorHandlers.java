package com.docvin.framedcolonies.handler.rotation;

import com.ldtteam.structurize.api.RotationMirror;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xfacthd.framedblocks.api.camo.block.AbstractBlockCamoContainer;
import xfacthd.framedblocks.common.blockentity.doubled.FramedDoubleBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class RotationMirrorHandlers {

    public static final List<IRotationMirrorHandler> handlers = new ArrayList<>();

    static {
        handlers.add(new FramedAdjustableDoublePanelBlockRotationHandler());
    }

    /**
     * Finds the appropriate {@link IRotationMirrorHandler} for the given location.
     *
     * @param newState the blockstate being rotated or mirroed.
     * @return the appropriate handler if one exists.
     */
    public static IRotationMirrorHandler getHandler(final BlockState newState) {
        for (final IRotationMirrorHandler rotationMirrorHandler : handlers) {
            if (rotationMirrorHandler.canHandle(newState)) {
                return rotationMirrorHandler;
            }
        }

        return null;
    }

    public static void rotateCamo(BlockPos pos, BlockState state, final CompoundTag compound, Level level, RotationMirror transformBy) {
        if (BlockEntity.loadStatic(pos, state, compound, level.registryAccess()) instanceof FramedDoubleBlockEntity framedBlockEntity) {
            framedBlockEntity.setLevel(level);

            AbstractBlockCamoContainer<?> newCamo = (AbstractBlockCamoContainer<?>) framedBlockEntity.getCamo();
            AbstractBlockCamoContainer<?> newCamo2 = (AbstractBlockCamoContainer<?>) framedBlockEntity.getCamoTwo();
            if (newCamo != null && newCamo.canRotateCamo()) {
                BlockState camoR = newCamo.getState().rotate(transformBy.rotation());
                AbstractBlockCamoContainer<?> camoRot = newCamo.copyWithState(camoR.mirror(transformBy.mirror()));
                framedBlockEntity.setCamo(camoRot, false);
            }
            if (newCamo2 != null && newCamo2.canRotateCamo()) {
                BlockState camoR = newCamo2.getState().rotate(transformBy.rotation());
                AbstractBlockCamoContainer<?> camoRot = newCamo2.copyWithState(camoR.mirror(transformBy.mirror()));
                framedBlockEntity.setCamo(camoRot, true);
            }

            CompoundTag tag = framedBlockEntity.saveCustomOnly(level.registryAccess());
            if (tag.get("camo") instanceof CompoundTag camoTag)
                compound.put("camo", camoTag);
            if (tag.get("camo_two") instanceof CompoundTag camoTag)
                compound.put("camo_two", camoTag);
        }
    }

}
