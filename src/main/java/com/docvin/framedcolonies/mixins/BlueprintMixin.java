package com.docvin.framedcolonies.mixins;

import com.ldtteam.structurize.api.BlockPosUtil;
import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blockentities.BlockEntityTagSubstitution;
import com.ldtteam.structurize.blockentities.ModBlockEntities;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.component.CapturedBlock;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xfacthd.framedblocks.api.camo.block.AbstractBlockCamoContainer;
import xfacthd.framedblocks.common.blockentity.doubled.FramedDoubleBlockEntity;

import java.util.*;

import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.*;

@Mixin(Blueprint.class)
public abstract class BlueprintMixin {

    @Mutable
    @Final
    @Shadow
    private final HolderLookup.Provider registryAccess;
    @Shadow
    private short sizeY;
    @Shadow
    private short sizeZ;
    @Shadow
    private short sizeX;
    @Shadow
    private CompoundTag[] entities;
    @Shadow
    private List<BlockState> palette;
    @Shadow
    private short[][][] structure;
    @Shadow
    private CompoundTag[][][] tileEntities;
    @Shadow
    private RotationMirror rotationMirror;

    public BlueprintMixin(HolderLookup.Provider registryAccess) {
        this.registryAccess = registryAccess;
    }

    @Shadow
    private static CompoundTag transformEntityInfoWithSettings(CompoundTag entityInfo, Level world, BlockPos pos, RotationMirror rotationMirror) {
        final Optional<EntityType<?>> type = EntityType.by(entityInfo);
        if (type.isPresent()) {
            final Entity finalEntity = type.get().create(world);

            if (finalEntity != null) {
                try {
                    finalEntity.load(entityInfo);

                    final Vec3 entityVec = rotationMirror
                            .applyToPos(
                                    finalEntity instanceof HangingEntity hang ? Vec3.atCenterOf(hang.getPos()) : finalEntity.position())
                            .add(Vec3.atLowerCornerOf(pos));
                    finalEntity.setYRot(finalEntity.mirror(rotationMirror.mirror()));
                    finalEntity.setYRot(finalEntity.rotate(rotationMirror.rotation()));
                    finalEntity.moveTo(entityVec.x, entityVec.y, entityVec.z, finalEntity.getYRot(), finalEntity.getXRot());

                    final CompoundTag newEntityInfo = new CompoundTag();
                    finalEntity.save(newEntityInfo);
                    return newEntityInfo;
                } catch (final Exception ex) {
                    Log.getLogger().error("Entity: " + type.get().getDescriptionId() + " failed to load. ", ex);
                    return null;
                }
            }
        }
        return null;
    }

    @Shadow
    public abstract BlockPos getPrimaryBlockOffset();

    @Shadow
    public abstract void setCachePrimaryOffset(BlockPos cachePrimaryOffset);

    @Shadow
    protected abstract void cacheReset(boolean resetPrimaryOffset);

    @Inject(at = @At("HEAD"), method = "setRotationMirrorRelative", cancellable = true)
    public void setRotationMirrorRelative(RotationMirror transformBy, Level level, CallbackInfo ci) {
        if (level.registryAccess() != registryAccess) {
            throw new IllegalStateException("World mismatch");
        }
        if (transformBy == RotationMirror.NONE) {
            return;
        }

        final DynamicOps<Tag> dynamicNbtOps = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        final BlockPos primaryOffset = getPrimaryBlockOffset();
        final short newSizeX, newSizeZ, newSizeY = sizeY;

        newSizeZ = switch (transformBy.rotation()) {
            case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> {
                newSizeX = sizeZ;
                yield sizeX;
            }
            default -> {
                newSizeX = sizeX;
                yield sizeZ;
            }
        };

        final short[][][] newStructure = new short[newSizeY][newSizeZ][newSizeX];
        final CompoundTag[] newEntities = new CompoundTag[entities.length];
        final CompoundTag[][][] newTileEntities = new CompoundTag[newSizeY][newSizeZ][newSizeX];

        final List<BlockState> palette = new ArrayList<>();
        for (int i = 0; i < this.palette.size(); i++) {
            palette.add(i, transformBy.applyToBlockState(this.palette.get(i)));
        }

        final BlockPos extremes = transformBy.applyToPos(new BlockPos(sizeX, sizeY, sizeZ));
        int minX = extremes.getX() < 0 ? -extremes.getX() - 1 : 0;
        int minY = extremes.getY() < 0 ? -extremes.getY() - 1 : 0;
        int minZ = extremes.getZ() < 0 ? -extremes.getZ() - 1 : 0;

        this.palette = palette;

        for (short x = 0; x < this.sizeX; x++) {
            for (short y = 0; y < this.sizeY; y++) {
                for (short z = 0; z < this.sizeZ; z++) {
                    final short value = structure[y][z][x];
                    final BlockState state = palette.get(value & 0xFFFF);
                    if (state.getBlock() == Blocks.STRUCTURE_VOID) {
                        continue;
                    }
                    final BlockPos tempPos = transformBy.applyToPos(new BlockPos(x, y, z)).offset(minX, minY, minZ);
                    newStructure[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = value;

                    final CompoundTag compound = tileEntities[y][z][x];
                    if (compound != null) {
                        compound.putInt("x", tempPos.getX());
                        compound.putInt("y", tempPos.getY());
                        compound.putInt("z", tempPos.getZ());

                        // TODO: ideally this would be generalised to any IRotatableBlockEntity or we would instead
                        //       reinflate the entity and use the Forge rotation method, but the latter requires a
                        //       Level with blockstate and entity and the former requires reinflating everything
                        //       before we can test whether it's rotatable or not, neither of which is ideal.  So
                        //       for now this is the minimal requirement.
                        if (compound.getString("id").equals(ModBlockEntities.TAG_SUBSTITUTION.getId().toString())) {
                            CapturedBlock replacement = BlockEntityTagSubstitution.deserializeReplacement(compound, dynamicNbtOps);
                            replacement = replacement.applyRotationMirror(transformBy, level);
                            BlockEntityTagSubstitution.serializeReplacement(compound, dynamicNbtOps, replacement);
                        }

                        if (BlockEntity.loadStatic(tempPos, state, compound, level.registryAccess()) instanceof FramedDoubleBlockEntity framedBlockEntity) {
                            framedBlockEntity.setLevel(level);

                            System.out.println(compound);
                            System.out.println(framedBlockEntity.saveCustomOnly(level.registryAccess()));
                            AbstractBlockCamoContainer<?> newCamo = (AbstractBlockCamoContainer<?>) framedBlockEntity.getCamo();
                            AbstractBlockCamoContainer<?> newCamo2 = (AbstractBlockCamoContainer<?>) framedBlockEntity.getCamoTwo();
                            if (newCamo != null && newCamo.canRotateCamo()) {

                                AbstractBlockCamoContainer<?> camoRot = newCamo.copyWithState(newCamo.getState().rotate(transformBy.rotation()));
                                framedBlockEntity.setCamo(camoRot, false);
                            }
                            if (newCamo2 != null && newCamo2.canRotateCamo()) {
                                AbstractBlockCamoContainer<?> camoRot = newCamo2.copyWithState(newCamo2.getState().rotate(transformBy.rotation()));
                                framedBlockEntity.setCamo(camoRot, true);
                            }

                            CompoundTag tag = framedBlockEntity.saveCustomOnly(level.registryAccess());
                            if (tag.get("camo") instanceof CompoundTag camoTag)
                                compound.put("camo", camoTag);
                            if (tag.get("camo_two") instanceof CompoundTag camoTag)
                                compound.put("camo_two", camoTag);
                        }

                        if (compound.contains(TAG_BLUEPRINTDATA)) {
                            CompoundTag dataCompound = compound.getCompound(TAG_BLUEPRINTDATA);

                            // Rotate tag map
                            final Map<BlockPos, List<String>> tagPosMap = IBlueprintDataProviderBE.readTagPosMapFrom(dataCompound);
                            final Map<BlockPos, List<String>> newTagPosMap = new HashMap<>();

                            for (Map.Entry<BlockPos, List<String>> entry : tagPosMap.entrySet()) {
                                newTagPosMap.put(transformBy.applyToPos(entry.getKey()), entry.getValue());
                            }

                            IBlueprintDataProviderBE.writeMapToCompound(dataCompound, newTagPosMap);

                            // Rotate corners
                            BlockPosUtil.writeToNBT(dataCompound, TAG_CORNER_ONE, transformBy.applyToPos(BlockPosUtil.readFromNBT(dataCompound, TAG_CORNER_ONE)));
                            BlockPosUtil.writeToNBT(dataCompound, TAG_CORNER_TWO, transformBy.applyToPos(BlockPosUtil.readFromNBT(dataCompound, TAG_CORNER_TWO)));
                        }
                    }
                    newTileEntities[tempPos.getY()][tempPos.getZ()][tempPos.getX()] = compound;
                }
            }
        }

        for (int i = 0; i < entities.length; i++) {
            final CompoundTag entitiesCompound = entities[i];
            if (entitiesCompound != null) {
                newEntities[i] = transformEntityInfoWithSettings(entitiesCompound, level, new BlockPos(minX, minY, minZ), transformBy);
            }
        }

        this.setCachePrimaryOffset(transformBy.applyToPos(primaryOffset).offset(minX, minY, minZ));

        sizeX = newSizeX;
        sizeY = newSizeY;
        sizeZ = newSizeZ;

        this.structure = newStructure;
        this.entities = newEntities;
        this.tileEntities = newTileEntities;
        this.rotationMirror = this.rotationMirror.add(transformBy);

        this.cacheReset(false);
        ci.cancel();
    }
}
