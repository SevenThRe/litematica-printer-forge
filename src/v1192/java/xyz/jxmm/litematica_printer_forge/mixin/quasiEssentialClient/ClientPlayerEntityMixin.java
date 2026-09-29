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
 * Real order of ClientPacketListener#m_104955_ (send) calls inside
 * Forge 1.20.1 LocalPlayer#m_108640_ (sendPosition):
 *   ordinal 0: ServerboundPlayerCommandPacket (sneak)
 *   ordinal 1: ServerboundMovePlayerPacket.PosRot (fall-flying, y=-999)
 *   ordinal 2: ServerboundMovePlayerPacket.PosRot (normal: position + rotation both change)
 *   ordinal 3: ServerboundMovePlayerPacket.Pos    (position only, no rotation, no interception needed)
 *   ordinal 4: ServerboundMovePlayerPacket.Rot    (rotation only)
 *   ordinal 5: ServerboundMovePlayerPacket.StatusOnly
 *
 * The original port reused the Fabric Yarn bytecode ordinals (2/3/5), which are shifted on Forge:
 * the standing PosRot got rewritten into a y=-999 flying packet, while the rotation-only Rot packet
 * (ordinal 4) — the one that actually overwrites the fake orientation — was left unintercepted, so the
 * server-side player orientation was overwritten by the real view direction and orientation blocks
 * such as stairs faced the wrong way.
 */
@Mixin(value = {LocalPlayer.class}, priority = 1200)
public abstract class ClientPlayerEntityMixin extends Player {
    public ClientPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile, null);
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
