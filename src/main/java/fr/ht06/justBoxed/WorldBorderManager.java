package fr.ht06.justBoxed;

import fr.ht06.justBoxed.Box.Box;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldBorderManager {

    public static void setWorldBorder(Box box){
        World world = Bukkit.getWorld(box.getWorldName());
        world.getWorldBorder().setCenter(box.getLocation());
        world.getWorldBorder().setSize(box.getSize());
    }

    public static void setWorldBorder(Box box, int time){
        World world = Bukkit.getWorld(box.getWorldName());
        world.getWorldBorder().setCenter(box.getLocation());
        world.getWorldBorder().setSize(box.getSize(), time);
    }
}
