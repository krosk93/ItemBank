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
import net.ark3l.ItemBank.Chest.VirtualLargeChest;
import net.ark3l.ItemBank.ItemBankPlugin;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ItemBankPlayerListener extends PlayerListener {

    private final ItemBankPlugin plugin;
    private final BankManager bm;

    public ItemBankPlayerListener(ItemBankPlugin plugin) {
        this.plugin = plugin;
        this.bm = plugin.bankManager;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event == null)
            return;
        if (event.isCancelled())
            return;


        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Location l = block.getLocation();
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.CHEST && bm.isItemBank(player.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ())) {
            if (!player.hasPermission("itembank.use")) {
                player.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do that");
                event.setCancelled(true);
                return;
            }
            if (player.getGameMode().equals(GameMode.CREATIVE)) {
                player.sendMessage(ChatColor.DARK_RED + "You can't do that in creative mode!");
                event.setCancelled(true);
                return;
            }

            ArrayList<ItemStack> accContent = bm.getItems(player.getName());
            ItemStack[] content = new ItemStack[54];
            for (int i = 0; i < accContent.size(); i++) {
                content[i] = accContent.get(i);
            }

            net.minecraft.server.ItemStack[] mcContents = new net.minecraft.server.ItemStack[54];

            for (int i = 0; i < content.length; i++) {
                if (content[i] != null) {
                    mcContents[i] = new net.minecraft.server.ItemStack(content[i].getTypeId(), content[i].getAmount(), content[i].getDurability());
                }
            }
            VirtualLargeChest chest = new VirtualLargeChest("Bank");

            for (net.minecraft.server.ItemStack i : mcContents) {
                chest.addItemStack(i);
            }

            chest.openChest(player);

            bm.playersUsingBanks.add(player.getName());

            event.setCancelled(true);
        }
    }


    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        bm.playersUsingBanks.remove(event.getPlayer().getName());
    }
}
