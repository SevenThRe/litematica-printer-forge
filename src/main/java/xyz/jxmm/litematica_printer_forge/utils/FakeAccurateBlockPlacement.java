/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  fi.dy.masa.litematica.materials.MaterialCache
 *  fi.dy.masa.litematica.world.SchematicWorldHandler
 *  fi.dy.masa.malilib.util.BlockUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Rot
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BaseRailBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.DirectionalBlock
 *  net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock
 *  net.minecraft.world.level.block.GrindstoneBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.RedstoneWallTorchBlock
 *  net.minecraft.world.level.block.TorchBlock
 *  net.minecraft.world.level.block.TrapDoorBlock
 *  net.minecraft.world.level.block.WallTorchBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.AttachFace
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Half
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 */
package xyz.jxmm.litematica_printer_forge.utils;

import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.util.BlockUtils;
import java.util.Date;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.FacingData;
import xyz.jxmm.litematica_printer_forge.utils.InventoryUtils;
import xyz.jxmm.litematica_printer_forge.utils.MessageHolder;
import xyz.jxmm.litematica_printer_forge.utils.Printer;

public class FakeAccurateBlockPlacement {
    public static Direction fakeDirection = null;
    public static boolean shouldReturnValue = false;
    public static int requestedTicks = -3;
    public static float fakeYaw = 0.0f;
    public static float fakePitch = 0.0f;
    private static BlockState stateGrindStone = null;
    private static float previousFakeYaw = 0.0f;
    private static float previousFakePitch = 0.0f;
    private static int tickElapsed = 0;
    private static int blockPlacedInTick = 0;
    private static BlockState handlingState = null;
    public static Item currentHandling = Items.f_41852_;
    private static final Queue<PosWithBlock> waitingQueue = new ArrayBlockingQueue<PosWithBlock>(1){};
    private static final HashSet<Block> warningSet = new HashSet();

    public static boolean isHandling() {
        return requestedTicks > 0;
    }

    public static boolean canHandleOther() {
        return currentHandling == null || currentHandling == Items.f_41852_;
    }

    public static boolean canHandleOther(Item item) {
        if (FakeAccurateBlockPlacement.canHandleOther()) {
            return true;
        }
        return currentHandling == item;
    }

    public static void tick(ClientPacketListener clientPlayNetworkHandler, LocalPlayer playerEntity) {
        tickElapsed = 0;
        if (playerEntity == null || clientPlayNetworkHandler == null) {
            requestedTicks = -3;
            handlingState = null;
            fakeDirection = null;
            return;
        }
        if (requestedTicks >= -1 && (fakeYaw != previousFakeYaw || fakePitch != previousFakePitch)) {
            FakeAccurateBlockPlacement.sendLookPacket(clientPlayNetworkHandler, playerEntity);
            previousFakePitch = fakePitch;
            previousFakeYaw = fakeYaw;
        }
        if (requestedTicks <= -1) {
            currentHandling = Items.f_41852_;
            stateGrindStone = null;
            handlingState = null;
        }
        if (requestedTicks <= -3) {
            requestedTicks = -3;
            fakeDirection = null;
            previousFakePitch = playerEntity.m_146909_();
            previousFakeYaw = playerEntity.m_146908_();
        }
        if (requestedTicks == 0 && LitematicaMixinMod.PRINTER_ONLY_FAKE_ROTATION_MODE.getBooleanValue()) {
            FakeAccurateBlockPlacement.placeFromQueue();
        }
        --requestedTicks;
        blockPlacedInTick = 0;
    }

    public static void placeFromQueue() {
        if (requestedTicks > 0) {
            MessageHolder.sendOrderMessage("Requested tick was " + requestedTicks);
            return;
        }
        PosWithBlock obj = waitingQueue.poll();
        if (obj != null) {
            MessageHolder.sendOrderMessage("found block to place");
            if (FakeAccurateBlockPlacement.canPlace(obj.blockState, obj.pos)) {
                FakeAccurateBlockPlacement.placeBlock(obj.pos, obj.blockState);
                return;
            }
            MessageHolder.sendOrderMessage("found block to place but can't place");
        }
        waitingQueue.clear();
    }

    public static boolean emptyWaitingQueue() {
        if (requestedTicks > 0) {
            return false;
        }
        PosWithBlock obj = waitingQueue.poll();
        if (obj != null && FakeAccurateBlockPlacement.canPlace(obj.blockState, obj.pos)) {
            return FakeAccurateBlockPlacement.placeBlock(obj.pos, obj.blockState);
        }
        waitingQueue.clear();
        return false;
    }

    public static void sendLookPacket(ClientPacketListener networkHandler, LocalPlayer playerEntity) {
        networkHandler.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(fakeYaw, fakePitch, playerEntity.m_20096_()));
    }

    public static boolean request(float yaw, float pitch, Direction direction, int duration, boolean force) {
        if (FakeAccurateBlockPlacement.isHandling() && !force) {
            return false;
        }
        fakeDirection = direction;
        fakeYaw = yaw;
        fakePitch = pitch;
        requestedTicks = duration;
        Minecraft minecraftClient = Minecraft.m_91087_();
        ClientPacketListener networkHandler = minecraftClient.m_91403_();
        LocalPlayer playerEntity = minecraftClient.f_91074_;
        if (networkHandler != null && playerEntity != null) {
            FakeAccurateBlockPlacement.sendLookPacket(networkHandler, playerEntity);
            return true;
        }
        return false;
    }

    private static boolean canPlaceWallMounted(BlockState blockState) {
        if (blockState.m_60734_() instanceof TorchBlock) {
            if (blockState.m_60734_() instanceof WallTorchBlock || blockState.m_60734_() instanceof RedstoneWallTorchBlock) {
                return fakeDirection == ((Direction)blockState.m_61143_((Property)WallTorchBlock.f_58119_)).m_122424_();
            }
            return fakeDirection == Direction.DOWN;
        }
        if (blockState.m_60734_() instanceof FaceAttachedHorizontalDirectionalBlock) {
            AttachFace location = (AttachFace)blockState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_);
            if (location == AttachFace.WALL) {
                return true;
            }
            Direction facingSecond = (Direction)blockState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_);
            return fakeDirection == facingSecond;
        }
        return true;
    }

    private static boolean requestGrindStone(BlockState state, BlockPos blockPos) {
        Direction lookRefdir;
        Direction facing = (Direction)state.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_);
        AttachFace location = (AttachFace)state.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_);
        float fy = 0.0f;
        float fp = 0.0f;
        if (location == AttachFace.CEILING) {
            fp = -90.0f;
            lookRefdir = facing;
        } else if (location == AttachFace.FLOOR) {
            fp = 90.0f;
            lookRefdir = facing;
        } else {
            fp = 0.0f;
            lookRefdir = facing.m_122424_();
        }
        if (lookRefdir == Direction.EAST) {
            fy = -87.0f;
        } else if (lookRefdir == Direction.WEST) {
            fy = 87.0f;
        } else if (lookRefdir == Direction.NORTH) {
            fy = 177.0f;
        } else if (lookRefdir == Direction.SOUTH) {
            fy = 3.0f;
        }
        if (FakeAccurateBlockPlacement.isHandling()) {
            if (requestedTicks <= 0 && stateGrindStone != null && FakeAccurateBlockPlacement.canPlace(state, blockPos)) {
                FakeAccurateBlockPlacement.placeBlock(blockPos, state);
                return true;
            }
            return false;
        }
        stateGrindStone = state;
        if (waitingQueue.isEmpty()) {
            if (waitingQueue.offer(new PosWithBlock(blockPos, state))) {
                FakeAccurateBlockPlacement.request(fy, fp, lookRefdir, LitematicaMixinMod.FAKE_ROTATION_TICKS.getIntegerValue(), false);
            }
            return true;
        }
        return false;
    }

    /**
     * Minecraft yaw convention: 0 = SOUTH, 90 = WEST, 180 = NORTH, 270 = EAST.
     * Only used to record a direction alongside a raw yaw (skulls need the exact yaw, the
     * 16 ROTATION steps cannot be expressed with the 6 directions).
     */
    private static Direction yawToDirection(float yaw) {
        float normalized = ((yaw % 360.0f) + 360.0f) % 360.0f;
        if (normalized >= 45.0f && normalized < 135.0f) {
            return Direction.WEST;
        }
        if (normalized >= 135.0f && normalized < 225.0f) {
            return Direction.NORTH;
        }
        if (normalized >= 225.0f && normalized < 315.0f) {
            return Direction.EAST;
        }
        return Direction.SOUTH;
    }

    public static Direction getPlayerFacing() {
        if (fakeYaw == -87.0f) {
            return Direction.EAST;
        }
        if (fakeYaw == 87.0f) {
            return Direction.WEST;
        }
        if (fakeYaw == 177.0f) {
            return Direction.NORTH;
        }
        if (fakeYaw == 3.0f) {
            return Direction.SOUTH;
        }
        return null;
    }

    public static Direction[] getFacingOrder() {
        Direction directionZ;
        float theta = fakePitch * ((float)Math.PI / 180);
        float omega = -fakeYaw * ((float)Math.PI / 180);
        float unitHorizontal = Mth.m_14089_((float)theta);
        float yVector = -Mth.m_14031_((float)theta);
        float xVector = unitHorizontal * Mth.m_14031_((float)omega);
        float zVector = unitHorizontal * Mth.m_14089_((float)omega);
        float yScalar = Math.abs(yVector);
        float xScalar = Math.abs(xVector);
        float zScalar = Math.abs(zVector);
        Direction directionX = xVector > 0.0f ? Direction.EAST : Direction.WEST;
        Direction directionY = yVector > 0.0f ? Direction.UP : Direction.DOWN;
        Direction direction = directionZ = zVector > 0.0f ? Direction.SOUTH : Direction.NORTH;
        if (xScalar > zScalar) {
            if (yScalar > xScalar) {
                return FakeAccurateBlockPlacement.listClosest(directionY, directionX, directionZ);
            }
            return zScalar > yScalar ? FakeAccurateBlockPlacement.listClosest(directionX, directionZ, directionY) : FakeAccurateBlockPlacement.listClosest(directionX, directionY, directionZ);
        }
        if (yScalar > zScalar) {
            return FakeAccurateBlockPlacement.listClosest(directionY, directionZ, directionX);
        }
        return xScalar > yScalar ? FakeAccurateBlockPlacement.listClosest(directionZ, directionX, directionY) : FakeAccurateBlockPlacement.listClosest(directionZ, directionY, directionX);
    }

    private static Direction[] listClosest(Direction first, Direction second, Direction third) {
        return new Direction[]{first, second, third, third.m_122424_(), second.m_122424_(), first.m_122424_()};
    }

    public static synchronized boolean request(BlockState blockState, BlockPos blockPos) {
        if (!FakeAccurateBlockPlacement.canPlace(blockState, blockPos) || blockState.m_60795_() || MaterialCache.getInstance().getRequiredBuildItemForState(blockState, (Level)SchematicWorldHandler.getSchematicWorld(), blockPos).m_41720_() == Items.f_41852_) {
            MessageHolder.sendOrderMessage("Cannot place " + blockState.toString() + " at " + blockPos.m_123344_());
            return false;
        }
        if (blockState.m_60713_(Blocks.f_50623_)) {
            return FakeAccurateBlockPlacement.requestGrindStone(blockState, blockPos);
        }
        // Standing skulls keep their orientation in ROTATION (0-15), which the game derives from the
        // player's yaw at placement time. They carry no facing property, so without an explicit fake
        // rotation the skull simply copies wherever the player happens to be looking.
        if (blockState.m_60734_() instanceof SkullBlock && !(blockState.m_60734_() instanceof WallSkullBlock) && blockState.m_61138_((Property)SkullBlock.f_56314_)) {
            float skullYaw = (float)((Integer)blockState.m_61143_((Property)SkullBlock.f_56314_)).intValue() * 22.5f;
            float skullPitch = 12.0f;
            Direction skullDir = FakeAccurateBlockPlacement.yawToDirection(skullYaw);
            if (requestedTicks <= 0 && fakeYaw == skullYaw && fakePitch == skullPitch) {
                FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
                return true;
            }
            if (FakeAccurateBlockPlacement.isHandling()) {
                MessageHolder.sendOrderMessage("Cannot handle " + String.valueOf(blockState) + " at " + blockPos.m_123344_());
                return false;
            }
            if (waitingQueue.isEmpty()) {
                FakeAccurateBlockPlacement.request(skullYaw, skullPitch, skullDir, LitematicaMixinMod.FAKE_ROTATION_TICKS.getIntegerValue(), false);
                FakeAccurateBlockPlacement.pickFirst(blockState, blockPos);
                waitingQueue.offer(new PosWithBlock(blockPos, blockState));
                return false;
            }
            FakeAccurateBlockPlacement.placeFromQueue();
            return false;
        }
        if (blockState.m_60713_(Blocks.f_50332_) || blockState.m_204336_(BlockTags.f_13083_) || blockState.m_60713_(Blocks.f_152587_) || blockState.m_60713_(Blocks.f_50489_)) {
            FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            return true;
        }
        if (!(blockState.m_61138_((Property)BlockStateProperties.f_61372_) || blockState.m_61138_((Property)BlockStateProperties.f_61374_) || blockState.m_60734_() instanceof BaseRailBlock || blockState.m_60734_() instanceof TorchBlock)) {
            FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            return true;
        }
        FacingData facingData = FacingData.getFacingData(blockState);
        if (facingData == null && !(blockState.m_60734_() instanceof BaseRailBlock) && !(blockState.m_60734_() instanceof TorchBlock)) {
            if (!warningSet.contains(blockState.m_60734_())) {
                warningSet.add(blockState.m_60734_());
                System.out.printf("WARN : Block %s is not found\n", blockState.m_60734_().toString());
            }
            FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            return true;
        }
        Direction facing = BlockUtils.getFirstPropertyFacingValue((BlockState)blockState);
        if (facing == null && blockState.m_60734_() instanceof BaseRailBlock) {
            facing = Printer.convertRailShapetoFace(blockState);
        } else if (blockState.m_60734_() instanceof TorchBlock) {
            facing = blockState.m_60734_() instanceof WallTorchBlock || blockState.m_60734_() instanceof RedstoneWallTorchBlock ? ((Direction)blockState.m_61143_((Property)WallTorchBlock.f_58119_)).m_122424_() : Direction.DOWN;
        }
        if (facing == null) {
            FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            return true;
        }
        boolean reversed = facingData != null && facingData.isReversed;
        int order = facingData == null ? 0 : facingData.type;
        Direction direction1 = facing;
        float fy = 0.0f;
        float fp = 12.0f;
        if (order == 0 || order == 1) {
            direction1 = reversed ? facing.m_122424_() : facing;
        } else if (order == 2) {
            facing = (Direction)blockState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_54117_);
            Direction direction = direction1 = blockState.m_61138_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) && blockState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.WALL ? facing.m_122424_() : facing;
            fp = blockState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.CEILING ? -90.0f : (blockState.m_61143_((Property)FaceAttachedHorizontalDirectionalBlock.f_53179_) == AttachFace.FLOOR ? 90.0f : 12.0f);
        } else if (order == 3) {
            direction1 = facing.m_122428_();
        }
        if (order != 2 && (direction1 == null || requestedTicks <= 0 && fakeDirection == direction1 && fy == fakeYaw && fp == fakePitch) && FakeAccurateBlockPlacement.canPlaceWallMounted(blockState)) {
            FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            return true;
        }
        Direction lookRefdir = direction1;
        if (lookRefdir == Direction.UP) {
            fp = -90.0f;
        } else if (lookRefdir == Direction.DOWN) {
            fp = 90.0f;
        } else if (lookRefdir == Direction.EAST) {
            fy = -87.0f;
        } else if (lookRefdir == Direction.WEST) {
            fy = 87.0f;
        } else if (lookRefdir == Direction.NORTH) {
            fy = 177.0f;
        } else if (lookRefdir == Direction.SOUTH) {
            fy = 3.0f;
        } else {
            fy = 0.0f;
            fp = 12.0f;
        }
        if (LitematicaMixinMod.FAKE_ROTATION_TICKS.getIntegerValue() == 0) {
            if (lookRefdir != fakeDirection) {
                if (tickElapsed > LitematicaMixinMod.FAKE_ROTATION_LIMIT.getIntegerValue()) {
                    MessageHolder.sendDebugMessage("Failure because limited fake rotation per tick " + blockPos.m_123344_());
                    return false;
                }
                ++tickElapsed;
                FakeAccurateBlockPlacement.request(fy, fp, lookRefdir, LitematicaMixinMod.FAKE_ROTATION_TICKS.getIntegerValue(), true);
                FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            } else {
                FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            }
            return true;
        }
        if (FakeAccurateBlockPlacement.isHandling() && (lookRefdir != fakeDirection || fp != fakePitch || fy != fakeYaw || !FakeAccurateBlockPlacement.canPlaceWallMounted(blockState))) {
            MessageHolder.sendOrderMessage("Cannot handle " + String.valueOf(blockState) + " at " + blockPos.m_123344_());
            return false;
        }
        if (requestedTicks <= 0 && fakeDirection == lookRefdir && fp == fakePitch && fy == fakeYaw) {
            FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            return true;
        }
        if (waitingQueue.isEmpty()) {
            FakeAccurateBlockPlacement.request(fy, fp, lookRefdir, LitematicaMixinMod.FAKE_ROTATION_TICKS.getIntegerValue(), false);
            FakeAccurateBlockPlacement.pickFirst(blockState, blockPos);
            boolean offered = waitingQueue.offer(new PosWithBlock(blockPos, blockState));
            if (offered) {
                MessageHolder.sendOrderMessage("Offered " + String.valueOf(blockState) + " at " + blockPos.m_123344_());
            } else {
                MessageHolder.sendOrderMessage("Cannot offer " + String.valueOf(blockState) + " at " + blockPos.m_123344_());
            }
            return false;
        }
        PosWithBlock queued = waitingQueue.peek();
        MessageHolder.sendOrderMessage("Queue is holding " + String.valueOf(queued.blockState) + " at " + queued.pos.m_123344_());
        FakeAccurateBlockPlacement.placeFromQueue();
        queued = waitingQueue.peek();
        if (queued != null) {
            MessageHolder.sendOrderMessage("Tried emptying queue but still holding " + String.valueOf(queued.blockState) + " at " + queued.pos.m_123344_());
            return false;
        }
        // placeFromQueue just placed ANOTHER block using ITS fake rotation. Placing this block now
        // would use the previous block's stale rotation, so it gets oriented wrongly and the
        // directional-state check breaks it again next tick - an endless place/break loop
        // (barrel + fence gate next to each other, trapdoors). Only place when the rotation that
        // was requested for THIS block is already active; otherwise wait a tick.
        if (requestedTicks <= 0 && fakeDirection == lookRefdir && fp == fakePitch && fy == fakeYaw) {
            FakeAccurateBlockPlacement.placeBlock(blockPos, blockState);
            return true;
        }
        return false;
    }

    public static boolean canPlace(BlockState state, BlockPos pos) {
        if (!LitematicaMixinMod.FAKE_ROTATION_BETA.getBooleanValue()) {
            return true;
        }
        if (FakeAccurateBlockPlacement.canHandleOther(MaterialCache.getInstance().getRequiredBuildItemForState(state, (Level)SchematicWorldHandler.getSchematicWorld(), pos).m_41720_())) {
            if (state.m_60713_(Blocks.f_50623_)) {
                // stateGrindStone is only assigned inside requestGrindStone(), and requestGrindStone()
                // is only reached once this check has already passed - returning false here deadlocks,
                // so the grindstone never gets placed at all. Allow the first pass.
                if (stateGrindStone == null) {
                    return true;
                }
                return stateGrindStone.m_61143_((Property)GrindstoneBlock.f_53179_) == state.m_61143_((Property)GrindstoneBlock.f_53179_) && stateGrindStone.m_61143_((Property)GrindstoneBlock.f_54117_) == state.m_61143_((Property)GrindstoneBlock.f_54117_);
            }
            if (handlingState != null && (handlingState.m_60734_() instanceof DirectionalBlock || handlingState.m_60734_() instanceof HorizontalDirectionalBlock && !(handlingState.m_60734_() instanceof FaceAttachedHorizontalDirectionalBlock))) {
                Direction other;
                Direction handling = BlockUtils.getFirstPropertyFacingValue((BlockState)handlingState);
                return handling == (other = BlockUtils.getFirstPropertyFacingValue((BlockState)state));
            }
            return true;
        }
        return false;
    }

    private static synchronized boolean placeBlock(BlockPos pos, BlockState blockState) {
        if (!FakeAccurateBlockPlacement.pickFirst(blockState, pos)) {
            MessageHolder.sendDebugMessage("Cannot pick block for " + pos.m_123344_());
            return false;
        }
        MessageHolder.sendDebugMessage("Handling placeBlock for " + pos.m_123344_() + " and state " + blockState.toString());
        if (blockPlacedInTick > LitematicaMixinMod.PRINTER_MAX_BLOCKS.getIntegerValue()) {
            MessageHolder.sendDebugMessage("Handling placeBlock failed due to limiting max block" + pos.m_123344_());
            return false;
        }
        Minecraft minecraftClient = Minecraft.m_91087_();
        LocalPlayer player = minecraftClient.f_91074_;
        MultiPlayerGameMode interactionManager = minecraftClient.f_91072_;
        if (!minecraftClient.f_91073_.m_8055_(pos).m_247087_()) {
            MessageHolder.sendDebugMessage("Client block position was not replaceable at " + pos.m_123344_());
            return true;
        }
        Direction sideOrig = Direction.NORTH;
        Direction side = Printer.applyPlacementFacing(blockState, sideOrig, minecraftClient.f_91073_.m_8055_(pos));
        Vec3 appliedHitVec = Printer.applyHitVec(pos, blockState, side);
        if (blockState.m_60734_() instanceof TrapDoorBlock) {
            side = blockState.m_61143_((Property)TrapDoorBlock.f_57515_) == Half.BOTTOM ? Direction.UP : Direction.DOWN;
            // Hit slightly INSIDE the block on the clicked-face plane instead of the exact block
            // corner: a boundary-corner hit vector can be rejected by server-side click validation.
            boolean bottomHalf = blockState.m_61143_((Property)TrapDoorBlock.f_57515_) == Half.BOTTOM;
            appliedHitVec = new Vec3((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + (bottomHalf ? 0.1 : 0.9), (double)pos.m_123343_() + 0.5);
        } else if (blockState.m_60734_() instanceof GrindstoneBlock) {
            appliedHitVec = Vec3.m_82512_((Vec3i)pos);
            if (blockState.m_61143_((Property)GrindstoneBlock.f_53179_) == AttachFace.CEILING) {
                side = Direction.DOWN;
            } else if (blockState.m_61143_((Property)GrindstoneBlock.f_53179_) == AttachFace.FLOOR) {
                side = Direction.UP;
            }
        } else if (blockState.m_60734_() instanceof TorchBlock) {
            appliedHitVec = Vec3.m_82512_((Vec3i)pos);
        }
        BlockHitResult blockHitResult = new BlockHitResult(appliedHitVec, side, pos, true);
        ItemStack pickedItem = MaterialCache.getInstance().getRequiredBuildItemForState(blockState, (Level)SchematicWorldHandler.getSchematicWorld(), pos);
        if (pickedItem.m_41720_() == currentHandling && Printer.doSchematicWorldPickBlock(minecraftClient, blockState, pos)) {
            MessageHolder.sendOrderMessage("Placing " + blockState.m_60734_().m_7705_() + " at " + pos.m_123344_() + " stack at hand is " + String.valueOf(player.m_21205_()));
            MessageHolder.sendDebugMessage(player, "Placing " + blockState.m_60734_().m_7705_() + " at " + pos.m_123344_() + " facing : " + String.valueOf(BlockUtils.getFirstPropertyFacingValue((BlockState)blockState)));
            MessageHolder.sendDebugMessage(player, "Player facing is set to : " + String.valueOf(fakeDirection) + " Yaw : " + fakeYaw + " Pitch : " + fakePitch + " ticks : " + requestedTicks + " for pos " + pos.m_123344_());
            interactionManager.m_233732_(player, InteractionHand.MAIN_HAND, blockHitResult);
            InventoryUtils.decrementCount(player.m_150110_().f_35937_);
            ++blockPlacedInTick;
            if (!player.m_150110_().f_35937_ && InventoryUtils.lastCount <= 0 && LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue() > 0) {
                shouldReturnValue = true;
                Printer.lastPlaced = new Date().getTime() + (long)LitematicaMixinMod.SLEEP_AFTER_CONSUME.getIntegerValue();
            }
            Printer.cacheEasyPlacePosition(pos, false);
            return true;
        }
        MessageHolder.sendDebugMessage("Handling placeBlock failed due to pickBlock assertion failure" + pos.m_123344_() + " wanted item :" + String.valueOf(pickedItem.m_41720_()) + " current handling : " + String.valueOf(currentHandling.m_5456_()));
        return false;
    }

    private static boolean pickFirst(BlockState blockState, BlockPos pos) {
        Minecraft minecraftClient = Minecraft.m_91087_();
        if (Printer.doSchematicWorldPickBlock(minecraftClient, blockState, pos)) {
            currentHandling = MaterialCache.getInstance().getRequiredBuildItemForState(blockState, (Level)SchematicWorldHandler.getSchematicWorld(), pos).m_41720_();
            handlingState = blockState;
            requestedTicks = 0;
            return true;
        }
        return false;
    }

    private record PosWithBlock(BlockPos pos, BlockState blockState) {
    }
}

