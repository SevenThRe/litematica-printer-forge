/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.TorchBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package xyz.jxmm.litematica_printer_forge.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.Breaker;
import xyz.jxmm.litematica_printer_forge.utils.InventoryUtils;
import xyz.jxmm.litematica_printer_forge.utils.MessageHolder;
import xyz.jxmm.litematica_printer_forge.utils.Printer;
import xyz.jxmm.litematica_printer_forge.utils.positionStorage;

public class BedrockBreaker {
    public static long lastPlaced = new Date().getTime();
    public static Long CurrentTick = 0L;
    static List<Direction> HORIZONTAL = List.of(Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH);
    private static final Map<Long, PositionCache> targetPosMap = new LinkedHashMap<Long, PositionCache>();
    static int rangeX = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_X.getIntegerValue();
    static int rangeY = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
    static int rangeZ = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
    static int MaxReach = Math.max(Math.max(rangeX, rangeY), rangeZ);

    public static void clear() {
        targetPosMap.clear();
        positionStorage.clear();
    }

    private static boolean shouldExtend(Level world, BlockPos pos, Direction pistonFace) {
        for (Direction direction : Direction.values()) {
            if (direction == pistonFace || !world.m_276987_(pos.m_7724_(direction.m_122436_()), direction)) continue;
            return true;
        }
        if (world.m_276987_(pos, Direction.DOWN)) {
            return true;
        }
        BlockPos blockPos = pos.m_7494_();
        for (Direction qcDirections : Direction.values()) {
            if (qcDirections == Direction.DOWN || !world.m_276987_(blockPos.m_7724_(qcDirections.m_122436_()), qcDirections)) continue;
            return true;
        }
        return false;
    }

    @Nullable
    public static TorchPath getPistonTorchPosDir(Minecraft mc, BlockPos bedrockPos) {
        for (Direction lv : Direction.values()) {
            BlockPos pistonPos;
            if (!LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue() && lv != Direction.DOWN && lv != Direction.UP || !mc.f_91073_.m_8055_(pistonPos = bedrockPos.m_7724_(lv.m_122436_())).m_60795_() || !BedrockBreaker.isBlockPosinYRange(pistonPos)) continue;
            for (Direction pistonFacing : Direction.values()) {
                TorchData torchdata;
                BlockPos checkAir;
                if (pistonFacing.m_122424_() == lv || !BedrockBreaker.isBlockPosinYRange(checkAir = pistonPos.m_7724_(pistonFacing.m_122436_())) || BedrockBreaker.shouldExtend((Level)mc.f_91073_, pistonPos, pistonFacing) || !mc.f_91073_.m_8055_(checkAir).m_60795_() && !mc.f_91073_.m_8055_(checkAir).m_247087_() || (torchdata = BedrockBreaker.getPossiblePowerableTorchPosFace(mc, bedrockPos, pistonPos, checkAir)) == null) continue;
                TorchPath torchPath = new TorchPath(torchdata.TorchPos, torchdata.Torchfacing, pistonPos, pistonFacing, lv.m_122424_());
                if (torchdata.SlimePos != null) {
                    torchPath.slimePos = torchdata.SlimePos;
                }
                return torchPath;
            }
        }
        return null;
    }

    public static boolean isBlockPosinYRange(BlockPos checkPos) {
        return checkPos.m_123342_() < Printer.worldTopY && Printer.worldBottomY < checkPos.m_123342_();
    }

    @Nullable
    public static TorchData getPossiblePowerableTorchPosFace(Minecraft mc, BlockPos pos1, BlockPos pistonPos, BlockPos pos2) {
        TorchData torchData;
        BlockPos slimePos;
        BlockPos torchCheck;
        ClientLevel world = mc.f_91073_;
        boolean forceSlimeBlock = LitematicaMixinMod.BEDROCK_BREAKING_FORCE_TORCH.getBooleanValue();
        for (Direction hd : HORIZONTAL) {
            torchCheck = pistonPos.m_7724_(hd.m_122436_());
            if (!BedrockBreaker.isBlockPosinYRange(torchCheck) || torchCheck.equals((Object)pos1) || torchCheck.equals((Object)pos2) || !world.m_8055_(torchCheck).m_60795_() && !world.m_8055_(torchCheck).m_247087_()) continue;
            if (!world.m_8055_(torchCheck.m_7495_()).m_60713_(Blocks.f_50039_) && TorchBlock.m_49863_((LevelReader)world, (BlockPos)torchCheck.m_7495_(), (Direction)Direction.DOWN)) {
                return new TorchData(torchCheck, Direction.UP);
            }
            if (forceSlimeBlock && BedrockBreaker.canPlaceSlime(mc)) {
                slimePos = torchCheck.m_7495_();
                if (slimePos.equals((Object)pos2) || !world.m_8055_(slimePos).m_60795_() && !world.m_8055_(slimePos).m_247087_()) continue;
                torchData = new TorchData(torchCheck, Direction.UP);
                torchData.registerSlimePos(slimePos);
                return torchData;
            }
            for (Direction hd2 : HORIZONTAL) {
                if (hd2 == hd || !BedrockBreaker.canPlaceAt(hd2, (Level)world, torchCheck)) continue;
                return new TorchData(torchCheck, hd2);
            }
        }
        for (Direction hd : HORIZONTAL) {
            torchCheck = pistonPos.m_7494_().m_7724_(hd.m_122436_());
            if (!BedrockBreaker.isBlockPosinYRange(torchCheck) || torchCheck.equals((Object)pos1) || torchCheck.equals((Object)pos2) || !world.m_8055_(torchCheck).m_60795_() && !world.m_8055_(torchCheck).m_247087_()) continue;
            if (!world.m_8055_(torchCheck.m_7495_()).m_60713_(Blocks.f_50039_) && TorchBlock.m_49863_((LevelReader)world, (BlockPos)torchCheck.m_7495_(), (Direction)Direction.DOWN)) {
                return new TorchData(torchCheck, Direction.UP);
            }
            if (forceSlimeBlock && BedrockBreaker.canPlaceSlime(mc)) {
                slimePos = torchCheck.m_7495_();
                if (slimePos.equals((Object)pos2) || !BedrockBreaker.isBlockPosinYRange(slimePos) || !world.m_8055_(slimePos).m_60795_() && !world.m_8055_(slimePos).m_247087_()) continue;
                torchData = new TorchData(torchCheck, Direction.UP);
                torchData.registerSlimePos(slimePos);
                return torchData;
            }
            for (Direction hd2 : HORIZONTAL) {
                if (hd2 == hd || !BedrockBreaker.canPlaceAt(hd2, (Level)world, torchCheck)) continue;
                return new TorchData(torchCheck, hd2);
            }
        }
        BlockPos torchCheck2 = pistonPos.m_7495_();
        if (pos2 != torchCheck2 && BedrockBreaker.isBlockPosinYRange(torchCheck2)) {
            if (torchCheck2.equals((Object)pos1) || torchCheck2.equals((Object)pos2)) {
                return null;
            }
            if (!world.m_8055_(torchCheck2).m_60795_() && !world.m_8055_(torchCheck2).m_247087_()) {
                return null;
            }
            if (!world.m_8055_(torchCheck2.m_7495_()).m_60713_(Blocks.f_50039_) && TorchBlock.m_49863_((LevelReader)world, (BlockPos)torchCheck2.m_7495_(), (Direction)Direction.DOWN)) {
                return new TorchData(torchCheck2, Direction.UP);
            }
            if (forceSlimeBlock && BedrockBreaker.canPlaceSlime(mc)) {
                BlockPos slimePos2 = torchCheck2.m_7495_();
                if (slimePos2.equals((Object)pos2)) {
                    return null;
                }
                if (BedrockBreaker.isBlockPosinYRange(slimePos2) && world.m_8055_(slimePos2).m_60795_() || world.m_8055_(slimePos2).m_247087_()) {
                    TorchData torchData2 = new TorchData(torchCheck2, Direction.UP);
                    torchData2.registerSlimePos(slimePos2);
                    return torchData2;
                }
            }
        }
        return null;
    }

    public static void removeScheduledPos(Minecraft mc) {
        for (Long position2 : targetPosMap.keySet().stream().filter(position -> targetPosMap.get(position) != null && CurrentTick - BedrockBreaker.targetPosMap.get((Object)position).SysTime > 200L && targetPosMap.get(position).isIdle()).collect(Collectors.toList())) {
            targetPosMap.remove(position2);
        }
        for (Long position2 : targetPosMap.keySet().stream().filter(position -> targetPosMap.get(position).canSafeRemove((Level)mc.f_91073_)).toList()) {
            targetPosMap.remove(position2);
        }
    }

    public static boolean canPlaceAt(Direction lv, Level world, BlockPos pos) {
        BlockPos lv2 = pos.m_7724_(lv.m_122424_().m_122436_());
        BlockState lv3 = world.m_8055_(lv2);
        if (lv3.m_60713_(Blocks.f_50039_)) {
            return false;
        }
        return lv3.m_60783_((BlockGetter)world, lv2, lv);
    }

    public static void placePiston(Minecraft mc, BlockPos pos, Direction facing) {
        ItemStack PistonStack = Items.f_41869_.m_7968_();
        InventoryUtils.swapToItem(mc, PistonStack);
        MessageHolder.sendDebugMessage("Places piston at %s with facing %s".formatted(new Object[]{pos.m_123344_(), facing}));
        if (LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
            BedrockBreaker.placeViaCarpet(mc, pos, facing);
        } else {
            BedrockBreaker.placeViaPacketReversed(mc, pos, facing, false);
        }
    }

    public static void placePiston(Minecraft mc, BlockPos pos, Direction facing, boolean sync) {
        ItemStack PistonStack = Items.f_41869_.m_7968_();
        InventoryUtils.swapToItem(mc, PistonStack);
        if (sync) {
            mc.m_91403_().m_104955_((Packet)new ServerboundSetCarriedItemPacket(mc.f_91074_.m_150109_().f_35977_));
        }
        MessageHolder.sendDebugMessage("Places piston at %s with facing %s".formatted(new Object[]{pos.m_123344_(), facing}));
        if (LitematicaMixinMod.ACCURATE_BLOCK_PLACEMENT.getBooleanValue()) {
            BedrockBreaker.placeViaCarpet(mc, pos, facing);
        } else {
            BedrockBreaker.placeViaPacketReversed(mc, pos, facing, false);
        }
    }

    public static void placeSlime(Minecraft mc, BlockPos pos) {
        ItemStack SlimeStack = Items.f_42204_.m_7968_();
        InventoryUtils.swapToItem(mc, SlimeStack);
        MessageHolder.sendDebugMessage("Places slime at %s".formatted(new Object[]{pos.m_123344_()}));
        BedrockBreaker.placeViaCarpet(mc, pos, Direction.UP);
    }

    public static void placeViaCarpet(Minecraft mc, BlockPos pos, Direction facing) {
        positionStorage.registerPos(pos, true);
        Vec3 hitVec = new Vec3((double)(pos.m_123341_() + 2 + facing.m_122411_() * 2), (double)pos.m_123342_(), (double)pos.m_123343_());
        BlockHitResult hitResult = new BlockHitResult(hitVec, facing, pos, false);
        BedrockBreaker.InteractionHandleTweakPlacementPacket(mc, hitResult);
    }

    public static void placeViaPacketReversed(Minecraft mc, BlockPos pos, Direction facing, boolean ShouldOffset) {
        positionStorage.registerPos(pos, true);
        int px = pos.m_123341_();
        int py = pos.m_123342_();
        int pz = pos.m_123343_();
        Vec3 hitPos = new Vec3((double)px, (double)py, (double)pz);
        if (ShouldOffset) {
            if (facing == Direction.DOWN) {
                py += 0;
            } else if (facing == Direction.UP) {
                py += 0;
            } else if (facing == Direction.NORTH) {
                ++pz;
            } else if (facing == Direction.SOUTH) {
                --pz;
            } else if (facing == Direction.EAST) {
                --px;
            } else if (facing == Direction.WEST) {
                ++px;
            }
        }
        BlockPos npos = new BlockPos(px, py, pz);
        if (ShouldOffset) {
            hitPos = Printer.applyTorchHitVec(npos, new Vec3(0.5, 0.5, 0.5), facing);
            if (facing == Direction.DOWN) {
                facing = Direction.UP;
            }
        }
        float OriginPitch = mc.f_91074_.m_5686_(1.0f);
        float OriginYaw = mc.f_91074_.m_5675_(1.0f);
        if (facing == Direction.DOWN) {
            mc.m_91403_().m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(OriginYaw, -90.0f, mc.f_91074_.m_20096_()));
        } else if (facing == Direction.UP) {
            mc.m_91403_().m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(OriginYaw, 90.0f, mc.f_91074_.m_20096_()));
        } else if (facing == Direction.EAST) {
            mc.m_91403_().m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(90.0f, OriginPitch, mc.f_91074_.m_20096_()));
        } else if (facing == Direction.WEST) {
            mc.m_91403_().m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(-90.0f, OriginPitch, mc.f_91074_.m_20096_()));
        } else if (facing == Direction.NORTH) {
            mc.m_91403_().m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(0.0f, OriginPitch, mc.f_91074_.m_20096_()));
        } else if (facing == Direction.SOUTH) {
            mc.m_91403_().m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(180.0f, OriginPitch, mc.f_91074_.m_20096_()));
        }
        BlockHitResult hitResult = new BlockHitResult(hitPos, facing, npos, false);
        BedrockBreaker.InteractionHandleTweakPlacementPacket(mc, hitResult);
        mc.m_91403_().m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(OriginYaw, OriginPitch, mc.f_91074_.m_20096_()));
    }

    public static void InteractionHandleTweakPlacementPacket(Minecraft mc, BlockHitResult hitResult) {
        mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, hitResult);
    }

    public static void placeTorch(Minecraft mc, BlockPos pos, Direction torchFacing) {
        BlockPos npos;
        ItemStack redstoneTorchStack = Items.f_41978_.m_7968_();
        InventoryUtils.swapToItem(mc, redstoneTorchStack);
        if (torchFacing.m_122434_() == Direction.Axis.Y) {
            npos = pos.m_7495_();
            torchFacing = Direction.UP;
        } else {
            npos = pos.m_7724_(torchFacing.m_122424_().m_122436_());
        }
        MessageHolder.sendDebugMessage("Places torch at %s with facing %s".formatted(new Object[]{pos.m_123344_(), torchFacing}));
        Vec3 hitVec = Vec3.m_82512_((Vec3i)npos).m_82549_(Vec3.m_82528_((Vec3i)torchFacing.m_122436_()).m_82542_(0.5, 0.5, 0.5));
        BlockHitResult hitResult = new BlockHitResult(hitVec, torchFacing, npos, false);
        MessageHolder.sendDebugMessage("Hitresult is %s %s".formatted(new Object[]{hitVec, npos.m_123344_()}));
        mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, hitResult);
        positionStorage.registerPos(pos, true);
    }

    public static boolean canProcess(Minecraft mc, BlockPos pos) {
        double SafetyDistance = LitematicaMixinMod.BEDROCK_BREAKING_RANGE_SAFE.getIntegerValue();
        if (BedrockBreaker.positionAnyNear(mc, pos, SafetyDistance)) {
            return false;
        }
        if (targetPosMap.containsKey(pos.m_121878_())) {
            return targetPosMap.get(pos.m_121878_()).isIdle();
        }
        return true;
    }

    public static boolean positionAnyNear(Minecraft mc, BlockPos pos, double distance) {
        for (Long position : targetPosMap.keySet()) {
            @Nullable PositionCache item = targetPosMap.get(position);
            if (item == null || !BedrockBreaker.isPositionInRange(mc, BlockPos.m_122022_((long)position)) || !item.distanceLessThan(pos, distance) || item.isIdle()) continue;
            return true;
        }
        return false;
    }

    public static boolean isItemPrePared(Minecraft mc) {
        Inventory inv = mc.f_91074_.m_150109_();
        ItemStack PistonStack = Items.f_41869_.m_7968_();
        ItemStack RedstoneTorchStack = Items.f_41978_.m_7968_();
        return inv.m_36030_(PistonStack) != -1 && inv.m_36030_(RedstoneTorchStack) != -1;
    }

    public static boolean canPlaceSlime(Minecraft mc) {
        ItemStack SlimeStack;
        Inventory inv = mc.f_91074_.m_150109_();
        return inv.m_36030_(SlimeStack = Items.f_42204_.m_7968_()) != -1;
    }

    public static void switchTool(Minecraft mc) {
        int bestSlotId = Breaker.getBestItemSlotIdToMineState(mc, Blocks.f_50039_.m_49966_());
        if (bestSlotId == -1) {
            return;
        }
        ItemStack stack = mc.f_91074_.m_150109_().m_8020_(bestSlotId);
        MessageHolder.sendDebugMessage("Swaps to Pickaxe " + String.valueOf(stack));
        InventoryUtils.swapToItem(mc, stack);
        MessageHolder.sendDebugMessage("Holding stack " + String.valueOf(mc.f_91074_.m_21205_()));
        mc.m_91403_().m_104955_((Packet)new ServerboundSetCarriedItemPacket(mc.f_91074_.m_150109_().f_35977_));
    }

    public static void attackBlock(Minecraft mc, BlockPos pos, Direction direction) {
        if (mc.f_91073_.m_8055_(pos).m_60795_()) {
            return;
        }
        mc.m_91403_().m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction));
    }

    public static boolean isBlockNotInstantBreakable(Block block) {
        return block.equals(Blocks.f_50752_) || block.equals(Blocks.f_50080_);
    }

    public static boolean isPositionInRange(Minecraft mc, BlockPos pos) {
        int aZ;
        int aY;
        double pX = mc.f_91074_.m_20185_();
        double pY = mc.f_91074_.m_20186_();
        double pZ = mc.f_91074_.m_20189_();
        int aX = pos.m_123341_();
        return (pX - (double)aX) * (pX - (double)aX) + (pY - (double)(aY = pos.m_123342_())) * (pY - (double)aY) + (pZ - (double)(aZ = pos.m_123343_())) * (pZ - (double)aZ) < (double)(MaxReach * MaxReach);
    }

    public static int processRemainder(Minecraft mc, int maxInteract) {
        int ret = 0;
        BedrockBreaker.switchTool(mc);
        ArrayList<BlockPos> attackList = positionStorage.getFalseMarkedHasBlockPosInAttackRange((Level)mc.f_91073_, mc.f_91074_.m_20182_(), MaxReach);
        for (BlockPos position : attackList) {
            if (ret >= maxInteract) {
                return ret;
            }
            BedrockBreaker.attackBlock(mc, position, Direction.UP);
            ++ret;
        }
        return ret;
    }

    public static synchronized int scheduledTickHandler(Minecraft mc, @Nullable BlockPos pos) {
        if (!BedrockBreaker.isItemPrePared(mc)) {
            MessageHolder.sendUniqueMessage(mc.f_91074_, "[BedrockBreaking]Items is not prepared, requires Redstone torch, Piston block + haste2 + eff 5 diamond+ pickaxe.");
            return 0;
        }
        rangeX = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_X.getIntegerValue();
        rangeY = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
        rangeZ = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
        int maxInteract = LitematicaMixinMod.PRINTER_MAX_BLOCKS.getIntegerValue();
        int interacted = 0;
        if ((interacted += BedrockBreaker.processRemainder(mc, maxInteract)) >= maxInteract) {
            return interacted;
        }
        MaxReach = Math.max(Math.max(rangeX, rangeY), rangeZ);
        BedrockBreaker.removeScheduledPos(mc);
        if (pos != null && !targetPosMap.containsKey(pos.m_121878_()) && BedrockBreaker.isPositionInRange(mc, pos) && BedrockBreaker.canProcess(mc, pos)) {
            TorchPath torch;
            Date date = new Date();
            if ((double)(date.getTime() - lastPlaced) > 1000.0 * LitematicaMixinMod.EASY_PLACE_MODE_DELAY.getDoubleValue() && (torch = BedrockBreaker.getPistonTorchPosDir(mc, pos)) != null && torch.isAllPosInRange(mc)) {
                lastPlaced = new Date().getTime();
                BlockPos TorchPos = torch.TorchPos;
                Direction TorchFacing = torch.Torchfacing;
                BlockPos PistonPos = torch.PistonPos;
                Direction PistonFacing = torch.Pistonfacing;
                Direction PistonExtendFacing = torch.PistonBreakableFacing;
                BlockPos SlimePos = torch.slimePos;
                MessageHolder.sendDebugMessage("Will place Torch at %s, facing %s \n Piston at %s, Facing %s, and changes as %s \n Optional Slime at %s".formatted(new Object[]{TorchPos.m_123344_(), TorchFacing, PistonPos.m_123344_(), PistonFacing, PistonExtendFacing, SlimePos}));
                if (SlimePos != null) {
                    BedrockBreaker.placeSlime(mc, SlimePos);
                }
                BedrockBreaker.placeTorch(mc, TorchPos, TorchFacing);
                BedrockBreaker.placePiston(mc, PistonPos, PistonFacing);
                interacted += 2;
                targetPosMap.put(pos.m_121878_(), new PositionCache(PistonPos, PistonExtendFacing, TorchPos, pos, SlimePos));
            }
        }
        for (Long posLong : targetPosMap.keySet()) {
            if (interacted >= maxInteract) {
                return interacted;
            }
            PositionCache item = targetPosMap.get(posLong);
            if (item == null || !item.isAllPosInRange(mc)) continue;
            interacted += item.doSomething(mc);
        }
        return interacted;
    }

    public static void tick() {
        CurrentTick = CurrentTick + 1L;
    }

    public static class TorchData {
        private final BlockPos TorchPos;
        private final Direction Torchfacing;
        private BlockPos SlimePos = null;

        public TorchData(BlockPos TorchPos, Direction Torchfacing) {
            this.TorchPos = TorchPos;
            this.Torchfacing = Torchfacing;
        }

        public void registerSlimePos(BlockPos slimePos) {
            this.SlimePos = slimePos;
        }
    }

    public static class TorchPath {
        private final BlockPos TorchPos;
        private final Direction Torchfacing;
        private final BlockPos PistonPos;
        private final Direction Pistonfacing;
        private final Direction PistonBreakableFacing;
        private BlockPos slimePos;

        public TorchPath(BlockPos TorchPos, Direction Torchfacing, BlockPos PistonPos, Direction Pistonfacing, Direction PistonBreakableFacing) {
            this.TorchPos = TorchPos;
            this.Torchfacing = Torchfacing;
            this.Pistonfacing = Pistonfacing;
            this.PistonPos = PistonPos;
            this.PistonBreakableFacing = PistonBreakableFacing;
        }

        public boolean isAllPosInRange(Minecraft mc) {
            return mc.f_91074_.m_20238_(Vec3.m_82512_((Vec3i)this.TorchPos)) < (double)(MaxReach * MaxReach) && mc.f_91074_.m_20238_(Vec3.m_82512_((Vec3i)this.PistonPos)) < (double)(MaxReach * MaxReach) && (this.slimePos == null || mc.f_91074_.m_20238_(Vec3.m_82512_((Vec3i)this.slimePos)) < (double)(MaxReach * MaxReach));
        }
    }

    public static class PositionCache {
        public final BlockPos pistonPos;
        public final Direction facing;
        public final BlockPos torchPos;
        public final BlockPos targetPos;
        public long SysTime;
        public final BlockPos slimePos;
        public State state;

        private PositionCache(BlockPos pistonPos, Direction facing, BlockPos torchPos, BlockPos targetPos, BlockPos slimePos) {
            this.pistonPos = pistonPos;
            this.facing = facing;
            this.torchPos = torchPos;
            this.targetPos = targetPos;
            this.SysTime = CurrentTick;
            this.slimePos = slimePos;
            this.state = State.WAIT;
        }

        public void setFalse() {
            positionStorage.registerPos(this.pistonPos, false);
            positionStorage.registerPos(this.torchPos, false);
            if (this.slimePos != null) {
                positionStorage.registerPos(this.slimePos, false);
            }
        }

        public boolean isAllPosInRange(Minecraft mc) {
            return mc.f_91074_.m_20238_(Vec3.m_82512_((Vec3i)this.pistonPos)) < (double)(MaxReach * MaxReach) && mc.f_91074_.m_20238_(Vec3.m_82512_((Vec3i)this.torchPos)) < (double)(MaxReach * MaxReach) && mc.f_91074_.m_20238_(Vec3.m_82512_((Vec3i)this.targetPos)) < (double)(MaxReach * MaxReach) && (this.slimePos == null || mc.f_91074_.m_20238_(Vec3.m_82512_((Vec3i)this.slimePos)) < (double)(MaxReach * MaxReach));
        }

        public boolean canSafeRemove(Level world) {
            return !(this.state != State.DONE && this.state != State.CLEAR || !world.m_8055_(this.torchPos).m_60795_() || !world.m_8055_(this.pistonPos).m_60795_() || this.slimePos != null && !world.m_8055_(this.slimePos).m_60795_());
        }

        private void refresh(ClientLevel world) {
            switch (this.state) {
                case WAIT: {
                    if (CurrentTick == this.SysTime + 1L || CurrentTick > this.SysTime + 4L) {
                        this.state = State.EXTENDED;
                    }
                }
                case IDLE: {
                    if (this.SysTime + (long)LitematicaMixinMod.BEDROCK_BREAKING_CLEAR_WAIT.getIntegerValue() >= CurrentTick) break;
                    this.setFalse();
                    this.state = world.m_8055_(this.targetPos).m_60713_(Blocks.f_50752_) ? State.FAIL : State.DONE;
                }
            }
        }

        public int doSomething(Minecraft mc) {
            this.refresh(mc.f_91073_);
            switch (this.state) {
                case EXTENDED: {
                    this.processBreaking(mc);
                    return 4;
                }
                case FAIL: {
                    this.resetFailure(mc);
                    return 3;
                }
            }
            return 0;
        }

        public boolean isIdle() {
            return this.state == State.CLEAR || this.state == State.DONE;
        }

        public boolean distanceLessThan(BlockPos ReferPos, double distance) {
            int pZ;
            int pY;
            BlockPos pos = this.targetPos;
            int aX = pos.m_123341_();
            int aY = pos.m_123342_();
            int aZ = pos.m_123343_();
            int pX = ReferPos.m_123341_();
            return (double)((pX - aX) * (pX - aX) + ((pY = ReferPos.m_123342_()) - aY) * (pY - aY) + ((pZ = ReferPos.m_123343_()) - aZ) * (pZ - aZ)) < distance * distance;
        }

        public void processBreaking(Minecraft mc) {
            BedrockBreaker.switchTool(mc);
            if (this.slimePos != null && !mc.f_91073_.m_8055_(this.slimePos).m_60795_()) {
                BedrockBreaker.attackBlock(mc, this.torchPos, Direction.UP);
                BedrockBreaker.attackBlock(mc, this.slimePos, Direction.UP);
                MessageHolder.sendDebugMessage("Broke slime at " + this.slimePos.m_123344_());
            } else {
                BedrockBreaker.attackBlock(mc, this.torchPos, Direction.UP);
                MessageHolder.sendDebugMessage("Broke torch at " + this.torchPos.m_123344_());
            }
            BedrockBreaker.attackBlock(mc, this.pistonPos, Direction.UP);
            MessageHolder.sendDebugMessage("Broke piston at " + this.pistonPos.m_123344_());
            BedrockBreaker.placePiston(mc, this.pistonPos, this.facing, true);
            this.state = State.IDLE;
        }

        public void resetFailure(Minecraft mc) {
            BedrockBreaker.switchTool(mc);
            if (this.slimePos != null && !mc.f_91073_.m_8055_(this.slimePos).m_60795_()) {
                BedrockBreaker.attackBlock(mc, this.torchPos, Direction.UP);
                BedrockBreaker.attackBlock(mc, this.slimePos, Direction.UP);
                MessageHolder.sendDebugMessage("Broke slime at + (failure) " + this.slimePos.m_123344_());
            } else {
                BedrockBreaker.attackBlock(mc, this.torchPos, Direction.UP);
                MessageHolder.sendDebugMessage("Broke torch at + (failure) " + this.torchPos.m_123344_());
            }
            MessageHolder.sendDebugMessage("Broke piston at + (failure) " + this.pistonPos.m_123344_());
            BedrockBreaker.attackBlock(mc, this.pistonPos, Direction.UP);
            this.state = State.CLEAR;
            this.setFalse();
        }

        public static enum State {
            WAIT,
            EXTENDED,
            IDLE,
            FAIL,
            DONE,
            CLEAR;

        }
    }
}

