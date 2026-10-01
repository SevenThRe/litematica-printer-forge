package xyz.jxmm.litematica_printer_forge.fastbuild;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;

/**
 * Client side permission probe for the fast command builder.
 *
 * <p>The command tree the client holds is <b>not</b> a copy of the server tree. The server builds
 * it per player in {@code net.minecraft.commands.Commands.m_82095_(ServerPlayer)}: it walks the
 * root's children and copies a node only when {@code CommandNode.canUse(playerCommandSourceStack)}
 * passes - i.e. {@code getRequirement().test(source)} - and the copied node gets its requirement
 * replaced by a constant {@code true}. Commands that fail the test never reach the client at all.
 *
 * <p>That has two useful consequences:
 * <ul>
 *   <li>"is the {@code setblock} node in my tree" answers "may I run {@code /setblock}" using the
 *       very predicate the server evaluates when the command is executed, with no server mod and
 *       no extra round trip;</li>
 *   <li>a command that is unknown to the client tree is rejected locally by
 *       {@code ClientPacketListener.m_245186_} before any packet is sent, so probing first also
 *       keeps us from spamming the server with commands it will refuse.</li>
 * </ul>
 *
 * <p>Permission levels are inferred from vanilla command literals that require exactly that level.
 */
public final class OpGate {
    private OpGate() {
    }

    /** No usable command tree at all (not connected). */
    public static final int NONE = 0;
    /** Ordinary player: no op-only commands. */
    public static final int PLAYER = 1;
    /** Permission level 2 - the level /fill, /setblock, /gamemode, /give need. */
    public static final int OP = 2;
    /** Permission level 3 - also op / ban / kick / whitelist. */
    public static final int ADMIN = 3;
    /** Permission level 4 - also /stop, /save-off. */
    public static final int OWNER = 4;

    private static RootCommandNode<SharedSuggestionProvider> root(Minecraft mc) {
        if (mc == null) {
            return null;
        }
        ClientPacketListener listener = mc.m_91403_();
        if (listener == null) {
            return null;
        }
        CommandDispatcher<SharedSuggestionProvider> dispatcher = listener.m_105146_();
        return dispatcher == null ? null : dispatcher.getRoot();
    }

    /** True when the server put this top level literal into our command tree. */
    public static boolean has(Minecraft mc, String literal) {
        RootCommandNode<SharedSuggestionProvider> root = root(mc);
        if (root == null || literal == null) {
            return false;
        }
        CommandNode<SharedSuggestionProvider> node = root.getChild(literal);
        return node != null;
    }

    /**
     * Highest vanilla permission level we can prove from the command tree, 0 when unknown.
     * Only literals that exist at exactly one level are used, so the result never over-claims.
     */
    public static int level(Minecraft mc) {
        RootCommandNode<SharedSuggestionProvider> root = root(mc);
        if (root == null) {
            return NONE;
        }
        if (root.getChild("stop") != null) {
            return OWNER;
        }
        if (root.getChild("op") != null || root.getChild("ban") != null || root.getChild("whitelist") != null) {
            return ADMIN;
        }
        if (root.getChild("setblock") != null && root.getChild("fill") != null) {
            return OP;
        }
        if (root.getChild("help") != null || root.getChild("list") != null || root.getChild("msg") != null) {
            return PLAYER;
        }
        return NONE;
    }

    /** Both bulk writers must be present; they are what the fast builder emits. */
    public static boolean canFastBuild(Minecraft mc) {
        return has(mc, "setblock") && has(mc, "fill");
    }

    public static String describe(Minecraft mc) {
        int lvl = level(mc);
        String name;
        switch (lvl) {
            case OWNER:
                name = "level 4 (owner)";
                break;
            case ADMIN:
                name = "level 3 (admin)";
                break;
            case OP:
                name = "level 2 (op)";
                break;
            case PLAYER:
                name = "level 1 (player)";
                break;
            default:
                name = "unknown / not connected";
                break;
        }
        return name + ", setblock=" + has(mc, "setblock") + " fill=" + has(mc, "fill");
    }
}
