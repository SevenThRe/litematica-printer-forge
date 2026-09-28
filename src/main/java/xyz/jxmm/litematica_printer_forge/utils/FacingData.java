/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.AnvilBlock
 *  net.minecraft.world.level.block.BarrelBlock
 *  net.minecraft.world.level.block.BaseRailBlock
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.BeehiveBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.CarvedPumpkinBlock
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.ComparatorBlock
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.minecraft.world.level.block.DoorBlock
 *  net.minecraft.world.level.block.DropperBlock
 *  net.minecraft.world.level.block.EndPortalFrameBlock
 *  net.minecraft.world.level.block.EnderChestBlock
 *  net.minecraft.world.level.block.FenceGateBlock
 *  net.minecraft.world.level.block.FurnaceBlock
 *  net.minecraft.world.level.block.GlazedTerracottaBlock
 *  net.minecraft.world.level.block.GrindstoneBlock
 *  net.minecraft.world.level.block.LecternBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.LoomBlock
 *  net.minecraft.world.level.block.ObserverBlock
 *  net.minecraft.world.level.block.PumpkinBlock
 *  net.minecraft.world.level.block.RepeaterBlock
 *  net.minecraft.world.level.block.StairBlock
 *  net.minecraft.world.level.block.StonecutterBlock
 *  net.minecraft.world.level.block.TrapDoorBlock
 *  net.minecraft.world.level.block.piston.PistonBaseBlock
 *  net.minecraft.world.level.block.state.BlockState
 */
package xyz.jxmm.litematica_printer_forge.utils;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.GlazedTerracottaBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FacingData {
    public int type;
    public boolean isReversed;
    private static final Map<Class<? extends Block>, FacingData> facingMap = new LinkedHashMap<Class<? extends Block>, FacingData>();
    private static boolean setupFacing = false;

    public FacingData(int type, boolean isReversed) {
        this.type = type;
        this.isReversed = isReversed;
    }

    private static void setUpFacingData() {
        setupFacing = true;
        FacingData.addFD(PistonBaseBlock.class, new FacingData(0, true));
        FacingData.addFD(DispenserBlock.class, new FacingData(0, true));
        FacingData.addFD(DropperBlock.class, new FacingData(0, true));
        FacingData.addFD(ObserverBlock.class, new FacingData(0, false));
        FacingData.addFD(BaseRailBlock.class, new FacingData(0, false));
        FacingData.addFD(StairBlock.class, new FacingData(1, false));
        FacingData.addFD(DoorBlock.class, new FacingData(1, false));
        FacingData.addFD(BedBlock.class, new FacingData(1, false));
        FacingData.addFD(FenceGateBlock.class, new FacingData(1, false));
        FacingData.addFD(TrapDoorBlock.class, new FacingData(1, true));
        FacingData.addFD(BarrelBlock.class, new FacingData(1, true));
        FacingData.addFD(ChestBlock.class, new FacingData(1, true));
        FacingData.addFD(RepeaterBlock.class, new FacingData(1, true));
        FacingData.addFD(ComparatorBlock.class, new FacingData(1, true));
        FacingData.addFD(EnderChestBlock.class, new FacingData(1, true));
        FacingData.addFD(FurnaceBlock.class, new FacingData(1, true));
        FacingData.addFD(GlazedTerracottaBlock.class, new FacingData(1, true));
        FacingData.addFD(LecternBlock.class, new FacingData(1, true));
        FacingData.addFD(LoomBlock.class, new FacingData(1, true));
        FacingData.addFD(BeehiveBlock.class, new FacingData(1, true));
        FacingData.addFD(StonecutterBlock.class, new FacingData(1, true));
        FacingData.addFD(CarvedPumpkinBlock.class, new FacingData(1, true));
        FacingData.addFD(PumpkinBlock.class, new FacingData(1, true));
        FacingData.addFD(EndPortalFrameBlock.class, new FacingData(1, true));
        FacingData.addFD(LeverBlock.class, new FacingData(2, false));
        FacingData.addFD(GrindstoneBlock.class, new FacingData(2, true));
        FacingData.addFD(AnvilBlock.class, new FacingData(3, true));
        FacingData.addFD(BaseRailBlock.class, new FacingData(4, false));
    }

    private static void addFD(Class<? extends Block> c, FacingData data) {
        facingMap.put(c, data);
    }

    public static FacingData getFacingData(BlockState state) {
        if (!setupFacing) {
            FacingData.setUpFacingData();
        }
        Block block = state.m_60734_();
        for (Class<? extends Block> c : facingMap.keySet()) {
            if (!c.isInstance(block)) continue;
            return facingMap.get(c);
        }
        return null;
    }
}

