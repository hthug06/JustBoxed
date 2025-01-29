package fr.ht06.justBoxed.Commands;

import fr.ht06.justBoxed.AdvancementManager;
import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.Box.CreateBox;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.InviteRunnable;
import fr.ht06.justBoxed.WorldBorderManager;
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

        //Console or commands block can't use this command
        if (!(sender instanceof Player)){
            sender.sendMessage("You must be a player to use this command.");
            return true;
        }

        Player player = (Player) sender;

        //No args
        if (args.length == 0){
            //Need to make a /box help
            player.sendMessage("§cUsage: /boxed create | delete | invite | join | tp");
        }

        // Args
        else {

            //Create
            if (args[0].equalsIgnoreCase("create")) {

                //if he has a box, the player can't create another one
                if (manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou already have a box");
                    return true;
                }

                //if he already creating a box, he can't create another one
                if (JustBoxed.creatingWorld.contains(player.getUniqueId())){
                    player.sendMessage("§cYou're already creating a box");
                    return true;
                }

                //if he don't create a box, add to this list for verification in case he wan't to create another one
                JustBoxed.creatingWorld.add(player.getUniqueId());

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

                //can't delete a box as a member, only leave it
                if (manager.isMember(box, player)){
                    player.sendMessage("§cOnly the owner of the box can delete it.");
                    return true;
                }

                //Get the world Name
                worldName = box.getWorldName();

                //Delete it
                deleteWorld(worldName);

                //Send a message to the player
                player.sendMessage(Component.text("You successfully deleted your box", TextColor.color(0xa93226)));

                //send a message to every player
                box.broadcastMessage(Component.text("The owner delete the box", TextColor.color(0xa93226)));

                //remove the box from the manager
                manager.removeBox(box);

            }

            //invite a player in your box
            else if(args[0].equalsIgnoreCase("invite")){

                //if the args are not equal to 2, send an error message
                if (args.length != 2){
                    player.sendMessage("§c/box invite <player>");
                    return true;
                }

                //get the invited player
                Player playerInvited = Bukkit.getPlayerExact((args[1]));

                //Verification if the player exist and if he is online
                if (playerInvited == null || !playerInvited.isOnline()){
                    player.sendMessage("§cThis player is offline or didn't exist");
                    return true;
                }

                //if it's the same player, error message
                if (playerInvited.getUniqueId() == player.getUniqueId()){
                    player.sendMessage("§cYou can't invite yourself.");
                    return true;
                }

                //Verification if the player has a box (maybe deprecated later)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou don't have a box");
                    return true;
                }

                //get the box
                Box box = manager.getBoxByPlayer(player.getUniqueId());

                //only owner can invite another player
                if (!manager.isOwner(box, player)){
                    player.sendMessage("§cYou are not the owner of this box, contact him/her to invite this player.");
                    return true;
                }

                //size limit in config.yml
                if (box.getMembers().size() >= JustBoxed.getInstance().getConfig().getInt("memberLimit")){
                    player.sendMessage("You already reach the maximum number of members.");
                    return true;
                }

                //if he is already in the box, error
                if (box.getMembers().contains(playerInvited.getUniqueId())){
                    player.sendMessage("§cThis player is already part of your box");
                    return true;
                }

                //if all test are passed, create an invitation, inform the player the invitation is send and send an invitation msg to the invited player
                box.addInvitation(new InviteRunnable(box, playerInvited.getUniqueId()));
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

                //if the args are not equal to 2, error
                if (args.length != 2){
                    player.sendMessage("§c/box join <player>");
                    return true;
                }

                //player can't join another player if he already has a box
                if (manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou already have a box, leave him to join another box");
                    return true;
                }

                //if he's already creating a box, he can't join another one
                if (JustBoxed.creatingWorld.contains(player.getUniqueId())){
                    player.sendMessage("§cYou're already creating a box");
                    return true;
                }

                //OfflinePlayer because you can accept the invitation even if the inviter is offline
                OfflinePlayer playerWhoInvite = Bukkit.getOfflinePlayer(args[1]);

                //if the player you try to join doesn't have a box
                if (!manager.hasBox(playerWhoInvite.getUniqueId())){
                    player.sendMessage("§cThis player doesn't have a box");
                    return true;
                }

                //get the player you try to join box's
                Box box = manager.getBoxByPlayer(playerWhoInvite.getUniqueId());

                //Checks if he invites you
                if (box.isInvited(player.getUniqueId())){

                    //if yes, add you to the box, delete the invitation and send a message
                    box.addMember(player.getUniqueId());
                    box.removeInvitation(player.getUniqueId());
                    box.broadcastMessage(Component.text(player.getName() + " has joined the box !", NamedTextColor.GREEN));

                    //remove then add all the advancement*
                    AdvancementManager.revokeAllAdvancement(player);
                    box.getCompletedAdvancements().forEach(adv -> AdvancementManager.grantAdvancement(player, adv));

                    //this is for when the plugin get loaded (when a reload or a restart occur)
                    if (box.getSpawnForJoin(player)!= null){
                        player.teleport(box.getSpawnForJoin(player));
                    }

                }

                //if he didn't invite you, send an error message
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

                //get the box
                Box box = manager.getBoxByPlayer(player.getUniqueId());

                //Only player can leave a box, owner need to delete it or to set another owner to leave it
                if (manager.isOwner(box, player)){
                    player.sendMessage("§cYou can't leave your own box, transfer the box to someone else or delete it (/box delete)");
                    return true;
                }

                //For revoke all advancement
                AdvancementManager.revokeAllAdvancement(player);

                //Remove it from the box
                box.removeMember(player.getUniqueId());
                box.broadcastMessage(Component.text(player.getName() + " has left the box !", NamedTextColor.GOLD));
                player.sendMessage("§aYou have successfully left the box !");
            }

            else if (args[0].equalsIgnoreCase("setOwner")) {

                //if the args are not equal to 2, error
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

                //if he already is the owner, error
                if (!manager.isOwner(box, player)){
                    player.sendMessage("§cYou are not the owner of this box, only the owner can do this command");
                    return true;
                }

                //get the future owner with getOfflinePlayer, so you can set a member of the box owner even if he is offline
                OfflinePlayer futureOwner = Bukkit.getOfflinePlayer((args[1]));

                //already the owner = error
                if (futureOwner.getUniqueId().equals(player.getUniqueId())){
                    player.sendMessage("§cYou are already the owner of this box");
                    return true;
                }

                //Not in the box = error
                if (futureOwner == null || !box.getMembers().contains(futureOwner.getUniqueId())){
                    player.sendMessage("§cThis player is not in your box");
                    return true;
                }

                box.addMember(box.getOwner());
                box.removeMember(futureOwner.getUniqueId());
                box.setOwner(futureOwner.getUniqueId());

                //send to the ancien owner
                player.sendMessage("§6"+futureOwner.getName() + " is now the owner of the box !");

                //send him the message only if he is online
                if (futureOwner.isOnline()){
                    Player future = Bukkit.getPlayer(futureOwner.getUniqueId());
                    future.sendMessage("§6You are now the owner of the box !");
                }

            }

            else if (args[0].equalsIgnoreCase("kick")) {

                //if the args are not equal to 2 error
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

                OfflinePlayer playerToKick = Bukkit.getOfflinePlayer(args[1]);

                //can't kick himself
                if (playerToKick.getUniqueId().equals(player.getUniqueId())){
                    player.sendMessage("§cYou can't kick yourself");
                }

                //Check if the player is in the box
                if (playerToKick == null || !box.getMembers().contains(playerToKick.getUniqueId())){
                    player.sendMessage("§cThis player is not in your box");
                    return true;
                }

                //remove the member, all of his advancement and notify all member
                box.removeMember(playerToKick.getUniqueId());
                box.broadcastMessage(Component.text(playerToKick.getName() + " was kicked from the box !", NamedTextColor.GOLD));
                if (playerToKick.isOnline()){
                    //need to work on this one, because this won't work if the player is offline
                    AdvancementManager.revokeAllAdvancement(playerToKick.getPlayer());
                }


                //Verification if the player exists, and send a message if he is online
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

                //get the boxx
                Box box = manager.getBoxByPlayer(player.getUniqueId());
                player.sendMessage(Component.text("--- " + box.getName() + " ---"));

                //owner line
                if (box.isOnline(box.getOwner())){
                    player.sendMessage("Owner: "+ Bukkit.getOfflinePlayer(box.getOwner()).getName() + " §a•");
                }
                else {
                    player.sendMessage("Owner: "+ Bukkit.getOfflinePlayer(box.getOwner()).getName() + " §c•");
                }

                StringBuilder st = new StringBuilder();

                //member line(s)
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

                    //if the world is null, load it (async for no lag)
                    if (Bukkit.getWorld(box.getWorldName()) == null){

                        new BukkitRunnable() {

                            Box box = manager.getBoxByPlayer(player.getUniqueId());

                            @Override
                            public void run() {
                                try {
                                    player.sendActionBar(Component.text("Loading your world...", NamedTextColor.GOLD));
                                    new WorldCreator(box.getWorldName()).createWorld();
                                }catch (IllegalStateException ignored){}
                                cancel();
                            }
                        }.runTaskAsynchronously(JustBoxed.getInstance());

                        new BukkitRunnable() {

                            Box box = manager.getBoxByPlayer(player.getUniqueId());

                            @Override
                            public void run() {
                                //wait for the world to be loaded to tp the player
                                if (Bukkit.getWorld(box.getWorldName()) != null){
                                    Location loc = new Location(Bukkit.getWorld(box.getWorldName()),
                                            box.getSpawn().getX(),
                                            box.getSpawn().getY(),
                                            box.getSpawn().getZ());

                                    //set the wb
                                    WorldBorderManager.setWorldBorder(box);

                                    //and finally tp the player
                                    player.teleport(loc);
                                    cancel();
                                }
                                else {
                                    player.sendActionBar(Component.text("Loading your world...", NamedTextColor.GOLD));
                                }
                            }
                        }.runTaskTimer(JustBoxed.getInstance(), 20, 20);

                    }
                    //if the world is already loaded, set the wb and tp the player
                    else {
                        box = manager.getBoxByPlayer(player.getUniqueId());
                        WorldBorderManager.setWorldBorder(box);
                        Location loc = new Location(Bukkit.getWorld(box.getWorldName()),
                                box.getSpawn().getX(),
                                box.getSpawn().getY(),
                                box.getSpawn().getZ());
                        player.teleport(loc);
                    }
                }
                else {
                    player.sendMessage("§cYou don't have a box");
                }
            }

            else if (args[0].equalsIgnoreCase("setspawn")){
                //Verification if the player has a box (maybe deprecated later)
                if (!manager.hasBox(player.getUniqueId())){
                    player.sendMessage("§cYou don't have a box");
                    return true;
                }

                Box box = manager.getBoxByPlayer(player.getUniqueId());

                //only the owner can change
                if (!manager.isOwner(box, player)){
                    player.sendMessage("§cYou are not the owner of this box, only the owner can do this command");
                    return true;
                }

                //If the block under is Air
                if (!Bukkit.getWorld(box.getWorldName()).getBlockAt(player.getLocation().clone().add(0, -1, 0)).isSolid()){
                    player.sendMessage("§cThe spawn need to be on the ground to be changed!");
                    return true;
                }

                //Change the spawn and notify
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

                //if the desired player didn't have a box or didn't exist error
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
        Bukkit.getServer().unloadWorld(w, false);

        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


