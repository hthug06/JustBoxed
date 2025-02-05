package fr.ht06.justBoxed.Commands;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Inventory.BoxInfoInventory;
import fr.ht06.justBoxed.Inventory.MainInventory;
import fr.ht06.justBoxed.JustBoxed;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ABoxedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        Player player = (Player) sender;


        if (args.length == 0) {
            player.sendMessage("§cUsage: /abox <menu | info>");
            return true;
        }
        else{

            if (args[0].equalsIgnoreCase("menu")) {
                player.openInventory(new MainInventory().getInventory());
                return true;
            }

            //hidden commands for dev
            if (args[0].equalsIgnoreCase("worldinfo")) {
                if (JustBoxed.worldManager.getWorldsRunnables().isEmpty()) {
                    player.sendMessage("Empty");
                    player.sendMessage(String.valueOf(Bukkit.getWorlds()));
                    return true;
                }
                JustBoxed.worldManager.getWorldsRunnables()
                        .forEach(worldRunnable -> player.sendMessage(worldRunnable.getWorld().getName()+ ": " + worldRunnable.getActiveTime()+ "s"));
            }
            else if (args[0].equalsIgnoreCase("info")) {

                if(args.length < 2) {
                    player.sendMessage("/abox info <world | player | box>");
                    return true;
                }

                //get the box with the worldName
                if(args[1].equalsIgnoreCase("world")) {
                    if(args.length < 3) {
                        player.sendMessage("/abox info world <world name>");
                        return true;
                    }

                    Box box = JustBoxed.boxManager.getBoxByWorldName(args[2]);

                    if(box == null) {
                        player.sendMessage("§cA box with this world name don't exist");
                        return true;
                    }

                    player.openInventory(new BoxInfoInventory(box).getInventory());
                }

                if(args[1].equalsIgnoreCase("player")) {

                    if(args.length < 3) {
                        player.sendMessage("/abox info player <player name | player UUID>");
                        return true;
                    }

                    OfflinePlayer offlinePlayer = null;

                    //if lenght == 36, this can't be a minecraft name (longest mc name is 19 character)
                    //basic mc name are 16 character long MAX, but you can see exception here : https://laby.net/badge/ce1e18de-2b71-4f3e-9390-ff0e0267a829#
                    //Also a UUID is always 36 char long
                    if(args[2].length() == 36) {
                        try{
                            UUID uuid = UUID.fromString(args[2]);
                            offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        } catch (IllegalArgumentException exception){
                            player.sendMessage("§cThis UUID invalid");
                            return true;
                        }
                    }
                    //if this is a player name
                    else {
                        offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
                    }
                    Box box = JustBoxed.boxManager.getBoxByPlayer(offlinePlayer.getUniqueId());
                    if(box == null) {
                        player.sendMessage("§cThis player don't have a box");
                        return true;
                    }

                    player.openInventory(new BoxInfoInventory(box).getInventory());
                }

                if(args[1].equalsIgnoreCase("box")) {

                    if(args.length < 3) {
                        player.sendMessage("/abox info box <box name | box UUID>");
                        return true;
                    }

                    //Reminder : a UUID is always 36 char long
                    if(args[2].length() == 36) {
                        try{
                            UUID uuid = UUID.fromString(args[2]);
                            Box box = JustBoxed.boxManager.getBoxByUUID(uuid);
                            if(box == null) {
                                player.sendMessage("§cA box with this UUID don't exist");
                                return true;
                            }

                            player.openInventory(new BoxInfoInventory(box).getInventory());
                        } catch (IllegalArgumentException exception){
                            player.sendMessage("§cThis UUID invalid");
                            return true;
                        }
                    }
                    //if this is a box name
                    else {
                        StringBuilder name = new StringBuilder();
                        for (int i = 2; i < args.length; i++) {
                            name.append(args[i]);
                            name.append(" ");
                        }
                        name.deleteCharAt(name.length() - 1);

                        Box box = JustBoxed.boxManager.getBoxByPlainName(name.toString());
                        if(box == null) {
                            player.sendMessage("§cA box with this name don't exist");
                            return true;
                        }

                        player.openInventory(new BoxInfoInventory(box).getInventory());
                    }
                }
            }

            else{
                player.sendMessage("§cThis command don't exist");
            }
        }
        return true;
    }
}
