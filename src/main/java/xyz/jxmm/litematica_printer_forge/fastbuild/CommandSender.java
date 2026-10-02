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
 * back to the signed path: the old fallback sprayed doomed packets at the server.
 *
 * <p><b>Refusals are classified</b> so that one bad block cannot kill a whole build. A revoked
 * permission fails <i>every</i> command, while an unknown block fails only its own, and both look
 * identical at this level - so {@code m_246979_} returning false is reported as
 * {@link RejectKind#SKIP} and the caller distinguishes them by counting consecutive refusals
 * ({@code FastBuildJob.MAX_CONSECUTIVE_REFUSED}). Only things that cannot work at all - no
 * connection, an empty string - are {@link RejectKind#FATAL}.
 */
public final class CommandSender {
    private CommandSender() {
    }

    /** Hard protocol limit, see the class javadoc. */
    public static final int PROTOCOL_LIMIT = 256;
    /** We stop at this length so the block state and NBT never get truncated away. */
    public static final int SAFE_LIMIT = 250;

    /** How bad the most recent refusal was. */
    public enum RejectKind {
        /** Nothing was refused. */
        NONE,
        /** The command can never work - no connection. Give up on the job. */
        FATAL,
        /** Only this one command is bad - an unknown block, oversized NBT, a lost permission. The
         *  caller decides by counting how many refusals happen in a row. */
        SKIP
    }

    /** Why the most recent {@link #send} refused to dispatch, or null when it went out. */
    public static volatile String lastReject = null;
    /** How bad that refusal was; always {@link RejectKind#NONE} when the send succeeded. */
    public static volatile RejectKind lastRejectKind = RejectKind.NONE;

    public static boolean send(Minecraft mc, String command) {
        lastReject = null;
        lastRejectKind = RejectKind.NONE;
        if (mc == null || command == null || command.isEmpty()) {
            return reject(RejectKind.FATAL, "empty command");
        }
        // Normalise for callers that still build a display-style string with the slash.
        if (command.charAt(0) == '/') {
            command = command.substring(1);
            if (command.isEmpty()) {
                return reject(RejectKind.FATAL, "empty command");
            }
        }
        if (command.length() > SAFE_LIMIT) {
            return reject(RejectKind.SKIP,
                    "command is " + command.length() + " chars, over the " + SAFE_LIMIT + " limit");
        }
        ClientPacketListener listener = mc.m_91403_();
        if (listener == null) {
            return reject(RejectKind.FATAL, "not connected");
        }
        try {
            if (listener.m_246979_(command)) {
                return true;
            }
        } catch (Throwable t) {
            return reject(RejectKind.SKIP, "send threw " + t);
        }
        return reject(RejectKind.SKIP,
                "rejected by the client command tree (no permission, or a block the server rejects)");
    }

    private static boolean reject(RejectKind kind, String why) {
        lastReject = why;
        lastRejectKind = kind;
        return false;
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
