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

import net.ark3l.InventoryUtils.Persistence.PersistenceManager;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.inventory.SpoutItemStack;

import java.io.*;
import java.util.*;

public class BankManager extends PersistenceManager {

    private final ItemBankPlugin plugin;

    private final File bankFile;

    public final HashMap<String, String> playersUsingBanks = new HashMap<String, String>();
    private final HashMap<Integer, String> banks = new HashMap<Integer, String>();

    public BankManager(ItemBankPlugin instance) {
        super(instance);

        plugin = instance;
        bankFile = new File(instance.getDataFolder() + File.separator + "data" + File.separator + "banks");

        try {
            if (!bankFile.exists()) {
                bankFile.getParentFile().mkdirs();
                bankFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadBanks();
    }

    private void loadBanks() {
        List<String> banksText = getLinesFromFile(bankFile);

        for (String str : banksText) {
            Scanner sc = new Scanner(str);
            sc.useDelimiter(";");

            int hash = sc.nextInt();
            String network = sc.next();

            banks.put(hash, network);
        }
    }

    private void saveBanks() {
        List<String> banksAsStrings = new ArrayList<String>();
        for (Map.Entry<Integer, String> integerEntry : banks.entrySet()) {
            Map.Entry pairs = (Map.Entry) integerEntry;
            int hash = pairs.getKey().hashCode();
            String network = (String) pairs.getValue();
            String line = hash + ";" + network;
            banksAsStrings.add(line);
        }
        writeLinesToFile(banksAsStrings, bankFile);
    }

    /**
     * Checks whether the given location contains an ItemBank using a hashcode for comparison to a known list
     *
     * @param location The location to check
     * @return True if location contains an ItemBank
     */
    public boolean isItemBank(Location location) {
        return banks.containsKey(location.hashCode());
    }

    /**
     * Gets the name of the network that the ItemBank at the location belongs to
     *
     * @param location The location of the bank to get the network of
     * @return The name of the banks network. Returns "default" if the location doesn't contain a bank
     */
    public String getNetwork(Location location) {
        if (isItemBank(location)) {
            return banks.get(location.hashCode());
        } else {
            return "default";
        }
    }

    /**
     * Adds an ItemBank
     *
     * @param location The location of the ItemBank
     * @param network  The name of the network the ItemBank belongs to
     */
    public void addBank(Location location, String network) {
        banks.put(location.hashCode(), network);
        writeLineToFile(location.hashCode() + ";" + network, bankFile);
    }

    /**
     * Removes an ItemBank
     *
     * @param location The location of the ItemBank to remove
     */
    public void removeBank(Location location) {
        banks.remove(location.hashCode());

        // Wipe the file, create a new one, and write to it
        try {
            bankFile.delete();
            bankFile.createNewFile();
            saveBanks();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Wrapper class to save items
     */
    public void saveItems(String playerName, String network, ItemStack[] contents) {
        super.saveItems(new File(plugin.getDataFolder() + File.separator + "data" + File.separator + network + File.separator + playerName + ".bank"), contents);
    }

    /**
     * Wrapper class to get items
     */
    public List<SpoutItemStack> getItems(String playerName, String network) {
        return super.getItems(new File(plugin.getDataFolder() + File.separator + "data" + File.separator + network + File.separator + playerName + ".bank"));
    }
}
