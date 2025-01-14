package fr.ht06.justBoxed.Events;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;

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
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        //We didn't care about the main world
        if (player.getWorld().getName().equals("world") || player.getWorld().getName().equals("world_nether") || player.getWorld().getName().equals("world_the_end")) {
            return;
        }

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
}
