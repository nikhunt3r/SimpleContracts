package com.simplecontracts.registry;

import com.simplecontracts.SimpleContracts;
import com.simplecontracts.menu.ContractBoardMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SimpleContracts.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ContractBoardMenu>> CONTRACT_BOARD =
            MENUS.register(
                    "contract_board",
                    () -> new MenuType<>(ContractBoardMenu::new, FeatureFlags.VANILLA_SET)
            );

    private ModMenus() {
    }
}
