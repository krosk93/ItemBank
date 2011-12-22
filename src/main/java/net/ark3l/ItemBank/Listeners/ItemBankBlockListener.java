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
import org.bukkit.ChatColor;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class ItemBankBlockListener extends BlockListener {

    private final BankManager bm;

    public ItemBankBlockListener(ItemBankPlugin plugin) {
        bm = plugin.bankManager;
    }

    public void onBlockBreak(BlockBreakEvent event) {
        if (bm.isItemBank(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot destroy an ItemBank. Remove it first.");
        }
    }
}
