package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CreateBox {

    private final Random random = new Random();


    public CreateBox(Player player, String worldName, long seed) {

        //Add this in a list so we can verify if the player is already creating a box
        JustBoxed.creatingWorld.add(player.getUniqueId());

        //Create the world (async for 0 lags)
        createWorldAsync(worldName, seed);

        //Get the location of a log (async for no lag lol)
        final Location[] loc = new Location[1];
        new BukkitRunnable(){

            @Override
            public void run() {
                if(Bukkit.getWorld(worldName) != null) {

                    Bukkit.getScheduler().runTaskAsynchronously(JustBoxed.getInstance(), () -> {
                        cancel();
                        do {
                            loc[0] = searchNearestWood(worldName, player, 10);
                        } while (loc[0] == null);
                    });

                }
            }
        }.runTaskTimer(JustBoxed.getInstance(), 0, 20);

        //Wait while the location is found, and after create the box
        new BukkitRunnable(){
            @Override
            public void run() {
                player.sendActionBar(Component.text("Your box is being created...", NamedTextColor.GOLD));
                if (loc[0] != null) {
                    Location finalLoc = new Location(loc[0].getWorld(),
                            loc[0].getBlockX(),
                            Bukkit.getWorld(worldName).getHighestBlockYAt(loc[0].getBlockX(), loc[0].getBlockZ() + 1),
                            loc[0].getBlockZ());

                    if (Bukkit.getWorld(worldName).isChunkGenerated(finalLoc.getBlockX(), finalLoc.getBlockZ())){
                        Bukkit.getWorld(worldName).setChunkForceLoaded(finalLoc.getBlockX(), finalLoc.getBlockZ(), true);
                    };


                    Box box = new Box(player.getName()+"'s box", player.getUniqueId(), finalLoc, worldName);
                    JustBoxed.manager.add(box);
                    player.teleport(finalLoc);
                    JustBoxed.creatingWorld.remove(player.getUniqueId());
                    cancel();
                }
            }
        }.runTaskTimer(JustBoxed.getInstance(), 0, 20);

    }

    public void createWorldAsync(String worldName, long seed) {
        //try catch because we get an error because we create it async
        Bukkit.getScheduler().runTaskAsynchronously(JustBoxed.getInstance(), () -> {
            try{
                new WorldCreator(worldName)
                        .seed(seed)
                        .generateStructures(true)
                        .keepSpawnLoaded(TriState.FALSE)
                        .type(WorldType.NORMAL)
                        .createWorld();
            }catch (IllegalStateException ignored){}

        });
    }

    public Location searchNearestWood(String worldName, Player player, int recursion) {

        if (recursion == 0) {
            return null;
        }

        int xMax = JustBoxed.getInstance().getConfig().getInt("SearchRadius");
        int yMax = 110;
        int zMax = JustBoxed.getInstance().getConfig().getInt("SearchRadius");

        World w = Bukkit.getWorld(worldName);
        int startX = 0;
        int y = 60;
        int startZ = 0;

        List<Biome> wantedBiome = List.of(Biome.BIRCH_FOREST, Biome.CHERRY_GROVE, Biome.DARK_FOREST, Biome.FLOWER_FOREST,
                Biome.FOREST, Biome.GROVE, Biome.JUNGLE, Biome.OLD_GROWTH_BIRCH_FOREST, Biome.OLD_GROWTH_SPRUCE_TAIGA,
                Biome.OLD_GROWTH_SPRUCE_TAIGA, Biome.PALE_GARDEN, Biome.TAIGA);

        while (!wantedBiome.contains(w.getBiome(startX, y, startZ))){
            startX = random.nextInt(-50000, 50000);
            startZ = random.nextInt(-50000, 50000);
        }

        xMax = startX + xMax;
        zMax = startZ + zMax;

        List<Material> woodType = Arrays.asList(Material.ACACIA_LOG, Material.BIRCH_LOG, Material.CHERRY_LOG, Material.JUNGLE_LOG,
                Material.DARK_OAK_LOG, Material.OAK_LOG, Material.SPRUCE_LOG);

        //search the nearest log
        for (y = 60; y <= yMax; y++) {
            System.out.println("y: "+ y);
            for (int x = startX; x <= xMax; x++) {
                for (int z = startZ; z <= zMax; z++) {

                    if (!w.getBlockAt(x, y, z).getType().isAir() && woodType.contains(w.getBlockAt(x, y, z).getType())) {
                        player.sendActionBar(Component.text("Box found!", NamedTextColor.GOLD));
                        return new Location(Bukkit.getWorld(worldName), x, y, z);
                    }

                }
            }
        }
        return searchNearestWood(worldName, player, recursion-1);
    }
}
