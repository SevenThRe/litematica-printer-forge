package xyz.jxmm.litematica_printer_forge.mixin.quasiEssentialClient;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.jxmm.litematica_printer_forge.utils.FakeAccurateBlockPlacement;

/*
 * Forge 1.20.1 LocalPlayer#m_108640_ (sendPosition) 中
 * ClientPacketListener#m_104955_ (send) 调用的真实顺序：
 *   ordinal 0: ServerboundPlayerCommandPacket (sneak)
 *   ordinal 1: ServerboundMovePlayerPacket.PosRot (fall-flying, y=-999)
 *   ordinal 2: ServerboundMovePlayerPacket.PosRot (正常：位置+朝向都变化)
 *   ordinal 3: ServerboundMovePlayerPacket.Pos    (仅位置变化，不带朝向，无需拦截)
 *   ordinal 4: ServerboundMovePlayerPacket.Rot    (仅朝向变化)
 *   ordinal 5: ServerboundMovePlayerPacket.StatusOnly
 *
 * 原移植版沿用 Fabric Yarn 字节码的 ordinal(2/3/5)，在 Forge 上错位：
 * 站立 PosRot 被改成 y=-999 的飞行包，而真正会覆盖假朝向的仅朝向 Rot 包(ordinal 4)
 * 反而没有被拦截，导致服务器端玩家朝向被真实视角覆盖，楼梯等朝向方块方向错误。
 */
@Mixin(value = {LocalPlayer.class}, priority = 1200)
public abstract class ClientPlayerEntityMixin extends Player {
    public ClientPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Redirect(method = {"sendPosition"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1), require = 0)
    private void onSendPacketFlying(ClientPacketListener clientPlayNetworkHandler, Packet<?> packet) {
        if (FakeAccurateBlockPlacement.requestedTicks <= -3 || FakeAccurateBlockPlacement.fakeDirection == null) {
            clientPlayNetworkHandler.m_104955_(packet);
            return;
        }
        clientPlayNetworkHandler.m_104955_((Packet) new ServerboundMovePlayerPacket.PosRot(this.m_20185_(), -999.0, this.m_20189_(), FakeAccurateBlockPlacement.fakeYaw, FakeAccurateBlockPlacement.fakePitch, this.m_20096_()));
    }

    @Redirect(method = {"sendPosition"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 2), require = 0)
    private void onSendPacketAll(ClientPacketListener clientPlayNetworkHandler, Packet<?> packet) {
        if (FakeAccurateBlockPlacement.requestedTicks <= -3 || FakeAccurateBlockPlacement.fakeDirection == null) {
            clientPlayNetworkHandler.m_104955_(packet);
            return;
        }
        clientPlayNetworkHandler.m_104955_((Packet) new ServerboundMovePlayerPacket.PosRot(this.m_20185_(), this.m_20186_(), this.m_20189_(), FakeAccurateBlockPlacement.fakeYaw, FakeAccurateBlockPlacement.fakePitch, this.m_20096_()));
    }

    @Redirect(method = {"sendPosition"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 4), require = 0)
    private void onSendPacketLook(ClientPacketListener clientPlayNetworkHandler, Packet<?> packet) {
        if (FakeAccurateBlockPlacement.requestedTicks <= -3 || FakeAccurateBlockPlacement.fakeDirection == null) {
            clientPlayNetworkHandler.m_104955_(packet);
            return;
        }
        clientPlayNetworkHandler.m_104955_((Packet) new ServerboundMovePlayerPacket.Rot(FakeAccurateBlockPlacement.fakeYaw, FakeAccurateBlockPlacement.fakePitch, this.m_20096_()));
    }
}
