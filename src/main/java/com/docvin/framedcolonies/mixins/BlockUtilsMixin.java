package com.docvin.framedcolonies.mixins;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.domumornamentum.entity.block.MateriallyTexturedBlockEntity;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xfacthd.framedblocks.common.block.FramedBlock;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.ldtteam.domumornamentum.util.Constants.BLOCK_ENTITY_TEXTURE_DATA;

@Mixin(BlockUtils.class)
public class BlockUtilsMixin {

    @Inject(at = @At("HEAD"), method = "areBlockStatesEqual", cancellable = true)
    private static void areBlockStatesEqual(BlockState structureState, BlockState worldState, Predicate<BlockState> shallReplace, boolean fancy, BiPredicate<BlockState, BlockState> specialEqualRule, CompoundTag tileEntityData, BlockEntity worldEntity, CallbackInfoReturnable<Boolean> cir) {
        if (structureState == null || worldState == null) {
            cir.setReturnValue(true);
        }

        final Block structureBlock = structureState.getBlock();
        final Block worldBlock = worldState.getBlock();
        if (fancy && structureBlock == ModBlocks.blockSubstitution.get()) {
            cir.setReturnValue(true);
        }

        if (worldState.equals(structureState)) {
            if (tileEntityData == null) {
                cir.setReturnValue(true);
            } else if (worldEntity == null) {
                cir.setReturnValue(true);
            } else if (worldEntity instanceof final MateriallyTexturedBlockEntity mtbe && tileEntityData.contains(BLOCK_ENTITY_TEXTURE_DATA)) {
                cir.setReturnValue(mtbe.getTextureData().equals(MaterialTextureData.CODEC.decode(NbtOps.INSTANCE, tileEntityData.get(BLOCK_ENTITY_TEXTURE_DATA)).getOrThrow().getFirst()));
            } else if (structureBlock instanceof FramedBlock)
                cir.setReturnValue(false);
            cir.setReturnValue(true);
        } else if (worldEntity instanceof MateriallyTexturedBlockEntity) {
            cir.setReturnValue(true);
        }

        if (fancy) {
            if (structureBlock instanceof AirBlock && worldBlock instanceof AirBlock) {
                cir.setReturnValue(true);
            }

            if (structureBlock == Blocks.DIRT && worldState.is(BlockTags.DIRT)) {
                cir.setReturnValue(true);
            }


            if (structureBlock == ModBlocks.blockSolidSubstitution.get() && !shallReplace.test(worldState)) {
                cir.setReturnValue(true);
            }

            // if the other block has fluid already or is not waterloggable, take no action
            if (
                // structure -> world
                    (structureBlock == ModBlocks.blockFluidSubstitution.get() &&
                            (worldState.getFluidState().isSource() || !worldState.hasProperty(BlockStateProperties.WATERLOGGED) && BlockUtils.isAnySolid(worldState))) ||
                            // world -> structure
                            (worldBlock == ModBlocks.blockFluidSubstitution.get() &&
                                    (structureState.getFluidState().isSource() || !structureState.hasProperty(BlockStateProperties.WATERLOGGED) && BlockUtils.isAnySolid(structureState)))) {
                cir.setReturnValue(true);
            }
        }

        cir.setReturnValue(specialEqualRule.test(structureState, worldState));
    }
}
