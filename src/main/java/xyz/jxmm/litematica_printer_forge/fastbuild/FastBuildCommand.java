package xyz.jxmm.litematica_printer_forge.fastbuild;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Chat command for the OP fast builder.
 *
 * <p><b>Why a real client command and not a {@code ClientChatEvent} interceptor?</b>
 * In 1.20.1 {@code ChatScreen.handleChatInput} routes anything starting with {@code /} to
 * {@code ClientPacketListener.sendCommand} and only plain chat to {@code sendChat}. Forge posts
 * {@code ClientChatEvent} from {@code ForgeHooksClient.onClientSendMessage}, which is called by
 * {@code sendChat} <i>only</i> — so a chat-event handler can never see a command. The upstream
 * {@code ClientPacketListener.sendCommand} patch instead asks
 * {@code net.minecraftforge.client.ClientCommandHandler.runCommand(String)} first and returns
 * without sending the packet when it handled the line, which is exactly what we want. Commands
 * registered through {@link RegisterClientCommandsEvent} (posted on {@link MinecraftForge#EVENT_BUS}
 * from {@code ClientCommandHandler.mergeServerCommands}) therefore run purely client side.
 *
 * <pre>
 *   /fastbuild          start a build of every gap in the current blueprint
 *   /fastbuild start    same as above
 *   /fastbuild stop     abort the running job
 *   /fastbuild status   progress of the running job
 *   /fastbuild perm     what the client command tree says about our permissions
 *   /fastbuild help     usage
 * </pre>
 */
public final class FastBuildCommand {
    private static final String NAME = "fastbuild";

    private FastBuildCommand() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new FastBuildCommand());
    }

    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.m_82127_(NAME);
        // bare /fastbuild -> start
        root.executes(FastBuildCommand::runStart);
        root.then(Commands.m_82127_("start").executes(FastBuildCommand::runStart));
        root.then(Commands.m_82127_("stop").executes(FastBuildCommand::runStop));
        root.then(Commands.m_82127_("cancel").executes(FastBuildCommand::runStop));
        root.then(Commands.m_82127_("status").executes(FastBuildCommand::runStatus));
        root.then(Commands.m_82127_("perm").executes(FastBuildCommand::runPerm));
        root.then(Commands.m_82127_("permission").executes(FastBuildCommand::runPerm));
        root.then(Commands.m_82127_("help").executes(FastBuildCommand::runHelp));

        dispatcher.register(root);
    }

    private static int runStart(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc == null || mc.f_91074_ == null) {
            return 0;
        }
        FastBuildJob.start(mc);
        return 1;
    }

    private static int runStop(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc == null || mc.f_91074_ == null) {
            return 0;
        }
        FastBuildJob.stop(mc, true);
        return 1;
    }

    private static int runStatus(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc == null || mc.f_91074_ == null) {
            return 0;
        }
        if (FastBuildJob.isRunning()) {
            send(mc, colored("[FastBuild] " + FastBuildJob.progressLine(), ChatFormatting.YELLOW));
        } else if (FastBuildJob.isAborted()) {
            send(mc, colored("[FastBuild] not running (last run aborted)", ChatFormatting.RED));
        } else {
            send(mc, colored("[FastBuild] not running", ChatFormatting.GRAY));
        }
        return 1;
    }

    private static int runPerm(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc == null || mc.f_91074_ == null) {
            return 0;
        }
        send(mc, colored("[FastBuild] " + OpGate.describe(mc), ChatFormatting.AQUA));
        return 1;
    }

    private static int runHelp(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc == null || mc.f_91074_ == null) {
            return 0;
        }
        send(mc, colored("[FastBuild] /fastbuild [start|stop|status|perm]", ChatFormatting.YELLOW));
        return 1;
    }

    private static void send(Minecraft mc, Component component) {
        mc.f_91074_.m_5661_(component, false);
    }

    private static MutableComponent colored(String text, ChatFormatting format) {
        return Component.m_237113_(text).m_130940_(format);
    }
}
