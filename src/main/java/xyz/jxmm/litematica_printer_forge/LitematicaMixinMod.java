package xyz.jxmm.litematica_printer_forge;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.jxmm.litematica_printer_forge.utils.MissingBlockEsp;

@Mod(value="litematica_printer_forge")
public class LitematicaMixinMod {
    public static final String MODID = "litematica_printer_forge";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final ConfigBoolean DEBUG_MESSAGE = new ConfigBoolean("printerDebug", false, "Show debug messages");
    public static final ConfigBoolean DEBUG_EXTRA_MESSAGE = new ConfigBoolean("printerDebugExtra", false, "Show extra debug messages");
    public static final ConfigBoolean DEBUG_ORDER_PLACEMENTS = new ConfigBoolean("printerDebugOrder", false, "Show block placements order messages");
    public static final ConfigBoolean VERIFY_INVENTORY = new ConfigBoolean("printerVerifyInventory", true, "Verify inventory using baritone's inventory movement cache");
    public static final ConfigBoolean USE_INVENTORY_CACHE = new ConfigBoolean("printerUseInventoryCache", true, "use baritone's inventory movement cache");
    public static final ConfigBoolean PRINTER_OFF = new ConfigBoolean("printerOff", false, "Printer won't work (useful if you want to place only rotation at easyplace mode.)");
    public static final ConfigBoolean PRINTER_ONLY_FAKE_ROTATION_MODE = new ConfigBoolean("easyPlaceMode++", false, "Disables printer and only make fake rotation work.");
    public static final ConfigBoolean DISABLE_SINGLEPLAYER_HANDLE = new ConfigBoolean("printerIgnoreEasyPlaceSinglePlayer", false, "Should printer ignore when easyplace is set to SinglePlayer");
    public static final ConfigBoolean DEBUG_PACKET_SYNC = new ConfigBoolean("printerDebugPacketSync", false, "Show debug messages");
    public static final ConfigBoolean INVENTORY_OPERATIONS = new ConfigBoolean("printerUseInventoryOperations", true, "Allow printer to use ");
    public static final ConfigInteger INVENTORY_OPERATIONS_WAIT = new ConfigInteger("printerInventoryScreenWait", 200, 0, 8000, "Time(ms) to wait screen to be opened and synced");
    public static final ConfigInteger INVENTORY_OPERATIONS_RETRY = new ConfigInteger("printerInventoryOperationRetry", 3, 1, 32, "Times to retry inventory operations");
    public static final ConfigBoolean INVENTORY_OPERATIONS_CLOSE_SCREEN = new ConfigBoolean("printerCloseInventoryScreen", true, "Close inventory screen after operations (requires accurateblockplacement)");
    public static final ConfigBoolean INVENTORY_OPERATIONS_FILTER_ALLOW_NAMED = new ConfigBoolean("printerAllowNamedItemInventoryOperations", false, "Allow named items in inventory operations (requires accurateblockplacement)");
    public static final ConfigInteger SLEEP_AFTER_CONSUME = new ConfigInteger("printerSleepStackEmptied", 200, 0, 8000, "Sleeps after stack is emptied (ms)");
    public static final ConfigInteger EASY_PLACE_CACHE_TIME = new ConfigInteger("printerCacheTime", 4, 0, 1000000, "Printer will cache block position for this amount of ticks");
    public static final ConfigInteger EASY_PLACE_MODE_RANGE_X = new ConfigInteger("easyPlaceModePrinterRangeX", 4, 0, 1024, "X Range for EasyPlace");
    public static final ConfigInteger EASY_PLACE_MODE_RANGE_Y = new ConfigInteger("easyPlaceModePrinterRangeY", 4, 0, 1024, "Y Range for EasyPlace");
    public static final ConfigInteger EASY_PLACE_MODE_RANGE_Z = new ConfigInteger("easyPlaceModePrinterRangeZ", 4, 0, 1024, "Z Range for EasyPlace");
    public static final ConfigInteger PRINTER_MAX_BLOCKS = new ConfigInteger("easyPlaceModePrinterMaxBlocks", 6, 1, 1000000, "Max block interactions per cycle");
    public static final ConfigInteger PRINTER_MAX_ITEM_CHANGES = new ConfigInteger("easyPlaceModePrinterMaxItemChanges", 0, 0, 1000000, "Max item categories per cycle");
    public static final ConfigBoolean PRINTER_BREAK_BLOCKS = new ConfigBoolean("printerBreakBlocks", false, "Automatically breaks blocks.");
    public static final ConfigBoolean PRINTER_BREAK_IGNORE_EXTRA = new ConfigBoolean("printerBreakIgnoresExtra", true, "Does not break extra blocks.");
    public static final ConfigBoolean PRINTER_BREAK_EXTRA_BLOCKS = new ConfigBoolean("printerBreakExtraBlocks", false, "Break blocks that exist where schematic expects air.");
    public static final ConfigBoolean DISABLE_SYNC = new ConfigBoolean("disableInventorySync", false, "Disables sync with inventory.");
    public static final ConfigDouble EASY_PLACE_MODE_DELAY = new ConfigDouble("easyPlaceModeDelay", 0.2, 0.0, 1.0, "Delay between printing blocks.\n Recommended to set value over 0.05(50ms).");
    public static final ConfigBoolean EASY_PLACE_MODE_HOTBAR_ONLY = new ConfigBoolean("easyPlaceModeHotbarOnly", false, "Only place blocks from your hotbar.");
    public static final ConfigBoolean FLIPPIN_CACTUS = new ConfigBoolean("printerFlippincactus", false, "If FlippinCactus is enabled and cactus is on mainhand, will not place block and do rotations only.");
    public static final ConfigBoolean CLEAR_AREA_MODE = new ConfigBoolean("printerClearFluids", false, "It will try to place slime blocks at fluids anywhere to clear");
    public static final ConfigBoolean CLEAR_AREA_MODE_COBBLESTONE = new ConfigBoolean("printerClearFluidsUseCobblestone", false, "It will try to place Cobblestone at anywhere to clear");
    public static final ConfigBoolean CLEAR_AREA_MODE_SNOWPREVENT = new ConfigBoolean("printerClearSnowLayer", false, "It will try to place string when snow layer is found");
    public static final ConfigBoolean ACCURATE_BLOCK_PLACEMENT = new ConfigBoolean("printerAccurateBlockPlacement", false, "if carpet extra/quickcarpet enabled it, turn on");
    public static final ConfigBoolean PRINTER_PUMPKIN_PIE_FOR_COMPOSTER = new ConfigBoolean("printerUsePumpkinpieForComposter", false, "use pumpkin pie to adjust composter level");
    public static final ConfigBoolean PRINTER_OBSERVER_AVOID_ALL = new ConfigBoolean("printerObserverAvoidAll", true, "Observer will avoid all state update, can cause deadlock");
    public static final ConfigBoolean AVOID_CHECK_ONLY_PISTONS = new ConfigBoolean("printerAvoidCheckOnlyPistons", true, "QC order checks will ignore Dispenser QC state");
    public static final ConfigBoolean PRINTER_WATERLOGGED_WATER_FIRST = new ConfigBoolean("printerCheckWaterFirstForWaterlogged", true, "Watterlogged blocks won't be placed before water in place(except leaves in 1.19)");
    public static final ConfigBoolean PRINTER_SMART_REDSTONE_AVOID = new ConfigBoolean("printerSmartRedstoneAvoid", true, "Pistons / Observers will avoid and respect its order");
    public static final ConfigBoolean PRINTER_SUPPRESS_PUSH_LIMIT = new ConfigBoolean("printerSuppressPushLimitPistons", true, "Pistons that is suppressed with push limit won't be placed.");
    public static final ConfigBoolean PRINTER_SKIP_UNKNOWN_BLOCKSTATE = new ConfigBoolean("printerSkipsUnknownBlockstates", true, "Printer will skip some directional blocks");
    public static final ConfigBoolean ADVANCED_ACCURATE_BLOCK_PLACEMENT = new ConfigBoolean("CarpetExtraFixedVersion", false, "If carpet extra is updated, turn on to allow all facingblock rotation");
    public static final ConfigBoolean BEDROCK_BREAKING = new ConfigBoolean("printerBedrockBreaking", false, "Clear Bedrock mismatch with Bedrock Breaker");
    public static final ConfigBoolean BEDROCK_BREAKING_FORCE_TORCH = new ConfigBoolean("printerBedrockBreakingUseSlimeblock", false, "BecrockBreaker uses slime block to force torch location");
    public static final ConfigInteger BEDROCK_BREAKING_RANGE_SAFE = new ConfigInteger("bedrockBreakingCheckRange", 3, 0, 1024, "Safety distance between bedrock breakings");
    public static final ConfigInteger BEDROCK_BREAKING_CLEAR_WAIT = new ConfigInteger("bedrockBreakingClearTicks", 6, 0, 1024, "Waiting ticks after processing bedrock");
    public static final ConfigBoolean PRINTER_PLACE_ICE = new ConfigBoolean("printerUseIceForWater", false, "Should printer place ice where water/waterlogged should be?");
    public static final ConfigBoolean PRINTER_PLACE_MINECART = new ConfigBoolean("printerPlaceMinecart", true, "Should printer place minecart?(its smarter than average)");
    public static final ConfigBoolean PRINTER_CLEAR_FLUIDS_AUTOMATICALLY = new ConfigBoolean("printerClearFluidsAutomatically", false, "Replace fluids with solid blocks before placing schematic blocks.");
    public static final ConfigBoolean FAKE_ROTATION_BETA = new ConfigBoolean("printerFakeRotation", true, "Printer will use fake rotations to place block correctly, at least in vanilla.");
    public static final ConfigInteger FAKE_ROTATION_TICKS = new ConfigInteger("printerFakeRotationTicks", 1, 0, 1000000, "Ticks between fake block packets");
    public static final ConfigInteger FAKE_ROTATION_LIMIT = new ConfigInteger("printerFakeRotationLimitPerTicks", 1, 1, 1000000, "Maximum fake placement per tick, prone to cause error(require:FakeRotationTick = 0)");
    public static final ConfigBoolean BII_ENABLED = new ConfigBoolean("printerBackpackInject", true, "When missing materials, inject them into Sophisticated Backpack via BII");
    public static final ConfigInteger BI_MAX_SLOTS = new ConfigInteger("printerBackpackInjectMaxSlots", 5, 1, 54, "Max backpack ghost slots to fill per cycle");
    public static final ConfigHotkey BII_INJECT_HOTKEY = new ConfigHotkey("printerBackpackInjectTrigger", "", KeybindSettings.PRESS_ALLOWEXTRA, "Trigger batch material injection into backpack");
    public static final ConfigBoolean BII_CREATE_BLUEPRINT = new ConfigBoolean("printerBackpackInjectUseCreateBlueprint", true, "BII uses Create blueprints (carried in the inventory/backpacks) as the material source when present, otherwise the litematica placements");
    public static final ConfigHotkey PRINTER_HOTKEY = new ConfigHotkey("printerActivationHotkey", "V", KeybindSettings.PRESS_ALLOWEXTRA, "Hold to activate printer (places blocks in range)");
    // Auto Build Director configs
    public static final ConfigHotkey AUTO_BUILD_TOGGLE_HOTKEY = new ConfigHotkey("autoBuildToggleHotkey", "", KeybindSettings.PRESS_ALLOWEXTRA, "Toggle auto build director on/off");
    public static final ConfigHotkey AUTO_BUILD_PAUSE_HOTKEY = new ConfigHotkey("autoBuildPauseHotkey", "", KeybindSettings.PRESS_ALLOWEXTRA, "Pause/resume auto build director");
    public static final ConfigHotkey AUTO_BUILD_MARK_TERMINAL_HOTKEY = new ConfigHotkey("autoBuildMarkTerminalHotkey", "", KeybindSettings.PRESS_ALLOWEXTRA, "Mark looked-at RS grid terminal for auto build");
    public static final ConfigBoolean AUTO_BUILD_SELECTED_ONLY = new ConfigBoolean("autoBuildSelectedPlacementOnly", false, "Only build the currently selected schematic placement");
    public static final ConfigBoolean AUTO_BUILD_SKIP_MISSING = new ConfigBoolean("autoBuildSkipMissing", false, "Skip blocks with missing materials instead of pausing");
    public static final ConfigBoolean AUTO_BUILD_PRINT_WHILE_MOVING = new ConfigBoolean("autoBuildPrintWhileMoving", false, "Allow printing while Baritone is navigating");
    public static final ConfigBoolean AUTO_BUILD_ALLOW_PATH_BREAK = new ConfigBoolean("autoBuildAllowPathBreak", true, "Allow Baritone to break blocks while pathing");
    public static final ConfigBoolean AUTO_BUILD_ALLOW_SCAFFOLD = new ConfigBoolean("autoBuildAllowScaffold", true, "Allow automatic scaffolding for high blocks");
    public static final ConfigBoolean AUTO_BUILD_RS_EXTRACT = new ConfigBoolean("autoBuildRsExtract", true, "Allow extracting materials from Refined Storage");
    public static final ConfigBoolean AUTO_BUILD_SB_TRANSFER = new ConfigBoolean("autoBuildSbTransfer", true, "Allow transferring materials from Sophisticated Backpacks");
    public static final ConfigInteger AUTO_BUILD_LOW_HEALTH = new ConfigInteger("autoBuildLowHealthThreshold", 6, 0, 20, "Pause auto build when health below this (0=disable)");
    public static final ConfigInteger AUTO_BUILD_ARRIVE_TIMEOUT = new ConfigInteger("autoBuildArriveTimeoutTicks", 200, 20, 2000, "Ticks to wait for Baritone arrival before retry");
    public static final ConfigInteger AUTO_BUILD_UNREACHABLE_RETRIES = new ConfigInteger("autoBuildUnreachableRetries", 3, 0, 10, "Retries before pausing when target unreachable");
    public static final ConfigInteger AUTO_BUILD_MENU_OP_RETRIES = new ConfigInteger("autoBuildMenuOpRetries", 3, 1, 10, "Retries for container menu operations");
    public static final ConfigString AUTO_BUILD_TERMINAL_POS = new ConfigString("autoBuildTerminalPos", "", "Saved RS grid terminal position (x,y,z). Empty = auto-detect");
    public static final ConfigBoolean AUTO_BUILD_SCAFFOLD_ESP = new ConfigBoolean("autoBuildScaffoldEsp", true, "Highlight auto-build scaffold blocks (will be dismantled at cleanup)");
    public static final ConfigColor AUTO_BUILD_SCAFFOLD_COLOR = new ConfigColor("autoBuildScaffoldColor", "#FF40FF40", "ESP highlight color for auto-build scaffold blocks");
    public static final ConfigBoolean ESP_HIGHLIGHT_MISSING = new ConfigBoolean("espHighlightMissingBlocks", true, "Highlight schematic missing blocks through walls (ESP)");
    public static final ConfigBoolean ESP_VERIFICATION_MODE = new ConfigBoolean("espVerificationMode", false, "Verification mode: highlight ALL missing blocks; when off, only highlight blocks matching held item");
    public static final ConfigBoolean ESP_SCAN_ALL = new ConfigBoolean("espScanEntireSchematic", true, "Scan the entire schematic for missing blocks instead of only around the player");
    public static final ConfigInteger ESP_SCAN_INTERVAL = new ConfigInteger("espScanInterval", 20, 1, 200, "Ticks between schematic missing block scans");
    public static final ConfigInteger ESP_SCAN_RANGE = new ConfigInteger("espScanRange", 64, 8, 256, "Radius around player to scan for missing blocks (only when espScanEntireSchematic is off)");
    public static final ConfigInteger ESP_MAX_RENDER = new ConfigInteger("espMaxRenderCount", 0, 0, 200000, "Max number of missing blocks to highlight (0 = no limit)");
    public static final ConfigColor ESP_MISSING_COLOR = new ConfigColor("espMissingColor", "#FFFF4040", "ESP highlight color for missing blocks (verification mode)");
    public static final ConfigBoolean ESP_HIGHLIGHT_BY_INVENTORY = new ConfigBoolean("espHighlightByInventory", false, "Highlight missing blocks whose material is present in player inventory (instead of held item only)");
    public static final ConfigColor ESP_HELD_COLOR = new ConfigColor("espHeldItemColor", "#FF40E0FF", "Color of held-item missing block ESP highlight (non-verification mode)");
    public static final ConfigBoolean CORAL_REPLACE_ENABLED = new ConfigBoolean("coralReplaceEnabled", false, "Enable schematic block replacement (applies to BII, ESP, and printer placement)");
    public static final ConfigHotkey REPLACE_EDITOR_HOTKEY = new ConfigHotkey("printerReplaceEditorHotkey", "", KeybindSettings.PRESS_ALLOWEXTRA, "Open the visual block replacement mapping editor");
    public static final ConfigStringList CORAL_REPLACE_MAPPINGS = new ConfigStringList("blockReplaceMappings",
            com.google.common.collect.ImmutableList.of(),
            "Block replacement mappings, one per line: from=to (e.g. minecraft:horn_coral_block=minecraft:sponge)");
    public static ImmutableList.Builder<IConfigBase> originalList = ImmutableList.builder();
    public static final ImmutableList<IConfigBase> betterList = originalList.addAll((Iterable)ImmutableList.of((Object)VERIFY_INVENTORY, (Object)USE_INVENTORY_CACHE, (Object)PRINTER_OFF, (Object)PRINTER_ONLY_FAKE_ROTATION_MODE, (Object)DISABLE_SYNC, (Object)DEBUG_MESSAGE, (Object)DEBUG_EXTRA_MESSAGE, (Object)DEBUG_ORDER_PLACEMENTS, (Object)DEBUG_PACKET_SYNC, (Object)DISABLE_SINGLEPLAYER_HANDLE, (Object)SLEEP_AFTER_CONSUME, (Object)EASY_PLACE_MODE_RANGE_X, (Object[])new ConfigBase[]{EASY_PLACE_MODE_RANGE_Y, EASY_PLACE_MODE_RANGE_Z, EASY_PLACE_CACHE_TIME, PRINTER_MAX_BLOCKS, PRINTER_MAX_ITEM_CHANGES, PRINTER_BREAK_BLOCKS, PRINTER_BREAK_IGNORE_EXTRA, PRINTER_BREAK_EXTRA_BLOCKS, PRINTER_SKIP_UNKNOWN_BLOCKSTATE, EASY_PLACE_MODE_DELAY, EASY_PLACE_MODE_HOTBAR_ONLY, FLIPPIN_CACTUS, INVENTORY_OPERATIONS, INVENTORY_OPERATIONS_WAIT, INVENTORY_OPERATIONS_RETRY, INVENTORY_OPERATIONS_CLOSE_SCREEN, INVENTORY_OPERATIONS_FILTER_ALLOW_NAMED, CLEAR_AREA_MODE, PRINTER_PLACE_ICE, PRINTER_PLACE_MINECART, PRINTER_CLEAR_FLUIDS_AUTOMATICALLY, CLEAR_AREA_MODE_COBBLESTONE, CLEAR_AREA_MODE_SNOWPREVENT, ACCURATE_BLOCK_PLACEMENT, PRINTER_WATERLOGGED_WATER_FIRST, PRINTER_PUMPKIN_PIE_FOR_COMPOSTER, ADVANCED_ACCURATE_BLOCK_PLACEMENT, PRINTER_SMART_REDSTONE_AVOID, PRINTER_OBSERVER_AVOID_ALL, PRINTER_SUPPRESS_PUSH_LIMIT, AVOID_CHECK_ONLY_PISTONS, BEDROCK_BREAKING, BEDROCK_BREAKING_FORCE_TORCH, BEDROCK_BREAKING_RANGE_SAFE, BEDROCK_BREAKING_CLEAR_WAIT, FAKE_ROTATION_BETA, FAKE_ROTATION_TICKS, FAKE_ROTATION_LIMIT, BII_ENABLED, BI_MAX_SLOTS, BII_INJECT_HOTKEY, BII_CREATE_BLUEPRINT, PRINTER_HOTKEY, ESP_HIGHLIGHT_MISSING, ESP_VERIFICATION_MODE, ESP_HIGHLIGHT_BY_INVENTORY, ESP_SCAN_ALL, ESP_SCAN_INTERVAL, ESP_SCAN_RANGE, ESP_MAX_RENDER, ESP_MISSING_COLOR, ESP_HELD_COLOR, CORAL_REPLACE_ENABLED, CORAL_REPLACE_MAPPINGS, REPLACE_EDITOR_HOTKEY, AUTO_BUILD_TOGGLE_HOTKEY, AUTO_BUILD_PAUSE_HOTKEY, AUTO_BUILD_MARK_TERMINAL_HOTKEY, AUTO_BUILD_SELECTED_ONLY, AUTO_BUILD_SKIP_MISSING, AUTO_BUILD_PRINT_WHILE_MOVING, AUTO_BUILD_ALLOW_PATH_BREAK, AUTO_BUILD_ALLOW_SCAFFOLD, AUTO_BUILD_RS_EXTRACT, AUTO_BUILD_SB_TRANSFER, AUTO_BUILD_LOW_HEALTH, AUTO_BUILD_ARRIVE_TIMEOUT, AUTO_BUILD_UNREACHABLE_RETRIES, AUTO_BUILD_MENU_OP_RETRIES, AUTO_BUILD_TERMINAL_POS, AUTO_BUILD_SCAFFOLD_ESP, AUTO_BUILD_SCAFFOLD_COLOR})).build();

    public LitematicaMixinMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[Printer] : YeeFuckinHaw");
        MinecraftForge.EVENT_BUS.register((Object)this);
        MinecraftForge.EVENT_BUS.register(MissingBlockEsp.class);
        MinecraftForge.EVENT_BUS.register(xyz.jxmm.litematica_printer_forge.autobuild.scaffold.ScaffoldEsp.class);
        xyz.jxmm.litematica_printer_forge.autobuild.director.BuildCommand.register();
    }

    private static boolean isHotkeyPressed(ConfigHotkey hotkey, net.minecraft.client.Minecraft mc) {
        boolean pressed = false;
        try {
            java.util.List<Integer> keys = hotkey.getKeybind().getKeys();
            if (keys != null) {
                long handle = mc.m_91268_().m_85439_();
                for (Integer keyCode : keys) {
                    if (keyCode == null) continue;
                    boolean down = keyCode < 0
                            ? org.lwjgl.glfw.GLFW.glfwGetMouseButton(handle, keyCode + 100) == 1
                            : keyCode > 0 && org.lwjgl.glfw.GLFW.glfwGetKey(handle, keyCode) == 1;
                    if (down) {
                        pressed = true;
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return pressed;
    }

    private static boolean defaultMappingsSeeded = false;
    private static boolean wasEditorPressed = false;
    private static boolean wasAutoBuildTogglePressed = false;
    private static boolean wasAutoBuildPausePressed = false;
    private static boolean wasMarkTerminalPressed = false;

    // Hotkey to open the visual block replacement mapping editor.
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.m_91087_();

        // Replacement mapping editor hotkey (edge-triggered)
        boolean pressed = isHotkeyPressed(REPLACE_EDITOR_HOTKEY, mc);
        boolean justPressed = pressed && !wasEditorPressed;
        wasEditorPressed = pressed;
        if (justPressed && mc.f_91073_ != null && mc.f_91080_ == null) {
            xyz.jxmm.litematica_printer_forge.gui.ReplaceMappingScreen.open(mc);
            return;
        }

        // Auto Build Director hotkeys (edge-triggered GLFW polling)
        boolean abToggle = isHotkeyPressed(AUTO_BUILD_TOGGLE_HOTKEY, mc);
        boolean abToggleJust = abToggle && !wasAutoBuildTogglePressed;
        wasAutoBuildTogglePressed = abToggle;
        if (abToggleJust && mc.f_91073_ != null) {
            xyz.jxmm.litematica_printer_forge.autobuild.director.AutoBuildDirector.toggle();
        }

        boolean abPause = isHotkeyPressed(AUTO_BUILD_PAUSE_HOTKEY, mc);
        boolean abPauseJust = abPause && !wasAutoBuildPausePressed;
        wasAutoBuildPausePressed = abPause;
        if (abPauseJust && mc.f_91073_ != null) {
            xyz.jxmm.litematica_printer_forge.autobuild.director.AutoBuildDirector.pauseOrResume();
        }

        boolean abMark = isHotkeyPressed(AUTO_BUILD_MARK_TERMINAL_HOTKEY, mc);
        boolean abMarkJust = abMark && !wasMarkTerminalPressed;
        wasMarkTerminalPressed = abMark;
        if (abMarkJust && mc.f_91073_ != null) {
            xyz.jxmm.litematica_printer_forge.autobuild.director.AutoBuildDirector.markTerminal();
        }

        // Auto Build Director tick (zero overhead when not running)
        xyz.jxmm.litematica_printer_forge.autobuild.director.AutoBuildDirector.tick(mc);

        // Scaffold marker lifecycle: load on world join, debounced save,
        // release markers whose cells became air (FR-17).
        xyz.jxmm.litematica_printer_forge.autobuild.scaffold.ScaffoldManager.onClientTick(mc);

        // Seed built-in replacement mapping lines into an empty blockReplaceMappings list.
        // Runs once per login AFTER litematica loaded the config (player in world), otherwise
        // the seeded values are overwritten by the config file load.
        // Also drops legacy lines with invalid copper registry names (exposed/weathered/oxidized_copper_block).
        if (mc.f_91074_ == null) {
            defaultMappingsSeeded = false;
            return;
        }
        if (defaultMappingsSeeded) {
            return;
        }
        defaultMappingsSeeded = true;
        try {
            java.util.List<String> cur = CORAL_REPLACE_MAPPINGS.getStrings();
            boolean changed = false;
            java.util.List<String> cleaned = new java.util.ArrayList<>();
            if (cur != null) {
                for (String s : cur) {
                    if (s != null && s.matches(".*minecraft:(exposed|weathered|oxidized)_copper_block\\s*=.*")) {
                        changed = true;
                        continue;
                    }
                    cleaned.add(s);
                }
            }
            if (cleaned.isEmpty()) {
                CORAL_REPLACE_MAPPINGS.setStrings(xyz.jxmm.litematica_printer_forge.utils.BlockReplacer.getDefaultMappingLines());
                LOGGER.info("[Printer] seeded default blockReplaceMappings into config");
            } else if (changed) {
                CORAL_REPLACE_MAPPINGS.setStrings(cleaned);
                LOGGER.info("[Printer] removed invalid legacy blockReplaceMappings lines");
            }
        } catch (Exception e) {
            LOGGER.warn("[Printer] failed to fix blockReplaceMappings", e);
        }
    }
}
