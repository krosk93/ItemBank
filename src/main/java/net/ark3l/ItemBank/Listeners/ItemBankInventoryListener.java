package net.ark3l.ItemBank.Listeners;

/*
* ItemBank - In game item banking for Bukkit Minecraft servers with Spout
* Copyright (C) 2011 Oliver Brown (Arkel)
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
* /
*/

import net.ark3l.ItemBank.BankManager;
import net.ark3l.ItemBank.ItemBankPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;

public class ItemBankInventoryListener extends InventoryListener {

    private final ItemBankPlugin plugin;
    private final BankManager bm;

    public ItemBankInventoryListener(ItemBankPlugin plugin) {
        super();
        this.plugin = plugin;
        this.bm = plugin.bankManager;
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        final Player player = event.getPlayer();
        if (bm.playersUsingBanks.contains(player.getName())) {
            final Inventory inv = event.getInventory();
            Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    bm.saveItems(player.getName(), inv.getContents().clone());
                }
            });
            bm.playersUsingBanks.remove(player.getName());
        }
    }

}