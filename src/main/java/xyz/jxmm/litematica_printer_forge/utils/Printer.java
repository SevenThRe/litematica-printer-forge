/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  fi.dy.masa.litematica.config.Configs$Generic
 *  fi.dy.masa.litematica.data.DataManager
 *  fi.dy.masa.litematica.materials.MaterialCache
 *  fi.dy.masa.litematica.schematic.placement.SchematicPlacement
 *  fi.dy.masa.litematica.schematic.placement.SubRegionPlacement$RequiredEnabled
 *  fi.dy.masa.litematica.selection.Box
 *  fi.dy.masa.litematica.util.EasyPlaceProtocol
 *  fi.dy.masa.litematica.util.EntityUtils
 *  fi.dy.masa.litematica.util.InventoryUtils
 *  fi.dy.masa.litematica.util.RayTraceUtils
 *  fi.dy.masa.litematica.util.RayTraceUtils$RayTraceWrapper
 *  fi.dy.masa.litematica.util.WorldUtils
 *  fi.dy.masa.litematica.world.SchematicWorldHandler
 *  fi.dy.masa.litematica.world.WorldSchematic
 *  fi.dy.masa.malilib.util.BlockUtils
 *  fi.dy.masa.malilib.util.LayerRange
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.SignEditScreen
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.AnvilBlock
 *  net.minecraft.world.level.block.BaseCoralFanBlock
 *  net.minecraft.world.level.block.BaseCoralWallFanBlock
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.BaseRailBlock
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.BubbleColumnBlock
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.ComparatorBlock
 *  net.minecraft.world.level.block.ComposterBlock
 *  net.minecraft.world.level.block.ConcretePowderBlock
 *  net.minecraft.world.level.block.CraftingTableBlock
 *  net.minecraft.world.level.block.DetectorRailBlock
 *  net.minecraft.world.level.block.DiodeBlock
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.minecraft.world.level.block.DoorBlock
 *  net.minecraft.world.level.block.DragonEggBlock
 *  net.minecraft.world.level.block.DropperBlock
 *  net.minecraft.world.level.block.EndRodBlock
 *  net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock
 *  net.minecraft.world.level.block.FallingBlock
 *  net.minecraft.world.level.block.FenceBlock
 *  net.minecraft.world.level.block.FenceGateBlock
 *  net.minecraft.world.level.block.GlazedTerracottaBlock
 *  net.minecraft.world.level.block.GravelBlock
 *  net.minecraft.world.level.block.GrindstoneBlock
 *  net.minecraft.world.level.block.HopperBlock
 *  net.minecraft.world.level.block.IronBarsBlock
 *  net.minecraft.world.level.block.LadderBlock
 *  net.minecraft.world.level.block.LeavesBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.LightningRodBlock
 *  net.minecraft.world.level.block.LiquidBlock
 *  net.minecraft.world.level.block.NetherPortalBlock
 *  net.minecraft.world.level.block.NoteBlock
 *  net.minecraft.world.level.block.ObserverBlock
 *  net.minecraft.world.level.block.PoweredBlock
 *  net.minecraft.world.level.block.PoweredRailBlock
 *  net.minecraft.world.level.block.RailBlock
 *  net.minecraft.world.level.block.RedStoneOreBlock
 *  net.minecraft.world.level.block.RedStoneWireBlock
 *  net.minecraft.world.level.block.RedstoneLampBlock
 *  net.minecraft.world.level.block.RedstoneTorchBlock
 *  net.minecraft.world.level.block.RedstoneWallTorchBlock
 *  net.minecraft.world.level.block.RepeaterBlock
 *  net.minecraft.world.level.block.RotatedPillarBlock
 *  net.minecraft.world.level.block.SandBlock
 *  net.minecraft.world.level.block.ScaffoldingBlock
 *  net.minecraft.world.level.block.SeaPickleBlock
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.level.block.SignBlock
 *  net.minecraft.world.level.block.SimpleWaterloggedBlock
 *  net.minecraft.world.level.block.SlabBlock
 *  net.minecraft.world.level.block.SnowLayerBlock
 *  net.minecraft.world.level.block.StairBlock
 *  net.minecraft.world.level.block.StandingSignBlock
 *  net.minecraft.world.level.block.TntBlock
 *  net.minecraft.world.level.block.TorchBlock
 *  net.minecraft.world.level.block.TrapDoorBlock
 *  net.minecraft.world.level.block.TripWireHookBlock
 *  net.minecraft.world.level.block.WallBlock
 *  net.minecraft.world.level.block.WallSignBlock
 *  net.minecraft.world.level.block.WallSkullBlock
 *  net.minecraft.world.level.block.WallTorchBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.SignBlockEntity
 *  net.minecraft.world.level.block.piston.PistonBaseBlock
 *  net.minecraft.world.level.block.piston.PistonHeadBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.AttachFace
 *  net.minecraft.world.level.block.state.properties.BedPart
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.ComparatorMode
 *  net.minecraft.world.level.block.state.properties.DoubleBlockHalf
 *  net.minecraft.world.level.block.state.properties.Half
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.block.state.properties.RailShape
 *  net.minecraft.world.level.block.state.properties.RedstoneSide
 *  net.minecraft.world.level.block.state.properties.SlabType
 *  net.minecraft.world.level.material.LavaFluid
 *  net.minecraft.world.level.portal.PortalShape
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.api.distmarker.OnlyIn
 */
package xyz.jxmm.litematica_printer_forge.utils;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.EasyPlaceProtocol;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.WorldUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.util.BlockUtils;
import fi.dy.masa.malilib.util.LayerRange;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BaseCoralFanBlock;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.GravelBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.BedrockBreaker;
import xyz.jxmm.litematica_printer_forge.utils.Breaker;
import xyz.jxmm.litematica_printer_forge.utils.FacingData;
import xyz.jxmm.litematica_printer_forge.utils.FakeAccurateBlockPlacement;
import xyz.jxmm.litematica_printer_forge.utils.InventoryUtils;
import xyz.jxmm.litematica_printer_forge.utils.ItemInputs;
import xyz.jxmm.litematica_printer_forge.utils.MessageHolder;
import xyz.jxmm.litematica_printer_forge.utils.positionStorage;

public class Printer {
    private static final HashSet<Long> signCache = new HashSet();
    private static final LinkedHashMap<Map.Entry<Long, Boolean>, PositionCache> positionCache = new LinkedHashMap();
    public static boolean isSleeping = false;
    public static long lastPlaced = new Date().getTime();
    private static boolean shouldSleepLonger = false;
    public static Breaker breaker = new Breaker();
    public static int worldBottomY = -64;
    public static int worldTopY = 320;
    private static final LinkedHashMap<Long, String> causeMap = new LinkedHashMap();
    private static final Long2LongOpenHashMap referenceSet = new Long2LongOpenHashMap();

    private static boolean simulateFacingData(BlockState state, BlockPos blockPos, Vec3 hitVec) {
        BlockState testState;
        if (!state.m_61147_().contains(BlockStateProperties.f_61372_) && !state.m_61147_().contains(BlockStateProperties.f_61374_)) {
            return true;
        }
        if (state.m_60713_(Blocks.f_50332_) || state.m_204336_(BlockTags.f_13083_)) {
            return true;
        }
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.NORTH, blockPos, false);
        Block block = state.m_60734_();
        BlockPlaceContext ctx = new BlockPlaceContext((Player)player, InteractionHand.MAIN_HAND, state.m_60734_().m_5456_().m_7968_(), hitResult);
        try {
            testState = block.m_5573_(ctx);
        }
        catch (Exception e) {
            MessageHolder.sendMessageUncheckedUnique("Cannot get tested orientation of given block " + String.valueOf(state.m_60734_().m_49954_()));
            return player.m_6350_() == getFacingValueQuietly((BlockState)state);
        }
        if (testState == null) {
            MessageHolder.sendMessageUncheckedUnique("Cannot get tested orientation of given block " + String.valueOf(state.m_60734_().m_49954_()));
            return player.m_6350_() == getFacingValueQuietly((BlockState)state);
        }
        Direction testFacing = getFacingValueQuietly((BlockState)testState);
        return testFacing == getFacingValueQuietly((BlockState)state);
    }

    public static boolean canPickBlock(Minecraft mc, BlockState preference, BlockPos pos) {
        ItemStack stack;
        WorldSchematic world = SchematicWorldHandler.getSchematicWorld();
        ItemStack itemStack = stack = Printer.isReplaceableWaterFluidSource(preference) && LitematicaMixinMod.PRINTER_PLACE_ICE.getBooleanValue() ? Items.f_41980_.m_7968_() : resolvePlacementStack(MaterialCache.getInstance().getRequiredBuildItemForState(preference, (Level)world, pos));
        if (!stack.m_41619_() && stack.m_41720_() != Items.f_41852_) {
            if (!mc.f_91074_.m_150110_().f_35937_) {
                int slot = InventoryUtils.findSlotMatchingItem(mc.f_91074_, stack);
                if (slot == -1) {
                    return false;
                }
                if (LitematicaMixinMod.EASY_PLACE_MODE_HOTBAR_ONLY.getBooleanValue()) {
                    return slot < 9;
                }
            }
            return true;
        }
        return true;
    }

    private static ItemStack resolvePlacementStack(ItemStack original) {
        if (original.m_41619_()) {
            return original;
        }
        Item resolved = BlockReplacer.resolve(original.m_41720_());
        if (resolved == original.m_41720_()) {
            return original;
        }
        return resolved.m_7968_();
    }

    /**
     * Match rule: the real block equals the schematic block, or equals the schematic block's replacement target.
     */
    public static boolean blocksMatch(Block schematicBlock, Block clientBlock) {
        return schematicBlock == clientBlock || clientBlock == BlockReplacer.resolveBlock(schematicBlock);
    }

    public static boolean canPickItem(Minecraft mc, ItemStack stack) {
        if (!stack.m_41619_()) {
            if (!mc.f_91074_.m_150110_().f_35937_) {
                int slot = InventoryUtils.findSlotMatchingItem(mc.f_91074_, stack);
                if (slot == -1) {
                    return false;
                }
                if (LitematicaMixinMod.EASY_PLACE_MODE_HOTBAR_ONLY.getBooleanValue()) {
                    return slot < 9;
                }
            }
            return true;
        }
        return false;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static synchronized boolean doSchematicWorldPickBlock(Minecraft mc, BlockState preference, BlockPos pos) {
        ItemStack stack;
        WorldSchematic world = SchematicWorldHandler.getSchematicWorld();
        ItemStack itemStack = stack = Printer.isReplaceableWaterFluidSource(preference) && LitematicaMixinMod.PRINTER_PLACE_ICE.getBooleanValue() ? Items.f_41980_.m_7968_() : resolvePlacementStack(MaterialCache.getInstance().getRequiredBuildItemForState(preference, (Level)world, pos));
        if (!FakeAccurateBlockPlacement.canHandleOther(stack.m_41720_())) {
            return false;
        }
        if (!stack.m_41619_()) {
            return InventoryUtils.swapToItem(mc, stack);
        }
        return false;
    }

    @OnlyIn(value=Dist.CLIENT)
    public static synchronized boolean doSchematicWorldPickBlock(Minecraft mc, ItemStack stack) {
        if (!FakeAccurateBlockPlacement.canHandleOther(stack.m_41720_())) {
            return false;
        }
        if (!stack.m_41619_()) {
            return InventoryUtils.swapToItem(mc, stack);
        }
        return false;
    }

    public static InteractionResult doEasyPlaceFakeRotation(Minecraft mc) {
        if (FakeAccurateBlockPlacement.isHandling()) {
            MessageHolder.sendDebugMessage(mc.f_91074_, "Passed because already handling something");
            return InteractionResult.PASS;
        }
        RayTraceUtils.RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace((Level)mc.f_91073_, (Entity)mc.f_91074_, (double)6.0);
        FakeAccurateBlockPlacement.requestedTicks = Math.max(-2, FakeAccurateBlockPlacement.requestedTicks);
        if (traceWrapper == null) {
            return InteractionResult.PASS;
        }
        BlockHitResult trace = traceWrapper.getBlockHitResult();
        if (trace == null) {
            return InteractionResult.PASS;
        }
        WorldSchematic world = SchematicWorldHandler.getSchematicWorld();
        ClientLevel clientWorld = mc.f_91073_;
        BlockPos blockPos = trace.m_82425_();
        if (Printer.isPositionCached(blockPos, false)) {
            MessageHolder.sendDebugMessage(mc.f_91074_, "Passed because position " + blockPos.m_123344_() + " is cached");
            return InteractionResult.PASS;
        }
        BlockState schematicState = world.m_8055_(blockPos);
        BlockState clientState = clientWorld.m_8055_(blockPos);
        if (Printer.blocksMatch(schematicState.m_60734_(), clientState.m_60734_()) || schematicState.m_60795_()) {
            MessageHolder.sendDebugMessage(mc.f_91074_, "Passed because position " + blockPos.m_123344_() + " is satisfied");
            return InteractionResult.FAIL;
        }
        if (FakeAccurateBlockPlacement.canHandleOther(schematicState.m_60734_().m_5456_()) && Printer.canPickBlock(mc, schematicState, blockPos)) {
            MessageHolder.sendOrderMessage("Requested " + String.valueOf(schematicState) + " at " + blockPos.m_123344_());
            FakeAccurateBlockPlacement.request(schematicState, blockPos);
            return InteractionResult.SUCCESS;
        }
        MessageHolder.sendDebugMessage(mc.f_91074_, "Passed because position " + blockPos.m_123344_() + " cannot pick block or cannot handle other, handling " + String.valueOf(FakeAccurateBlockPlacement.currentHandling));
        return InteractionResult.FAIL;
    }

    public static InteractionResult doEasyPlaceNormally(Minecraft mc) {
        RayTraceUtils.RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace((Level)mc.f_91073_, (Entity)mc.f_91074_, (double)6.0);
        if (traceWrapper == null) {
            return InteractionResult.PASS;
        }
        BlockHitResult trace = traceWrapper.getBlockHitResult();
        if (trace == null) {
            return InteractionResult.PASS;
        }
        WorldSchematic world = SchematicWorldHandler.getSchematicWorld();
        ClientLevel clientWorld = mc.f_91073_;
        BlockPos blockPos = trace.m_82425_();
        BlockState schematicState = world.m_8055_(blockPos);
        BlockState clientState = clientWorld.m_8055_(blockPos);
        if (Printer.blocksMatch(schematicState.m_60734_(), clientState.m_60734_())) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = MaterialCache.getInstance().getRequiredBuildItemForState(schematicState);
        if (!stack.m_41619_()) {
            boolean canContinue;
            fi.dy.masa.litematica.util.InventoryUtils.schematicWorldPickBlock((ItemStack)stack, (BlockPos)blockPos, (Level)world, (Minecraft)mc);
            InteractionHand hand = EntityUtils.getUsedHandForItem((Player)mc.f_91074_, (ItemStack)stack);
            if (hand == null) {
                return InteractionResult.FAIL;
            }
            Direction sideOrig = trace.m_82434_();
            Direction side = Printer.applyPlacementFacing(schematicState, sideOrig, clientState);
            Vec3 hitPos = LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue() ? Printer.applyCarpetProtocolHitVec(blockPos, schematicState) : Printer.applyHitVec(blockPos, schematicState, side);
            BlockHitResult hitResult = new BlockHitResult(hitPos, side, blockPos, false);
            if (!LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue() || LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
                canContinue = mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult).m_19077_();
                Printer.cacheEasyPlacePosition(blockPos, false);
            } else {
                canContinue = FakeAccurateBlockPlacement.request(schematicState, blockPos);
            }
            if (canContinue) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }

    private static void recordCause(BlockPos pos, String reason, BlockPos reasonPos) {
        if (!LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue()) {
            return;
        }
        if (reasonPos != null) {
            if (pos.m_121878_() == reasonPos.m_121878_()) {
                causeMap.put(pos.m_121878_(), "self registered+\n");
            }
            referenceSet.put(pos.m_121878_(), reasonPos.m_121878_());
        }
        causeMap.put(pos.m_121878_(), reason + "\n");
    }

    private static void recordCause(BlockPos pos, String reason) {
        Printer.recordCause(pos, reason, null);
    }

    private static String getReason(Long pos) {
        return "<" + Printer.internalGetReason(pos, null, 0) + ">";
    }

    private static String internalGetReason(Long pos, LongOpenHashSet set, int count) {
        if (count > 10) {
            return BlockPos.m_122022_((long)pos).m_123344_() + "RECURSIVE_COUNT_EXCEED";
        }
        if (set == null) {
            set = new LongOpenHashSet();
        }
        if (set.contains(pos.longValue())) {
            return BlockPos.m_122022_((long)pos).m_123344_() + "Recursive ";
        }
        if (referenceSet.containsKey(pos.longValue())) {
            set.add(pos.longValue());
            return causeMap.getOrDefault(pos, BlockPos.m_122022_((long)pos).m_123344_() + " : Not registered") + " " + Printer.internalGetReason(referenceSet.get(pos.longValue()), set, count + 1);
        }
        return causeMap.getOrDefault(pos, BlockPos.m_122022_((long)pos).m_123344_() + " : Not registered");
    }

    private static boolean isPositionWithinBox(Box box, BlockPos pos) {
        if (box == null) {
            return true;
        }
        BlockPos start = box.getPos1();
        BlockPos end = box.getPos2();
        BlockPos ref1 = new BlockPos(Math.min(start.m_123341_(), end.m_123341_()), Math.min(start.m_123342_(), end.m_123342_()), Math.min(start.m_123343_(), end.m_123343_()));
        BlockPos ref2 = new BlockPos(Math.max(start.m_123341_(), end.m_123341_()), Math.max(start.m_123342_(), end.m_123342_()), Math.max(start.m_123343_(), end.m_123343_()));
        return ref1.m_123341_() <= pos.m_123341_() && pos.m_123341_() <= ref2.m_123341_() && ref1.m_123342_() <= pos.m_123342_() && pos.m_123342_() <= ref2.m_123342_() && ref1.m_123343_() <= pos.m_123343_() && pos.m_123343_() <= ref2.m_123343_();
    }

    @OnlyIn(value=Dist.CLIENT)
    public static synchronized InteractionResult doPrinterAction(Minecraft mc) {
        Direction primaryFacing;
        BlockPos playerPos;
        InventoryUtils.itemChangeCount = 0;
        if (!LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue()) {
            causeMap.clear();
        }
        if (!LitematicaMixinMod.BEDROCK_BREAKING.getBooleanValue()) {
            BedrockBreaker.clear();
        }
        FakeAccurateBlockPlacement.requestedTicks = Math.max(-2, FakeAccurateBlockPlacement.requestedTicks);
        if (breaker.isBreakingBlock()) {
            System.out.println("[PRINTER-DEBUG] Early return: breaker is breaking block");
            mc.f_91074_.m_5661_(Component.m_130674_((String)"Handling breakBlock!"), true);
            return InteractionResult.SUCCESS;
        }
        if (LitematicaMixinMod.INVENTORY_OPERATIONS.getBooleanValue()) {
            if (ItemInputs.execute(mc)) {
                mc.f_91074_.m_5661_(Component.m_130674_((String)"Handling inventory operation!"), true);
                return InteractionResult.PASS;
            }
        }
        ItemInputs.clear();
        Date date = new Date();
        if ((double)date.getTime() < (double)lastPlaced + 1000.0 * LitematicaMixinMod.EASY_PLACE_MODE_DELAY.getDoubleValue()) {
            System.out.println("[PRINTER-DEBUG] Early return: delay not elapsed (lastPlaced=" + lastPlaced + " now=" + new Date().getTime() + " delay=" + LitematicaMixinMod.EASY_PLACE_MODE_DELAY.getDoubleValue() + ")");
            mc.f_91074_.m_5661_(Component.m_130674_((String)"Handling delay"), true);
            return InteractionResult.PASS;
        }
        isSleeping = false;
        boolean isCreative = mc.f_91074_.m_7500_();
        BlockPos tracePos = playerPos = mc.f_91074_.m_20183_();
        RayTraceUtils.RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace((Level)mc.f_91073_, (Entity)mc.f_91074_, (double)6.0);
        if (traceWrapper != null) {
            BlockHitResult trace = traceWrapper.getBlockHitResult();
            tracePos = trace.m_82425_();
        }
        int posX = tracePos.m_123341_();
        int posY = tracePos.m_123342_();
        int posZ = tracePos.m_123343_();
        boolean ClearArea = LitematicaMixinMod.CLEAR_AREA_MODE.getBooleanValue();
        boolean UseCobble = LitematicaMixinMod.CLEAR_AREA_MODE_COBBLESTONE.getBooleanValue() && ClearArea;
        boolean ClearSnow = LitematicaMixinMod.CLEAR_AREA_MODE_SNOWPREVENT.getBooleanValue() && ClearArea;
        boolean CanUseProtocol = LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue();
        boolean FillInventory = LitematicaMixinMod.PRINTER_PUMPKIN_PIE_FOR_COMPOSTER.getBooleanValue();
        ItemStack composableItem = Items.f_42687_.m_7968_();
        int rangeX = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_X.getIntegerValue();
        int rangeY = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
        int rangeZ = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
        if (rangeX == 0 && rangeY == 0 && rangeZ == 0) {
            if (RayTraceUtils.getGenericTrace((Level)mc.f_91073_, (Entity)mc.f_91074_, (double)6.0) != null) {
                return Printer.doEasyPlaceNormally(mc);
            }
            return InteractionResult.PASS;
        }
        int playerMinX = playerPos.m_123341_() - rangeX;
        int playerMinY = playerPos.m_123342_() - rangeY;
        int playerMinZ = playerPos.m_123343_() - rangeZ;
        int playerMaxX = playerPos.m_123341_() + rangeX;
        int playerMaxY = playerPos.m_123342_() + rangeY;
        int playerMaxZ = playerPos.m_123343_() + rangeZ;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        boolean foundBox = false;
        if (ClearArea) {
            foundBox = true;
            maxX = posX + rangeX;
            maxY = posY + rangeY;
            maxZ = posZ + rangeZ;
            minX = posX - rangeX;
            minY = posY - rangeY;
            minZ = posZ - rangeZ;
        } else {
            List<SchematicPlacement> allPlacements = DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
            System.out.println("[PRINTER-DEBUG] playerPos=" + playerPos.m_123344_() + " allPlacements=" + allPlacements.size());
            for (SchematicPlacement placement : allPlacements) {
                if (!placement.isEnabled()) continue;
                ImmutableMap<String, Box> boxes = placement.getSubRegionBoxes(SubRegionPlacement.RequiredEnabled.PLACEMENT_ENABLED);
                for (Box box : boxes.values()) {
                    int boxXMin = Math.min(box.getPos1().m_123341_(), box.getPos2().m_123341_());
                    int boxYMin = Math.min(box.getPos1().m_123342_(), box.getPos2().m_123342_());
                    int boxZMin = Math.min(box.getPos1().m_123343_(), box.getPos2().m_123343_());
                    int boxXMax = Math.max(box.getPos1().m_123341_(), box.getPos2().m_123341_());
                    int boxYMax = Math.max(box.getPos1().m_123342_(), box.getPos2().m_123342_());
                    int boxZMax = Math.max(box.getPos1().m_123343_(), box.getPos2().m_123343_());
                    if (boxXMax < playerMinX || boxXMin > playerMaxX || boxYMax < playerMinY || boxYMin > playerMaxY || boxZMax < playerMinZ || boxZMin > playerMaxZ) continue;
                    minX = Math.min(minX, boxXMin);
                    maxX = Math.max(maxX, boxXMax);
                    minY = Math.min(minY, boxYMin);
                    maxY = Math.max(maxY, boxYMax);
                    minZ = Math.min(minZ, boxZMin);
                    maxZ = Math.max(maxZ, boxZMax);
                    foundBox = true;
                }
            }
        }
        if (!foundBox) {
            System.out.println("[PRINTER-DEBUG] Early return: no schematic box found in player range");
            if (LitematicaMixinMod.BEDROCK_BREAKING.getBooleanValue()) {
                BedrockBreaker.scheduledTickHandler(mc, null);
            }
            return InteractionResult.PASS;
        }
        LayerRange range = DataManager.getRenderLayerRange();
        int MaxReach = Math.max(Math.max(rangeX, rangeY), rangeZ);
        boolean breakBlocks = LitematicaMixinMod.PRINTER_BREAK_BLOCKS.getBooleanValue();
        boolean Flippincactus = LitematicaMixinMod.FLIPPIN_CACTUS.getBooleanValue();
        boolean ExplicitObserver = LitematicaMixinMod.PRINTER_OBSERVER_AVOID_ALL.getBooleanValue();
        ItemStack Mainhandstack = mc.f_91074_.m_21205_();
        boolean Cactus = Mainhandstack.m_41720_().m_5524_().contains("cactus") && Flippincactus;
        boolean MaxFlip = Flippincactus && Cactus;
        boolean smartRedstone = LitematicaMixinMod.PRINTER_SMART_REDSTONE_AVOID.getBooleanValue();
        Direction[] facingSides = Direction.m_122382_((Entity)mc.f_91074_);
        Direction horizontalFacing = primaryFacing = facingSides[0];
        int index = 0;
        while (horizontalFacing.m_122434_() == Direction.Axis.Y && index < facingSides.length) {
            horizontalFacing = facingSides[index++];
        }
        WorldSchematic world = SchematicWorldHandler.getSchematicWorld();
        int maxInteract = LitematicaMixinMod.PRINTER_MAX_BLOCKS.getIntegerValue();
        int interact = 0;
        int fromX = Math.max((int)mc.f_91074_.m_20185_() - rangeX, minX);
        int fromY = Math.max((int)mc.f_91074_.m_20186_() - rangeY, minY);
        int fromZ = Math.max((int)mc.f_91074_.m_20189_() - rangeZ, minZ);
        int toX = Math.min((int)mc.f_91074_.m_20185_() + rangeX, maxX);
        int toY = Math.min((int)mc.f_91074_.m_20186_() + rangeY, maxY);
        int toZ = Math.min((int)mc.f_91074_.m_20189_() + rangeZ, maxZ);
        toY = Math.max(Math.min(toY, worldTopY), worldBottomY);
        fromY = Math.max(Math.min(fromY, worldTopY), worldBottomY);
        System.out.println("[PRINTER-DEBUG] Scan range: from=[" + fromX + "," + fromY + "," + fromZ + "] to=[" + toX + "," + toY + "," + toZ + "] box=[" + minX + "," + minY + "," + minZ + "]-[" + maxX + "," + maxY + "," + maxZ + "] maxInteract=" + maxInteract);
        for (int y = fromY; y <= toY; ++y) {
            for (int x = fromX; x <= toX; ++x) {
                for (int z = fromZ; z <= toZ; ++z) {
                    BlockHitResult hitResult;
                    double dz;
                    double dy;
                    if (interact >= maxInteract) {
                        if (shouldSleepLonger) {
                            shouldSleepLonger = false;
                            lastPlaced = Math.max(lastPlaced, new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
                        } else {
                            lastPlaced = Math.max(lastPlaced, new Date().getTime());
                        }
                        return InteractionResult.SUCCESS;
                    }
                    if (FakeAccurateBlockPlacement.emptyWaitingQueue()) {
                        ++interact;
                    }
                    if (FakeAccurateBlockPlacement.shouldReturnValue) {
                        FakeAccurateBlockPlacement.shouldReturnValue = false;
                        return InteractionResult.SUCCESS;
                    }
                    double dx = mc.f_91074_.m_20185_() - (double)x - 0.5;
                    if (dx * dx + (dy = mc.f_91074_.m_20186_() - (double)y - 0.5) * dy + (dz = mc.f_91074_.m_20189_() - (double)z - 0.5) * dz > (double)(MaxReach * MaxReach)) continue;
                    BlockPos pos = new BlockPos(x, y, z);
                    if (InventoryUtils.hasItemInSchematic((Level)world, pos)) {
                        MessageHolder.sendUniqueMessageAlways("Inventory in " + pos.m_123344_() + " has Item inside!");
                    }
                    Printer.updateSignText(mc, (Level)world, pos);
                    BlockState stateSchematic = world.m_8055_(pos);
                    BlockState stateClient = mc.f_91073_.m_8055_(pos);
                    // Intercept extra blocks early (must run before the PRINTER_BREAK_IGNORE_EXTRA continue below)
                    if (!ClearArea && LitematicaMixinMod.PRINTER_BREAK_EXTRA_BLOCKS.getBooleanValue() && stateSchematic.m_60795_() && !stateClient.m_60795_() && !stateClient.m_247087_()) {
                        float hardness = stateClient.m_60800_((BlockGetter)mc.f_91073_, pos);
                        if (hardness >= 0.0f) {
                            if (hardness == 0.0f) {
                                mc.f_91072_.m_105269_(pos, Direction.DOWN);
                                ++interact;
                                if (interact >= maxInteract) {
                                    if (shouldSleepLonger) {
                                        shouldSleepLonger = false;
                                        lastPlaced = Math.max(lastPlaced, new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
                                    } else {
                                        lastPlaced = Math.max(lastPlaced, new Date().getTime());
                                    }
                                    return InteractionResult.SUCCESS;
                                }
                                continue;
                            }
                            // Hard block: start one continuous dig only while Breaker is idle (startDestroyBlock every tick resets progress so the block never breaks)
                            if (!breaker.isBreakingBlock()) {
                                breaker.startBreakingBlock(pos, mc);
                                ++interact;
                                if (interact >= maxInteract) {
                                    if (shouldSleepLonger) {
                                        shouldSleepLonger = false;
                                        lastPlaced = Math.max(lastPlaced, new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
                                    } else {
                                        lastPlaced = Math.max(lastPlaced, new Date().getTime());
                                    }
                                    return InteractionResult.SUCCESS;
                                }
                            }
                            continue;
                        }
                    }
                    if (!breakBlocks && !ClearArea && !Flippincactus && !LitematicaMixinMod.BEDROCK_BREAKING.getBooleanValue() && (world.m_46859_(pos) || world.m_8055_(pos) == mc.f_91073_.m_8055_(pos) || Printer.blocksMatch(world.m_8055_(pos).m_60734_(), mc.f_91073_.m_8055_(pos).m_60734_())) || breakBlocks && LitematicaMixinMod.PRINTER_BREAK_IGNORE_EXTRA.getBooleanValue() && world.m_46859_(pos)) continue;
                    if (!ClearArea) {
                        if (!range.isPositionWithinRange(pos)) continue;
                        if (!(!breakBlocks || stateSchematic == null || stateClient.m_60734_() instanceof SnowLayerBlock || stateClient.m_60795_() || stateClient.m_60713_(Blocks.f_49990_) || stateClient.m_60713_(Blocks.f_49991_) || stateClient.m_60713_(Blocks.f_50628_) || stateClient.m_60713_(Blocks.f_50040_) || stateClient.m_60713_(Blocks.f_50110_) || Printer.blocksMatch(stateSchematic.m_60734_(), stateClient.m_60734_()) && (!(stateClient.m_60734_() instanceof SlabBlock) || !(stateSchematic.m_60734_() instanceof SlabBlock) || stateClient.m_61143_((Property)SlabBlock.f_56353_) == stateSchematic.m_61143_((Property)SlabBlock.f_56353_) || !(dx * dx + Math.pow(dy + 1.5, 2.0) + dz * dz <= (double)(MaxReach * MaxReach))))) {
                            if (mc.f_91074_.m_150110_().f_35937_) {
                                mc.f_91072_.m_105269_(pos, Direction.DOWN);
                                if (++interact >= maxInteract) {
                                    if (shouldSleepLonger) {
                                        shouldSleepLonger = false;
                                        lastPlaced = Math.max(lastPlaced, new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
                                    } else {
                                        lastPlaced = Math.max(lastPlaced, new Date().getTime());
                                    }
                                    return InteractionResult.SUCCESS;
                                }
                            } else {
                                if (BedrockBreaker.isBlockNotInstantBreakable(stateClient.m_60734_()) && LitematicaMixinMod.BEDROCK_BREAKING.getBooleanValue()) {
                                    mc.f_91074_.m_5661_(Component.m_130674_((String)"Handling printerBedrockBreaking!"), true);
                                    interact += BedrockBreaker.scheduledTickHandler(mc, pos);
                                    continue;
                                }
                                if (LitematicaMixinMod.BEDROCK_BREAKING.getBooleanValue()) {
                                    mc.f_91074_.m_5661_(Component.m_130674_((String)"Handling printerBedrockBreaking!"), true);
                                    interact += BedrockBreaker.scheduledTickHandler(mc, null);
                                    continue;
                                }
                                if (!positionStorage.hasPos(pos)) {
                                    boolean replaceable = mc.f_91073_.m_8055_(pos).m_247087_();
                                    if (!replaceable && mc.f_91073_.m_8055_(pos).m_60800_((BlockGetter)world, pos) == -1.0f) continue;
                                    if (replaceable || mc.f_91073_.m_8055_(pos).m_60800_((BlockGetter)world, pos) == 0.0f) {
                                        mc.f_91072_.m_105269_(pos, Direction.DOWN);
                                        return InteractionResult.SUCCESS;
                                    }
                                    if (replaceable || !breaker.startBreakingBlock(pos, mc)) continue;
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                        if (MaxFlip || Printer.printerCheckCancel(stateSchematic, stateClient)) {
                            Block sBlock;
                            if (!(MaxFlip || stateClient.m_60795_() || mc.f_91074_.m_6144_() || Printer.isPositionCached(pos, true))) {
                                Direction facingClient;
                                Direction facingSchematic;
                                Block cBlock = stateClient.m_60734_();
                                sBlock = stateSchematic.m_60734_();
                                // Correct block type but a fundamentally wrong orientation/half (a stair facing the
                                // wrong way, a slab placed on the top half instead of the bottom half, ...).
                                // Right clicking cannot fix these, and unlike connection driven states
                                // (fence / pane / wall / mushroom) they never settle once the neighbours are built,
                                // so break the block and let the printer place it again with the correct state.
                                if (breakBlocks && !ClearArea && Printer.hasDirectionalStateError(stateSchematic, stateClient)) {
                                    float wrongStateHardness = stateClient.m_60800_((BlockGetter)mc.f_91073_, pos);
                                    if (wrongStateHardness >= 0.0f) {
                                        if (wrongStateHardness == 0.0f) {
                                            mc.f_91072_.m_105269_(pos, Direction.DOWN);
                                        } else if (!Printer.breaker.isBreakingBlock()) {
                                            Printer.breaker.startBreakingBlock(pos, mc);
                                        }
                                        // Throttle: without this the printer would re-break the very same spot
                                        // every tick while the breaker is still working on it.
                                        Printer.cacheEasyPlacePosition(pos, true);
                                        return InteractionResult.SUCCESS;
                                    }
                                }
                                if (!Printer.blocksMatch(sBlock, cBlock) || (facingSchematic = getFacingValueQuietly((BlockState)stateSchematic)) != (facingClient = getFacingValueQuietly((BlockState)stateClient))) continue;
                                int clickTimes = 0;
                                Direction side = Direction.NORTH;
                                if (sBlock instanceof RepeaterBlock && !LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
                                    int schematicDelay;
                                    int clientDelay = (Integer)stateClient.m_61143_((Property)RepeaterBlock.f_55798_);
                                    if (clientDelay != (schematicDelay = ((Integer)stateSchematic.m_61143_((Property)RepeaterBlock.f_55798_)).intValue())) {
                                        if (clientDelay < schematicDelay) {
                                            clickTimes = schematicDelay - clientDelay;
                                        } else if (clientDelay > schematicDelay) {
                                            clickTimes = schematicDelay + (4 - clientDelay);
                                        }
                                    }
                                    side = Direction.UP;
                                } else if (sBlock instanceof ComparatorBlock && !LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
                                    if (stateSchematic.m_61143_((Property)ComparatorBlock.f_51854_) != stateClient.m_61143_((Property)ComparatorBlock.f_51854_)) {
                                        clickTimes = 1;
                                    }
                                    side = Direction.UP;
                                } else if (sBlock instanceof LeverBlock) {
                                    if (stateSchematic.m_61143_((Property)LeverBlock.f_54622_) != stateClient.m_61143_((Property)LeverBlock.f_54622_)) {
                                        clickTimes = 1;
                                    }
                                    side = stateClient.m_61143_((Property)LeverBlock.f_53179_) == AttachFace.CEILING ? Direction.DOWN : (stateClient.m_61143_((Property)LeverBlock.f_53179_) == AttachFace.FLOOR ? Direction.UP : (Direction)stateClient.m_61143_((Property)LeverBlock.f_54117_));
                                } else if (sBlock instanceof TrapDoorBlock) {
                                    if (!stateSchematic.m_60713_(Blocks.f_50376_) && stateSchematic.m_61143_((Property)TrapDoorBlock.f_57514_) != stateClient.m_61143_((Property)TrapDoorBlock.f_57514_)) {
                                        clickTimes = 1;
                                    }
                                } else if (sBlock instanceof FenceGateBlock) {
                                    if (stateSchematic.m_61143_((Property)FenceGateBlock.f_53341_) != stateClient.m_61143_((Property)FenceGateBlock.f_53341_)) {
                                        clickTimes = 1;
                                    }
                                } else if (sBlock instanceof DoorBlock) {
                                    if (!stateClient.m_60713_(Blocks.f_50166_) && stateSchematic.m_61143_((Property)DoorBlock.f_52727_) != stateClient.m_61143_((Property)DoorBlock.f_52727_)) {
                                        clickTimes = 1;
                                    }
                                } else if (sBlock instanceof NoteBlock) {
                                    int targetNote;
                                    int note = (Integer)stateClient.m_61143_((Property)NoteBlock.f_55013_);
                                    if (note != (targetNote = ((Integer)stateSchematic.m_61143_((Property)NoteBlock.f_55013_)).intValue())) {
                                        if (note < targetNote) {
                                            clickTimes = targetNote - note;
                                        } else if (note > targetNote) {
                                            clickTimes = targetNote + (25 - note);
                                        }
                                    }
                                } else if (sBlock instanceof ComposterBlock && FillInventory) {
                                    int Schematiclevel;
                                    if (!FakeAccurateBlockPlacement.canHandleOther(composableItem.m_41720_())) continue;
                                    int level = (Integer)stateClient.m_61143_((Property)ComposterBlock.f_51913_);
                                    if (level != (Schematiclevel = ((Integer)stateSchematic.m_61143_((Property)ComposterBlock.f_51913_)).intValue()) && (level != 7 || Schematiclevel != 8)) {
                                        InteractionHand hand = InteractionHand.MAIN_HAND;
                                        if (InventoryUtils.swapToItem(mc, composableItem)) {
                                            Vec3 hitPos = new Vec3((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5);
                                            BlockHitResult hitResult2 = new BlockHitResult(hitPos, side, pos, false);
                                            mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult2);
                                            InventoryUtils.decrementCount(isCreative);
                                            Printer.cacheEasyPlacePosition(pos, false);
                                            if (shouldSleepLonger) {
                                                shouldSleepLonger = false;
                                                lastPlaced = Math.max(lastPlaced, new Date().getTime() + 200L + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
                                            } else {
                                                lastPlaced = Math.max(lastPlaced, new Date().getTime() + 200L);
                                            }
                                            return InteractionResult.SUCCESS;
                                        }
                                    } else {
                                        Printer.cacheEasyPlacePosition(pos, true);
                                    }
                                } else if (!Printer.isPositionCached(pos, false) && LitematicaMixinMod.PRINTER_PLACE_MINECART.getBooleanValue() && sBlock instanceof DetectorRailBlock && cBlock instanceof DetectorRailBlock && !Printer.shouldAvoidPlaceCart(pos, (Level)world) && Printer.placeCart(stateSchematic, mc, pos)) continue;
                                for (int i = 0; i < clickTimes; ++i) {
                                    InteractionHand hand = InteractionHand.MAIN_HAND;
                                    Vec3 hitPos = Vec3.m_82512_((Vec3i)pos);
                                    hitResult = new BlockHitResult(hitPos, side, pos, false);
                                    mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult);
                                    ++interact;
                                }
                                if (clickTimes <= 0) continue;
                                Printer.cacheEasyPlacePosition(pos, true, 3600);
                                continue;
                            }
                            if (ClearArea || !MaxFlip) continue;
                            Block cBlock = stateClient.m_60734_();
                            sBlock = stateSchematic.m_60734_();
                            if (!Printer.blocksMatch(sBlock, cBlock)) continue;
                            boolean ShapeBoolean = false;
                            boolean ShouldFix = false;
                            if (sBlock instanceof BaseRailBlock) {
                                if (sBlock instanceof RailBlock) {
                                    String SchematicRailShape = ((RailShape)stateSchematic.m_61143_((Property)RailBlock.f_55392_)).toString();
                                    String ClientRailShape;
                                    ShouldFix = !Objects.equals(SchematicRailShape, ClientRailShape = ((RailShape)stateClient.m_61143_((Property)RailBlock.f_55392_)).toString());
                                    ShapeBoolean = !(Objects.equals(SchematicRailShape, ClientRailShape) || (!Objects.equals(SchematicRailShape, "south_west") && !Objects.equals(SchematicRailShape, "north_west") && !Objects.equals(SchematicRailShape, "south_east") && !Objects.equals(SchematicRailShape, "north_east") || !Objects.equals(ClientRailShape, "south_west") && !Objects.equals(ClientRailShape, "north_west") && !Objects.equals(ClientRailShape, "south_east") && !Objects.equals(ClientRailShape, "north_east")) && (!Objects.equals(SchematicRailShape, "east_west") && !Objects.equals(SchematicRailShape, "north_south") || !Objects.equals(ClientRailShape, "east_west") && !Objects.equals(ClientRailShape, "north_south")));
                                } else {
                                    String SchematicRailShape = ((RailShape)stateSchematic.m_61143_((Property)PoweredRailBlock.f_55214_)).toString();
                                    String ClientRailShape;
                                    ShouldFix = !Objects.equals(SchematicRailShape, ClientRailShape = ((RailShape)stateClient.m_61143_((Property)PoweredRailBlock.f_55214_)).toString());
                                    ShapeBoolean = !(Objects.equals(SchematicRailShape, ClientRailShape) || !Objects.equals(SchematicRailShape, "east_west") && !Objects.equals(SchematicRailShape, "north_south") || !Objects.equals(ClientRailShape, "east_west") && !Objects.equals(ClientRailShape, "north_south"));
                                }
                            } else if (sBlock instanceof ObserverBlock || sBlock instanceof PistonBaseBlock || sBlock instanceof RepeaterBlock || sBlock instanceof ComparatorBlock || sBlock instanceof FenceGateBlock || sBlock instanceof TrapDoorBlock) {
                                Direction facingClient;
                                Direction facingSchematic = getFacingValueQuietly((BlockState)stateSchematic);
                                ShouldFix = facingSchematic != (facingClient = getFacingValueQuietly((BlockState)stateClient));
                                ShapeBoolean = facingClient.m_122424_().equals((Object)facingSchematic);
                            }
                            Direction side = Direction.UP;
                            if (ShapeBoolean) {
                                InteractionHand hand = InteractionHand.MAIN_HAND;
                                Vec3 hitPos = Vec3.m_82512_((Vec3i)pos);
                                BlockHitResult hitResult3 = new BlockHitResult(hitPos, side, pos, false);
                                mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult3);
                                Printer.cacheEasyPlacePosition(pos, true);
                                ++interact;
                                continue;
                            }
                            if (!breakBlocks || !ShouldFix) continue;
                            mc.f_91072_.m_105269_(pos, Direction.DOWN);
                            breaker.startBreakingBlock(pos, mc);
                            return InteractionResult.SUCCESS;
                        }
                    }
                    if (!ClearArea && MaxFlip) {
                        mc.f_91074_.m_5661_(Component.m_130674_((String)"Handling printerFlippinCactus!"), true);
                        continue;
                    }
                    if (Printer.isPositionCached(pos, false) || LitematicaMixinMod.BEDROCK_BREAKING.getBooleanValue() || !(stateSchematic.m_60734_() instanceof NetherPortalBlock) && stateSchematic.m_60795_() && !ClearArea) continue;
                    ItemStack stack = MaterialCache.getInstance().getRequiredBuildItemForState(stateSchematic);
                    Block cBlock = stateClient.m_60734_();
                    Block sBlock = stateSchematic.m_60734_();
                    if (ClearArea) {
                        mc.f_91074_.m_5661_(Component.m_130674_((String)"Handling printerClear*!"), true);
                        if (Printer.isReplaceableWaterFluidSource(stateClient)) {
                            stack = !UseCobble ? Items.f_41902_.m_7968_() : Items.f_42594_.m_7968_();
                        } else if (stateClient.m_60819_().m_76152_() instanceof LavaFluid && stateClient.m_61138_((Property)LiquidBlock.f_54688_) && (Integer)stateClient.m_61143_((Property)LiquidBlock.f_54688_) == 0) {
                            stack = !UseCobble ? Items.f_42204_.m_7968_() : Items.f_42594_.m_7968_();
                        } else {
                            if (!ClearSnow || !(cBlock instanceof SnowLayerBlock)) continue;
                            stack = Items.f_42401_.m_7968_();
                        }
                    }
                    if (ClearArea) {
                        InteractionHand hand = InteractionHand.MAIN_HAND;
                        if (!ClearArea || !InventoryUtils.swapToItem(mc, stack)) continue;
                        Vec3 hitPos = Vec3.m_82512_((Vec3i)pos).m_82520_(0.0, 0.5, 0.0);
                        BlockHitResult hitResult4 = new BlockHitResult(hitPos, Direction.UP, pos, false);
                        mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult4);
                        InventoryUtils.decrementCount(isCreative);
                        ++interact;
                        Printer.cacheEasyPlacePosition(pos, false);
                        Printer.sleepWhenRequired(mc);
                        if (!Printer.isReplaceableFluidSource(stateClient) && !(cBlock instanceof SnowLayerBlock)) continue;
                        lastPlaced = new Date().getTime() + 200L;
                        continue;
                    }
                    if (!FakeAccurateBlockPlacement.canPlace(stateSchematic, pos) || sBlock instanceof PistonHeadBlock || stateSchematic.m_60713_(Blocks.f_50110_)) continue;
                    if (stateSchematic == stateClient || BlockReplacer.clientMatchesReplacement(stateSchematic, stateClient)) {
                        causeMap.remove(pos.m_121878_());
                        continue;
                    }
                    if (cBlock != sBlock && !stateClient.m_247087_()) {
                        MessageHolder.sendUniqueMessage(mc.f_91074_, sBlock.m_7705_() + " at " + pos.m_123344_() + " is blocking placement of " + cBlock.m_7705_() + "!!");
                        // With break mode on, a different-type wrong block occupying a schematic position is
                        // broken here so the correct block can be placed on a later pass; otherwise falling
                        // blocks above it wait forever for support that never appears
                        if (breakBlocks && !ClearArea) {
                            float blockerHardness = stateClient.m_60800_(mc.f_91073_, pos);
                            if (blockerHardness >= 0.0f) {
                                if (blockerHardness == 0.0f) {
                                    mc.f_91072_.m_105269_(pos, Direction.DOWN);
                                } else if (!Printer.breaker.isBreakingBlock()) {
                                    Printer.breaker.startBreakingBlock(pos, mc);
                                }
                            }
                        }
                        continue;
                    }
                    // Huge mushroom blocks (red/brown): the 6 face flags are booleans driven by adjacent "same-type" blocks;
                    // the server's getStateForPlacement ignores the clicked face/player orientation, so fake rotation cannot set their state.
                    // When a same-type neighbor is placed, updateShape automatically flips the shared face to false (skin), so the state converges once neighbors are filled:
                    //  - Same type, state temporarily mismatched: do not re-useItemOn (it disturbs convergence or misplaces); just wait for the neighbors.
                    //  - Only irreversible case: an extra same-type neighbor in the world keeps a face that should be true (pore) stuck at false (skin).
                    //    updateShape only flips faces to false, never back to true; the block must be broken and re-placed once the extra block is removed.
                    if (sBlock instanceof HugeMushroomBlock && cBlock == sBlock) {
                        if (Printer.mushroomBlockHasStaleSkin(stateSchematic, stateClient, (Level) world, pos)) {
                            float mushroomHardness = stateClient.m_60800_(mc.f_91073_, pos);
                            if (mushroomHardness == 0.0f) {
                                mc.f_91072_.m_105269_(pos, Direction.DOWN);
                            } else if (!Printer.breaker.isBreakingBlock()) {
                                Printer.breaker.startBreakingBlock(pos, mc);
                            }
                        }
                        continue;
                    }
                    // m_247087_ is also true for replaceable blocks (grass/fern/snow layers), so filter again with FluidState
                    if (!stateSchematic.m_60795_() && stateClient.m_247087_() && !stateClient.m_60819_().m_76178_() && LitematicaMixinMod.PRINTER_CLEAR_FLUIDS_AUTOMATICALLY.getBooleanValue()) {
                        ItemStack[] candidates = new ItemStack[]{new ItemStack(Items.f_42594_), new ItemStack(Items.f_41901_), new ItemStack(Items.f_42116_)};
                        for (ItemStack candidate : candidates) {
                            if (InventoryUtils.swapToItem(mc, candidate)) {
                                Vec3 hitPos = Vec3.m_82512_((Vec3i)pos).m_82520_(0.5, 0.5, 0.5);
                                BlockHitResult hitResult6 = new BlockHitResult(hitPos, Direction.UP, pos, false);
                                mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, hitResult6);
                                ++interact;
                                if (interact >= maxInteract) {
                                    if (shouldSleepLonger) {
                                        shouldSleepLonger = false;
                                        lastPlaced = Math.max(lastPlaced, new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
                                    } else {
                                        lastPlaced = Math.max(lastPlaced, new Date().getTime());
                                    }
                                    return InteractionResult.SUCCESS;
                                }
                                break;
                            }
                        }
                        continue;
                    }
                    if (Printer.canPickBlock(mc, stateSchematic, pos)) {
                        Direction facing;
                        if (Printer.willFall(stateSchematic, (Level)mc.f_91073_, pos)) {
                            Printer.recordCause(pos, stateSchematic.m_60734_().m_7705_() + " at " + pos.m_123344_() + " is Falling block", pos.m_7495_());
                            MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                            continue;
                        }
                        if (!LitematicaMixinMod.PRINTER_PLACE_ICE.getBooleanValue() && stateSchematic.m_60713_(Blocks.f_49990_)) {
                            Printer.recordCause(pos, stateSchematic.m_60734_().m_7705_() + " at " + pos.m_123344_() + " is water");
                            MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                            continue;
                        }
                        if (!LitematicaMixinMod.PRINTER_PLACE_ICE.getBooleanValue() && stateSchematic.m_60713_(Blocks.f_49991_)) {
                            Printer.recordCause(pos, stateSchematic.m_60734_().m_7705_() + " at " + pos.m_123344_() + " is lava");
                            MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                            continue;
                        }
                        if (sBlock instanceof SandBlock || sBlock instanceof DragonEggBlock || sBlock instanceof ConcretePowderBlock || sBlock instanceof GravelBlock || sBlock instanceof AnvilBlock) {
                            BlockPos Offsetpos = new BlockPos(x, y - 1, z);
                            BlockState OffsetstateSchematic = world.m_8055_(Offsetpos);
                            BlockState OffsetstateClient = mc.f_91073_.m_8055_(Offsetpos);
                            // A replaced block (e.g. waxed copper) sitting below is valid support even though
                            // its loot table differs from the schematic block it replaces
                            boolean belowLootMatches = OffsetstateClient.m_60734_().m_49954_().equals((Object)OffsetstateSchematic.m_60734_().m_49954_())
                                    || BlockReplacer.clientMatchesReplacement(OffsetstateSchematic, OffsetstateClient);
                            if (OffsetstateClient.m_60795_() || breakBlocks && !belowLootMatches) {
                                // Distinguish "waiting for the block below to be printed" from "the schematic itself
                                // floats this falling block" (vanilla survival cannot place the latter at all)
                                String fallExtra = OffsetstateSchematic.m_60795_() ? " (no schematic support below, needs manual placement)" : "";
                                Printer.recordCause(pos, stateSchematic.m_60734_().m_7705_() + " at " + pos.m_123344_() + " is Falling block" + fallExtra, pos.m_7495_());
                                MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                continue;
                            }
                        }
                        if (smartRedstone) {
                            if (sBlock instanceof PoweredBlock) {
                                if (Printer.isQCable(mc, (Level)world, pos)) {
                                    Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + "will QC, waiting other block");
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                    continue;
                                }
                            } else if (sBlock instanceof TntBlock) {
                                if (mc.f_91073_.m_276867_(pos)) {
                                    Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + " is now receiving power!");
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                    continue;
                                }
                            } else if (sBlock instanceof PistonBaseBlock) {
                                if (!Printer.shouldExtendQC(mc, (Level)world, pos)) {
                                    Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + " is QC");
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                    continue;
                                }
                                if (Printer.hasNearbyRedirectDust(mc, (Level)world, pos)) {
                                    Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + " has redirectable dust nearby at " + Printer.hasNearbyRedirectDustPos(mc, (Level)world, pos).m_123344_());
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                    continue;
                                }
                                if (Printer.cantAvoidExtend((Level)mc.f_91073_, pos, (Level)world)) {
                                    Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + " will unexpectedly extend");
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                    continue;
                                }
                                if (Printer.shouldSuppressExtend((Level)world, pos) && Printer.hasWrongStateNearby(mc, (Level)world, pos)) {
                                    Printer.recordCause(pos, sBlock.m_7705_() + " at  is BUD but has wrong state nearby \n" + Printer.hasWrongStateNearbyReason(mc, (Level)world, pos), Printer.hasWrongStateNearbyPos(mc, (Level)world, pos));
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                    continue;
                                }
                                if (Printer.willExtendInWorld((Level)world, pos, (Direction)stateSchematic.m_61143_((Property)PistonBaseBlock.f_52588_)) != (Boolean)stateSchematic.m_61143_((Property)PistonBaseBlock.f_60153_) && Printer.directlyPowered((Level)world, pos, (Direction)stateSchematic.m_61143_((Property)PistonBaseBlock.f_52588_))) {
                                    if (LitematicaMixinMod.PRINTER_SUPPRESS_PUSH_LIMIT.getBooleanValue()) {
                                        Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + " should respect push limit because its directly powered");
                                        MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                        continue;
                                    }
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, sBlock.m_7705_() + " at  is placed ignoring push limit checks, check printerSuppressPushLimitPistons option.");
                                }
                            } else if (sBlock instanceof ObserverBlock && Printer.ObserverUpdateOrder(mc, (Level)world, pos, null)) {
                                if (LitematicaMixinMod.FLIPPIN_CACTUS.getBooleanValue() && Printer.canBypass(mc, (Level)world, pos)) {
                                    stateSchematic = (BlockState)stateSchematic.m_61124_((Property)ObserverBlock.f_52588_, (Comparable)((Direction)stateSchematic.m_61143_((Property)ObserverBlock.f_52588_)).m_122424_());
                                } else {
                                    BlockPos causedPos = Printer.ObserverUpdateOrderPos(mc, (Level)world, pos);
                                    if (causedPos.m_121878_() == pos.m_121878_()) {
                                        MessageHolder.sendUniqueMessage(mc.f_91074_, "Observer at " + pos.m_123344_() + " is causing self-blocking, check manually");
                                    }
                                    Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + " is waiting for ", causedPos);
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                    continue;
                                }
                            }
                        }
                        if (smartRedstone && ExplicitObserver) {
                            Map.Entry<Boolean, BlockPos> value;
                            BlockPos observerPos = Printer.isObserverCantAvoidOutput(mc, (Level)world, pos);
                            if (observerPos != null) {
                                Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + " is waiting for preceded observer at " + observerPos.m_123344_(), observerPos);
                                MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                continue;
                            }
                            if (sBlock instanceof ObserverBlock && !(value = Printer.isWatchingCorrectState(mc, (Level)world, pos, null, true)).getKey().booleanValue()) {
                                Printer.recordCause(pos, sBlock.m_7705_() + " at " + pos.m_123344_() + " can't be placed due to " + value.getValue().m_123344_(), value.getValue());
                                MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                continue;
                            }
                        }
                        if (sBlock instanceof NetherPortalBlock && !sBlock.m_49954_().equals((Object)cBlock.m_49954_()) && PortalShape.m_77708_((LevelAccessor)mc.f_91073_, (BlockPos)pos, (Direction.Axis)Direction.Axis.X).isPresent()) {
                            ItemStack lightStack = Items.f_42613_.m_7968_();
                            if (mc.f_91074_.m_150109_().m_36030_(lightStack) == -1) {
                                lightStack = Items.f_42409_.m_7968_();
                            }
                            InteractionHand hand = InteractionHand.MAIN_HAND;
                            BlockPos offsetPos = new BlockPos(x, y - 1, z);
                            BlockState offsetStateSchematic = world.m_8055_(offsetPos);
                            BlockState offsetStateClient = mc.f_91073_.m_8055_(offsetPos);
                            if (mc.f_91074_.m_150109_().m_36030_(lightStack) == -1 || offsetStateClient.m_60795_() || !offsetStateClient.m_60734_().m_49954_().equals((Object)offsetStateSchematic.m_60734_().m_49954_())) continue;
                            if (Printer.doSchematicWorldPickBlock(mc, lightStack)) {
                                Vec3 hitPos = Vec3.m_82512_((Vec3i)new BlockPos(x, y - 1, z)).m_82520_(0.0, 0.5, 0.0);
                                hitResult = new BlockHitResult(hitPos, Direction.UP, new BlockPos(x, y - 1, z), false);
                                mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult);
                                Printer.cacheEasyPlacePosition(pos, false);
                                Printer.sleepWhenRequired(mc);
                                if (shouldSleepLonger) {
                                    shouldSleepLonger = false;
                                    lastPlaced = Math.max(lastPlaced, new Date().getTime() + 200L + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
                                } else {
                                    lastPlaced = Math.max(lastPlaced, new Date().getTime() + 200L);
                                }
                                ++interact;
                            }
                        }
                        if ((facing = getFacingValueQuietly((BlockState)stateSchematic)) != null) {
                            facing = facing.m_122424_();
                        }
                        if (stateSchematic.m_60734_() instanceof BaseRailBlock) {
                            facing = Printer.convertRailShapetoFace(stateSchematic);
                        }
                        if (facing != null) {
                            FacingData facedata = FacingData.getFacingData(stateSchematic);
                            if (facedata == null && !(stateSchematic.m_60734_() instanceof BaseRailBlock) && !Printer.simulateFacingData(stateSchematic, pos, Vec3.m_82512_((Vec3i)pos))) {
                                MessageHolder.sendMessageUncheckedUnique(mc.f_91074_, String.valueOf(stateSchematic.m_60734_()) + " does not have facing data, please add this!");
                                if (LitematicaMixinMod.PRINTER_SKIP_UNKNOWN_BLOCKSTATE.getBooleanValue()) continue;
                            }
                            // Rod blocks (end rod / lightning rod) FACING comes from the clicked face (6-way), so the player-facing gate does not apply
                            boolean isBarrel = stateSchematic.m_60734_() instanceof BarrelBlock || stateSchematic.m_60734_() instanceof RodBlock;
                            if (!isBarrel && ((!CanUseProtocol || !Printer.IsBlockSupportedCarpet(stateSchematic.m_60734_()).booleanValue()) && !LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue() && !Printer.canPlaceFace(facedata, stateSchematic, primaryFacing, horizontalFacing) || stateSchematic.m_60734_() instanceof DoorBlock && stateSchematic.m_61143_((Property)DoorBlock.f_52730_) == DoubleBlockHalf.UPPER || stateSchematic.m_60734_() instanceof BedBlock && stateSchematic.m_61143_((Property)BedBlock.f_49440_) == BedPart.HEAD)) continue;
                        }
                        if (stateSchematic.m_60734_() instanceof SignBlock && !(stateSchematic.m_60734_() instanceof WallSignBlock) && (Mth.m_14107_((double)((double)((180.0f + mc.f_91074_.m_146908_()) * 16.0f / 360.0f) + 0.5)) & 0xF) != (Integer)stateSchematic.m_61143_((Property)StandingSignBlock.f_56987_)) continue;
                        Direction sideOrig = Direction.NORTH;
                        BlockPos npos = pos;
                        Direction side = Printer.applyPlacementFacing(stateSchematic, sideOrig, stateClient);
                        Block blockSchematic = stateSchematic.m_60734_();
                        if (LitematicaMixinMod.PRINTER_PLACE_ICE.getBooleanValue() && (Printer.isReplaceableWaterFluidSource(stateSchematic) && stateClient.m_247087_() && !Printer.isReplaceableWaterFluidSource(stateClient) && !stateClient.m_60713_(Blocks.f_49991_) || LitematicaMixinMod.PRINTER_WATERLOGGED_WATER_FIRST.getBooleanValue() && stateClient.m_247087_() && Printer.containsWaterloggable(stateSchematic))) {
                            ItemStack iceStack = Items.f_41980_.m_7968_();
                            if (!FakeAccurateBlockPlacement.canHandleOther(iceStack.m_41720_())) continue;
                            if (InventoryUtils.swapToItem(mc, iceStack)) {
                                mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3((double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_()), Direction.DOWN, pos, false));
                                InventoryUtils.decrementCount(isCreative);
                                Printer.cacheEasyPlacePosition(pos, false);
                                Printer.sleepWhenRequired(mc);
                                ++interact;
                                continue;
                            }
                            Printer.recordCause(pos, "Can't pick item " + Items.f_41980_.m_5524_() + " at " + pos.m_123344_());
                            continue;
                        }
                        if (!Printer.canPickBlock(mc, stateSchematic, pos)) {
                            Printer.recordCause(pos, "Can't pick item " + stateSchematic.m_60734_().m_5456_().m_5524_() + " at " + pos.m_123344_());
                            MessageHolder.sendUniqueMessage(mc.f_91074_, "Can't pick item " + stateSchematic.m_60734_().m_5456_().m_5524_() + " at " + pos.m_123344_());
                            continue;
                        }
                        if (!blockSchematic.m_7898_(stateSchematic, (LevelReader)mc.f_91073_, pos)) {
                            Printer.recordCause(pos, stateSchematic.m_60734_().toString() + "(" + pos.m_123344_() + ", can't be placed)");
                            MessageHolder.sendUniqueMessage(mc.f_91074_, stateSchematic.m_60734_().m_7705_() + " can't be placed at " + pos.m_123344_());
                            continue;
                        }
                        if (blockSchematic instanceof GrindstoneBlock) {
                            Printer.placeGrindStone(stateSchematic, mc, pos);
                            ++interact;
                            continue;
                        }
                        if (blockSchematic instanceof TrapDoorBlock && !CanUseProtocol && !LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue()) {
                            Printer.placeTrapDoor(stateSchematic, mc, pos);
                            ++interact;
                            continue;
                        }
                        if (blockSchematic instanceof EndRodBlock) {
                            if (Printer.placeRod(stateSchematic, mc, pos, isCreative)) ++interact;
                            continue;
                        }
                        if (blockSchematic instanceof FlowerPotBlock) {
                            if (Printer.placePottedPlant((FlowerPotBlock)blockSchematic, stateSchematic, mc, pos, isCreative)) ++interact;
                            continue;
                        }
                        int miliseconds = LitematicaMixinMod.EASY_PLACE_CACHE_TIME.getIntegerValue();
                        if (blockSchematic instanceof FaceAttachedHorizontalDirectionalBlock || blockSchematic instanceof TorchBlock || blockSchematic instanceof WallSkullBlock || blockSchematic instanceof LadderBlock || blockSchematic instanceof TripWireHookBlock || blockSchematic instanceof WallSignBlock || blockSchematic instanceof EndRodBlock || blockSchematic instanceof BaseCoralFanBlock) {
                            if (blockSchematic instanceof ButtonBlock || blockSchematic instanceof LeverBlock) {
                                AttachFace wallMountLocation = (AttachFace)stateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_);
                                npos = wallMountLocation == AttachFace.FLOOR ? pos.m_7495_() : (wallMountLocation == AttachFace.CEILING ? pos.m_7494_() : pos.m_121955_(((Direction)stateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_)).m_122424_().m_122436_()));
                            } else if (blockSchematic instanceof TorchBlock) {
                                npos = blockSchematic instanceof WallTorchBlock || blockSchematic instanceof RedstoneWallTorchBlock ? pos.m_121955_(((Direction)stateSchematic.m_61143_((Property)WallTorchBlock.f_58119_)).m_122424_().m_122436_()) : pos.m_7495_();
                                if (Printer.hasGui(world.m_8055_(npos).m_60734_())) {
                                    if (LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue() && interact < maxInteract) {
                                        if (!FakeAccurateBlockPlacement.request(stateSchematic, pos)) continue;
                                        ++interact;
                                        continue;
                                    }
                                    Printer.recordCause(pos, "Torch at " + pos.m_123344_() + " can't be placed due to " + world.m_8055_(npos).m_60734_().m_7705_() + "at " + npos.m_123344_() + " has GUI");
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, "Torch at " + pos.m_123344_() + " can't be placed due to " + world.m_8055_(npos).m_60734_().m_7705_() + "at " + npos.m_123344_() + " has GUI");
                                    continue;
                                }
                            } else {
                                npos = blockSchematic instanceof BaseCoralFanBlock ? (blockSchematic instanceof BaseCoralWallFanBlock ? pos.m_121955_(((Direction)stateSchematic.m_61143_((Property)BaseCoralWallFanBlock.f_49192_)).m_122424_().m_122436_()) : pos.m_7495_()) : pos.m_121955_(side.m_122424_().m_122436_());
                            }
                            if (!mc.f_91073_.m_8055_(npos).m_247087_()) {
                                Block checkGui = mc.f_91073_.m_8055_(npos).m_60734_();
                                if (!mc.f_91074_.m_36341_() && Printer.hasGui(checkGui)) {
                                    if (blockSchematic instanceof TrapDoorBlock && LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue() && interact < maxInteract) {
                                        if (!FakeAccurateBlockPlacement.request(stateSchematic, pos)) continue;
                                        ++interact;
                                        continue;
                                    }
                                    Printer.recordCause(pos, stateSchematic.m_60734_().m_7705_() + " can't be placed at " + pos.m_123344_() + "because " + npos.m_123344_() + " has GUI");
                                    MessageHolder.sendUniqueMessage(mc.f_91074_, Printer.getReason(pos.m_121878_()));
                                    continue;
                                }
                                if (blockSchematic instanceof TorchBlock) {
                                    Vec3 hitVec;
                                    if (blockSchematic instanceof WallTorchBlock || blockSchematic instanceof RedstoneWallTorchBlock) {
                                        MessageHolder.sendDebugMessage(mc.f_91074_, "placing wall torch clicking " + npos.m_123344_() + " torch facing : " + ((Direction)stateSchematic.m_61143_((Property)WallTorchBlock.f_58119_)).toString());
                                        hitVec = Vec3.m_82512_((Vec3i)npos).m_82549_(Vec3.m_82528_((Vec3i)((Direction)stateSchematic.m_61143_((Property)WallTorchBlock.f_58119_)).m_122436_()).m_82542_(0.5, 0.5, 0.5));
                                        if (stateSchematic.m_61138_((Property)RedstoneTorchBlock.f_55674_) && !((Boolean)stateSchematic.m_61143_((Property)RedstoneTorchBlock.f_55674_)).booleanValue()) {
                                            Printer.cacheEasyPlacePosition(pos.m_7494_(), false, miliseconds);
                                        }
                                        Direction required = (Direction)stateSchematic.m_61143_((Property)WallTorchBlock.f_58119_);
                                        if (!Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                                        Printer.cacheEasyPlacePosition(pos, false);
                                        ++interact;
                                        if (stateSchematic.m_61138_((Property)RedstoneTorchBlock.f_55674_) && !((Boolean)stateSchematic.m_61143_((Property)RedstoneTorchBlock.f_55674_)).booleanValue()) {
                                            Printer.cacheEasyPlacePosition(pos.m_7494_(), false, miliseconds);
                                        }
                                        mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, required, npos, false));
                                        InventoryUtils.decrementCount(isCreative);
                                        Printer.sleepWhenRequired(mc);
                                        continue;
                                    }
                                    hitVec = Vec3.m_82512_((Vec3i)npos).m_82549_(Vec3.m_82528_((Vec3i)Direction.UP.m_122436_()).m_82542_(0.5, 0.5, 0.5));
                                    if (!Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                                    MessageHolder.sendDebugMessage(mc.f_91074_, "Placing torch clicking " + npos.m_123344_());
                                    MessageHolder.sendDebugMessage(mc.f_91074_, "\t Wanted torch pos : " + pos.m_123344_());
                                    MessageHolder.sendDebugMessage(mc.f_91074_, "\t HitVec applied : " + String.valueOf(hitVec));
                                    MessageHolder.sendDebugMessage(mc.f_91074_, "\t Side applied : " + String.valueOf(Direction.UP));
                                    Printer.cacheEasyPlacePosition(pos, false);
                                    ++interact;
                                    if (stateSchematic.m_61138_((Property)RedstoneTorchBlock.f_55674_) && !((Boolean)stateSchematic.m_61143_((Property)RedstoneTorchBlock.f_55674_)).booleanValue()) {
                                        Printer.cacheEasyPlacePosition(pos.m_7494_(), false, miliseconds);
                                    }
                                    mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, Direction.UP, npos, false));
                                    InventoryUtils.decrementCount(isCreative);
                                    Printer.sleepWhenRequired(mc);
                                    continue;
                                }
                                if (Printer.canPlaceFace(FacingData.getFacingData(stateSchematic), stateSchematic, primaryFacing, horizontalFacing)) {
                                    Direction required = getFacingValueQuietly((BlockState)stateSchematic);
                                    required = Printer.applyPlacementFacing(stateSchematic, required, stateClient);
                                    Vec3 hitVec = Printer.applyHitVec(npos, stateSchematic, required);
                                    if (!Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                                    Printer.cacheEasyPlacePosition(pos, false);
                                    ++interact;
                                    if (stateSchematic.m_61138_((Property)RedstoneTorchBlock.f_55674_) && !((Boolean)stateSchematic.m_61143_((Property)RedstoneTorchBlock.f_55674_)).booleanValue()) {
                                        Printer.cacheEasyPlacePosition(pos.m_7494_(), false, 700);
                                    }
                                    mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, required, npos, false));
                                    InventoryUtils.decrementCount(isCreative);
                                    Printer.sleepWhenRequired(mc);
                                    continue;
                                }
                            } else if (blockSchematic instanceof TrapDoorBlock) {
                                Direction trapdoor = (Direction)stateSchematic.m_61143_((Property)TrapDoorBlock.f_54117_);
                                if (horizontalFacing.m_122424_() == trapdoor) {
                                    if (!Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                                    Printer.cacheEasyPlacePosition(pos, false);
                                    mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.m_82528_((Vec3i)pos), ((Direction)stateSchematic.m_61143_((Property)TrapDoorBlock.f_54117_)).m_122424_(), pos, false));
                                    InventoryUtils.decrementCount(isCreative);
                                    Printer.sleepWhenRequired(mc);
                                    ++interact;
                                    continue;
                                }
                            } else {
                                if (blockSchematic instanceof GrindstoneBlock) {
                                    Direction direction = (Direction)stateSchematic.m_61143_((Property)GrindstoneBlock.f_54117_);
                                    if ((primaryFacing.m_122434_() != Direction.Axis.Y || horizontalFacing != direction) && (primaryFacing.m_122434_() == Direction.Axis.Y || horizontalFacing != direction.m_122424_()) || !Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                                    Printer.cacheEasyPlacePosition(pos, false);
                                    mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.m_82528_((Vec3i)pos), ((Direction)stateSchematic.m_61143_((Property)GrindstoneBlock.f_54117_)).m_122424_(), pos, false));
                                    InventoryUtils.decrementCount(isCreative);
                                    Printer.sleepWhenRequired(mc);
                                    ++interact;
                                    continue;
                                }
                                if (!(blockSchematic instanceof EndRodBlock) || !Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                                Printer.cacheEasyPlacePosition(pos, false);
                                mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.m_82512_((Vec3i)pos), (Direction)stateSchematic.m_61143_((Property)EndRodBlock.f_52588_), pos, false));
                                InventoryUtils.decrementCount(isCreative);
                                ++interact;
                                Printer.sleepWhenRequired(mc);
                                continue;
                            }
                        }
                        InteractionHand hand = InteractionHand.MAIN_HAND;
                        if (blockSchematic instanceof GlazedTerracottaBlock) {
                            // Glazed terracotta FACING comes from the player's orientation: the carpet-protocol hitVec is rejected on
                            // vanilla servers as out of bounds, so use fake rotation instead (send a fake yaw packet then useItem;
                            // the server computes the correct FACING from the fake orientation)
                            if (interact < maxInteract && FakeAccurateBlockPlacement.request(stateSchematic, pos)) {
                                ++interact;
                            }
                            continue;
                        }
                        Vec3 hitPos = CanUseProtocol && Printer.IsBlockSupportedCarpet(stateSchematic.m_60734_()) != false ? Printer.applyCarpetProtocolHitVec(npos, stateSchematic) : Printer.applyHitVec(npos, stateSchematic, side);
                        BlockHitResult hitResult5 = new BlockHitResult(hitPos, side, npos, false);
                        if (stateSchematic.m_60734_() instanceof SnowLayerBlock) {
                            stateClient = mc.f_91073_.m_8055_(npos);
                            if (!stateClient.m_60795_() && (!(stateClient.m_60734_() instanceof SnowLayerBlock) || (Integer)stateClient.m_61143_((Property)SnowLayerBlock.f_56581_) >= (Integer)stateSchematic.m_61143_((Property)SnowLayerBlock.f_56581_))) continue;
                            side = Direction.UP;
                            hitResult5 = new BlockHitResult(hitPos, side, npos, false);
                            if (!Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                            Printer.cacheEasyPlacePosition(pos, false);
                            ++interact;
                            mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult5);
                            InventoryUtils.decrementCount(isCreative);
                            Printer.sleepWhenRequired(mc);
                            continue;
                        }
                        if (smartRedstone) {
                            Set<BlockPos> shouldCache;
                            if (stateSchematic.m_61138_((Property)RedstoneTorchBlock.f_55674_) && !((Boolean)stateSchematic.m_61143_((Property)RedstoneTorchBlock.f_55674_)).booleanValue()) {
                                Printer.cacheEasyPlacePosition(pos.m_7494_(), false, 700);
                            }
                            if (!(shouldCache = Printer.ObserverCantAvoidPos(mc, (Level)world, pos)).isEmpty()) {
                                shouldCache.forEach(a -> {
                                    MessageHolder.sendDebugMessage("Caching position " + a.m_123344_() + " because observer can't avoid ");
                                    Printer.cacheEasyPlacePosition(a, true, (int)Math.ceil(Math.sqrt(a.m_123331_((Vec3i)pos)) * 100.0));
                                });
                            }
                        }
                        if (!LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue() || LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
                            if (!Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                            MessageHolder.sendOrderMessage("Places block " + String.valueOf(blockSchematic) + " at " + pos.m_123344_());
                            mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult5);
                            InventoryUtils.decrementCount(isCreative);
                            Printer.cacheEasyPlacePosition(pos, false);
                            Printer.sleepWhenRequired(mc);
                            ++interact;
                            continue;
                        }
                        if (!(sBlock instanceof LiquidBlock) && interact < maxInteract && FakeAccurateBlockPlacement.request(stateSchematic, pos)) {
                            ++interact;
                        }
                        if (stateSchematic.m_60734_() instanceof SlabBlock && stateSchematic.m_61143_((Property)SlabBlock.f_56353_) == SlabType.DOUBLE && (stateClient = mc.f_91073_.m_8055_(npos)).m_60734_() instanceof SlabBlock && stateClient.m_61143_((Property)SlabBlock.f_56353_) != SlabType.DOUBLE) {
                            side = Printer.applyPlacementFacing(stateSchematic, sideOrig, stateClient);
                            hitResult5 = new BlockHitResult(hitPos, side, npos, false);
                            if (!Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                            mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult5);
                            InventoryUtils.decrementCount(isCreative);
                            Printer.cacheEasyPlacePosition(pos, false);
                            Printer.sleepWhenRequired(mc);
                            ++interact;
                            continue;
                        }
                        if (stateSchematic.m_60734_() instanceof SeaPickleBlock && (Integer)stateSchematic.m_61143_((Property)SeaPickleBlock.f_56074_) > 1 && (stateClient = mc.f_91073_.m_8055_(npos)).m_60734_() instanceof SeaPickleBlock && (Integer)stateClient.m_61143_((Property)SeaPickleBlock.f_56074_) < (Integer)stateSchematic.m_61143_((Property)SeaPickleBlock.f_56074_)) {
                            side = Printer.applyPlacementFacing(stateSchematic, sideOrig, stateClient);
                            hitResult5 = new BlockHitResult(hitPos, side, npos, false);
                            if (!Printer.doSchematicWorldPickBlock(mc, stateSchematic, pos)) continue;
                            mc.f_91072_.m_233732_(mc.f_91074_, hand, hitResult5);
                            InventoryUtils.decrementCount(isCreative);
                            Printer.cacheEasyPlacePosition(pos, false);
                            Printer.sleepWhenRequired(mc);
                            ++interact;
                            continue;
                        }
                        if (interact < maxInteract) continue;
                        if (shouldSleepLonger) {
                            shouldSleepLonger = false;
                            lastPlaced = Math.max(lastPlaced, new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
                        } else {
                            lastPlaced = Math.max(lastPlaced, new Date().getTime());
                        }
                        return InteractionResult.SUCCESS;
                    }
                    // canPickBlock failed = survival mode and the required item is not in the player inventory;
                    // name the resolved item so replacement mappings (schematic block != needed item) are obvious
                    ItemStack missingStack = resolvePlacementStack(MaterialCache.getInstance().getRequiredBuildItemForState(stateSchematic, (Level) world, pos));
                    MessageHolder.sendUniqueMessage(mc.f_91074_, sBlock.m_7705_() + " can't be picked !! (missing item: "
                            + (missingStack.m_41619_() ? sBlock.m_5456_().toString() : missingStack.m_41786_().getString()) + ")");
                }
            }
        }
        if (interact > 0) {
            System.out.println("[PRINTER-DEBUG] Completed: interact=" + interact + " actions taken");
            if (shouldSleepLonger) {
                shouldSleepLonger = false;
                lastPlaced = Math.max(lastPlaced, new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue());
            } else {
                lastPlaced = Math.max(lastPlaced, new Date().getTime());
            }
            return InteractionResult.SUCCESS;
        }
        if (!(mc.f_91074_.m_21205_().m_41720_() instanceof BlockItem) && !(mc.f_91074_.m_21206_().m_41720_() instanceof BlockItem)) {
            System.out.println("[PRINTER-DEBUG] Completed: no block items in hand, returning PASS");
            return InteractionResult.PASS;
        }
        System.out.println("[PRINTER-DEBUG] Completed: interact=0, holding block but nothing placed, returning FAIL");
        return InteractionResult.FAIL;
    }

    private static boolean willFall(BlockState stateSchematic, Level clientWorld, BlockPos pos) {
        if (stateSchematic.m_60734_() instanceof ScaffoldingBlock) {
            return !stateSchematic.m_60734_().m_7898_(stateSchematic, (LevelReader)clientWorld, pos);
        }
        return false;
    }

    private static boolean isQCable(Minecraft mc, Level world, BlockPos pos) {
        BlockPos posoffset = pos.m_7495_();
        BlockPos poseast = posoffset.m_122029_();
        BlockPos poswest = posoffset.m_122024_();
        BlockPos posnorth = posoffset.m_122012_();
        BlockPos possouth = posoffset.m_122019_();
        List<BlockPos> OffsetIterable = List.of(poseast, poswest, posnorth, possouth);
        for (BlockPos Position : OffsetIterable) {
            BlockState stateClient = mc.f_91073_.m_8055_(Position);
            BlockState stateSchematic = world.m_8055_(Position);
            if (!(stateSchematic.m_60734_() instanceof PistonBaseBlock) || ((Boolean)stateSchematic.m_61143_((Property)PistonBaseBlock.f_60153_)).booleanValue()) continue;
            if (stateClient.m_60795_()) {
                return true;
            }
            if (!Printer.hasNoUpdatableState(mc, world, Position)) {
                return true;
            }
            if (!(stateClient.m_60734_() instanceof PistonBaseBlock) || !((Direction)stateSchematic.m_61143_((Property)PistonBaseBlock.f_52588_)).equals((Object)Direction.UP) || world.m_8055_(Position.m_7494_()).m_60734_().equals(mc.f_91073_.m_8055_(Position.m_7494_()).m_60734_())) continue;
            return true;
        }
        BlockState stateSchematic = world.m_8055_(posoffset.m_7495_());
        return stateSchematic.m_60734_() instanceof PistonBaseBlock && (Boolean)stateSchematic.m_61143_((Property)PistonBaseBlock.f_60153_) == false && !world.m_8055_(posoffset).m_60734_().equals(mc.f_91073_.m_8055_(posoffset).m_60734_());
    }

    private static boolean hasNoUpdatableState(Minecraft mc, Level world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (world.m_8055_(pos.m_121955_(direction.m_122436_())) == mc.f_91073_.m_8055_(pos.m_121955_(direction.m_122436_())) || Printer.isNoteBlockInstrumentError(mc, world, pos.m_121955_(direction.m_122436_())) || Printer.isDoorHingeError(mc, world, pos.m_121955_(direction.m_122436_())) || world.m_46859_(pos.m_121955_(direction.m_122436_())) && mc.f_91073_.m_46859_(pos.m_121955_(direction.m_122436_()))) continue;
            return false;
        }
        return true;
    }

    private static boolean hasNearbyRedirectDust(Minecraft mc, Level world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!Printer.isCorrectDustState(mc, world, pos.m_121955_(direction.m_122436_()))) {
                return true;
            }
            if (direction.m_122434_() != Direction.Axis.Y && !Printer.isCorrectDustState(mc, world, BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)))) {
                return true;
            }
            if (!Printer.isCorrectDustState(mc, world, pos.m_121955_(direction.m_122436_()).m_7494_())) {
                return true;
            }
            if (direction.m_122434_() == Direction.Axis.Y || Printer.isCorrectDustState(mc, world, BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)).m_7494_())) continue;
            return true;
        }
        return false;
    }

    private static BlockPos hasNearbyRedirectDustPos(Minecraft mc, Level world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!Printer.isCorrectDustState(mc, world, pos.m_121955_(direction.m_122436_()))) {
                return pos.m_121955_(direction.m_122436_());
            }
            if (!Printer.isCorrectDustState(mc, world, BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)))) {
                return BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction));
            }
            if (!Printer.isCorrectDustState(mc, world, pos.m_121955_(direction.m_122436_()).m_7494_())) {
                return pos.m_121955_(direction.m_122436_()).m_7494_();
            }
            if (Printer.isCorrectDustState(mc, world, BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)).m_7494_())) continue;
            return BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)).m_7494_();
        }
        return null;
    }

    private static boolean cantAvoidExtend(Level world, BlockPos pos, Level schematicWorld) {
        if (!((Boolean)schematicWorld.m_8055_(pos).m_61143_((Property)PistonBaseBlock.f_60153_)).booleanValue()) {
            return Printer.willExtendInWorld(world, pos, (Direction)schematicWorld.m_8055_(pos).m_61143_((Property)PistonBaseBlock.f_52588_));
        }
        return false;
    }

    private static boolean isCorrectDustState(Minecraft mc, Level world, BlockPos pos) {
        BlockState ClientState = mc.f_91073_.m_8055_(pos);
        BlockState SchematicState = world.m_8055_(pos);
        if (!SchematicState.m_60713_(Blocks.f_50088_)) {
            return true;
        }
        if (!ClientState.m_60713_(Blocks.f_50088_)) {
            return false;
        }
        return SchematicState.m_61143_((Property)RedStoneWireBlock.f_55497_) == ClientState.m_61143_((Property)RedStoneWireBlock.f_55497_) && SchematicState.m_61143_((Property)RedStoneWireBlock.f_55499_) == ClientState.m_61143_((Property)RedStoneWireBlock.f_55499_) && SchematicState.m_61143_((Property)RedStoneWireBlock.f_55498_) == ClientState.m_61143_((Property)RedStoneWireBlock.f_55498_) && SchematicState.m_61143_((Property)RedStoneWireBlock.f_55496_) == ClientState.m_61143_((Property)RedStoneWireBlock.f_55496_) && Objects.equals((Integer)SchematicState.m_61143_((Property)RedStoneWireBlock.f_55500_) == 0, (Integer)ClientState.m_61143_((Property)RedStoneWireBlock.f_55500_) == 0);
    }

    private static boolean shouldExtendQC(Minecraft mc, Level world, BlockPos pos) {
        return Printer.willExtendInWorld((Level)mc.f_91073_, pos, (Direction)world.m_8055_(pos).m_61143_((Property)PistonBaseBlock.f_52588_)) == (Boolean)world.m_8055_(pos).m_61143_((Property)PistonBaseBlock.f_60153_);
    }

    private static boolean shouldSuppressExtend(Level world, BlockPos pos) {
        return Printer.willExtendInWorld(world, pos, (Direction)world.m_8055_(pos).m_61143_((Property)PistonBaseBlock.f_52588_)) && (Boolean)world.m_8055_(pos).m_61143_((Property)PistonBaseBlock.f_60153_) == false;
    }

    private static boolean directlyPowered(Level schematicWorld, BlockPos pos, Direction pistonFace) {
        for (Direction lv : Direction.values()) {
            if (lv == pistonFace || !schematicWorld.m_8055_(pos.m_121955_(lv.m_122436_())).m_60713_(Blocks.f_50330_)) continue;
            return true;
        }
        return false;
    }

    private static boolean willExtendInWorld(Level world, BlockPos pos, Direction pistonFace) {
        for (Direction lv : Direction.values()) {
            BlockState adjState;
            if (lv == pistonFace || !world.m_276987_(pos.m_121955_(lv.m_122436_()), lv)) continue;
            boolean hasObserver = false;
            for (Direction dir : Direction.values()) {
                BlockState observerState = world.m_8055_(pos.m_121955_(lv.m_122436_()).m_121955_(dir.m_122436_()));
                if (!observerState.m_60713_(Blocks.f_50455_) || !((Boolean)observerState.m_61143_((Property)ObserverBlock.f_55082_)).booleanValue()) continue;
                hasObserver = true;
                break;
            }
            if ((adjState = world.m_8055_(pos.m_121955_(lv.m_122436_()))).m_60713_(Blocks.f_50455_) && ((Boolean)adjState.m_61143_((Property)ObserverBlock.f_55082_)).booleanValue()) {
                hasObserver = true;
            }
            if (hasObserver) continue;
            return true;
        }
        if (world.m_276987_(pos, Direction.DOWN)) {
            return true;
        }
        BlockPos lv2 = pos.m_7494_();
        for (Direction lv3 : Direction.values()) {
            BlockState qcState;
            if (lv3 == Direction.DOWN || !world.m_276987_(lv2.m_121955_(lv3.m_122436_()), lv3) || (qcState = world.m_8055_(lv2.m_121955_(lv3.m_122436_()))).m_60713_(Blocks.f_50455_) && qcState.m_61143_((Property)ObserverBlock.f_52588_) == lv3 && ((Boolean)qcState.m_61143_((Property)ObserverBlock.f_55082_)).booleanValue()) continue;
            return true;
        }
        return false;
    }

    private static BlockPos isObserverCantAvoidOutput(Minecraft mc, Level schematicWorld, BlockPos pos) {
        if (Printer.isQCableBlock(schematicWorld.m_8055_(pos)) && schematicWorld.m_8055_(pos.m_6630_(2)).m_60713_(Blocks.f_50455_) && schematicWorld.m_8055_(pos.m_6630_(2)).m_61143_((Property)ObserverBlock.f_52588_) == Direction.UP && (mc.f_91073_.m_8055_(pos.m_6630_(3)) != schematicWorld.m_8055_(pos.m_6630_(3)) || mc.f_91073_.m_8055_(pos.m_6630_(2)).m_61138_((Property)ObserverBlock.f_55082_) && ((Boolean)mc.f_91073_.m_8055_(pos.m_6630_(2)).m_61143_((Property)ObserverBlock.f_55082_)).booleanValue())) {
            MessageHolder.sendDebugMessage("Position at " + pos.m_123344_() + " has observer that will QC, but not watching correct state");
            return pos.m_6630_(3);
        }
        for (Direction direction : Direction.values()) {
            Map.Entry<Boolean, BlockPos> value;
            Map.Entry<Boolean, BlockPos> value2;
            BlockState offsetState = schematicWorld.m_8055_(pos.m_121955_(direction.m_122436_()));
            if (offsetState.m_60734_() instanceof ObserverBlock && offsetState.m_61143_((Property)ObserverBlock.f_52588_) == direction && !(value2 = Printer.isWatchingCorrectState(mc, schematicWorld, pos.m_121955_(direction.m_122436_()), null, false)).getKey().booleanValue()) {
                return pos.m_121955_(direction.m_122436_());
            }
            if (direction == Direction.UP || direction == Direction.DOWN || !Printer.isQCableBlock(schematicWorld, pos)) continue;
            BlockPos qcPos = pos.m_121955_(direction.m_122436_()).m_7494_();
            BlockState qcState = schematicWorld.m_8055_(qcPos);
            BlockState existingState = mc.f_91073_.m_8055_(qcPos);
            if (!(qcState.m_60734_() instanceof ObserverBlock && !existingState.m_60713_(Blocks.f_50455_) && qcState.m_61143_((Property)ObserverBlock.f_52588_) == direction ? (value = Printer.isWatchingCorrectState(mc, schematicWorld, qcPos, null, false)).getKey() == false : qcState.m_60796_((BlockGetter)schematicWorld, qcPos) && (qcState = schematicWorld.m_8055_(qcPos = qcPos.m_121955_(direction.m_122436_()))).m_60734_() instanceof ObserverBlock && !existingState.m_60713_(Blocks.f_50455_) && qcState.m_61143_((Property)ObserverBlock.f_52588_) == direction && (value = Printer.isWatchingCorrectState(mc, schematicWorld, qcPos, null, false)).getKey() == false)) continue;
            return pos.m_121955_(direction.m_122436_());
        }
        return null;
    }

    private static boolean sleepWhenRequired(Minecraft mc) {
        if (!LitematicaMixinMod.USE_INVENTORY_CACHE.getBooleanValue()) {
            return false;
        }
        if (LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue() > 0 && InventoryUtils.lastCount <= 0) {
            shouldSleepLonger = true;
            lastPlaced = new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue();
            MessageHolder.sendUniqueMessageActionBar(mc.f_91074_, "Sleeping because stack is emptied!");
            isSleeping = true;
            return true;
        }
        return false;
    }

    private static boolean isQCableBlock(Level world, BlockPos pos) {
        Block block = world.m_8055_(pos).m_60734_();
        return !LitematicaMixinMod.AVOID_CHECK_ONLY_PISTONS.getBooleanValue() && block instanceof DispenserBlock || block instanceof PistonBaseBlock;
    }

    private static boolean isQCableBlock(BlockState blockState) {
        Block block = blockState.m_60734_();
        return !LitematicaMixinMod.AVOID_CHECK_ONLY_PISTONS.getBooleanValue() && block instanceof DispenserBlock || block instanceof PistonBaseBlock;
    }

    private static Map.Entry<Boolean, BlockPos> isWatchingCorrectState(Minecraft mc, Level schematicWorld, BlockPos pos, Set<Long> recursive, boolean allowFirst) {
        if (recursive == null) {
            recursive = new HashSet<Long>();
        }
        if (recursive.contains(pos.m_121878_())) {
            return Map.entry(true, pos);
        }
        BlockState clientState = mc.f_91073_.m_8055_(pos);
        BlockState schematicState = schematicWorld.m_8055_(pos);
        if (schematicState.m_60734_() instanceof ObserverBlock) {
            Direction facing = (Direction)schematicState.m_61143_((Property)ObserverBlock.f_52588_);
            recursive.add(pos.m_121878_());
            if (allowFirst && Printer.ObserverCantAvoid(mc, schematicWorld, facing, pos)) {
                return Map.entry(true, pos);
            }
            Map.Entry<Boolean, BlockPos> entry = Printer.isWatchingCorrectState(mc, schematicWorld, pos.m_121955_(facing.m_122436_()), recursive, allowFirst);
            if (entry.getKey().booleanValue()) {
                return entry;
            }
            return Map.entry(false, pos);
        }
        if (schematicState == Blocks.f_50626_.m_49966_() || schematicState == Blocks.f_50375_.m_49966_() || schematicState.m_60795_()) {
            return Map.entry(true, pos);
        }
        if (clientState != schematicState) {
            if (Printer.isNoteBlockInstrumentError(mc, schematicWorld, pos) || Printer.isDoorHingeError(mc, schematicWorld, pos)) {
                return Map.entry(true, pos);
            }
            if (Printer.isClientPowerError(mc, schematicWorld, clientState, schematicState, pos)) {
                return Map.entry(true, pos);
            }
            return Map.entry(false, pos);
        }
        return Map.entry(true, pos);
    }

    private static boolean isClientPowerError(Minecraft mc, Level world, BlockState clientState, BlockState schematicState, BlockPos pos) {
        if (clientState.m_60734_() != schematicState.m_60734_()) {
            return false;
        }
        if (schematicState.m_60713_(Blocks.f_50286_) || schematicState.m_60713_(Blocks.f_50061_)) {
            if (schematicState.m_61143_((Property)DropperBlock.f_52660_) != clientState.m_61143_((Property)DropperBlock.f_52660_)) {
                boolean isReceiving;
                boolean bl = isReceiving = mc.f_91073_.m_276867_(pos) || mc.f_91073_.m_276867_(pos.m_7494_());
                if (isReceiving != (Boolean)clientState.m_61143_((Property)DropperBlock.f_52660_)) {
                    mc.f_91073_.m_46597_(pos, (BlockState)clientState.m_61124_((Property)DropperBlock.f_52660_, (Comparable)Boolean.valueOf(isReceiving)));
                }
                return mc.f_91073_.m_8055_(pos) == schematicState;
            }
        } else if (schematicState.m_60713_(Blocks.f_50065_)) {
            if (schematicState.m_61143_((Property)NoteBlock.f_55012_) != clientState.m_61143_((Property)NoteBlock.f_55012_)) {
                boolean isReceiving = mc.f_91073_.m_276867_(pos);
                mc.f_91073_.m_46597_(pos, (BlockState)clientState.m_61124_((Property)NoteBlock.f_55012_, (Comparable)Boolean.valueOf(isReceiving)));
                return Printer.isNoteBlockInstrumentError(mc, world, pos);
            }
        } else if (schematicState.m_60713_(Blocks.f_50332_) && schematicState.m_61143_((Property)HopperBlock.f_54022_) != clientState.m_61143_((Property)HopperBlock.f_54022_)) {
            boolean isReceiving = mc.f_91073_.m_276867_(pos);
            mc.f_91073_.m_46597_(pos, (BlockState)clientState.m_61124_((Property)HopperBlock.f_54022_, (Comparable)Boolean.valueOf(!isReceiving)));
            return mc.f_91073_.m_8055_(pos) == schematicState;
        }
        return false;
    }

    private static boolean ObserverCantAvoid(Minecraft mc, Level world, Direction facingSchematic, BlockPos pos) {
        BlockPos posOffset = pos.m_121955_(facingSchematic.m_122436_());
        BlockState OffsetStateSchematic = world.m_8055_(posOffset);
        Block offsetBlock = OffsetStateSchematic.m_60734_();
        if (OffsetStateSchematic.m_60713_(Blocks.f_50065_) && (Printer.isNoteBlockInstrumentError(mc, world, posOffset) || Printer.isDoorHingeError(mc, world, posOffset))) {
            return true;
        }
        if (facingSchematic.equals((Object)Direction.UP)) {
            return offsetBlock instanceof WallBlock || offsetBlock instanceof ComparatorBlock || offsetBlock instanceof DoorBlock || offsetBlock instanceof RepeaterBlock || offsetBlock instanceof FallingBlock || offsetBlock instanceof BaseRailBlock || offsetBlock instanceof NoteBlock || offsetBlock instanceof BubbleColumnBlock || offsetBlock instanceof RedStoneWireBlock || offsetBlock instanceof FaceAttachedHorizontalDirectionalBlock && OffsetStateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.FLOOR;
        }
        if (facingSchematic.equals((Object)Direction.DOWN)) {
            return offsetBlock instanceof WallBlock || offsetBlock instanceof FaceAttachedHorizontalDirectionalBlock && OffsetStateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.CEILING;
        }
        return offsetBlock instanceof WallBlock || offsetBlock instanceof IronBarsBlock || offsetBlock instanceof FenceBlock || OffsetStateSchematic.m_60713_(Blocks.f_50183_) || offsetBlock instanceof FaceAttachedHorizontalDirectionalBlock && OffsetStateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.WALL && OffsetStateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_) == facingSchematic || Printer.hasDustOrAscendingRails(world, facingSchematic, pos);
    }

    private static boolean hasDustOrAscendingRails(Level schematicWorld, Direction watching, BlockPos observerPos) {
        BlockPos possible = observerPos.m_121955_(watching.m_122436_());
        BlockState state = schematicWorld.m_8055_(possible);
        if (state.m_60713_(Blocks.f_50088_)) {
            RedstoneSide connection = (RedstoneSide)state.m_61143_((Property)RedStoneWireBlock.f_55501_.get(watching.m_122424_()));
            return connection == RedstoneSide.UP;
        }
        if (state.m_60734_() instanceof PoweredRailBlock) {
            switch (watching) {
                case NORTH: {
                    return state.m_61143_((Property)PoweredRailBlock.f_55214_) == RailShape.ASCENDING_SOUTH;
                }
                case SOUTH: {
                    return state.m_61143_((Property)PoweredRailBlock.f_55214_) == RailShape.ASCENDING_NORTH;
                }
                case EAST: {
                    return state.m_61143_((Property)PoweredRailBlock.f_55214_) == RailShape.ASCENDING_WEST;
                }
                case WEST: {
                    return state.m_61143_((Property)PoweredRailBlock.f_55214_) == RailShape.ASCENDING_EAST;
                }
            }
            return false;
        }
        return false;
    }

    private static List<BlockPos> getNeighborsExcept(BlockPos pos, Direction except) {
        ArrayList<BlockPos> retVal = new ArrayList<BlockPos>(5);
        for (Direction direction : Direction.values()) {
            if (direction == except.m_122424_()) continue;
            retVal.add(pos.m_121955_(direction.m_122436_()));
        }
        return retVal;
    }

    private static Set<BlockPos> ObserverCantAvoidPos(Minecraft mc, Level world, BlockPos pos) {
        HashSet<BlockPos> relatedPos = new HashSet<BlockPos>(6);
        BlockState targetState = world.m_8055_(pos);
        Block block = targetState.m_60734_();
        if ((block instanceof ComparatorBlock || block instanceof RepeaterBlock || block instanceof FallingBlock || block instanceof BaseRailBlock || block instanceof RedStoneWireBlock || block instanceof DoorBlock || block instanceof FaceAttachedHorizontalDirectionalBlock && targetState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.FLOOR) && world.m_8055_(pos.m_7495_()).m_60713_(Blocks.f_50455_) && world.m_8055_(pos.m_7495_()).m_61143_((Property)ObserverBlock.f_52588_) == Direction.UP) {
            relatedPos.add(pos.m_6625_(2));
            relatedPos.addAll(Printer.getNeighborsExcept(pos, Direction.DOWN));
            if (world.m_8055_(pos.m_6625_(3)).m_60734_() instanceof PistonBaseBlock) {
                relatedPos.add(pos.m_6625_(3));
            }
            return relatedPos;
        }
        if (block instanceof NoteBlock) {
            if (!Printer.isNoteBlockInstrumentError(mc, world, pos)) {
                relatedPos.add(pos.m_6625_(2));
                relatedPos.addAll(Printer.getNeighborsExcept(pos, Direction.DOWN));
                if (world.m_8055_(pos.m_6625_(3)).m_60734_() instanceof PistonBaseBlock) {
                    relatedPos.add(pos.m_6625_(3));
                }
                return relatedPos;
            }
        } else if (block instanceof DoorBlock && !Printer.isDoorHingeError(mc, world, pos)) {
            relatedPos.add(pos.m_6625_(2));
            relatedPos.addAll(Printer.getNeighborsExcept(pos, Direction.DOWN));
            if (world.m_8055_(pos.m_6625_(3)).m_60734_() instanceof PistonBaseBlock) {
                relatedPos.add(pos.m_6625_(3));
            }
            return relatedPos;
        }
        for (Direction direction : Direction.values()) {
            BlockPos posOffset = pos.m_121955_(direction.m_122436_());
            BlockState offsetStateSchematic = world.m_8055_(posOffset);
            if (!offsetStateSchematic.m_60713_(Blocks.f_50455_) || offsetStateSchematic.m_61143_((Property)ObserverBlock.f_52588_) != direction.m_122424_()) continue;
            if (block instanceof WallBlock) {
                relatedPos.add(BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)));
                relatedPos.addAll(Printer.getNeighborsExcept(pos, direction));
                if (!(world.m_8055_(BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)).m_7495_()).m_60734_() instanceof PistonBaseBlock)) continue;
                relatedPos.add(BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)).m_7495_());
                continue;
            }
            if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
                if (!(targetState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.CEILING && direction == Direction.UP || targetState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.FLOOR && direction == Direction.DOWN) && (targetState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) != AttachFace.WALL || targetState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_) != direction.m_122424_())) continue;
                relatedPos.add(BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)));
                relatedPos.addAll(Printer.getNeighborsExcept(pos, direction));
                if (!(world.m_8055_(BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)).m_7495_()).m_60734_() instanceof PistonBaseBlock)) continue;
                relatedPos.add(BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)).m_7495_());
                continue;
            }
            if (!(block instanceof PoweredRailBlock) && !(block instanceof RedStoneWireBlock) || !Printer.hasDustOrAscendingRails(world, direction.m_122424_(), pos)) continue;
            relatedPos.add(BlockPos.m_122022_((long)BlockPos.m_121915_((long)2L, (Direction)direction)));
            relatedPos.addAll(Printer.getNeighborsExcept(pos, direction));
        }
        return relatedPos;
    }

    private static boolean shouldAvoidPlaceCart(BlockPos pos, Level schematicWorld) {
        for (Direction direction : Direction.values()) {
            if (!schematicWorld.m_8055_(pos.m_7495_().m_121955_(direction.m_122436_())).m_60713_(Blocks.f_50077_)) continue;
            return true;
        }
        return false;
    }

    private static boolean placeCart(BlockState state, Minecraft client, BlockPos pos) {
        if (state.m_60713_(Blocks.f_50031_) && state.m_61143_((Property)DetectorRailBlock.f_52428_) != client.f_91073_.m_8055_(pos).m_61143_((Property)DetectorRailBlock.f_52428_) && Printer.canPickItem(client, Items.f_42449_.m_7968_()) && client.f_91074_.m_20182_().m_82554_(Vec3.m_82528_((Vec3i)pos)) < 4.5) {
            InteractionResult actionResult;
            Vec3 clickPos = Vec3.m_82528_((Vec3i)pos).m_82520_(0.5, 0.125, 0.5);
            if (!FakeAccurateBlockPlacement.canHandleOther(Items.f_42449_)) {
                return false;
            }
            if (InventoryUtils.swapToItem(client, Items.f_42449_.m_7968_()) && (actionResult = client.f_91072_.m_233732_(client.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(clickPos, Direction.UP, pos, false))).m_19077_()) {
                Printer.cacheEasyPlacePosition(pos, false, 600);
                return true;
            }
            return false;
        }
        return false;
    }

    private static void placeGrindStone(BlockState state, Minecraft client, BlockPos pos) {
        if (LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue()) {
            FakeAccurateBlockPlacement.request(state, pos);
            return;
        }
        if (!Printer.canAttachGrindstone(state, client, pos) && !Printer.isFacingCorrectly(state, client.f_91074_)) {
            return;
        }
        Direction side = (Direction)state.m_61143_((Property)GrindstoneBlock.f_54117_);
        AttachFace location = (AttachFace)state.m_61143_((Property)GrindstoneBlock.f_53179_);
        if (Printer.canAttachGrindstone(state, client, pos)) {
            BlockPos clickPos = location == AttachFace.CEILING ? pos.m_7494_() : (location == AttachFace.FLOOR ? pos.m_7495_() : pos.m_121955_(side.m_122424_().m_122436_()));
            Vec3 hitVec = Vec3.m_82512_((Vec3i)clickPos).m_82549_(Vec3.m_82528_((Vec3i)side.m_122436_()).m_82542_(0.5, 0.5, 0.5));
            if (Printer.doSchematicWorldPickBlock(client, state, pos)) {
                Printer.cacheEasyPlacePosition(pos, false);
                client.f_91072_.m_233732_(client.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, side, clickPos, false));
            }
        } else if (Printer.isFacingCorrectly(state, client.f_91074_)) {
            Vec3 hitVec = Vec3.m_82512_((Vec3i)pos);
            BlockPos clickPos = pos;
            if (Printer.doSchematicWorldPickBlock(client, state, pos)) {
                Printer.cacheEasyPlacePosition(pos, false);
                client.f_91072_.m_233732_(client.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, side, clickPos, false));
            }
        }
    }

    // Rod blocks (end rod / lightning rod) take FACING straight from the clicked face, so the
    // required orientation is always achievable by clicking the schematic facing's support face —
    // no player rotation (real or fake) is involved. Clicking the air target itself works too:
    // BlockPlaceContext.replaceClicked then places at the clicked position (see vanilla
    // BlockPlaceContext.getClickedPos).
    private static boolean placeRod(BlockState state, Minecraft client, BlockPos pos, boolean isCreative) {
        Direction facing = (Direction)state.m_61143_((Property)EndRodBlock.f_52588_);
        BlockPos support = pos.m_121955_(facing.m_122424_().m_122436_());
        BlockHitResult hit;
        if (client.f_91073_.m_8055_(support).m_247087_()) {
            // support is air: click the target position itself (replaceClicked -> places at pos)
            hit = new BlockHitResult(Vec3.m_82512_((Vec3i)pos), facing, pos, false);
        } else {
            // click the solid support block on the face pointing toward the rod position
            Vec3 hitVec = Vec3.m_82512_((Vec3i)support).m_82549_(Vec3.m_82528_((Vec3i)facing.m_122436_()).m_82542_(0.5, 0.5, 0.5));
            hit = new BlockHitResult(hitVec, facing, support, false);
        }
        if (!Printer.doSchematicWorldPickBlock(client, state, pos)) return false;
        Printer.cacheEasyPlacePosition(pos, false);
        client.f_91072_.m_233732_(client.f_91074_, InteractionHand.MAIN_HAND, hit);
        InventoryUtils.decrementCount(isCreative);
        Printer.sleepWhenRequired(client);
        return true;
    }

    // Potted plants have no item form of their own: the generic path would try to place the bare
    // plant block and fail. Vanilla requires two steps: (1) place an empty flower pot, (2) right-
    // click the pot with the plant (FlowerPotBlock.use plants it). A pot holding the wrong plant
    // is broken first so both steps can be redone. An empty pot in the schematic is placed the
    // same way (and a wrong plant in it is dumped out by breaking).
    private static boolean placePottedPlant(FlowerPotBlock sPot, BlockState stateSchematic, Minecraft client, BlockPos pos, boolean isCreative) {
        Block content = sPot.m_53560_();
        boolean schematicEmpty = content == null || content.m_5456_() == Items.f_41852_;
        BlockState clientState = client.f_91073_.m_8055_(pos);
        Block clientBlock = clientState.m_60734_();
        if (!(clientBlock instanceof FlowerPotBlock)) {
            // step 1: no pot yet -> place an empty flower pot by clicking the air target itself
            // (BlockPlaceContext.replaceClicked then places at the clicked position)
            if (!clientState.m_247087_()) return false;
            if (!Printer.doSchematicWorldPickBlock(client, Items.f_42618_.m_7968_())) return false;
            client.f_91072_.m_233732_(client.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3((double)pos.m_123341_() + 0.5, (double)pos.m_123342_(), (double)pos.m_123343_() + 0.5), Direction.DOWN, pos, false));
            InventoryUtils.decrementCount(isCreative);
            // short cooldown: let the server state settle before the plant step runs
            Printer.cacheEasyPlacePosition(pos, false, 200);
            Printer.sleepWhenRequired(client);
            return true;
        }
        Block clientContent = ((FlowerPotBlock)clientBlock).m_53560_();
        boolean clientEmpty = clientContent == null || clientContent.m_5456_() == Items.f_41852_;
        if (schematicEmpty) {
            if (clientEmpty) return false; // already correct
        } else {
            if (clientContent == content) return false; // already correct
        }
        if (clientEmpty) {
            // step 2: right-click the empty pot with the plant item -> FlowerPotBlock.use plants it
            if (!Printer.doSchematicWorldPickBlock(client, stateSchematic, pos)) return false;
            client.f_91072_.m_233732_(client.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.m_82512_((Vec3i)pos), Direction.UP, pos, false));
            InventoryUtils.decrementCount(isCreative);
            Printer.cacheEasyPlacePosition(pos, false);
            Printer.sleepWhenRequired(client);
            return true;
        }
        // wrong plant in the pot -> break it; the printer redoes both steps on a later tick
        client.f_91072_.m_105269_(pos, Direction.DOWN);
        Printer.cacheEasyPlacePosition(pos, true);
        return true;
    }

    private static boolean canAttachGrindstone(BlockState state, Minecraft client, BlockPos pos) {
        if (LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue()) {
            return true;
        }
        Direction facing = (Direction)state.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_);
        AttachFace location = (AttachFace)state.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_);
        if (location == AttachFace.CEILING) {
            return !client.f_91073_.m_8055_(pos.m_7494_()).m_247087_() && client.f_91074_.m_6350_() == facing.m_122424_() && !Printer.hasGui(client.f_91073_.m_8055_(pos.m_7494_()).m_60734_()) || client.f_91074_.m_36341_();
        }
        if (location == AttachFace.FLOOR) {
            return !client.f_91073_.m_8055_(pos.m_7495_()).m_247087_() && client.f_91074_.m_6350_() == facing.m_122424_() && !Printer.hasGui(client.f_91073_.m_8055_(pos.m_7495_()).m_60734_()) || client.f_91074_.m_36341_();
        }
        return !client.f_91073_.m_8055_(pos.m_121955_(facing.m_122424_().m_122436_())).m_247087_() && !Printer.hasGui(client.f_91073_.m_8055_(pos.m_121955_(facing.m_122424_().m_122436_())).m_60734_()) || client.f_91074_.m_36341_();
    }

    private static boolean isFacingCorrectly(BlockState state, LocalPlayer player) {
        Direction facing = (Direction)state.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_);
        AttachFace location = (AttachFace)state.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_);
        Direction[] facingOrder = Direction.m_122382_((Entity)player);
        if (location == AttachFace.CEILING) {
            return facingOrder[0] == Direction.UP && player.m_6350_() == facing;
        }
        if (location == AttachFace.FLOOR) {
            return facingOrder[0] == Direction.DOWN && player.m_6350_() == facing;
        }
        return facingOrder[0] == facing.m_122424_();
    }

    private static void placeTrapDoor(BlockState state, Minecraft client, BlockPos pos) {
        Vec3 hitVec;
        BlockPos clickPos;
        Direction side = (Direction)state.m_61143_((Property)TrapDoorBlock.f_54117_);
        if (client.f_91073_.m_8055_(pos.m_121955_(side.m_122424_().m_122436_())).m_247087_()) {
            clickPos = pos;
            if (client.f_91074_.m_6350_().m_122424_() != side) {
                return;
            }
            side = state.m_61143_((Property)TrapDoorBlock.f_57515_) == Half.TOP ? Direction.DOWN : Direction.UP;
            hitVec = Vec3.m_82528_((Vec3i)clickPos).m_82520_(0.5, 0.5, 0.5);
        } else {
            clickPos = pos.m_121955_(side.m_122424_().m_122436_());
            hitVec = Vec3.m_82528_((Vec3i)clickPos).m_82520_(0.5, 0.5, 0.5).m_82549_(Vec3.m_82528_((Vec3i)side.m_122436_()).m_82542_(0.5, 0.5, 0.5));
        }
        if (Printer.doSchematicWorldPickBlock(client, state, pos)) {
            Printer.cacheEasyPlacePosition(pos, false);
            client.f_91072_.m_233732_(client.f_91074_, InteractionHand.MAIN_HAND, new BlockHitResult(hitVec, side, clickPos, false));
        }
    }

    private static boolean isNoteBlockInstrumentError(Minecraft mc, Level world, BlockPos pos) {
        BlockState stateA = world.m_8055_(pos);
        BlockState stateB = mc.f_91073_.m_8055_(pos);
        return stateA.m_60713_(Blocks.f_50065_) && stateB.m_60713_(Blocks.f_50065_) && stateA.m_61143_((Property)NoteBlock.f_55012_) == stateB.m_61143_((Property)NoteBlock.f_55012_) && world.m_8055_(pos.m_7495_()).m_247087_() == mc.f_91073_.m_8055_(pos.m_121955_(Direction.DOWN.m_122436_())).m_247087_();
    }

    private static boolean isDoorHingeError(Minecraft mc, Level world, BlockPos pos) {
        BlockState stateA = world.m_8055_(pos);
        BlockState stateB = mc.f_91073_.m_8055_(pos);
        return stateA.m_61138_((Property)DoorBlock.f_52728_) && stateB.m_61138_((Property)DoorBlock.f_52728_) && stateA.m_61143_((Property)DoorBlock.f_52729_) == stateB.m_61143_((Property)DoorBlock.f_52729_) && stateA.m_61143_((Property)DoorBlock.f_52726_) == stateB.m_61143_((Property)DoorBlock.f_52726_) && stateA.m_61143_((Property)DoorBlock.f_52727_) == stateB.m_61143_((Property)DoorBlock.f_52727_) && stateA.m_61143_((Property)DoorBlock.f_52730_) == stateB.m_61143_((Property)DoorBlock.f_52730_);
    }

    private static boolean ObserverUpdateOrder(Minecraft mc, Level world, BlockPos pos, Box selectedBox) {
        boolean ExplicitObserver = LitematicaMixinMod.PRINTER_OBSERVER_AVOID_ALL.getBooleanValue();
        BlockState stateSchematic = world.m_8055_(pos);
        if (((Boolean)stateSchematic.m_61143_((Property)ObserverBlock.f_55082_)).booleanValue()) {
            return false;
        }
        Direction facingSchematic = getFacingValueQuietly((BlockState)stateSchematic);
        assert (facingSchematic != null);
        boolean observerCantAvoid = Printer.ObserverCantAvoid(mc, world, facingSchematic, pos);
        if (observerCantAvoid) {
            return false;
        }
        BlockPos posOffset = pos.m_121955_(facingSchematic.m_122436_());
        if (!Printer.isPositionWithinBox(selectedBox, posOffset)) {
            return false;
        }
        BlockState OffsetStateSchematic = world.m_8055_(posOffset);
        BlockState OffsetStateClient = mc.f_91073_.m_8055_(posOffset);
        if (OffsetStateSchematic.m_60713_(Blocks.f_50375_)) {
            return false;
        }
        if (OffsetStateSchematic.m_60713_(Blocks.f_50455_) && OffsetStateSchematic.m_61143_((Property)ObserverBlock.f_52588_) == facingSchematic.m_122424_()) {
            return false;
        }
        if (OffsetStateSchematic.m_60734_() instanceof DoorBlock && OffsetStateClient.m_60734_() instanceof DoorBlock && OffsetStateSchematic.m_61143_((Property)DoorBlock.f_52729_) == OffsetStateClient.m_61143_((Property)DoorBlock.f_52729_) && OffsetStateSchematic.m_61143_((Property)DoorBlock.f_52726_) == OffsetStateClient.m_61143_((Property)DoorBlock.f_52726_)) {
            return false;
        }
        if (ExplicitObserver) {
            if (OffsetStateSchematic.m_60713_(Blocks.f_50375_) || OffsetStateClient.m_60795_() && OffsetStateSchematic.m_60795_() || OffsetStateSchematic.m_60713_(Blocks.f_50626_)) {
                return false;
            }
            if (Printer.isClientPowerError(mc, world, OffsetStateClient, OffsetStateSchematic, posOffset)) {
                return false;
            }
            return !OffsetStateSchematic.toString().equals(OffsetStateClient.toString());
        }
        return !OffsetStateClient.m_60734_().equals(OffsetStateSchematic.m_60734_());
    }

    private static BlockPos ObserverUpdateOrderPos(Minecraft mc, Level world, BlockPos pos) {
        boolean ExplicitObserver = LitematicaMixinMod.PRINTER_OBSERVER_AVOID_ALL.getBooleanValue();
        BlockState stateSchematic = world.m_8055_(pos);
        if (((Boolean)stateSchematic.m_61143_((Property)ObserverBlock.f_55082_)).booleanValue()) {
            return null;
        }
        Direction facingSchematic = getFacingValueQuietly((BlockState)stateSchematic);
        assert (facingSchematic != null);
        boolean observerCantAvoid = Printer.ObserverCantAvoid(mc, world, facingSchematic, pos);
        if (observerCantAvoid) {
            return null;
        }
        BlockPos posOffset = pos.m_121955_(facingSchematic.m_122436_());
        assert (pos != posOffset);
        BlockState OffsetStateSchematic = world.m_8055_(posOffset);
        BlockState OffsetStateClient = mc.f_91073_.m_8055_(posOffset);
        if (OffsetStateSchematic.m_60713_(Blocks.f_50375_)) {
            return null;
        }
        if (OffsetStateSchematic.m_60713_(Blocks.f_50455_) && OffsetStateSchematic.m_61143_((Property)ObserverBlock.f_52588_) == facingSchematic.m_122424_()) {
            return null;
        }
        if (OffsetStateSchematic.m_60734_() instanceof DoorBlock && OffsetStateClient.m_60734_() instanceof DoorBlock && OffsetStateSchematic.m_61143_((Property)DoorBlock.f_52729_) == OffsetStateClient.m_61143_((Property)DoorBlock.f_52729_) && OffsetStateSchematic.m_61143_((Property)DoorBlock.f_52726_) == OffsetStateClient.m_61143_((Property)DoorBlock.f_52726_)) {
            return null;
        }
        if (ExplicitObserver) {
            if (OffsetStateSchematic.m_60713_(Blocks.f_50375_) || OffsetStateClient.m_60795_() && OffsetStateSchematic.m_60795_()) {
                return null;
            }
            if (!OffsetStateSchematic.toString().equals(OffsetStateClient.toString())) {
                if (Printer.isClientPowerError(mc, world, OffsetStateClient, OffsetStateSchematic, posOffset)) {
                    return null;
                }
                return posOffset;
            }
        }
        if (!OffsetStateClient.m_60734_().equals(OffsetStateSchematic.m_60734_())) {
            return posOffset;
        }
        return null;
    }

    private static boolean canPlaceFace(FacingData facedata, BlockState stateSchematic, Direction primaryFacing, Direction horizontalFacing) {
        if (stateSchematic.m_60713_(Blocks.f_50623_)) {
            return true;
        }
        Direction facing = getFacingValueQuietly((BlockState)stateSchematic);
        if (stateSchematic.m_60734_() instanceof BaseRailBlock) {
            facing = Printer.convertRailShapetoFace(stateSchematic);
        }
        if (facing != null && facedata != null) {
            switch (facedata.type) {
                case 0: {
                    if (facedata.isReversed) {
                        return facing.m_122424_() == primaryFacing;
                    }
                    return facing == primaryFacing;
                }
                case 1: {
                    if (facedata.isReversed) {
                        return facing.m_122424_() == horizontalFacing;
                    }
                    return facing == horizontalFacing;
                }
                case 2: {
                    return stateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.WALL || (facing == horizontalFacing && stateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.CEILING ? primaryFacing == Direction.UP && horizontalFacing == stateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_) : primaryFacing == Direction.DOWN && horizontalFacing == stateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_));
                }
                case 3: {
                    return horizontalFacing.m_122427_() == facing;
                }
                case 4: {
                    return facing == horizontalFacing || facing == horizontalFacing.m_122424_();
                }
            }
            return true;
        }
        if (stateSchematic.m_60734_() instanceof TorchBlock && !(stateSchematic.m_60734_() instanceof WallTorchBlock) && !(stateSchematic.m_60734_() instanceof RedstoneWallTorchBlock)) {
            return Direction.DOWN == primaryFacing;
        }
        return true;
    }

    private static boolean isReplaceableFluidSource(BlockState checkState) {
        return checkState.m_60734_() instanceof LiquidBlock && (Integer)checkState.m_61143_((Property)LiquidBlock.f_54688_) == 0 || checkState.m_60734_() instanceof BubbleColumnBlock || checkState.m_60713_(Blocks.f_50037_) || checkState.m_60713_(Blocks.f_50038_) || checkState.m_60734_() instanceof SimpleWaterloggedBlock && (Boolean)checkState.m_61143_((Property)BlockStateProperties.f_61362_) != false && checkState.m_247087_();
    }

    private static boolean isReplaceableWaterFluidSource(BlockState checkState) {
        return checkState.m_60713_(Blocks.f_50037_) || checkState.m_60713_(Blocks.f_50038_) || checkState.m_60713_(Blocks.f_49990_) && checkState.m_61138_((Property)LiquidBlock.f_54688_) && (Integer)checkState.m_61143_((Property)LiquidBlock.f_54688_) == 0 || checkState.m_60734_() instanceof BubbleColumnBlock || checkState.m_60734_() instanceof SimpleWaterloggedBlock && checkState.m_61138_((Property)BlockStateProperties.f_61362_) && (Boolean)checkState.m_61143_((Property)BlockStateProperties.f_61362_) != false && checkState.m_247087_();
    }

    private static boolean containsWaterloggable(BlockState state) {
        return state.m_60734_() instanceof SimpleWaterloggedBlock && (Boolean)state.m_61143_((Property)BlockStateProperties.f_61362_) != false;
    }

    /**
     * Silent replacement for MaFgLib's BlockUtils.getFirstPropertyFacingValue: that method sends
     * a chat warning ("... does not have facing data, please add this!") for every block without a
     * facing property, which spams the chat when called on state-only mismatches such as chiseled
     * bookshelves. Same lookup, no warning.
     */
    private static Direction getFacingValueQuietly(BlockState state) {
        for (Property<?> property : state.m_61147_()) {
            if (property instanceof DirectionProperty) {
                return (Direction)state.m_61143_((Property)property);
            }
        }
        return null;
    }

    /**
     * Detects a state error that can never converge on its own: the block type already matches the
     * schematic, but the orientation (or the top/bottom half) is wrong.
     *
     * Examples: a stair rotated the wrong way, a slab placed on the top half instead of the bottom
     * half, an observer facing the wrong direction. None of these can be fixed by right clicking,
     * and they are not neighbour driven either, so they stay wrong forever unless the block is
     * broken and placed again.
     *
     * Deliberately NOT compared (they converge by themselves once the neighbours exist):
     * stair shape, fence/pane/wall/mushroom connection flags, powered/open/waterlogged style flags.
     */
    public static boolean hasDirectionalStateError(BlockState stateSchematic, BlockState stateClient) {
        Block block = stateSchematic.m_60734_();
        if (block != stateClient.m_60734_()) {
            return false;
        }
        if (getFacingValueQuietly(stateSchematic) != getFacingValueQuietly(stateClient)) {
            return true;
        }
        if ((block instanceof StairBlock || block instanceof TrapDoorBlock) && stateSchematic.m_61143_((Property)StairBlock.f_56842_) != stateClient.m_61143_((Property)StairBlock.f_56842_)) {
            return true;
        }
        if (block instanceof SlabBlock && stateSchematic.m_61143_((Property)SlabBlock.f_56353_) != stateClient.m_61143_((Property)SlabBlock.f_56353_)) {
            return true;
        }
        return false;
    }

    private static boolean printerCheckCancel(BlockState stateSchematic, BlockState stateClient) {
        Block blockClient;
        Block blockSchematic = stateSchematic.m_60734_();
        if (blockSchematic instanceof SeaPickleBlock && (Integer)stateSchematic.m_61143_((Property)SeaPickleBlock.f_56074_) > 1 && (blockClient = stateClient.m_60734_()) instanceof SeaPickleBlock && !Objects.equals(stateClient.m_61143_((Property)SeaPickleBlock.f_56074_), stateSchematic.m_61143_((Property)SeaPickleBlock.f_56074_))) {
            return blockSchematic != blockClient;
        }
        if (blockSchematic instanceof SnowLayerBlock && (blockClient = stateClient.m_60734_()) instanceof SnowLayerBlock && (Integer)stateClient.m_61143_((Property)SnowLayerBlock.f_56581_) < (Integer)stateSchematic.m_61143_((Property)SnowLayerBlock.f_56581_)) {
            return false;
        }
        if (blockSchematic instanceof SlabBlock && stateSchematic.m_61143_((Property)SlabBlock.f_56353_) == SlabType.DOUBLE && (blockClient = stateClient.m_60734_()) instanceof SlabBlock && stateClient.m_61143_((Property)SlabBlock.f_56353_) != SlabType.DOUBLE) {
            return blockSchematic != blockClient;
        }
        if (blockSchematic instanceof ComposterBlock && (Integer)stateSchematic.m_61143_((Property)ComposterBlock.f_51913_) > 0 && stateClient.m_60734_() instanceof ComposterBlock) {
            return !Objects.equals(stateClient.m_61143_((Property)ComposterBlock.f_51913_), stateSchematic.m_61143_((Property)ComposterBlock.f_51913_));
        }
        blockClient = stateClient.m_60734_();
        if (blockClient instanceof SnowLayerBlock && (Integer)stateClient.m_61143_((Property)SnowLayerBlock.f_56581_) < 3 && !(stateSchematic.m_60734_() instanceof SnowLayerBlock)) {
            return false;
        }
        return !stateClient.m_60795_() && !stateClient.m_247087_();
    }

    public static Vec3 applyHitVec(BlockPos pos, BlockState state, Direction side) {
        Block block = state.m_60734_();
        Vec3 clickPos = Vec3.m_82528_((Vec3i)pos);
        if (!(block instanceof GrindstoneBlock) && block instanceof FaceAttachedHorizontalDirectionalBlock || block instanceof TorchBlock || block instanceof WallSkullBlock || block instanceof LadderBlock || block instanceof TripWireHookBlock || block instanceof WallSignBlock || block instanceof EndRodBlock || block instanceof BaseCoralFanBlock) {
            if (block instanceof BaseCoralFanBlock && !(block instanceof BaseCoralWallFanBlock)) {
                side = Direction.UP;
                clickPos = Vec3.m_82512_((Vec3i)pos.m_7495_()).m_82549_(Vec3.m_82528_((Vec3i)side.m_122436_()).m_82542_(0.5, 0.5, 0.5));
            } else if (block instanceof TorchBlock && !(block instanceof WallTorchBlock) && !(block instanceof RedstoneWallTorchBlock)) {
                side = Direction.UP;
                clickPos = Vec3.m_82512_((Vec3i)pos.m_7495_()).m_82549_(Vec3.m_82528_((Vec3i)side.m_122436_()).m_82542_(0.5, 0.5, 0.5));
            } else if (side == null || state.m_61138_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) && state.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) != AttachFace.WALL) {
                side = state.m_61138_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) && state.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.CEILING ? Direction.DOWN : Direction.UP;
                clickPos = clickPos.m_82520_(0.5, 0.5, 0.5).m_82549_(Vec3.m_82528_((Vec3i)side.m_122436_()).m_82542_(0.5, 0.5, 0.5));
            } else {
                clickPos = clickPos.m_82520_(0.5, 0.5, 0.5).m_82549_(Vec3.m_82528_((Vec3i)side.m_122436_()).m_82542_(0.5, 0.5, 0.5));
            }
        } else if (block instanceof BarrelBlock) {
            // Clicked-face block: aim slightly INSIDE the block near the schematic-facing plane;
            // a boundary-corner hit vector can be rejected by server-side click validation.
            Direction sdir = side == null ? Direction.UP : side;
            clickPos = Vec3.m_82512_((Vec3i)pos).m_82549_(Vec3.m_82528_((Vec3i)sdir.m_122436_()).m_82542_(0.45, 0.45, 0.45));
        }
        double dx = clickPos.f_82479_;
        double dy = clickPos.f_82480_;
        double dz = clickPos.f_82481_;
        if (block instanceof StairBlock) {
            // 楼梯(side=水平面时)：TOP 需 clickY-posY>0.5，BOTTOM 需 <=0.5
            dy = state.m_61143_((Property)StairBlock.f_56842_) == Half.TOP ? (dy += 0.9) : (dy += 0.1);
        } else if (block instanceof SlabBlock && state.m_61143_((Property)SlabBlock.f_56353_) != SlabType.DOUBLE) {
            // 半砖(side=水平面时)：TOP 需 clickY-posY>=0.5，BOTTOM 需 <0.5
            dy = state.m_61143_((Property)SlabBlock.f_56353_) == SlabType.TOP ? (dy += 0.9) : (dy += 0.1);
        } else if (block instanceof TrapDoorBlock) {
            // 活板门：placeBlock 内会按 half 把 side 改成 UP/DOWN，这里保留相对偏移作兼容
            dy = state.m_61143_((Property)TrapDoorBlock.f_57515_) == Half.TOP ? (dy += 0.9) : (dy += 0.1);
        }
        return new Vec3(dx, dy, dz);
    }

    private static boolean canBypass(Minecraft mc, Level world, BlockPos pos) {
        Direction direction = (Direction)world.m_8055_(pos).m_61143_((Property)ObserverBlock.f_52588_);
        BlockPos posOffset = pos.m_121955_(direction.m_122424_().m_122436_());
        return world.m_8055_(posOffset) == null || world.m_8055_(posOffset).m_60795_() || !Printer.hasPowerRelatedState(mc.f_91073_.m_8055_(posOffset).m_60734_()) && mc.f_91073_.m_8055_(posOffset).m_60734_().m_49954_() == world.m_8055_(posOffset).m_60734_().m_49954_();
    }

    private static boolean hasGui(Block checkGui) {
        return checkGui instanceof CraftingTableBlock || checkGui instanceof GrindstoneBlock || checkGui instanceof LeverBlock || checkGui instanceof TrapDoorBlock || checkGui instanceof ButtonBlock || checkGui instanceof DoorBlock || checkGui instanceof FenceGateBlock || checkGui instanceof BedBlock || checkGui instanceof NoteBlock || checkGui instanceof BaseEntityBlock;
    }

    private static boolean hasPowerRelatedState(Block block) {
        return block instanceof LeavesBlock || block instanceof LiquidBlock || block instanceof ObserverBlock || block instanceof PistonBaseBlock || block instanceof PoweredRailBlock || block instanceof DetectorRailBlock || block instanceof DispenserBlock || block instanceof DiodeBlock || block instanceof LeverBlock || block instanceof TrapDoorBlock || block instanceof RedstoneTorchBlock || block instanceof DoorBlock || block instanceof RedStoneWireBlock || block instanceof RedStoneOreBlock || block instanceof RedstoneLampBlock || block instanceof NoteBlock || block instanceof FenceGateBlock || block instanceof ScaffoldingBlock;
    }

    private static boolean hasWrongStateNearby(Minecraft mc, Level schematicWorld, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos checkPos = pos.m_121955_(direction.m_122436_());
            if (!Printer.hasPowerRelatedState(schematicWorld.m_8055_(checkPos).m_60734_()) || schematicWorld.m_8055_(checkPos) == mc.f_91073_.m_8055_(checkPos)) continue;
            return true;
        }
        return false;
    }

    private static String hasWrongStateNearbyReason(Minecraft mc, Level schematicWorld, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos checkPos = pos.m_121955_(direction.m_122436_());
            if (!Printer.hasPowerRelatedState(schematicWorld.m_8055_(checkPos).m_60734_()) || schematicWorld.m_8055_(checkPos) == mc.f_91073_.m_8055_(checkPos)) continue;
            return "!" + checkPos.m_123344_() + " STATE " + schematicWorld.m_8055_(checkPos).toString() + " does not match with current state : " + mc.f_91073_.m_8055_(checkPos).toString() + "!";
        }
        return null;
    }

    private static BlockPos hasWrongStateNearbyPos(Minecraft mc, Level schematicWorld, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos checkPos = pos.m_121955_(direction.m_122436_());
            if (!Printer.hasPowerRelatedState(schematicWorld.m_8055_(checkPos).m_60734_()) || schematicWorld.m_8055_(checkPos) == mc.f_91073_.m_8055_(checkPos)) continue;
            return checkPos;
        }
        return null;
    }

    public static Vec3 applyTorchHitVec(BlockPos pos, Vec3 hitVecIn, Direction side) {
        double x = pos.m_123341_();
        double y = pos.m_123342_();
        double z = pos.m_123343_();
        double dx = hitVecIn.f_82479_;
        double dy = hitVecIn.f_82480_;
        double dz = hitVecIn.f_82481_;
        if (side == Direction.UP) {
            dy = 1.0;
        } else if (side == Direction.DOWN) {
            dy = -1.0;
        } else if (side == Direction.EAST) {
            dx = 1.0;
        } else if (side == Direction.WEST) {
            dx = -1.0;
        } else if (side == Direction.SOUTH) {
            dz = 1.0;
        } else if (side == Direction.NORTH) {
            dz = -1.0;
        }
        return new Vec3(x + dx, y + dy, z + dz);
    }

    private static void updateSignText(Minecraft mc, Level schematicWorld, BlockPos pos) {
        if (Printer.isPositionCached(pos, false)) {
            return;
        }
        if (mc.f_91080_ instanceof SignEditScreen || !schematicWorld.m_8055_(pos).m_204336_(BlockTags.f_13068_) || signCache.contains(pos.m_121878_())) {
            return;
        }
        BlockEntity entity = schematicWorld.m_7702_(pos);
        if (entity == null) {
            return;
        }
        BlockEntity clientEntity = mc.f_91073_.m_7702_(pos);
        if (clientEntity == null) {
            return;
        }
        if (entity instanceof SignBlockEntity) {
            SignBlockEntity signBlockEntity = (SignBlockEntity)entity;
            if (clientEntity instanceof SignBlockEntity) {
                SignBlockEntity clientSignEntity = (SignBlockEntity)clientEntity;
                if (clientSignEntity.m_277142_().m_277138_(0, false).m_214077_() != ComponentContents.f_237124_ || clientSignEntity.m_277142_().m_277138_(1, false).m_214077_() != ComponentContents.f_237124_ || clientSignEntity.m_277142_().m_277138_(2, false).m_214077_() != ComponentContents.f_237124_ || clientSignEntity.m_277142_().m_277138_(3, false).m_214077_() != ComponentContents.f_237124_) {
                    MessageHolder.sendDebugMessage("Text already exists in " + pos.m_123344_());
                    signCache.add(pos.m_121878_());
                    return;
                }
                MessageHolder.sendDebugMessage("Tries to copy sign text in " + pos.m_123344_());
                signCache.add(pos.m_121878_());
                mc.m_91403_().m_104955_((Packet)new ServerboundSignUpdatePacket(signBlockEntity.m_58899_(), true, signBlockEntity.m_277142_().m_277138_(0, false).getString(), signBlockEntity.m_277142_().m_277138_(1, false).getString(), signBlockEntity.m_277142_().m_277138_(2, false).getString(), signBlockEntity.m_277142_().m_277138_(3, false).getString()));
            }
        }
    }

    private static Boolean IsBlockSupportedCarpet(Block SchematicBlock) {
        if (SchematicBlock instanceof FaceAttachedHorizontalDirectionalBlock || SchematicBlock instanceof WallSkullBlock || SchematicBlock instanceof BaseRailBlock || SchematicBlock instanceof TorchBlock || SchematicBlock instanceof BaseCoralFanBlock) {
            return false;
        }
        return LitematicaMixinMod.ADVANCED_ACCURATE_BLOCK_PLACEMENT.getBooleanValue() || SchematicBlock instanceof GlazedTerracottaBlock || SchematicBlock instanceof ObserverBlock || SchematicBlock instanceof RepeaterBlock || SchematicBlock instanceof TrapDoorBlock || SchematicBlock instanceof ComparatorBlock || SchematicBlock instanceof DispenserBlock || SchematicBlock instanceof PistonBaseBlock || SchematicBlock instanceof StairBlock;
    }

    /**
     * Whether a huge mushroom block's state is irreversibly wrong due to an "extra same-type neighbor":
     * some face is true (exposed pore) in the schematic but false (skin) in the world, and the schematic
     * neighbor in that direction is not the same mushroom block. Vanilla updateShape only flips faces to
     * false and never back to true, so this can only be fixed by breaking and re-placing the block.
     * Property order matches Direction.values(): NORTH/EAST/SOUTH/WEST/UP/DOWN.
     */
    static boolean mushroomBlockHasStaleSkin(BlockState schematic, BlockState client, Level schematicWorld, BlockPos pos) {
        BooleanProperty[] props = new BooleanProperty[] {
            HugeMushroomBlock.f_54127_, HugeMushroomBlock.f_54128_, HugeMushroomBlock.f_54129_,
            HugeMushroomBlock.f_54130_, HugeMushroomBlock.f_54131_, HugeMushroomBlock.f_54132_
        };
        Direction[] dirs = new Direction[] {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN
        };
        for (int i = 0; i < 6; ++i) {
            boolean schematicOpen = (Boolean) schematic.m_61143_((Property) props[i]);
            boolean worldOpen = (Boolean) client.m_61143_((Property) props[i]);
            if (schematicOpen && !worldOpen
                    && schematicWorld.m_8055_(pos.m_121945_(dirs[i])).m_60734_() != schematic.m_60734_()) {
                return true;
            }
        }
        return false;
    }

    static Direction applyPlacementFacing(BlockState stateSchematic, Direction side, BlockState stateClient) {
        Block blockSchematic = stateSchematic.m_60734_();
        Block blockClient = stateClient.m_60734_();
        if (blockSchematic instanceof SlabBlock) {
            if (stateSchematic.m_61143_((Property)SlabBlock.f_56353_) == SlabType.DOUBLE && blockClient instanceof SlabBlock && stateClient.m_61143_((Property)SlabBlock.f_56353_) != SlabType.DOUBLE) {
                if (stateClient.m_61143_((Property)SlabBlock.f_56353_) == SlabType.TOP) {
                    return Direction.DOWN;
                }
                return Direction.UP;
            }
            return Direction.NORTH;
        }
        if (blockSchematic instanceof RotatedPillarBlock) {
            Direction.Axis axis = (Direction.Axis)stateSchematic.m_61143_((Property)RotatedPillarBlock.f_55923_);
            if (axis == Direction.Axis.X) {
                return Direction.WEST;
            }
            if (axis == Direction.Axis.Y) {
                return Direction.DOWN;
            }
            if (axis == Direction.Axis.Z) {
                return Direction.NORTH;
            }
        } else {
            if (blockSchematic instanceof WallSignBlock) {
                return (Direction)stateSchematic.m_61143_((Property)WallSignBlock.f_58064_);
            }
            if (blockSchematic instanceof WallSkullBlock) {
                return (Direction)stateSchematic.m_61143_((Property)WallSignBlock.f_58064_);
            }
            if (blockSchematic instanceof SignBlock) {
                return Direction.UP;
            }
            if (blockSchematic instanceof FaceAttachedHorizontalDirectionalBlock) {
                AttachFace location = (AttachFace)stateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_);
                if (location == AttachFace.FLOOR) {
                    return Direction.UP;
                }
                if (location == AttachFace.CEILING) {
                    return Direction.DOWN;
                }
                return (Direction)stateSchematic.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_);
            }
            if (blockSchematic instanceof BaseCoralWallFanBlock) {
                return (Direction)stateSchematic.m_61143_((Property)BaseCoralWallFanBlock.f_49192_);
            }
            if (blockSchematic instanceof BaseCoralFanBlock) {
                return Direction.UP;
            }
            if (blockSchematic instanceof HopperBlock) {
                return ((Direction)stateSchematic.m_61143_((Property)HopperBlock.f_54021_)).m_122424_();
            }
            if (blockSchematic instanceof LightningRodBlock) {
                return (Direction)stateSchematic.m_61143_((Property)LightningRodBlock.f_52588_);
            }
            if (stateSchematic.m_204336_(BlockTags.f_13083_)) {
                return (Direction)stateSchematic.m_61143_((Property)ShulkerBoxBlock.f_56183_);
            }
            if (blockSchematic instanceof TorchBlock) {
                if (blockSchematic instanceof WallTorchBlock || blockSchematic instanceof RedstoneWallTorchBlock) {
                    return (Direction)stateSchematic.m_61143_((Property)WallTorchBlock.f_58119_);
                }
                return Direction.UP;
            }
            if (blockSchematic instanceof LadderBlock) {
                return (Direction)stateSchematic.m_61143_((Property)LadderBlock.f_54337_);
            }
            if (blockSchematic instanceof TrapDoorBlock) {
                if (LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
                    return Direction.UP;
                }
                return (Direction)stateSchematic.m_61143_((Property)TrapDoorBlock.f_54117_);
            }
            if (blockSchematic instanceof TripWireHookBlock) {
                return (Direction)stateSchematic.m_61143_((Property)TripWireHookBlock.f_57667_);
            }
            if (blockSchematic instanceof EndRodBlock) {
                return (Direction)stateSchematic.m_61143_((Property)EndRodBlock.f_52588_);
            }
            if (blockSchematic instanceof AnvilBlock) {
                if (LitematicaMixinMod.ADVANCED_ACCURATE_BLOCK_PLACEMENT.getBooleanValue() || LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue() && Printer.IsBlockSupportedCarpet(blockSchematic).booleanValue()) {
                    return (Direction)stateSchematic.m_61143_((Property)AnvilBlock.f_48764_);
                }
                return ((Direction)stateSchematic.m_61143_((Property)AnvilBlock.f_48764_)).m_122428_();
            }
            if (blockSchematic instanceof BaseRailBlock) {
                return Printer.convertRailShapetoFace(stateSchematic);
            }
            // Amethyst clusters: FACING taken directly from the clicked face; without this branch the wrong orientation is rejected by the server
            if (blockSchematic instanceof AmethystClusterBlock) {
                return (Direction)stateSchematic.m_61143_((Property)DirectionalBlock.f_52588_);
            }
            // Barrel: FACING likewise taken directly from the clicked face (6-way); clicked face = schematic orientation places correctly
            if (blockSchematic instanceof BarrelBlock) {
                return (Direction)stateSchematic.m_61143_((Property)BarrelBlock.f_49042_);
            }
        }
        return side;
    }

    public static Direction convertRailShapetoFace(BlockState state) {
        String RailShape2 = state.m_60734_() instanceof RailBlock ? ((RailShape)state.m_61143_((Property)RailBlock.f_55392_)).toString() : ((RailShape)state.m_61143_((Property)PoweredRailBlock.f_55214_)).toString();
        if (RailShape2.contains("east") || RailShape2.contains("west")) {
            return Direction.EAST;
        }
        return Direction.NORTH;
    }

    public static boolean isPositionCached(BlockPos pos, boolean useClicked) {
        long currentTime = System.nanoTime();
        boolean cached = false;
        for (Map.Entry keys : List.copyOf(positionCache.keySet())) {
            PositionCache val = positionCache.get(keys);
            boolean expired = val.hasExpired(currentTime);
            if (expired) {
                positionCache.remove(keys);
                continue;
            }
            if (!val.getPos().equals((Object)pos)) continue;
            if (!useClicked || val.hasClicked) {
                cached = true;
            }
            if (positionCache.size() >= 16) continue;
            break;
        }
        return cached;
    }

    public static void cacheEasyPlacePosition(BlockPos pos, boolean useClicked) {
        Map.Entry entry;
        PositionCache item = new PositionCache(pos, System.nanoTime(), useClicked ? (long)LitematicaMixinMod.EASY_PLACE_CACHE_TIME.getIntegerValue() * 1000000L : 2800000000L);
        if (useClicked) {
            item.hasClicked = true;
        }
        if (positionCache.containsKey(entry = Map.entry((Object)pos.m_121878_(), (Object)useClicked))) {
            PositionCache value = positionCache.get(entry);
            if (item.timeout > value.timeout) {
                positionCache.put(entry, item);
            }
        } else {
            positionCache.put(entry, item);
        }
    }

    public static void cacheEasyPlacePosition(BlockPos pos, boolean useClicked, int miliseconds) {
        Map.Entry entry;
        PositionCache item = new PositionCache(pos, System.nanoTime(), (long)miliseconds * 1000000L);
        if (useClicked) {
            item.hasClicked = true;
        }
        if (positionCache.containsKey(entry = Map.entry((Object)pos.m_121878_(), (Object)useClicked))) {
            PositionCache value = positionCache.get(entry);
            if (item.timeout > value.timeout) {
                positionCache.put(entry, item);
            }
        } else {
            positionCache.put(entry, item);
        }
    }

    public static Vec3 applyCarpetProtocolHitVec(BlockPos pos, BlockState state) {
        if (Configs.Generic.EASY_PLACE_PROTOCOL.getOptionListValue() == EasyPlaceProtocol.V3) {
            return WorldUtils.applyPlacementProtocolV3((BlockPos)pos, (BlockState)state, (Vec3)new Vec3((double)pos.m_123341_(), (double)pos.m_123342_(), (double)pos.m_123343_()));
        }
        double code = 0.0;
        double y = pos.m_123342_();
        double z = pos.m_123343_();
        Block block = state.m_60734_();
        Direction facing = getFacingValueQuietly((BlockState)state);
        int railEnumCode = Printer.getRailShapeOrder(state);
        int propertyIncrement = 16;
        if (facing == null && railEnumCode == 32 && !(block instanceof SlabBlock)) {
            return new Vec3((double)pos.m_123341_(), y, z);
        }
        if (facing != null) {
            code = facing.m_122411_();
        } else if (railEnumCode != 32) {
            code = railEnumCode;
        }
        if (block instanceof RepeaterBlock) {
            code += (double)((Integer)state.m_61143_((Property)RepeaterBlock.f_55798_) * 16);
        } else if (block instanceof TrapDoorBlock && state.m_61143_((Property)TrapDoorBlock.f_57515_) == Half.TOP) {
            code += 16.0;
        } else if (block instanceof ComparatorBlock && state.m_61143_((Property)ComparatorBlock.f_51854_) == ComparatorMode.SUBTRACT) {
            code += 16.0;
        } else if (block instanceof StairBlock && state.m_61143_((Property)StairBlock.f_56842_) == Half.TOP) {
            code += 16.0;
        } else if (block instanceof SlabBlock && state.m_61143_((Property)SlabBlock.f_56353_) != SlabType.DOUBLE && state.m_61143_((Property)SlabBlock.f_56353_) == SlabType.TOP) {
            y += 0.9;
        }
        if (code >= 0.0) {
            return new Vec3(code * 2.0 + 2.0 + (double)pos.m_123341_(), y, z);
        }
        return new Vec3((double)pos.m_123341_(), y, z);
    }

    public static Integer getRailShapeOrder(BlockState state) {
        Block stateBlock = state.m_60734_();
        if (stateBlock instanceof BaseRailBlock) {
            if (stateBlock instanceof RailBlock) {
                return ((RailShape)state.m_61143_((Property)RailBlock.f_55392_)).ordinal();
            }
            if (stateBlock instanceof DetectorRailBlock) {
                return ((RailShape)state.m_61143_((Property)DetectorRailBlock.f_52427_)).ordinal();
            }
            return ((RailShape)state.m_61143_((Property)PoweredRailBlock.f_55214_)).ordinal();
        }
        return 32;
    }

    public static class PositionCache {
        private final BlockPos pos;
        private final long timeout;
        public boolean hasClicked = false;

        private PositionCache(BlockPos pos, long time, long timeout) {
            this.pos = pos;
            this.timeout = time + timeout;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public boolean hasExpired(long currentTime) {
            return currentTime > this.timeout;
        }
    }
}

