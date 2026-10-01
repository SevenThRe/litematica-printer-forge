package xyz.jxmm.litematica_printer_forge.fastbuild;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/**
 * Sends a vanilla command from the client, with the limits vanilla itself enforces.
 *
 * <p><b>The command string must NOT have a leading {@code /}.</b> {@code ChatScreen.handleChatInput}
 * strips it ({@code message.substring(1)}) before calling
 * {@code ClientPacketListener.sendCommand}, and {@code ServerboundChatCommandPacket} carries the
 * bare command on the wire. Sending {@code "/fill ..."} makes the server look for a root literal
 * literally named {@code /fill}, which does not exist, so the whole line is rejected with
 * "Unknown or incomplete command" and the cursor at position 0. (2026-10-01: that is exactly why
 * the first live test sprayed rejected commands.)
 *
 * <p>{@code ServerboundChatCommandPacket} serialises its command string with
 * {@code FriendlyByteBuf.writeUtf(command, 256)} and reads it back with {@code readUtf(256)}
 * (both verified from the 1.20.1 SRG jar). Anything longer is silently truncated on the wire, so
 * a command string is hard capped at 256 characters and we stay a little below that.
 *
 * <p>{@code ClientPacketListener.m_246979_} (sendUnsignedCommand) parses against the client command
 * tree - the same tree the server handed us - and returns <b>false</b> when the server would reject
 * it. That is a hard verdict, not a transient failure, so {@link #send} refuses instead of falling
 * back to the signed path: the old fallback sprayed doomed packets at the server. Callers can read
 * {@link #lastReject} to abort cleanly.
 */
public final class CommandSender {
    private CommandSender() {
    }

    /** Hard protocol limit, see the class javadoc. */
    public static final int PROTOCOL_LIMIT = 256;
    /** We stop at this length so the block state and NBT never get truncated away. */
    public static final int SAFE_LIMIT = 250;

    /** Why the most recent {@link #send} refused to dispatch, or null when it went out. */
    public static volatile String lastReject = null;

    public static boolean send(Minecraft mc, String command) {
        lastReject = null;
        if (mc == null || command == null || command.isEmpty()) {
            lastReject = "empty command";
            return false;
        }
        // Normalise for callers that still build a display-style string with the slash.
        if (command.charAt(0) == '/') {
            command = command.substring(1);
            if (command.isEmpty()) {
                lastReject = "empty command";
                return false;
            }
        }
        if (command.length() > SAFE_LIMIT) {
            lastReject = "command is " + command.length() + " chars, over the " + SAFE_LIMIT + " limit";
            return false;
        }
        ClientPacketListener listener = mc.m_91403_();
        if (listener == null) {
            lastReject = "not connected";
            return false;
        }
        try {
            if (listener.m_246979_(command)) {
                return true;
            }
            // The client tree mirrors the server's: a local parse failure means the server would
            // reject it too. Refuse rather than send.
            lastReject = "rejected by the client command tree (need OP level 2 for /fill and /setblock)";
            return false;
        } catch (Throwable t) {
            lastReject = "send failed: " + t;
            return false;
        }
    }

    /** "fill x1 y1 z1 x2 y2 z2 &lt;state&gt;" (no leading slash - see the class javadoc). */
    public static String fill(int x1, int y1, int z1, int x2, int y2, int z2, String stateText) {
        return "fill " + x1 + ' ' + y1 + ' ' + z1 + ' ' + x2 + ' ' + y2 + ' ' + z2 + ' ' + stateText;
    }

    /** "setblock x y z &lt;state&gt;" + optional SNBT block entity data glued straight onto the state. */
    public static String setblock(int x, int y, int z, String stateText, String snbt) {
        StringBuilder sb = new StringBuilder(96);
        sb.append("setblock ").append(x).append(' ').append(y).append(' ').append(z).append(' ')
                .append(stateText);
        if (snbt != null && !snbt.isEmpty()) {
            sb.append(snbt);
        }
        return sb.toString();
    }
}
