package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class UnloadInactiveBox {


    public static void unloadAll() {
        //get all the world we can unload
        List<String> untouchableWorld = new ArrayList<>(List.of("world", "world_nether", "world_the_end"));
        untouchableWorld.addAll(JustBoxed.getInstance().getConfig().getStringList("alwaysLoadedWorld"));

        //get the second before unloading
        int sec = JustBoxed.getInstance().getConfig().getInt("inactivityUnload");

        Bukkit.getWorlds().forEach(world -> {

            //unload each world but not the untouchable
            if (!untouchableWorld.contains(world.getName())) {
                //unload only if there is no player
                if (Bukkit.getWorld(world.getName()).getPlayers().isEmpty()) {
                    JustBoxed.getInstance()
                            .getComponentLogger()
                            .info(Component.text("Unload " + world.getName() + " for " + sec + " seconds of inactivity", NamedTextColor.RED));

                    Bukkit.unloadWorld(world, false);
                }
            }
        });
    }

    public static void unload(Box box) {
        //the world can be unloaded before this
        if (Bukkit.getWorld(box.getWorldName()) == null) {
            return;
        }
        //get all the world we can unload
        List<String> untouchableWorld = new ArrayList<>(List.of("world", "world_nether", "world_the_end"));
        untouchableWorld.addAll(JustBoxed.getInstance().getConfig().getStringList("alwaysLoadedWorld"));
        int sec = JustBoxed.getInstance().getConfig().getInt("inactivityUnload");

        new BukkitRunnable() {
            @Override
            public void run() {
                //the world can be unloaded before this
                if (Bukkit.getWorld(box.getWorldName()) == null) {
                    cancel();
                    return;
                }
                else {
                    //unload only if there is no player
                    if (Bukkit.getWorld(box.getWorldName()).getPlayers().isEmpty()) {
                        JustBoxed.getInstance()
                                .getComponentLogger()
                                .info(Component.text("Unload " + box.getWorldName() + " for " + sec + " seconds of inactivity", NamedTextColor.RED));
                        Bukkit.unloadWorld(box.getWorldName(), true);
                        cancel();
                    }
                }
            }
        }.runTaskLater(JustBoxed.getInstance(), 20L *sec);
    }
}
