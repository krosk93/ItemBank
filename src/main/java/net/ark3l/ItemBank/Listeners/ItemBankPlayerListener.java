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

import net.ark3l.InventoryUtils.VirtualChest.VirtualLargeChest;
import net.ark3l.ItemBank.BankManager;
import net.ark3l.ItemBank.ItemBankPlugin;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.player.SpoutPlayer;

import java.util.List;

public class ItemBankPlayerListener implements Listener {

    private final BankManager bm;

    public ItemBankPlayerListener(ItemBankPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.bm = plugin.bankManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Location l = block.getLocation();
        SpoutPlayer player = (SpoutPlayer) event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.CHEST && bm.isItemBank(l)) {
            String network = bm.getNetwork(l);

            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                player.sendMessage(ChatColor.DARK_RED + "You can't do that in creative mode!");
                event.setCancelled(true);
            } else if (bm.playersUsingBanks.containsKey(player.getName())) {
                player.sendMessage((ChatColor.DARK_RED + "You are already using a bank"));
                event.setCancelled(true);
            } else if (!player.hasPermission("itembank." + bm.getNetwork(l)) && !player.hasPermission("itembank.admin")) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have permission to use " + network + " banks");
                event.setCancelled(true);
            } else {


                VirtualLargeChest virtualChest = new VirtualLargeChest(Character.toUpperCase(network.charAt(0)) + network.substring(1) + " bank");
                List<SpoutItemStack> accContent = bm.getItems(player.getName(), network);

                for (SpoutItemStack item : accContent) {
                    virtualChest.addItem(item);
                }

                virtualChest.openChest(player);

                bm.playersUsingBanks.put(player.getName(), network);

                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        bm.playersUsingBanks.remove(event.getPlayer().getName());
    }
}
