package fr.ht06.justBoxed.Events;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

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
                box.setSize(box.getSize() + 2);
                box.addDoneAdvancement(event.getAdvancement());

                //Send a message to the player
                player.sendMessage(Component.text("Achievement complete: ").append(event.getAdvancement().displayName()));
            }
        }
    }
}
