package me.mchiappinam.pdgharmas;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;

public class InventoryHelper
{
    public static int amtItem(final Inventory inventory, final int itemid, final byte dat) {
        int ret = 0;
        if (inventory != null) {
            final ItemStack[] items = inventory.getContents();
            for (int slot = 0; slot < items.length; ++slot) {
                if (items[slot] != null) {
                    final int id = items[slot].getTypeId();
                    final int itmDat = items[slot].getData().getData();
                    final int amt = items[slot].getAmount();
                    if (id == itemid && (dat == itmDat || dat == -1)) {
                        ret += amt;
                    }
                }
            }
        }
        return ret;
    }
    
    public static void removeItem(final Inventory inventory, final int itemid, final byte dat, int amt) {
        final int start = amt;
        if (inventory != null) {
            final ItemStack[] items = inventory.getContents();
            for (int slot = 0; slot < items.length; ++slot) {
                if (items[slot] != null) {
                    final int id = items[slot].getTypeId();
                    final int itmDat = items[slot].getData().getData();
                    int itmAmt = items[slot].getAmount();
                    if (id == itemid && (dat == itmDat || dat == -1)) {
                        if (amt > 0) {
                            if (itmAmt >= amt) {
                                itmAmt -= amt;
                                amt = 0;
                            }
                            else {
                                amt = start - itmAmt;
                                itmAmt = 0;
                            }
                            if (itmAmt > 0) {
                                inventory.getItem(slot).setAmount(itmAmt);
                            }
                            else {
                                inventory.setItem(slot, (ItemStack)null);
                            }
                        }
                        if (amt <= 0) {
                            return;
                        }
                    }
                }
            }
        }
    }
}
