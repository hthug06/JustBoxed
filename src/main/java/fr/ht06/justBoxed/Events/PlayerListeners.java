package fr.ht06.justBoxed.Events;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import org.bukkit.GameRule;
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

        //Need to put this when the world create (this is not optimized here)
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {
            if(world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS).equals(true)) {
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            }});

        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {
            if(world.getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK).equals(true)) {
                world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);
            }});

        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {
            if(world.getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK).equals(true)) {
                world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
            }});

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
}
