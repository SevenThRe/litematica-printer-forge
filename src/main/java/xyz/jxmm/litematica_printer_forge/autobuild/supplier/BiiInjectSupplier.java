package xyz.jxmm.litematica_printer_forge.autobuild.supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.jxmm.litematica_printer_forge.utils.BackpackInjector;
import xyz.jxmm.litematica_printer_forge.utils.InjectPlan;
import xyz.jxmm.litematica_printer_forge.utils.MessageHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BII ghost-injection supplier (Task 9).
 *
 * Falls back to the BackpackInjector ghost protocol when real transfers
 * (SB / RS) leave a residual shortage. Sends SetGhostSlotMessage packets to
 * conjure the missing items directly into player inventory or backpack slots.
 *
 * start() builds InjectPlans from the shortages map and calls
 * BackpackInjector.ensureMaterials(). tick() polls BackpackInjector.isBusy().
 * abort() drops the pending result listener so no stale callback fires.
 */
public final class BiiInjectSupplier implements MaterialBroker.MaterialSupplier {

    private static final int TIMEOUT_TICKS = 600;

    private boolean started = false;
    private boolean finished = false;
    private int ticks = 0;

    @Override
    public String name() {
        return "BII";
    }

    @Override
    public boolean isAvailable(Minecraft mc) {
        return BackpackInjector.isAvailable(mc);
    }

    @Override
    public void start(Minecraft mc, Map<Item, Integer> shortages) {
        if (shortages == null || shortages.isEmpty()) {
            finished = true;
            return;
        }
        List<InjectPlan> plans = new ArrayList<>();
        for (Map.Entry<Item, Integer> e : shortages.entrySet()) {
            plans.add(new InjectPlan(new ItemStack(e.getKey(), 1), e.getValue()));
        }
        BackpackInjector.applySort(plans, BackpackInjector.lastSortMode);
        started = true;
        ticks = 0;
        BackpackInjector.ensureMaterials(plans, mc, (gotInv, gotStored) -> {
            // Listener is informational only; the broker re-counts from scratch after SETTLE_TICKS.
        });
    }

    @Override
    public boolean tick(Minecraft mc) {
        if (!started || finished) {
            return false;
        }
        ticks++;
        if (ticks > TIMEOUT_TICKS) {
            MessageHolder.sendMessageUnchecked("[BII] 注入超时，强制结束");
            BackpackInjector.clearProgrammaticState();
            finished = true;
            return false;
        }
        if (!BackpackInjector.isBusy()) {
            finished = true;
            return false;
        }
        return true;
    }

    @Override
    public void abort(Minecraft mc) {
        BackpackInjector.clearProgrammaticState();
        finished = true;
    }
}
