/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraftforge.fml.loading.LoadingModList
 *  org.objectweb.asm.tree.ClassNode
 *  org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
 *  org.spongepowered.asm.mixin.extensibility.IMixinInfo
 */
package xyz.jxmm.litematica_printer_forge.utils;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinConfigPlugin
implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        ArrayList desyncOptions = Lists.newArrayList((Object[])new String[]{"xyz.jxmm.litematica_printer_forge.mixin.MinecraftClient.ClientPlayerInteractionManagerMixin", "xyz.jxmm.litematica_printer_forge.mixin.MinecraftClient.ClientPlayNetworkHandlerMixin"});
        if (desyncOptions.contains(mixinClassName)) {
            return LoadingModList.get().getModFiles().contains(LoadingModList.get().getModFileById("matchrevisions"));
        }
        return true;
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}

