package xyz.jxmm.litematica_printer_forge.fastbuild;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Hides the chat lines the fast builder's own commands produce.
 *
 * <p>Every accepted /fill answers the sender with a system chat line - "已成功填充2个方块",
 * "Successfully filled 2 block(s)", and the matching /setblock message. On a decorated build one
 * command barely covers two blocks, so a single build can fire tens of thousands of them: measured
 * 2026-10-01, 15067 commands produced 27032 chat lines, grew latest.log to 13 MB, and dragged the
 * render thread down far enough that only 49.5 commands/s went out against the 100/s the config
 * asked for.
 *
 * <p>The primary fix is {@code FastBuildJob.muteCommandFeedback}, which turns
 * {@code sendCommandFeedback} off on the server for the duration of the job so the packets are
 * never sent. This class is the belt to that pair of braces: it catches the lines anyway, in case
 * the gamerule could not be changed (a server that hands out level 2 without granting gamerule) or
 * the user turned the mute option off.
 *
 * <p>{@code ClientChatReceivedEvent} carries {@code @Cancelable} in Forge 1.20.1 - verified from
 * {@code forge-1.20.1-47.4.12-universal.jar} - so cancelling it keeps the message out of the chat
 * window and out of the log entirely, which {@code setMessage(Component.empty())} would not.
 */
public final class FastBuildFeedback {
    private FastBuildFeedback() {
    }

    /** "填充" and "方块", escaped so the literals survive a GBK javac (the shell here is cp936). */
    private static final String CN_FILL = "\u586B\u5145";
    private static final String CN_BLOCK = "\u65B9\u5757";

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new FastBuildFeedback());
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!FastBuildJob.isRunning() || !FastBuildJob.isFeedbackMuted()) {
            return;
        }
        // Only system lines. Player chat is a different subclass and must never be touched.
        if (!(event instanceof ClientChatReceivedEvent.System)) {
            return;
        }
        Component message = event.getMessage();
        if (message != null && looksLikeBuildFeedback(message.getString())) {
            event.setCanceled(true);
        }
    }

    /**
     * Matches the success message vanilla sends back for /fill and /setblock. Keyed on the rendered
     * text rather than the translation key so it still works when a server plugin rewrites the
     * component, and because {@code Component.getString()} keeps its name in SRG while the
     * contents accessor does not.
     */
    private static boolean looksLikeBuildFeedback(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("filled") && lower.contains("block")) {
            return true;
        }
        if (lower.contains("changed the block")) {
            return true;
        }
        return text.contains(CN_FILL) && text.contains(CN_BLOCK);
    }
}
