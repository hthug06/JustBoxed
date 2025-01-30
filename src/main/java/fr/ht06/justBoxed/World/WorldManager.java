package fr.ht06.justBoxed.World;

import fr.ht06.justBoxed.Runnable.WorldRunnable;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class WorldManager {

    List<WorldRunnable> worldsRunnables = new ArrayList<>();

    public WorldManager() {}
    
    public void add(WorldRunnable worldRunnable) {
        worldsRunnables.add(worldRunnable);
    }

    public void remove(WorldRunnable worldRunnable) {
        worldsRunnables.remove(worldRunnable);
    }

    public WorldRunnable get(World world) {
        for (WorldRunnable worldRunnable : worldsRunnables) {
            if (worldRunnable.getWorld().equals(world)) {
                return worldRunnable;
            }
        }
        return null;
    }

    public List<WorldRunnable> getWorldsRunnables() {
        return worldsRunnables;
    }
}
