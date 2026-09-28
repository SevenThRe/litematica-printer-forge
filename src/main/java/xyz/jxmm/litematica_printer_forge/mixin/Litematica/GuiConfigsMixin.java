/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  fi.dy.masa.litematica.gui.GuiConfigs
 *  fi.dy.masa.malilib.config.IConfigBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package xyz.jxmm.litematica_printer_forge.mixin.Litematica;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.gui.GuiConfigs;
import fi.dy.masa.malilib.config.IConfigBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

@Mixin(value={GuiConfigs.class}, remap=false)
public class GuiConfigsMixin {
    @Redirect(method={"getConfigs"}, at=@At(value="FIELD", target="Lfi/dy/masa/litematica/config/Configs$Generic;OPTIONS:Lcom/google/common/collect/ImmutableList;"))
    private ImmutableList<IConfigBase> moreOptions() {
        return LitematicaMixinMod.betterList;
    }
}

