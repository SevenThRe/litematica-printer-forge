package xyz.jxmm.litematica_printer_forge.fastbuild;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Chat side of the OP fast builder. Like /autobuild this never leaves the client: the event is
 * cancelled before the vanilla chat path would forward it to the server.
 *
 * <pre>
 *   /fastbuild          start (or toggle) a build of every enabled placement
 *   /fastbuild stop     abort the running job
 *   /fastbuild status   progress of the running job
 *   /fastbuild perm     what the client command tree says about our permissions
 * </pre>
 */
public final class FastBuildCommand {
    private static final String PREFIX = "/fastbuild";

    private FastBuildCommand() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new FastBuildCommand());
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        String trimmed = msg.trim();
        if (!trimmed.equals(PREFIX) && !trimmed.startsWith(PREFIX + " ")) {
            return;
        }
        event.setCanceled(true);

        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null) {
            return;
        }
        String[] parts = trimmed.split("\\s+");
        String sub = parts.length >= 2 ? parts[1].toLowerCase() : "start";

        switch (sub) {
            case "stop":
            case "cancel":
                FastBuildJob.stop(mc, true);
                return;
            case "status":
                if (FastBuildJob.isRunning()) {
                    send(mc, colored("[FastBuild] " + FastBuildJob.progressLine(), ChatFormatting.YELLOW));
                } else if (FastBuildJob.isAborted()) {
                    send(mc, colored("[FastBuild] not running (last run aborted)", ChatFormatting.RED));
                } else {
                    send(mc, colored("[FastBuild] not running", ChatFormatting.GRAY));
                }
                return;
            case "perm":
            case "permission":
                send(mc, colored("[FastBuild] " + OpGate.describe(mc), ChatFormatting.AQUA));
                return;
            case "help":
                help(mc);
                return;
            default:
                FastBuildJob.start(mc);
        }
    }

    private static void help(Minecraft mc) {
        send(mc, colored("[FastBuild] /fastbuild [start|stop|status|perm]", ChatFormatting.YELLOW));
    }

    private static void send(Minecraft mc, Component component) {
        mc.f_91074_.m_5661_(component, false);
    }

    private static MutableComponent colored(String text, ChatFormatting format) {
        return Component.m_237113_(text).m_130940_(format);
    }
}
