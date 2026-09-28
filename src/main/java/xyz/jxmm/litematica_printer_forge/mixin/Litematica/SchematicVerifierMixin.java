/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ArrayListMultimap
 *  fi.dy.masa.litematica.schematic.verifier.SchematicVerifier
 *  fi.dy.masa.litematica.schematic.verifier.SchematicVerifier$BlockMismatch
 *  fi.dy.masa.litematica.schematic.verifier.SchematicVerifier$MismatchType
 *  fi.dy.masa.litematica.util.ItemUtils
 *  fi.dy.masa.litematica.world.WorldSchematic
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.apache.commons.lang3.tuple.MutablePair
 *  org.apache.commons.lang3.tuple.Pair
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package xyz.jxmm.litematica_printer_forge.mixin.Litematica;

import com.google.common.collect.ArrayListMultimap;
import fi.dy.masa.litematica.schematic.verifier.SchematicVerifier;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.litematica.world.WorldSchematic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

@Mixin(value={SchematicVerifier.class})
public class SchematicVerifierMixin {
    @Shadow
    private WorldSchematic worldSchematic;
    @Shadow
    @Final
    private ArrayListMultimap<Pair<BlockState, BlockState>, BlockPos> wrongStatesPositions;
    @Shadow
    @Final
    private static MutablePair<BlockState, BlockState> MUTABLE_PAIR;
    @Shadow
    @Final
    private HashSet<Pair<BlockState, BlockState>> ignoredMismatches;
    @Shadow
    @Final
    private Object2ObjectOpenHashMap<BlockPos, SchematicVerifier.BlockMismatch> blockMismatches;
    @Shadow
    private ClientLevel worldClient;

    @Inject(method={"checkBlockStates"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void handleInventory(int x, int y, int z, BlockState stateSchematic, BlockState stateClient, CallbackInfo ci) {
        RandomizableContainerBlockEntity containerBlockEntity;
        BlockPos pos;
        WorldSchematic schematic;
        BlockEntity entity;
        if (!LitematicaMixinMod.VERIFY_INVENTORY.getBooleanValue()) {
            return;
        }
        MUTABLE_PAIR.setLeft(stateSchematic);
        MUTABLE_PAIR.setRight(stateClient);
        if (!this.ignoredMismatches.contains(MUTABLE_PAIR) && stateClient == stateSchematic && stateSchematic.m_60734_() instanceof BaseEntityBlock && (entity = (schematic = this.worldSchematic).m_7702_(pos = new BlockPos(x, y, z))) instanceof RandomizableContainerBlockEntity && !(containerBlockEntity = (RandomizableContainerBlockEntity)entity).m_7983_()) {
            SchematicVerifier.BlockMismatch mismatch = new SchematicVerifier.BlockMismatch(SchematicVerifier.MismatchType.WRONG_STATE, stateSchematic, stateClient, 1);
            this.wrongStatesPositions.put(Pair.of(stateSchematic, stateClient), new BlockPos(x, y, z));
            this.blockMismatches.put(pos, mismatch);
            ItemUtils.setItemForBlock((Level)this.worldClient, (BlockPos)pos, (BlockState)stateClient);
            ItemUtils.setItemForBlock((Level)this.worldSchematic, (BlockPos)pos, (BlockState)stateSchematic);
            ci.cancel();
        }
    }
}

