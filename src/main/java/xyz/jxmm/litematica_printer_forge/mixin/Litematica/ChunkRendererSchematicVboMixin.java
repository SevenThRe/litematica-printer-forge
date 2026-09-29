package xyz.jxmm.litematica_printer_forge.mixin.Litematica;

import fi.dy.masa.litematica.render.schematic.ChunkRendererSchematicVbo;
import fi.dy.masa.litematica.util.OverlayType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.jxmm.litematica_printer_forge.utils.BlockReplacer;

/**
 * The schematic overlay classifier marks any block difference as WRONG_BLOCK (red).
 * A position whose schematic block is replaced via the mapping and whose client block
 * matches the replacement target is correct and must not be highlighted at all.
 */
@Mixin(value = {ChunkRendererSchematicVbo.class})
public class ChunkRendererSchematicVboMixin {
    @Inject(method = {"getOverlayType"}, at = {@At(value = "HEAD")}, cancellable = true, remap = false)
    private void handleReplacedBlocks(BlockState stateSchematic, BlockState stateClient, CallbackInfoReturnable<OverlayType> cir) {
        if (BlockReplacer.clientMatchesReplacement(stateSchematic, stateClient)) {
            cir.setReturnValue(OverlayType.NONE);
        }
    }
}
