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

        if(!event.getAdvancement().getKey().getKey().contains("recipes")) {
            if (boxManager.hasBox(player)) {
                Box box = boxManager.getBoxByPlayer(player.getUniqueId());
                box.setSize(box.getSize() + 2);

                player.sendMessage(Component.text("Achievement complete: ").append(event.getAdvancement().displayName()));
            }
        }
    }
}
