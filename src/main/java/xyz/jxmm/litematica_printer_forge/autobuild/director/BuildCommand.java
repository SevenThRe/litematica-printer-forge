package xyz.jxmm.litematica_printer_forge.autobuild.director;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.jxmm.litematica_printer_forge.autobuild.planner.AutoBuildPlanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Chat command handler for /autobuild (FR-25).
 *
 * <p><b>History / why this is a real client command.</b> This used to intercept
 * {@code ClientChatEvent}, which never worked: in 1.20.1 {@code ChatScreen.handleChatInput} sends
 * {@code /}-prefixed lines through {@code ClientPacketListener.sendCommand}, and Forge only posts
 * {@code ClientChatEvent} from the plain-chat path ({@code ForgeHooksClient.onClientSendMessage}
 * called by {@code sendChat}). {@code sendCommand} instead consults
 * {@code net.minecraftforge.client.ClientCommandHandler.runCommand(String)} first and skips the
 * packet when it returns {@code true} — so registering through
 * {@link RegisterClientCommandsEvent} is the only way to get a client-only command. (Verified
 * 2026-10-01: the old handler was dead code; the hotkey was the only working entry point.)
 *
 * <pre>
 *   /autobuild          start build flow (with multi-schematic picker when needed)
 *   /autobuild pick N   choose schematic N from the pending list
 *   /autobuild cancel   abort picker
 * </pre>
 *
 * When multiple enabled placements exist, prints a clickable chat list. Each entry uses
 * {@code ClickEvent.RUN_COMMAND} + {@code HoverEvent.SHOW_TEXT} for the summary. Every command
 * stays on the client; nothing is forwarded to the server.
 */
public final class BuildCommand {
    private static final String NAME = "autobuild";
    private static List<SchematicPlacement> pendingChoices = null;

    private BuildCommand() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new BuildCommand());
    }

    @SubscribeEvent
    public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> root = Commands.m_82127_(NAME);
        root.executes(BuildCommand::runStart);
        root.then(Commands.m_82127_("start").executes(BuildCommand::runStart));
        root.then(Commands.m_82127_("cancel").executes(BuildCommand::runCancel));
        root.then(Commands.m_82127_("pick")
                .then(Commands.m_82129_("index", IntegerArgumentType.integer(0))
                        .executes(BuildCommand::runPick)));

        dispatcher.register(root);
    }

    private static int runStart(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc == null || mc.f_91074_ == null) {
            return 0;
        }
        startBuildFlow(mc);
        return 1;
    }

    private static int runCancel(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc == null || mc.f_91074_ == null) {
            return 0;
        }
        pendingChoices = null;
        sendLocal(mc, text("[AutoBuild] Cancelled").m_130940_(ChatFormatting.GRAY));
        return 1;
    }

    private static int runPick(CommandContext<CommandSourceStack> ctx) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc == null || mc.f_91074_ == null) {
            return 0;
        }
        int idx = IntegerArgumentType.getInteger(ctx, "index");
        return handlePick(mc, idx) ? 1 : 0;
    }

    /**
     * Entry point shared by hotkey and /autobuild command.
     * If multiple enabled placements exist, shows the chat picker.
     */
    public static void startBuildFlow(Minecraft mc) {
        List<SchematicPlacement> enabled = new ArrayList<>();
        for (SchematicPlacement p : DataManager.getSchematicPlacementManager().getAllSchematicsPlacements()) {
            if (p != null && p.isEnabled()) {
                enabled.add(p);
            }
        }

        if (enabled.isEmpty()) {
            sendLocal(mc, text("[AutoBuild] No enabled schematic placements").m_130940_(ChatFormatting.RED));
            return;
        }

        if (enabled.size() == 1) {
            AutoBuildDirector.start(enabled.get(0));
            return;
        }

        pendingChoices = enabled;
        sendLocal(mc, text("[AutoBuild] Multiple schematics detected. Click one to build:").m_130940_(ChatFormatting.YELLOW));

        for (int i = 0; i < enabled.size(); i++) {
            SchematicPlacement p = enabled.get(i);
            MutableComponent line = buildEntryLine(mc, p, i);
            sendLocal(mc, line);
        }

        MutableComponent cancel = text("[Cancel]")
                .m_130940_(ChatFormatting.RED)
                .m_130948_(Style.f_131099_.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/autobuild cancel")));
        sendLocal(mc, cancel);
    }

    private static boolean handlePick(Minecraft mc, int idx) {
        if (pendingChoices == null || pendingChoices.isEmpty()) {
            sendLocal(mc, text("[AutoBuild] No pending selection. Run /autobuild first.").m_130940_(ChatFormatting.RED));
            return false;
        }
        if (idx < 0 || idx >= pendingChoices.size()) {
            sendLocal(mc, text("[AutoBuild] Index out of range: " + idx).m_130940_(ChatFormatting.RED));
            return false;
        }

        SchematicPlacement chosen = pendingChoices.get(idx);
        pendingChoices = null;
        AutoBuildDirector.start(chosen);
        return true;
    }

    private static MutableComponent buildEntryLine(Minecraft mc, SchematicPlacement p, int index) {
        AutoBuildPlanner.PlacementStats stats = AutoBuildPlanner.summarizeNow(mc, p);
        BlockPos origin = p.getOrigin();
        int sizeX = stats.max != null ? stats.max.m_123341_() - stats.min.m_123341_() + 1 : 0;
        int sizeY = stats.max != null ? stats.max.m_123342_() - stats.min.m_123342_() + 1 : 0;
        int sizeZ = stats.max != null ? stats.max.m_123343_() - stats.min.m_123343_() + 1 : 0;

        String hoverText = String.format(
                "Size: %dx%dx%d\nNon-air blocks: %d\nMissing (loaded): %d\nMissing (unloaded): %d\nOrigin: [%d, %d, %d]",
                sizeX, sizeY, sizeZ,
                stats.nonAirTotal,
                stats.missingLoaded,
                stats.missingUnloaded,
                origin.m_123341_(), origin.m_123342_(), origin.m_123343_()
        );

        MutableComponent name = text(p.getName())
                .m_130940_(ChatFormatting.GREEN)
                .m_130948_(Style.f_131099_
                        .m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/autobuild pick " + index))
                        .m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, text(hoverText))));

        return text(" [" + index + "] ").m_130940_(ChatFormatting.GRAY).m_7220_(name);
    }

    private static MutableComponent text(String s) {
        return Component.m_237113_(s);
    }

    private static void sendLocal(Minecraft mc, Component component) {
        mc.f_91074_.m_5661_(component, false);
    }
}
