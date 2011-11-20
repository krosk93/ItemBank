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

import java.util.logging.Logger;

class Log {

    private static final Logger log = Logger.getLogger("Minecraft");

    public static void info(String msg) {
        log.info("[ItemBank] " + msg);
    }

    public static void warning(String msg) {
        log.warning("[ItemBank] " + msg);
    }

    public static void severe(String msg) {
        log.severe("[ItemBank] " + msg);
    }


}

