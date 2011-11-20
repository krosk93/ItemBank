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

import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class BankManager {

    private static final String mainDirectory = "plugins" + File.separator + "ItemBank" + File.separator;

    private ArrayList<Bank> bankList;
    public final LinkedList<String> playersUsingBanks = new LinkedList<String>();

    private static final String sqlite = "jdbc:sqlite:" + mainDirectory + "ItemBanksDB.sqlite";

    public BankManager() {

        File dataDir = new File(mainDirectory);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }

        if (!this.checkTable()) {
            try {
                Log.info("Creating new tables");
                createTables();
            } catch (SQLException e) {
                Log.severe("SQL Exception");
                e.printStackTrace();
            }
        }
    }

    private void createTables() throws SQLException {
        Connection conn = this.connection();
        Statement st = conn.createStatement();
        st.executeUpdate("CREATE TABLE banks (world VARCHAR(255), x INT, y INT, z INT);");
        st.executeUpdate("CREATE TABLE items (owner VARCHAR(255), itemID INT, amount INT);");
    }

    private boolean checkTable() {
        Connection conn = null;
        ResultSet rs = null;
        boolean result = false;

        try {
            conn = this.connection();
            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, "banks", null);
            result = rs.next();
        } catch (SQLException ex) {
            Log.severe("Table check failed: " + ex);
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Log.severe("Failed to close connection");
            }
        }

        return result;
    }

    /**
     * Check whether an item bank exists at the specified location
     * @param world the world
     * @param x the x-coordinate of the location
     * @param y the y-coordinate of the location
     * @param z the z-coordinate of the location
     * @return true if the block at the given location is an item bank
     */
    public boolean isItemBank(String world, int x, int y, int z) {
        Bank b = new Bank(world, x, y, z);
        return bankList.contains(b);
    }

    private Connection connection() {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(sqlite);
        } catch (ClassNotFoundException e) {
            Log.severe("Couldn't find SQLite database driver");
            e.printStackTrace();
        } catch (SQLException e1) {
            Log.severe("SQL Exception");
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * Initialize by loading the bankList from the database
     */
    public void initialize() {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.connection();
            ps = conn.prepareStatement("SELECT * FROM banks;");
            ResultSet results = ps.executeQuery();
            bankList = new ArrayList<Bank>();

            while (results != null && results.next()) {
                Bank b = new Bank(results.getString(1), results.getInt(2), results.getInt(3), results.getInt(4));
                bankList.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Log.severe("Failed to close connection");
            }
        }
    }

    /**
     * Add the bank to the database and list
     * @param bank the bank to add
     */
    public void addBank(Bank bank) {
        bankList.add(bank);

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.connection();
            ps = conn.prepareStatement("INSERT INTO banks (world, x, y, z) VALUES(?,?,?,?)");
            ps.setString(1, bank.worldname);
            ps.setInt(2, bank.x);
            ps.setInt(3, bank.y);
            ps.setInt(4, bank.z);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Log.severe("Failed to close connection");
            }
        }

    }

    /**
     * Remove the given bank from the database and list
     * @param bank the bank to remove
     */
    public void removeBank(Bank bank) {
        bankList.remove(bank);

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.connection();
            ps = conn.prepareStatement("DELETE FROM banks WHERE world = ? AND x = ? AND y = ? AND z = ?");
            ps.setString(1, bank.worldname);
            ps.setInt(2, bank.x);
            ps.setInt(3, bank.y);
            ps.setInt(4, bank.z);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Log.severe("Failed to close connection");
            }
        }

    }

    /**
     * Save the given items in the database as the given player
     * @param playerName the name of the player who owns the items
     * @param items an array containing the items to add to the database
     */
    public void saveItems(String playerName, ItemStack[] items) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.connection();
            ps = conn.prepareStatement("DELETE FROM items WHERE owner = ?");
            ps.setString(1, playerName);
            ps.executeUpdate();

            for (ItemStack i : items) {
                if (i != null) {
                    ps = conn.prepareStatement("INSERT INTO items (owner, itemID, amount) VALUES (?,?,?)");
                    ps.setString(1, playerName);
                    ps.setInt(2, i.getTypeId());
                    ps.setInt(3, i.getAmount());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Log.severe("Failed to close connection");
            }
        }

    }

    /**
     * Retrieve a players items from the database
     * @param playerName the name of the player to retrieve items for
     * @return an ArrayList containing the items
     */
    public ArrayList<ItemStack> getItems(String playerName) {

        ArrayList<ItemStack> items = new ArrayList<ItemStack>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.connection();
            ps = conn.prepareStatement("SELECT * FROM items WHERE owner = ?");
            ps.setString(1, playerName);
            rs = ps.executeQuery();

            while (rs.next()) {
                items.add(new ItemStack(rs.getInt("itemID"), rs.getInt("amount")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                Log.severe("Failed to close connection");
            }
        }

        return items;
    }


}

