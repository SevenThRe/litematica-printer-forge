# litematica-printer-forge

Minecraft 1.20.1 Forge 自动打印模组（Litematica 打印机增强重建版）。

基于原版 Litematica Printer 逆向重建源码，并针对实际使用中的大量问题做了修复与增强。

> **状态：开发中，新功能未经完整测试，暂未发布 release。**

## 功能

### 自动打印
- 原理图自动放置方块，逐 tick 批量打印
- 硬方块持续挖掘、额外方块清除、流体清除
- 紫水晶簇/带釉陶瓦等特殊方块朝向修复

### 方块替换（coralReplace）
打印、高亮、材料统计全链路一致的方块替换系统：

- **内置默认表**（1.20.1 放置后会变化的方块）：
  - 全部未涂蜡铜系（铜块/切制铜/楼梯/台阶/避雷针及三阶氧化）→ 对应涂蜡版本
  - 黄珊瑚块 → 黄色带釉陶瓦
- 用户自定义映射（`from=to`，注册名格式）优先于内置默认表
- 开关关闭时完全不干预原理图

### ESP 缺失方块高亮
- **验证模式**：高亮整张原理图所有未放置/错误方块（红色）
- **手持模式**：仅高亮与手持方块相同的缺失位置（青蓝色）
- **物品栏模式**：按玩家物品栏材料种类高亮，配合打印机的方块 swap 不闪烁
- 增量分片扫描（不卡顿）、跳过未加载区块、已放置位置实时消除

### BII 背包注入
一键将原理图缺失材料注入到背包模组（下界合金背包等）：

- 每种材料至多一组直接进入玩家物品栏，剩余注入背包存储
- **按 Y 层级排序**：默认从建筑底部往上注入（可切换倒序/扫描顺序），配合施工不用来回跑
- 材料已足够时自动跳过
- 缺失材料计算与验证器、ESP 同口径（多区域/带偏移原理图已修正）

## 主要配置项

| 配置 | 说明 |
|---|---|
| `printerEnabled` | 打印机总开关 |
| `coralReplaceEnabled` | 方块替换开关（默认关闭） |
| `blockReplaceMappings` | 自定义替换表，每行 `minecraft:from=minecraft:to` |
| `espHighlightMissingBlocks` | ESP 高亮总开关 |
| `espVerificationMode` | 验证模式（全图红框） |
| `espHighlightByInventory` | 按物品栏材料高亮 |
| `espMaxRenderCount` | 高亮渲染上限（0 = 不限制） |
| `fakeRotationTicks` | Fake rotation 时长（带釉陶瓦等依赖玩家朝向的方块） |

配置均可在 Litematica 配置 GUI 中修改。

## 构建

```bash
gradlew build
```

- 需要 **JDK 21**
- 构建依赖本机 `.minecraft` 的 1.20.1 Forge libraries（精确清单见 `gradle/compile-classpath.txt`），无法开箱构建；请先在同一机器安装 Minecraft 1.20.1 + Forge 47.x 启动一次，再进行构建

## 致谢

- [Litematica](https://github.com/masa-fm/Litematica) — masa
- Litematica Printer 原项目及其贡献者
