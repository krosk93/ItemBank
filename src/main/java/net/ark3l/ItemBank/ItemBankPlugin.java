package net.ark3l.ItemBank;

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

import net.ark3l.ItemBank.Listeners.ItemBankBlockListener;
import net.ark3l.ItemBank.Listeners.ItemBankInventoryListener;
import net.ark3l.ItemBank.Listeners.ItemBankPlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class ItemBankPlugin extends JavaPlugin {

    public BankManager bankManager;


    public void onDisable() {
        Log.info("Disabled");
    }

    public void onEnable() {

        bankManager = new BankManager(this);

        ItemBankPlayerListener playerListener = new ItemBankPlayerListener(this);
        ItemBankInventoryListener inventoryListener = new ItemBankInventoryListener(this);
        ItemBankBlockListener blockListener = new ItemBankBlockListener(this);

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Highest, this);
        pm.registerEvent(Event.Type.CUSTOM_EVENT, inventoryListener, Priority.Normal, this);

        Log.info(this + " enabled");
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("itembank")) {
            if (!sender.hasPermission("itembank.admin")) {
                sender.sendMessage(ChatColor.DARK_RED + "This command is only available to ops");
                return true;
            }
        }

        if (args.length > 0) {

            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args[0].equalsIgnoreCase("add")) {
                    addBank(player, args.length > 1 ? args[1] : null);
                    return true;
                } else if (args[0].equalsIgnoreCase("remove")) {
                    removeBank(player);
                    return true;
                }
            }
        }
        return false;
    }

    private void addBank(Player player, String arg) {
        if (arg == null) {
            arg = "default";
        }

        Block b = player.getTargetBlock(null, 10);
        if (b.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.DARK_RED + "The block you are looking at is not a chest");
            return;
        }
        Location bankLocation = new Location(player.getWorld(), b.getX(), b.getY(), b.getZ());

        if (bankManager.isItemBank(bankLocation)) {
            player.sendMessage(ChatColor.DARK_RED + "That block is already an ItemBank");
            return;
        }
        bankManager.addBank(bankLocation, arg);

        Block other = checkForAdjacentChests(b);
        if (other != null) {
            Location otherBankLocation = new Location(player.getWorld(), other.getX(), other.getY(), other.getZ());
            bankManager.addBank(otherBankLocation, arg);
        }

        player.sendMessage(ChatColor.DARK_GREEN + "ItemBank added on the " + ChatColor.WHITE + arg + ChatColor.DARK_GREEN + " network");
    }

    private void removeBank(Player player) {
        Block b = player.getTargetBlock(null, 10);
        if (b.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.DARK_RED + "The block you are looking at is not a chest");
            return;
        }

        if (bankManager.isItemBank(b.getLocation())) {
            bankManager.removeBank(b.getLocation());
            player.sendMessage(ChatColor.DARK_GREEN + "ItemBank removed");
        } else {
            player.sendMessage(ChatColor.DARK_RED + "The block you are looking at is not a bank");
        }

        Block other = checkForAdjacentChests(b);
        if (other != null) {
            bankManager.removeBank(other.getLocation());
        }
    }

    private Block checkForAdjacentChests(Block b) {
        if (b.getRelative(BlockFace.NORTH).getType() == Material.CHEST) {
            return b.getRelative(BlockFace.NORTH);
        } else if (b.getRelative(BlockFace.EAST).getType() == Material.CHEST) {
            return b.getRelative(BlockFace.EAST);
        } else if (b.getRelative(BlockFace.SOUTH).getType() == Material.CHEST) {
            return b.getRelative(BlockFace.SOUTH);
        } else if (b.getRelative(BlockFace.WEST).getType() == Material.CHEST) {
            return b.getRelative(BlockFace.WEST);
        }

        return null;
    }

}
