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

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.inventory.SpoutItemStack;

import java.io.*;
import java.util.*;

public class BankManager {

    private final ItemBankPlugin plugin;

    private final File bankFile;

    public final HashMap<String, String> playersUsingBanks = new HashMap<String, String>();
    private final HashMap<Integer, String> banks = new HashMap<Integer, String>();

    public BankManager(ItemBankPlugin instance) {

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
     * Get the players Items on the specified network
     *
     * @param playerName The name of the player
     * @param network    The network to retrieve the items from
     * @return A list containing the Items
     */
    public List<SpoutItemStack> getItems(String playerName, String network) {
        File f = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + network + File.separator + playerName + ".bank");

        // Create any missing stuff
        if (!f.exists()) {
            try {
                f.getParentFile().mkdirs();
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayList<SpoutItemStack> itemStacks = new ArrayList<SpoutItemStack>();
        List<String> lines = getLinesFromFile(f);

        for (String line : lines) {
            itemStacks.add(stringToItemStack(line));
        }

        return itemStacks;
    }

    /**
     * Save the items for the player on the specified network
     *
     * @param playerName The player's name
     * @param network    The network to save the items on
     * @param items      An array containing the items
     */
    public void saveItems(String playerName, String network, ItemStack[] items) {
        File f = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + network + File.separator + playerName + ".bank");

        // Create the folder for the network if it doesn't exist
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        // Wipe the file and save a new one in its place
        if (f.exists()) {
            f.delete();
        }
        try {
            f.createNewFile();
            List<String> itemsAsStrings = new ArrayList<String>();
            for (ItemStack i : items) {
                itemsAsStrings.add(itemStackToString(i));
            }
            writeLinesToFile(itemsAsStrings, f);
        } catch (IOException e) {
            Log.severe("Error creating file " + f.getPath());
            e.printStackTrace();
        }

    }

    /**
     * Writes a line to a file
     *
     * @param line The line to be written
     * @param file The file to write to
     */
    private void writeLineToFile(String line, File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(line);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            Log.severe("Error writing to file " + file.getPath());
            e.printStackTrace();
        }
    }

    /**
     * Write several lines to a file
     *
     * @param lines A list containing the lines to be written
     * @param file
     */
    private void writeLinesToFile(List<String> lines, File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            Log.severe("Error writing to file " + file.getPath());
            e.printStackTrace();
        }
    }

    /**
     * Gets all the lines from a file
     *
     * @param file The file to retrieve the lines from
     * @return A list containing the lines retrieved from the file
     */
    private List<String> getLinesFromFile(File file) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                if (!strLine.isEmpty())
                    lines.add(strLine);
            }

            br.close();
        } catch (Exception e) {
            Log.severe("Error reading file " + file.getPath());
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * Serializes an ItemStack, turning it into a series of strings seperated by semi-colons
     *
     * @param itemstack The ItemStack to serialize
     * @return A string in the format ITEMID;AMOUNT;DATA;DURABILITY;ENCHANTMENTID;ENCHANTMENTLEVEL;ENCHA....
     */
    private String itemStackToString(ItemStack itemstack) {
        StringBuilder sb = new StringBuilder();

        if (itemstack != null) {
            sb.append(itemstack.getTypeId());
            sb.append(";");
            sb.append(itemstack.getAmount());
            sb.append(";");
            sb.append(itemstack.getData().getData());
            sb.append(";");
            sb.append(itemstack.getDurability());
            for (Map.Entry<Enchantment, Integer> enchantmentEntry : itemstack.getEnchantments().entrySet()) {
                Map.Entry pairs = (Map.Entry) enchantmentEntry;
                Enchantment ench = (Enchantment) pairs.getKey();
                int level = (Integer) pairs.getValue();
                sb.append(";").append(ench.getId()).append(";").append(level);
            }
        }

        return sb.toString();
    }

    /**
     * Converts a string into a SpoutItemStack
     *
     * @param string A string in the format ITEMID;AMOUNT;DATA;DURABILITY;ENCHANTMENTID;ENCHANTMENTLEVEL;ENCHA....
     * @return The ItemStack produced by the string
     */
    private SpoutItemStack stringToItemStack(String string) {
        Scanner sc = new Scanner(string);
        sc.useDelimiter(";");

        int typeid = sc.nextInt();
        int amount = sc.nextInt();
        byte data = sc.nextByte();
        int durability = sc.nextInt();

        SpoutItemStack itemStack = new SpoutItemStack(typeid, amount, data);

        while (sc.hasNextInt()) {
            int id = sc.nextInt();
            int level = sc.nextInt();
            try {
                itemStack.addEnchantment(Enchantment.getById(id), level);
            } catch (IllegalArgumentException e) {
                itemStack.addUnsafeEnchantment(Enchantment.getById(id), level);
            }
        }

        itemStack.setDurability((short) durability);

        return itemStack;
    }

}
