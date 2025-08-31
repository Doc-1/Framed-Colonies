package com.docvin.framedcolonies.handler.rotation;

import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class RotationMirrorHandlers {

    public static final List<IRotationMirrorHandler> handlers = new ArrayList<>();

    static {
        handlers.add(new FramedAdjustableDoubleBlockRotationHandler());
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
}
