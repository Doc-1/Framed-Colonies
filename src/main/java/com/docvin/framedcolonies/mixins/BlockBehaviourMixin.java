package com.docvin.framedcolonies.mixins;

import com.docvin.framedcolonies.handler.rotation.IRotationMirrorHandler;
import com.docvin.framedcolonies.handler.rotation.RotationMirrorHandlers;
import com.ldtteam.structurize.api.RotationMirror;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    @Inject(at = @At("HEAD"), method = "rotate", cancellable = true)
    protected void rotate(BlockState state, Rotation rotation, CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (!rotation.equals(Rotation.NONE)) {
            IRotationMirrorHandler rotationHandler = RotationMirrorHandlers.getHandler(state);
            if (rotationHandler != null && rotationHandler.canHandle(state)) {
                callbackInfoReturnable.setReturnValue(rotationHandler.handleRotationMirror(state, RotationMirror.of(rotation, Mirror.NONE)));
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "mirror", cancellable = true)
    protected void mirror(BlockState state, Mirror mirror, CallbackInfoReturnable<BlockState> callbackInfoReturnable) {
        if (!mirror.equals(Mirror.NONE)) {
            IRotationMirrorHandler rotationHandler = RotationMirrorHandlers.getHandler(state);
            if (rotationHandler != null && rotationHandler.canHandle(state)) {
                callbackInfoReturnable.setReturnValue(rotationHandler.handleRotationMirror(state, RotationMirror.of(Rotation.NONE, mirror)));
            }
        }
    }
}