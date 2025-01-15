package fr.ht06.justBoxed.Events;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class PlayerListeners implements Listener {
    BoxManager boxManager = JustBoxed.manager;


    //revoke all player achievements (for test)
//    @EventHandler
//    public void onPlayerJoin(PlayerJoinEvent event) {
//        Player player = event.getPlayer();
//       AdvancementManager.grantAdvancement(player, "justboxed:joinadvancement");
//        AdvancementManager.revokeAllAdvancement(player);
//    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        //if the player didn't have a box, we can't load the world
        if (!boxManager.hasBox(player.getUniqueId())) {
            return;
        }

        Box box = boxManager.getBoxByPlayer(player.getUniqueId());

//        new WorldCreator(box.getWorldName()).createWorld();

    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        if (!boxManager.hasBox(player.getUniqueId())) {
            return;
        }

        Box box = boxManager.getBoxByPlayer(player.getUniqueId());

        UUID owner = box.getOwner();
        List<UUID> uuidList = box.getMembers();

        //if the player who disconnects is the owner, we verify every member
        if (player.getUniqueId().equals(owner)) {
            for (UUID uuid : uuidList) {
                if (box.isOnline(uuid)) {
                    return;
                }
            }
        }

        //if the player who disconnects is a member, we check the owner and every other player
        if (uuidList.contains(player.getUniqueId())) {
            uuidList.remove(player.getUniqueId());
            if (box.isOnline(box.getOwner())) {
                return;
            }
            for (UUID uuid : uuidList) {
                if (box.isOnline(uuid)) {
                    return;
                }
            }
        }

        //Wait for the player to be fully disconnected
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if(!player.isOnline()){
//                    Bukkit.getWorld(box.getWorldName()).save();
//                    Bukkit.unloadWorld(box.getWorldName(), false);
//                    cancel();
//                }
//            }
//        }.runTaskTimer(JustBoxed.getInstance(), 0,20);


    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();

        //if this is not a 'recipe advancement'
        if(!event.getAdvancement().getKey().getKey().contains("recipes")) {

            //If the player have a box
            if (boxManager.hasBox(player.getUniqueId())) {

                //get the box and add the size to the worldBorder
                Box box = boxManager.getBoxByPlayer(player.getUniqueId());
                if (!box.getCompletedAdvancements().contains(event.getAdvancement().getKey())) {
                    box.setSize(box.getSize() + JustBoxed.getInstance().getConfig().getInt("borderExpand"));
                    box.addDoneAdvancement(event.getAdvancement().getKey());

//                player.sendMessage(String.valueOf(event.getAdvancement()));
//                player.sendMessage(String.valueOf(event.getAdvancement().getKey()));
//                player.sendMessage(event.getAdvancement().getKey().getKey());

                    //Send a message to the player
                    box.broadcastMessage(Component.text("Advancement complete: ").append(event.getAdvancement().displayName()));
                }
            }
        }
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        //We didn't care about the main world
        if (player.getWorld().getName().equals("world") || player.getWorld().getName().equals("world_nether") || player.getWorld().getName().equals("world_the_end")) {
            return;
        }


        if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) {
            return;
        }

        boolean isFood = player.getInventory().getItemInMainHand().getDataTypes().stream().anyMatch(data -> data.getKey().getKey().equals("food"));

        //need to do something, you can only eat an item while looking at the ground, he can't eat

        //if the player didn't have a box, he can't interact
        if(!boxManager.hasBox(player.getUniqueId())) {
            player.sendMessage("§cVous ne pouvez pas interagir avec des block qui ne sont pas dans votre box.");
            event.setCancelled(true);
            return;
        }

        Box box = boxManager.getBoxByPlayer(player.getUniqueId());

        //if the player is not on his box, he can't interact
        if (!player.getWorld().getName().equals(box.getWorldName())){
            player.sendMessage("§cVous ne pouvez pas interagir avec des block qui ne sont pas dans votre box.");
            event.setCancelled(true);
            return;
        }
    }


    @EventHandler
    public void onHit(EntityDamageEvent event) {
        //event.getEntity()  -> l'entité tapé
        //event.getDamageSource().getCausingEntity() ->  l'attaquant

        //if this isnot a player, we don't care
        if (!(event.getDamageSource().getCausingEntity() instanceof Player)){
            return;
        }

        Player player = (Player) event.getDamageSource().getCausingEntity();

        //We didn't care about the main world
        if (player.getWorld().getName().equals("world") || player.getWorld().getName().equals("world_nether") || player.getWorld().getName().equals("world_the_end")) {
            return;
        }

        //if the player didn't have a box, he can't interact
        if(!boxManager.hasBox(player.getUniqueId())) {
            player.sendMessage("§cYou can't attack in other players box");
            event.setCancelled(true);
            return;
        }

        Box box = boxManager.getBoxByPlayer(player.getUniqueId());

        //if the player is not on his box, he can't interact
        if (!player.getWorld().getName().equals(box.getWorldName())){
            player.sendMessage("§cYou can't attack on other players box");
            event.setCancelled(true);
            return;
        }
    }
}
