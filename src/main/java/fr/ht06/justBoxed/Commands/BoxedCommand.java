package fr.ht06.justBoxed.Commands;

import fr.ht06.justBoxed.AdvancementManager;
import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.Box.CreateBox;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.InviteRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

        //No args
        if (args.length == 0){
            player.sendMessage("§cUsage: /boxed create | delete | invite | join | tp");
        }

        // Args
        else {

            //Create
            if (args[0].equalsIgnoreCase("create")) {
                if (manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou already have a box");
                    return true;
                }

                if (JustBoxed.creatingWorld.contains(player.getUniqueId())){
                    player.sendMessage("§cYou're already creating a box");
                    return true;
                }

                //Create or import the seed
                long seed;
                if (JustBoxed.getInstance().getConfig().get("baseSeed").getClass() == Integer.class || JustBoxed.getInstance().getConfig().get("baseSeed").getClass() == Long.class){
                    seed = JustBoxed.getInstance().getConfig().getLong("baseSeed");

                }
                else {
                    seed = rand.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
                }

                //Create world name
                int number = 1;
                StringBuilder worldName = new StringBuilder(player.getName() + "_box");
                List<String> dataList = Arrays.stream(JustBoxed.getInstance().getServer().getWorldContainer().list()).toList();

                if (dataList.contains(worldName.toString())){
                    worldName = new StringBuilder(player.getName()+ "_box" + "_" + number);
                }

                while (dataList.contains(worldName.toString())){
                    number++;
                    worldName = new StringBuilder(player.getName() + "_box" + "_" + number);

                }

                //Create the world and the box
                new CreateBox(player, worldName.toString(), seed);
            }

            //Delete the box
            else if(args[0].equalsIgnoreCase("delete")){

                //player don't have a box (he is the owner)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou do not have a box (/box create)");
                    return true;
                }

                //Player have 100% a box, so get the player's box
                Box box = manager.getBoxByPlayer(player.getUniqueId());

                if (manager.isMember(box, player)){
                    player.sendMessage("§cOnly the owner of the box can delete it.");
                    return true;
                }

                //Get the world Name
                worldName = box.getWorldName();

                //Delete it
                deleteWorld(worldName);

                //remove the box from the manager
                manager.removeBox(manager.getBoxByPlayer(player.getUniqueId()));

                //Send a message to the player
                player.sendMessage(Component.text("You successfully deleted your box", TextColor.color(0xa93226)));
                AdvancementManager.revokeAllAdvancement(player);

            }

            //invite a player in your box
            else if(args[0].equalsIgnoreCase("invite")){

                //if the args are not equal to 2
                if (args.length != 2){
                    player.sendMessage("§c/box invite <player>");
                    return true;
                }

                Player playerInvited = Bukkit.getPlayerExact((args[1]));

                //Verification if the player exist
                if (playerInvited == null || !playerInvited.isOnline()){
                    player.sendMessage("§cThis player is offline or didn't exist");
                    return true;
                }

                //if it's the same player
                if (playerInvited.getUniqueId() == player.getUniqueId()){
                    player.sendMessage("§cYou can't invite yourself.");
                    return true;
                }

                //Verification if the player has a box (maybe deprecated later)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou don't have a box");
                    return true;
                }

                Box box = manager.getBoxByPlayer(player.getUniqueId());
                box.addInvitation(new InviteRunnable(box, playerInvited.getUniqueId()));

                if (!manager.isOwner(box, player)){
                    player.sendMessage("§cYou are not the owner of this box, contact him/her to invite this player.");
                    return true;
                }

                if (box.getMembers().size() >= JustBoxed.getInstance().getConfig().getInt("memberLimit")){
                    player.sendMessage("You already reach the maximum number of members.");
                    return true;
                }

                if (box.getMembers().contains(playerInvited.getUniqueId())){
                    player.sendMessage("§cThis player is already part of your box");
                    return true;
                }

                player.sendMessage("§aInvitation send to " + playerInvited.getName());

                int timeRemaining = JustBoxed.getInstance().getConfig().getInt("inviteTime");
                Component msg = Component.text(player.getName() + " invite you to his box ", NamedTextColor.GREEN)
                                        .append(Component.text("click here to join him.")
                                                .clickEvent(ClickEvent.runCommand("/bx join " + player.getName()))
                                                .hoverEvent(HoverEvent.showText(Component.text("Click here to accept the invitation"))))
                        .append(Component.text(" (you have "+ timeRemaining +" seconds to accept the invitation)", NamedTextColor.GRAY)
                                .decorate(TextDecoration.ITALIC));

                playerInvited.sendMessage(msg);
            }

            //Join a box
            else if (args[0].equalsIgnoreCase("join")){

                //if the args are not equal to 2
                if (args.length != 2){
                    player.sendMessage("§c/box join <player>");
                    return true;
                }

                if (manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou already have a box, leave him to join another box");
                    return true;
                }

                //OfflinePlayer because you can accept the invitation even if the inviter is offline
                OfflinePlayer playerWhoInvite = Bukkit.getOfflinePlayer(args[1]);

                //if the player you try to join doesn't have a box
                if (!manager.hasBox(playerWhoInvite.getUniqueId())){
                    player.sendMessage("§cThis player doesn't have a box");
                    return true;
                }

                //get the player you try to join box
                Box box = manager.getBoxByPlayer(playerWhoInvite.getUniqueId());

                //Checks if he invites you
                if (box.isInvited(player.getUniqueId())){

                    //if yes, add you to the box, delete the invitation and send a message
                    box.addMember(player.getUniqueId());
                    box.removeInvitation(player.getUniqueId());
                    box.broadcastMessage(Component.text(player.getName() + " has joined the box !", NamedTextColor.GREEN));

                    box.getCompletedAdvancements().forEach(adv -> AdvancementManager.grantAdvancement(player, adv));

                    player.teleport(box.getSpawn());
                }

                //if he didn't invite you, send a message
                else{
                    player.sendMessage("§cYou didn't get an invitation from this player");
                }
            }

            else if (args[0].equalsIgnoreCase("leave")) {

                //Verification if the player has a box (maybe deprecated later)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou don't have a box");
                    return true;
                }

                Box box = manager.getBoxByPlayer(player.getUniqueId());

                if (manager.isOwner(box, player)){
                    player.sendMessage("§cYou can't leave your own box, transfer the box to someone else or delete it (/box delete)");
                    return true;
                }

                box.removeMember(player.getUniqueId());
                box.broadcastMessage(Component.text(player.getName() + " has left the box !", NamedTextColor.GOLD));
                player.sendMessage("§aYou have successfully left the box !");
                AdvancementManager.revokeAllAdvancement(player);
            }

            else if (args[0].equalsIgnoreCase("setOwner")) {

                //if the args are not equal to 2
                if (args.length != 2){
                    player.sendMessage("§c/box setowner <player>");
                    return true;
                }

                //Verification if the player has a box (maybe deprecated later)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou don't have a box");
                    return true;
                }

                Box box = manager.getBoxByPlayer(player.getUniqueId());

                if (!manager.isOwner(box, player)){
                    player.sendMessage("§cYou are not the owner of this box, only the owner can do this command");
                    return true;
                }

                OfflinePlayer futureOwner = Bukkit.getPlayerExact(args[1]);

                if (futureOwner == null || !box.getMembers().contains(futureOwner.getUniqueId())){
                    player.sendMessage("§cThis player is not in your box");
                    return true;
                }

                box.addMember(box.getOwner());
                box.setOwner(futureOwner.getUniqueId());

                //send to the ancien owner
                player.sendMessage("§6"+futureOwner.getName() + " is now the owner of the box !");

                if (futureOwner.isOnline()){
                    Player future = Bukkit.getPlayer(futureOwner.getUniqueId());
                    future.sendMessage("§6You are now the owner of the box !");
                }

            }

            else if (args[0].equalsIgnoreCase("kick")) {

                //if the args are not equal to 2
                if (args.length != 2){
                    player.sendMessage("§c/box kick <player>");
                    return true;
                }

                //Verification if the player has a box (maybe deprecated later)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou don't have a box");
                    return true;
                }

                Box box = manager.getBoxByPlayer(player.getUniqueId());

                if (!manager.isOwner(box, player)){
                    player.sendMessage("§cYou are not the owner of this box, only the owner can do this command");
                    return true;
                }

                OfflinePlayer playerToKick = Bukkit.getPlayerExact(args[1]);

                //Check if the player is in the box
                if (playerToKick == null || !box.getMembers().contains(playerToKick.getUniqueId())){
                    player.sendMessage("§cThis player is not in your box");
                    return true;
                }

                box.removeMember(playerToKick.getUniqueId());
                box.broadcastMessage(Component.text(playerToKick.getName() + " was kicked from the box !", NamedTextColor.GOLD));
                if (playerToKick.isOnline()){
                    //need to work on this one, because this won't work if the player is offline
                    AdvancementManager.revokeAllAdvancement(playerToKick.getPlayer());
                }


                //Verification if the player exists
                if (playerToKick != null && playerToKick.isOnline()){
                    Player playerToKickPlayer = playerToKick.getPlayer();
                    playerToKickPlayer.sendMessage("§6You have been kicked from " +box.getName()+"!");
                }

            }

            else if(args[0].equalsIgnoreCase("team")) {
                //Verification if the player has a box (maybe deprecated later)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou don't have a box");
                    return true;
                }

                Box box = manager.getBoxByPlayer(player.getUniqueId());

                if (box.isOnline(box.getOwner())){
                    player.sendMessage("Owner: "+ Bukkit.getOfflinePlayer(box.getOwner()).getName() + " §a•");
                }
                else {
                    player.sendMessage("Owner: "+ Bukkit.getOfflinePlayer(box.getOwner()).getName() + " §c•");
                }

                StringBuilder st = new StringBuilder();

                box.getMembers().forEach(member -> {
                    if (box.isOnline(member)){
                        st.append("§r").append(Bukkit.getOfflinePlayer(member).getName()).append(" §a• ");
                    }
                    else {
                        st.append("§r").append(Bukkit.getOfflinePlayer(member).getName()).append(" §c• ");
                    }
                });
                if (!st.isEmpty()){
                    player.sendMessage("Member: " + st);
                }

            }

            //tp to the spawn of the box
            else if (args[0].equalsIgnoreCase("spawn")){
                if (manager.hasBox(player.getUniqueId())){
                    Box box = manager.getBoxByPlayer(player.getUniqueId());
                    player.teleport(box.getSpawn());
                }
                else {
                    player.sendMessage("§cYou do not have a box");
                }
            }

            else if (args[0].equalsIgnoreCase("setspawn")){
                //Verification if the player has a box (maybe deprecated later)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou don't have a box");
                    return true;
                }

                Box box = manager.getBoxByPlayer(player.getUniqueId());

                if (!manager.isOwner(box, player)){
                    player.sendMessage("§cYou are not the owner of this box, only the owner can do this command");
                    return true;
                }

                //If the block under is Air
                if (!Bukkit.getWorld(box.getWorldName()).getBlockAt(player.getLocation().clone().add(0, -1, 0)).isSolid()){
                    player.sendMessage("§cThe spawn need to be on the ground to be changed!");
                    return true;
                }


                box.setSpawn(player.getLocation());
                player.sendMessage("§aYou have change the spawn of the box");
            }

            else if(args[0].equalsIgnoreCase("visit")){

                //if the args are not equal to 2
                if (args.length != 2){
                    player.sendMessage("§c/box visit <player>");
                    return true;
                }

                OfflinePlayer playerToVisit = Bukkit.getOfflinePlayer(args[1]);

                if (!manager.hasBox(playerToVisit.getUniqueId())){
                    player.sendMessage("§cThis player doesn't have a box");
                    return true;
                }

                Box box = manager.getBoxByPlayer(playerToVisit.getUniqueId());

                player.teleport(box.getSpawn());
                player.sendMessage("§aTeleportation to " + box.getName());
                box.broadcastMessage(Component.text(player.getName()+ " is visiting your box"));

            }

            else{
                //To finish later with /box help
                sender.sendMessage("§c this command does not exist");
            }
        }

        return true;
    }

    public void deleteWorld(String worldName) {
        //Récup le monde
        World w = Bukkit.getWorld(worldName);

        //On téléporte le joueur dans le monde de base au 0 0
        for (Player player : w.getPlayers()) {
        player.teleport(new Location(Bukkit.getWorld("world"),
                0,
                Bukkit.getWorld("world").getHighestBlockYAt(0, 0)+1,
                0));
        }

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


