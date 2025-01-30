package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.WorldRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BiomeSearchResult;

import java.util.List;

public class CreateBox {

    //Create the world -> search a good biome (biome with wood) -> search a log -> create the box
    MiniMessage miniMessage = MiniMessage.miniMessage();

    boolean finished = true;
    boolean needBiome = true;
    boolean needTreeCoordinates = true;
    Boolean failed = false;
    boolean inSearchOfTree = false;
    boolean biomeSearchFinished = false;
    int tries = 0;
    BiomeSearchResult biomeSearchResult;
    Location locTree = null;
    List<Material> woodList = List.of(Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.DARK_OAK_LOG, Material.CHERRY_LOG);

    public CreateBox(Player player, String worldName, Long seed) {
        createWorldAsync(worldName, seed, player);
    }

    public void createWorldAsync(String worldName, long seed, Player player) {
        //try catch because we get an error because we create it async
        player.sendActionBar(Component.text("Creating world", NamedTextColor.RED));
        new BukkitRunnable(){
            @Override
            public void run() {
                try{
                    new WorldCreator(worldName)
                            .seed(seed)
                            .generateStructures(true)
                            .type(WorldType.NORMAL)
                            .createWorld();
                    cancel();
                }catch (IllegalStateException ignored){}
            }
        }.runTaskAsynchronously(JustBoxed.getInstance());

        new BukkitRunnable(){
            @Override
            public void run() {

                //wait for the world to be created
                if(Bukkit.getWorld(worldName) != null) {
                    player.sendActionBar(Component.text("World Created !", NamedTextColor.GREEN));

                    //create a new location to start the search for a good biome
                    Location loc = new Location(Bukkit.getWorld(worldName), tries * 20000, 70, tries * 20000);

                    //start searching a biome (or restart everything if something fail
                    if (needBiome || failed) {
                        SearchBiome(Bukkit.getWorld(worldName), loc, player);
                        needBiome = false;
                        if (failed) {
                            needTreeCoordinates = true;
                            inSearchOfTree = false;
                        }
                        failed = false;
                        return;
                    }

                    player.sendActionBar(Component.text("Biome search started", NamedTextColor.GREEN));

                    //If the biome is still in search
                    if (!biomeSearchFinished) {
//                        player.sendMessage("Biome Search not Finished");
                        return;
                    }

                    player.sendActionBar(Component.text("Biome search in progress...", NamedTextColor.GOLD));

                    //We know biome search is now finished

                    //if the search didn't find any good biome, restart the process
                    if (biomeSearchResult == null) {
                        failed = true;
                        tries++;
                        return;
                    }

                    player.sendActionBar(Component.text("Biome find!", NamedTextColor.GREEN));

//                    player.sendMessage("Biome Location: " + biomeSearchResult.getLocation().getBlockX() + " " + biomeSearchResult.getLocation().getBlockY() + " " + biomeSearchResult.getLocation().getBlockZ());
//                    player.sendMessage("Biome Name: " + biomeSearchResult.getBiome().getKey().getKey());

                    //Now we have the biome, we can start searching a tree
                    if (needTreeCoordinates) {
                        Bukkit.getScheduler().runTaskAsynchronously(JustBoxed.getInstance(), () -> {
                            player.sendActionBar(Component.text("Start searching a tree", NamedTextColor.GREEN));
                            locTree = findTree(biomeSearchResult.getLocation(), worldName, player);
                        });
                        needTreeCoordinates = false;
                        return;
                    }


                    //wait while the tree is not found
                    if (inSearchOfTree) {
                        player.sendActionBar(Component.text("Tree searching in progress...", NamedTextColor.GOLD));
                        return;
                    }

                    //if we didn't find the tree, we restart
                    if (locTree == null) {
                        failed = true;
                        player.sendActionBar(Component.text("tree search failed, restarting the process...", NamedTextColor.RED));
                        tries++;
                        return;
                    }

                    //All test passed, creating the box

                    player.sendActionBar(Component.text("Everything is ok, creating the box...", NamedTextColor.GREEN));

//                    player.sendMessage("loctree " + locTree.getBlockX() + " " + locTree.getBlockY() + " " + locTree.getBlockZ());

                    //Creating the box spawn location
                    Location finalLoc = new Location(Bukkit.getWorld(worldName),
                            locTree.getBlockX(),
                            Bukkit.getWorld(worldName).getHighestBlockYAt(locTree.getBlockX(), locTree.getBlockZ())+1,
                            locTree.getBlockZ());

                    //creating the box
                    Box box = new Box(miniMessage.deserialize(player.getName() + " box"), player.getUniqueId(), finalLoc, worldName);
                    JustBoxed.boxManager.add(box);
                    JustBoxed.creatingWorld.remove(player.getUniqueId());

                    //notify the player
                    player.sendActionBar(Component.text("Box created!", NamedTextColor.DARK_GREEN));

                    player.sendMessage(Component.text("Box created, use /box spawn to teleport to ", NamedTextColor.DARK_GREEN)
                            .append(Component.text("(or click here)", NamedTextColor.GRAY)
                                    .clickEvent(ClickEvent.runCommand("/box spawn"))
                                    .hoverEvent(HoverEvent.showText(Component.text("Click here to teleport to your box", NamedTextColor.GRAY)))));

                    //add a world runnable
                    JustBoxed.worldManager.add(new WorldRunnable(Bukkit.getWorld(worldName)));

                    //Stop the runnable because everything is ok now
                    cancel();

                }
            }
        //every one sec
        }.runTaskTimer(JustBoxed.getInstance(), 0L, 20L);
    }

    public void SearchBiome(World world, Location location, Player player) {
        biomeSearchFinished = false;

        new BukkitRunnable(){
            @Override
            public void run() {

                //all the biome we search for
                biomeSearchResult = world.locateNearestBiome(location, 20000, Biome.BIRCH_FOREST, Biome.CHERRY_GROVE, Biome.DARK_FOREST, Biome.FLOWER_FOREST,
                        Biome.FOREST, Biome.JUNGLE, Biome.OLD_GROWTH_BIRCH_FOREST, Biome.OLD_GROWTH_SPRUCE_TAIGA,
                        Biome.OLD_GROWTH_SPRUCE_TAIGA/*, Biome.PALE_GARDEN, Biome.TAIGA*/, Biome.OLD_GROWTH_PINE_TAIGA);

                //if didn't find a biome in a 20000 block radius (yes, it's possible...), restart everything
                if (biomeSearchResult == null){
                    player.sendActionBar(Component.text("Biome Search Failed, restarting the process...", NamedTextColor.RED));
                    failed = true;
                    cancel();
                    return;
                }

                //else, this step is finished
                finished = true;
                biomeSearchFinished = true;
        //Async for less / no lag
            }
        }.runTaskAsynchronously(JustBoxed.getInstance());
    }

    public Location findTree(Location location, String worldname, Player player) {
        inSearchOfTree = true;
        World world = Bukkit.getWorld(worldname);
        //y+=4 = optimization cause a tree is at least 5 block (I think...)
        for (int y = 60; y < 100; y+=4) {
            for (int x = location.getBlockX()-25; x < location.getBlockX()+25; x++) {
                for (int z = location.getBlockZ()-25; z < location.getBlockZ()+25; z++) {

                    //if this is AIR, we pass (optimization)
                    if (!world.getBlockAt(x, y, z).getType().equals(Material.AIR)) {

                        //if the block is in the list, the search end
                        if (woodList.contains(world.getBlockAt(x, y, z).getType())) {
                            player.sendActionBar(Component.text("Tree found!", NamedTextColor.GREEN));
                            inSearchOfTree = false;
                            return new Location(world, x, y, z);
                        }
                    }

                }
            }
        }

        //if no log / tree found, restart the process
        inSearchOfTree = false;
        player.sendActionBar(Component.text("Tree not found... Restarting the process", NamedTextColor.RED));
        return null;
    }
}
