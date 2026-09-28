/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  fi.dy.masa.litematica.config.Configs
 *  fi.dy.masa.malilib.config.IConfigBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package xyz.jxmm.litematica_printer_forge.mixin.Litematica;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

@Mixin(value={Configs.class}, remap=false)
public class ConfigsMixin {
    @Redirect(method={"loadFromFile"}, at=@At(value="FIELD", target="Lfi/dy/masa/litematica/config/Configs$Generic;OPTIONS:Lcom/google/common/collect/ImmutableList;"))
    private static ImmutableList<IConfigBase> moreOptions() {
        return LitematicaMixinMod.betterList;
    }

    @Redirect(method={"saveToFile"}, at=@At(value="FIELD", target="Lfi/dy/masa/litematica/config/Configs$Generic;OPTIONS:Lcom/google/common/collect/ImmutableList;"))
    private static ImmutableList<IConfigBase> moreeOptions() {
        return LitematicaMixinMod.betterList;
    }
}

