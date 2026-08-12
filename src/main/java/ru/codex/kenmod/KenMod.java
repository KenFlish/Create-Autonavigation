package ru.codex.kenmod;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import ru.codex.kenmod.block.NavigationWorkbenchBlock;
import ru.codex.kenmod.block.entity.NavigationWorkbenchBlockEntity;
import ru.codex.kenmod.menu.NavigationWorkbenchMenu;
import ru.codex.kenmod.network.ModPayloadRegistrar;

@Mod(KenMod.MOD_ID)
public class KenMod {
    public static final String MOD_ID = "create_autonavigation";
    private static final ResourceLocation CREATE_BASE_TAB_ID = ResourceLocation.fromNamespaceAndPath("create", "base");
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    public static final DeferredBlock<Block> NAVIGATION_WORKBENCH = BLOCKS.register("navigation_workbench", () -> new NavigationWorkbenchBlock());
    public static final DeferredItem<BlockItem> NAVIGATION_WORKBENCH_ITEM = ITEMS.registerSimpleBlockItem("navigation_workbench", NAVIGATION_WORKBENCH);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NavigationWorkbenchBlockEntity>> NAVIGATION_WORKBENCH_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("navigation_workbench", () -> BlockEntityType.Builder.of(
                    NavigationWorkbenchBlockEntity::new,
                    NAVIGATION_WORKBENCH.get()
            ).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<NavigationWorkbenchMenu>> NAVIGATION_WORKBENCH_MENU =
            MENU_TYPES.register("navigation_workbench", () -> IMenuTypeExtension.create(NavigationWorkbenchMenu::new));

    public KenMod(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreativeTabItems);
        modEventBus.addListener(ModPayloadRegistrar::register);

        LOGGER.info("Create: Autonavigation loaded");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Common setup complete for {}", MOD_ID);
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(CREATE_BASE_TAB_ID)) {
            event.accept(NAVIGATION_WORKBENCH_ITEM);
        }
    }
}
