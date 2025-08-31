package com.docvin.framedcolonies.handler.rotation;

import com.ldtteam.structurize.api.RotationMirror;
import net.minecraft.world.level.block.state.BlockState;

public interface IRotationMirrorHandler {
    /**
     * Check if a Rotation or Mirroring handler can handle a certain block.
     *
     * @param blockState the blockState.
     * @return true if so.
     */


    boolean canHandle(final BlockState blockState);

    /**
     * Method used to handle the processing of a Rotation or Mirroring of a block.
     *
     * @param blockState the blockState.
     * @param settings   the settings to use to rotate or mirror it.
     */
    BlockState handleRotationMirror(final BlockState blockState, final RotationMirror settings);
}
