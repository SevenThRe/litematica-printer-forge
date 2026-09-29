package xyz.jxmm.litematica_printer_forge.utils;

import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class Breaker implements IClientTickHandler {
    private boolean breakingBlock = false;
    private BlockPos pos;

    public Breaker() {
        TickHandler.getInstance().registerClientTickHandler((IClientTickHandler)this);
    }

    public boolean startBreakingBlock(BlockPos pos, Minecraft mc) {
        BlockState blockState;
        this.breakingBlock = true;
        this.pos = pos;
        if (mc.f_91073_.m_8055_(pos).m_60800_((BlockGetter)mc.f_91073_, pos) == 0.0f) {
            mc.f_91072_.m_105269_(pos, Direction.UP);
            return false;
        }
        int bestSlotId = Breaker.getBestItemSlotIdToMineBlock(mc, pos);
        if (bestSlotId != -1) {
            ItemStack stack = mc.f_91074_.m_150109_().m_8020_(bestSlotId);
            InventoryUtils.swapToItem(mc, stack);
        }
        if ((blockState = mc.f_91073_.m_8055_(pos)).m_60625_((Player)mc.f_91074_, (BlockGetter)mc.f_91074_.m_20193_(), pos) >= 1.0f) {
            mc.f_91072_.m_105269_(pos, Direction.UP);
            return false;
        }
        TickHandler.getInstance().registerClientTickHandler((IClientTickHandler)this);
        return true;
    }

    public boolean isBreakingBlock() {
        if (this.pos == null || Minecraft.m_91087_().f_91073_ == null) {
            return false;
        }
        if (Minecraft.m_91087_().f_91073_.m_8055_(this.pos).m_247087_()) {
            this.breakingBlock = false;
        }
        return this.breakingBlock;
    }

    public static int getBestItemSlotIdToMineBlock(Minecraft mc, BlockPos blockToMine) {
        int bestSlot = -1;
        float bestSpeed = 0.0f;
        BlockState state = mc.f_91073_.m_8055_(blockToMine);
        for (int i = mc.f_91074_.m_150109_().m_6643_(); i >= 0; --i) {
            float speed = Breaker.getBlockBreakingSpeed(state, mc, i);
            if (!(speed > bestSpeed && speed > 1.0f) && (!(speed >= bestSpeed) || mc.f_91074_.m_150109_().m_8020_(i).m_41763_())) continue;
            bestSlot = i;
            bestSpeed = speed;
        }
        return bestSlot;
    }

    public static int getBestItemSlotIdToMineState(Minecraft mc, BlockState state) {
        int bestSlot = -1;
        float bestSpeed = 0.0f;
        for (int i = mc.f_91074_.m_150109_().m_6643_(); i >= 0; --i) {
            float speed = Breaker.getBlockBreakingSpeed(state, mc, i);
            if (!(speed > bestSpeed && speed > 1.0f) && (!(speed >= bestSpeed) || mc.f_91074_.m_150109_().m_8020_(i).m_41763_())) continue;
            bestSlot = i;
            bestSpeed = speed;
        }
        return bestSlot;
    }

    public static float getBlockBreakingSpeed(BlockState block, Minecraft mc, int slotId) {
        if (slotId < -1 || slotId >= 36) {
            return 0.0f;
        }
        float f = ((ItemStack)mc.f_91074_.m_150109_().f_35974_.get(slotId)).m_41691_(block);
        if (f > 1.0f) {
            int i = EnchantmentHelper.m_44926_((LivingEntity)mc.f_91074_);
            ItemStack itemStack = mc.f_91074_.m_150109_().m_36056_();
            if (i > 0 && !itemStack.m_41619_()) {
                f += (float)(i * i + 1);
            }
        }
        return f;
    }

    public void onClientTick(Minecraft mc) {
        if (!this.isBreakingBlock() || mc.f_91074_ == null) {
            this.breakingBlock = false;
            return;
        }
        // Fix: removed the EASY_PLACE_ACTIVATION key check; always mine while the printer is active
        Direction side = Direction.values()[0];
        if (mc.f_91072_.m_105283_(this.pos, side)) {
            mc.f_91061_.m_107367_(this.pos, side);
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
        }
        if (!mc.f_91073_.m_8055_(this.pos).m_247087_()) {
            this.breakingBlock = false;
            return;
        }
        this.breakingBlock = false;
        mc.f_91072_.m_105276_();
    }
}
