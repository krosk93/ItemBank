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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
import org.getspout.spoutapi.player.SpoutPlayer;

public class ItemBankInventoryListener implements Listener {

    private final BankManager bm;

    public ItemBankInventoryListener(ItemBankPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.bm = plugin.bankManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        SpoutPlayer player = (SpoutPlayer) event.getPlayer();

        if (bm.playersUsingBanks.containsKey(player.getName())) {
            final Inventory inv = event.getInventory();
            bm.saveItems(player.getName(), bm.playersUsingBanks.get(player.getName()), inv.getContents());
            bm.playersUsingBanks.remove(player.getName());
        }
    }
}
