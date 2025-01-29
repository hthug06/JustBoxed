package fr.ht06.justBoxed;

import fr.ht06.justBoxed.Box.Box;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class WorldBorderManager {

    //used to change the worldborder in every box world

    public static void setWorldBorder(Box box){
        World world = Bukkit.getWorld(box.getWorldName());
        Location loc = new Location(Bukkit.getWorld(box.getWorldName()),
                box.getSpawn().getX(),
                box.getSpawn().getY(),
                box.getSpawn().getZ());
        world.getWorldBorder().setCenter(loc);
        world.getWorldBorder().setSize(box.getSize());
    }

    public static void setWorldBorder(Box box, int time){
        World world = Bukkit.getWorld(box.getWorldName());
        Location loc = new Location(Bukkit.getWorld(box.getWorldName()),
                box.getSpawn().getX(),
                box.getSpawn().getY(),
                box.getSpawn().getZ());
        world.getWorldBorder().setCenter(loc);
        world.getWorldBorder().setSize(box.getSize(), time);
    }
}
