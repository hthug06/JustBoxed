package fr.ht06.justBoxed.World;

import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.WorldRunnable;
import fr.ht06.justBoxed.WorldBorderManager;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;

public class LoadWorld {

    public static void load(String worldName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
//                    player.sendActionBar(Component.text("Loading the world...", NamedTextColor.GOLD));
                    new WorldCreator(worldName).createWorld();
                }catch (IllegalStateException ignored){}

                cancel();
            }
        }.runTaskAsynchronously(JustBoxed.getInstance());

        new BukkitRunnable() {


            @Override
            public void run() {
                //wait for the world to be loaded to tp the player
                if (Bukkit.getWorld(worldName) != null){

                    JustBoxed.worldManager.add(new WorldRunnable(Bukkit.getWorld(worldName)));

                    //set the wb
                    WorldBorderManager.setWorldBorder(JustBoxed.boxManager.getBoxByWorldName(worldName));
                    cancel();
                }
            }
        }.runTaskTimer(JustBoxed.getInstance(), 20, 20);
    }



}
