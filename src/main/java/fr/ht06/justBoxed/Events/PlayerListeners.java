package fr.ht06.justBoxed.Events;

import fr.ht06.justBoxed.AdvancementManager;
import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.WorldBorderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class PlayerListeners implements Listener {
    BoxManager boxManager = JustBoxed.boxManager;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!boxManager.hasBox(player.getUniqueId())) {
            AdvancementManager.revokeAllAdvancement(player);
            return;
        }

        Box box = boxManager.getBoxByPlayer(player.getUniqueId());

        //add advancement if his teammate does some when the payer was offline
        List<Advancement> doneOffline = new ArrayList<>();

        //Check all advancement, if the player did not have it, award it
        for (NamespacedKey namespacedKey: box.getCompletedAdvancements()){
            if (!player.getAdvancementProgress(Bukkit.getAdvancement(namespacedKey)).isDone()) {
                doneOffline.add(Bukkit.getAdvancement(namespacedKey));
                AdvancementManager.grantAdvancement(player, namespacedKey);
            }

        }

        if (doneOffline.isEmpty()) {
            return;
        }

        //Wait else the message sends before the player joins
        new BukkitRunnable() {
            @Override
            public void run() {

                TextComponent.Builder doneOfflineMessage = Component.text("While you were offline, members of your box did some advancement: ", TextColor.color(0x1E8449)).toBuilder();

                doneOffline.forEach(advancement -> doneOfflineMessage.append(advancement.displayName().asComponent()).appendSpace());

                //send what advancements were made while the player was offline
                player.sendMessage(doneOfflineMessage);
            }
        }.runTaskLater(JustBoxed.getInstance(), 20L);

    }


    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();

        //if this is a 'recipe advancement', cancel cause this is not a reel advancement
        if(event.getAdvancement().getKey().getKey().contains("recipes")) return;

        //If the player have a box
        if (boxManager.hasBox(player.getUniqueId())) {

            //get the box and add the size to the worldBorder
            Box box = boxManager.getBoxByPlayer(player.getUniqueId());

            if (!box.getCompletedAdvancements().contains(event.getAdvancement().getKey())) {
                box.setSize(box.getSize() + (JustBoxed.getInstance().getConfig().getInt("borderExpand")*2));
                box.addDoneAdvancement(event.getAdvancement().getKey());

                //redo the wb
                if (Bukkit.getWorld(box.getWorldName()) != null) {
                    WorldBorderManager.setWorldBorder(box, 2);
                }

                //Send a message every member of the box
                box.broadcastMessage(Component.text("Advancement complete: ").append(event.getAdvancement().displayName()));
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

        //to avoid error
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) {
            return;
        }

        //op player can do whatever they want
        if (player.isOp()){
            return;
        }

//        boolean isFood = player.getInventory().getItemInMainHand().getDataTypes().stream().anyMatch(data -> data.getKey().getKey().equals("food"));

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
        }
    }


    @EventHandler
    public void onHit(EntityDamageEvent event) {
        //event.getEntity()  -> victim
        //event.getDamageSource().getCausingEntity() ->  attacker

        //if the attacker is not a player, we don't care
        if (!(event.getDamageSource().getCausingEntity() instanceof Player)){
            return;
        }

        Player player = (Player) event.getDamageSource().getCausingEntity();

        //op player can do whatever they want
        if (player.isOp()){
            return;
        }

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
        }
    }
}
