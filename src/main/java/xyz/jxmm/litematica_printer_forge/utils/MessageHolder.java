/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 */
package xyz.jxmm.litematica_printer_forge.utils;

import java.util.HashSet;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

public class MessageHolder {
    private static final HashSet<String> uniqueStrings = new HashSet();
    private static final HashSet<String> uniqueStringsAlways = new HashSet();
    private static final HashSet<String> uniquePacketInfos = new HashSet();
    private static final HashSet<String> errorLogger = new HashSet();
    private static String orderPreviousMessage = "";

    public static void sendPacketOrders(String string) {
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (uniquePacketInfos.contains(string)) {
            return;
        }
        uniquePacketInfos.add(string);
        player.m_5661_(Component.m_130674_((String)string), false);
    }

    public static void sendDebugMessage(LocalPlayer player, String string) {
        if (LitematicaMixinMod.DEBUG_EXTRA_MESSAGE.getBooleanValue()) {
            player.m_5661_(Component.m_130674_((String)string), false);
        }
    }

    public static void sendDebugMessage(String string) {
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (LitematicaMixinMod.DEBUG_EXTRA_MESSAGE.getBooleanValue()) {
            player.m_5661_(Component.m_130674_((String)string), false);
        }
    }

    public static void sendOrderMessage(String string) {
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (LitematicaMixinMod.DEBUG_ORDER_PLACEMENTS.getBooleanValue() && !Objects.equals(orderPreviousMessage, string)) {
            orderPreviousMessage = string;
            player.m_5661_(Component.m_130674_((String)string), false);
        }
    }

    public static void sendUniqueDebugMessage(LocalPlayer player, String string) {
        if (LitematicaMixinMod.DEBUG_EXTRA_MESSAGE.getBooleanValue() && !uniqueStrings.contains(string)) {
            player.m_5661_(Component.m_130674_((String)string), false);
            uniqueStrings.add(string);
        }
    }

    public static void sendUniqueDebugMessage(String string) {
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (LitematicaMixinMod.DEBUG_EXTRA_MESSAGE.getBooleanValue() && !uniqueStrings.contains(string)) {
            player.m_5661_(Component.m_130674_((String)string), false);
            uniqueStrings.add(string);
        }
    }

    public static void sendMessageUncheckedUnique(LocalPlayer player, String string) {
        if (!errorLogger.contains(string)) {
            player.m_5661_(Component.m_130674_((String)string), false);
            errorLogger.add(string);
        }
    }

    public static void sendMessageUncheckedUnique(String string) {
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (!errorLogger.contains(string)) {
            player.m_5661_(Component.m_130674_((String)string), false);
            errorLogger.add(string);
        }
    }

    public static void sendMessageUnchecked(String string) {
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        player.m_5661_(Component.m_130674_((String)string), false);
    }

    public static void sendUniqueMessage(LocalPlayer player, String string) {
        if (!LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue()) {
            uniqueStrings.clear();
            return;
        }
        if (!uniqueStrings.contains(string)) {
            player.m_5661_(Component.m_130674_((String)string), false);
            uniqueStrings.add(string);
        }
    }

    public static void sendUniqueMessageAlways(String string) {
        LocalPlayer player = Minecraft.m_91087_().f_91074_;
        if (!uniqueStringsAlways.contains(string)) {
            player.m_5661_(Component.m_130674_((String)string), false);
            uniqueStringsAlways.add(string);
        }
    }

    public static void sendUniqueMessage(LocalPlayer player, Object object) {
        String string = object.toString();
        MessageHolder.sendUniqueMessage(player, string);
    }

    public static void sendUniqueMessageActionBar(LocalPlayer player, String string) {
        if (!LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue()) {
            return;
        }
        if (!uniqueStrings.contains(string)) {
            player.m_5661_(Component.m_130674_((String)string), true);
            uniqueStrings.add(string);
        }
    }

    public static void sendDebugMessageActionBar(LocalPlayer player, String string) {
        if (!LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue()) {
            return;
        }
        player.m_5661_(Component.m_130674_((String)string), true);
        uniqueStrings.add(string);
    }
}

