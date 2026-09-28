/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.Vec3
 */
package xyz.jxmm.litematica_printer_forge.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class positionStorage {
    private static final Map<Long, Boolean> positionMap = new LinkedHashMap<Long, Boolean>();

    public static void clear() {
        positionMap.clear();
    }

    public static boolean hasPos(BlockPos pos) {
        Long asLong = pos.m_121878_();
        if (positionMap.containsKey(asLong)) {
            return positionMap.get(asLong);
        }
        return false;
    }

    public static void registerPos(BlockPos pos, Boolean val) {
        positionMap.put(pos.m_121878_(), val);
    }

    public static void refresh(Level world) {
        for (Long longPos2 : positionMap.keySet().stream().filter(longPos -> positionMap.get(longPos) == false && !positionStorage.match(world.m_8055_(BlockPos.m_122022_((long)longPos)).m_60734_())).collect(Collectors.toList())) {
            positionMap.remove(longPos2);
        }
    }

    private static boolean match(Block block) {
        return block == Blocks.f_50039_ || block == Blocks.f_50174_ || block == Blocks.f_50374_;
    }

    public static ArrayList<BlockPos> getFalseMarkedHasBlockPosInAttackRange(Level world, Vec3 pos, int attackRange) {
        ArrayList<BlockPos> FalseMarkedList = new ArrayList<BlockPos>();
        for (Long position : positionMap.keySet()) {
            BlockPos blockPos = BlockPos.m_122022_((long)position);
            if (positionMap.get(position).booleanValue() || !positionStorage.match(world.m_8055_(blockPos).m_60734_()) || !blockPos.m_203195_((Position)pos, (double)attackRange)) continue;
            FalseMarkedList.add(blockPos);
        }
        return FalseMarkedList;
    }
}

