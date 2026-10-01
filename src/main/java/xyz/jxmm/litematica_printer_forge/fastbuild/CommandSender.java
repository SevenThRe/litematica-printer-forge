package xyz.jxmm.litematica_printer_forge.fastbuild;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * Sends a vanilla command from the client, with the limits vanilla itself enforces.
 *
 * <p>{@code ServerboundChatCommandPacket} serialises its command string with
 * {@code FriendlyByteBuf.writeUtf(command, 256)} and reads it back with {@code readUtf(256)}
 * (both verified from the 1.20.1 SRG jar). Anything longer is silently truncated on the wire, so
 * a command string is hard capped at 256 characters and we stay a little below that.
 *
 * <p>{@code ClientPacketListener.m_246979_} (sendUnsignedCommand) is preferred: it parses locally,
 * and when the command has no signable arguments - which /fill and /setblock never do - it sends an
 * unsigned ServerboundChatCommandPacket and returns true. Only when it returns false do we fall
 * back to {@code m_246623_} (sendCommand), which runs the signed chat chain and needs a profile key.
 */
public final class CommandSender {
    private CommandSender() {
    }

    /** Hard protocol limit, see the class javadoc. */
    public static final int PROTOCOL_LIMIT = 256;
    /** We stop at this length so the block state and NBT never get truncated away. */
    public static final int SAFE_LIMIT = 250;

    public static boolean send(Minecraft mc, String command) {
        if (mc == null || command == null || command.isEmpty() || command.length() > SAFE_LIMIT) {
            return false;
        }
        ClientPacketListener listener = mc.m_91403_();
        if (listener == null) {
            return false;
        }
        try {
            if (listener.m_246979_(command)) {
                return true;
            }
        } catch (Throwable ignored) {
            // fall through to the signed path
        }
        try {
            listener.m_246623_(command);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** "/fill x1 y1 z1 x2 y2 z2 <state>" */
    public static String fill(int x1, int y1, int z1, int x2, int y2, int z2, String stateText) {
        return "/fill " + x1 + ' ' + y1 + ' ' + z1 + ' ' + x2 + ' ' + y2 + ' ' + z2 + ' ' + stateText;
    }

    /** "/setblock x y z <state>" + optional SNBT block entity data glued straight onto the state. */
    public static String setblock(int x, int y, int z, String stateText, String snbt) {
        StringBuilder sb = new StringBuilder(96);
        sb.append("/setblock ").append(x).append(' ').append(y).append(' ').append(z).append(' ')
                .append(stateText);
        if (snbt != null && !snbt.isEmpty()) {
            sb.append(snbt);
        }
        return sb.toString();
    }
}
