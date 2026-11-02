/*
 * Portions derived from copper-hopper by pcal.net, used under the MIT License.
 * Modifications Copyright (c) 2024 nullcoil
 */

package net.nullcoil.paritem.gui.hoppers.copper;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.MenuType;
import net.nullcoil.paritem.gui.ModMenuTypes;

public class CopperHopperScreenHandler extends HopperMenu {

    public CopperHopperScreenHandler(int syncId, Inventory playerInventory) {
        super(syncId, playerInventory);
    }

    public CopperHopperScreenHandler(int syncId, Inventory playerInventory, Container inventory) {
        super(syncId, playerInventory, inventory);
    }

    @Override
    public MenuType<?> getType() {
        return ModMenuTypes.COPPER_HOPPER_MENU;
    }
}