package com.simplecontracts;

import com.mojang.logging.LogUtils;
import com.simplecontracts.registry.ModBlocks;
import com.simplecontracts.registry.ModItems;
import com.simplecontracts.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SimpleContracts.MOD_ID)
public final class SimpleContracts {
    public static final String MOD_ID = "simplecontracts";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpleContracts(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        LOGGER.info("Simple Contracts loaded");
    }
}
