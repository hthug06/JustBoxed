package fr.ht06.justBoxed.Runnable;

import fr.ht06.justBoxed.JustBoxed;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldRunnable extends BukkitRunnable {



    World world;
    int ActiveTime = 0;

    public WorldRunnable(World world) {
        this.world = world;
        this.runTaskTimer(JustBoxed.getInstance(), 0, 20L);
        launchInactiveCheck();
    }

    @Override
    public void run() {
        if(world == null) {
            cancel();
        }
        ActiveTime++;
    }


    public int getActiveTime() {
        return ActiveTime;
    }

    public World getWorld() {
        return world;
    }

    public WorldRunnable getRunnable() {
        return this;
    }

    private void launchInactiveCheck(){
        long inactiveTime = 20L*JustBoxed.getInstance().getConfig().getInt("inactivityUnload");
        new BukkitRunnable() {
            @Override
            public void run() {

                if(world != null) {
                    if(world.getPlayers().isEmpty()) {
//                        Bukkit.broadcast(Component.text("Inactive Check " + inactiveTime/20 + " second for " +world.getName()+": UNLOAD", NamedTextColor.RED));
                        Bukkit.unloadWorld(world, true);
                        JustBoxed.worldManager.remove(getRunnable());
                        cancel();
                    }
                    /*else{
                        Bukkit.broadcast(Component.text("Inactive Check " + inactiveTime/20 + "second for " +world.getName()+": NO UNLOAD", NamedTextColor.BLUE));
                    }*/
                }
            }
        }.runTaskTimer(JustBoxed.getInstance(), inactiveTime, inactiveTime);
    }
}
