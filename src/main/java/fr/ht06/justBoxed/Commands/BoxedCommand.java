package fr.ht06.justBoxed.Commands;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BoxedCommand implements CommandExecutor {

    Random rand = new Random();
    BoxManager manager = JustBoxed.manager;
    String worldName;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0){
            player.sendMessage("§cUsage: /boxed create | delete");
        }

        else {
            if (args[0].equalsIgnoreCase("create")) {
                if (manager.hasBox(player)){
                    player.sendMessage("§cYou already have a box");
                    return true;
                }

                long seed;
                if (JustBoxed.getInstance().getConfig().get("baseSeed").getClass() == Integer.class || JustBoxed.getInstance().getConfig().get("baseSeed").getClass() == Long.class){
                    seed = JustBoxed.getInstance().getConfig().getLong("baseSeed");

                }
                else {
                    seed = rand.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
                }

                //Nom du monde
                worldName = player.getName() + "_boxed";

                searchGoodWorld(worldName, seed, player);
            }

            else if(args[0].equalsIgnoreCase("delete")){

                if (!manager.hasBox(player)){
                    player.sendMessage("§cYou do not have a box (/box create)");
                    return true;
                }

                player.sendMessage(Component.text("You successfully deleted your box", TextColor.color(0xa93226)));

                //Récuperer le nom du monde
                worldName = player.getName() + "_boxed";// + JustBoxed.numberCreated.get(player);

                deleteWorld(player, worldName);

                manager.removeBox(manager.getBoxByPlayer(player.getUniqueId()));
            }
        }

        return true;
    }

    public void createWorld(String worldName, long seed) {
        //try catch car on a une erreur quand on le créer async
        try{
            new WorldCreator(worldName)
                    .seed(seed)
                    .generateStructures(true)
                    .keepSpawnLoaded(TriState.FALSE)
                    .type(WorldType.NORMAL)
                    .createWorld();
        }catch (IllegalStateException ignored){}
    }

    public void searchGoodWorld(String worldName, long seed, Player player) {
        //LE SEUL EN ASYNC
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendActionBar(Component.text("Searching a good world for you...", NamedTextColor.GREEN));
                try{
                    createWorld(worldName, seed);
                }
                catch (IllegalStateException ignored){}

                Bukkit.getScheduler().runTaskLater(JustBoxed.getInstance(), () -> {

                    Location loc = searchNearestWood(worldName, player);
                    if (loc != null){
                        loc = new Location(Bukkit.getWorld(worldName), loc.getX(),Bukkit.getWorld(worldName).getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()), loc.getZ());
//                        player.sendMessage(Component.text("You found a box"));
                        player.teleport(loc.clone().add(1, 1, 0));
                        Box box = new Box(worldName, player.getUniqueId(), loc.clone().add(1, 1, 0));
                        manager.add(box);
                    }
                    else {
                        player.sendMessage(Component.text("Box not found, try again (/box create)", TextColor.color(0x5d6d7e)).decorate(TextDecoration.ITALIC));
                        deleteWorldWhileSearching(worldName);
                    }

                    cancel();
                }, JustBoxed.getInstance().getConfig().getLong("tpDelay"));
            }
        }.runTaskAsynchronously(JustBoxed.getInstance());
    }

    public Location searchNearestWood(String worldName,Player player) {

        int xMax = JustBoxed.getInstance().getConfig().getInt("SearchRadius");
        int yMax = 110;
        int zMax = JustBoxed.getInstance().getConfig().getInt("SearchRadius");

        boolean found = false;
        World w = Bukkit.getWorld(worldName);
        Location loc = null;
        List<Material> woodType = Arrays.asList(Material.ACACIA_LOG, Material.BIRCH_LOG, Material.CHERRY_LOG, Material.JUNGLE_LOG,
                Material.DARK_OAK_LOG, Material.OAK_LOG, Material.SPRUCE_LOG, Material.MANGROVE_LOG);

        int y = 60; //Sea level
        while (!found && y != yMax){

            int x = -xMax;
            while (!found && x != xMax){

                int z = -zMax;
                while (!found && z != zMax){

                    if (!w.getBlockAt(x, y, z).getType().isAir() && woodType.contains(w.getBlockAt(x, y, z).getType())){
                        found = true;
                        loc = new Location(Bukkit.getWorld(worldName), x, y, z);
                        player.sendActionBar(Component.text("Box found!", NamedTextColor.GOLD));
                    }
                    z++;
                }
                x++;
            }
            y++;
        }
        return loc;
    }

    public void deleteWorld(Player player, String worldName) {
        //Récup le monde
        World w = Bukkit.getWorld(worldName);

        //On téléporte le joueur dans le monde de base au 0 0
        player.teleport(new Location(Bukkit.getWorld("world"),
                0,
                Bukkit.getWorld("world").getHighestBlockYAt(0, 0),
                0));

        //On delete le fichier du monde
        File folder = Bukkit.getWorld(w.getName()).getWorldFolder();

        //Unload le monde
        Bukkit.getServer().unloadWorld(w, true);

        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteWorldWhileSearching(String worldName) {
        //Récup le monde
        World w = Bukkit.getWorld(worldName);
        //player.sendMessage(String.valueOf(w));

        //On delete le fichier du monde
        File folder = Bukkit.getWorld(w.getName()).getWorldFolder();

        //Unload le monde
        Bukkit.getServer().unloadWorld(w, true);

        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


