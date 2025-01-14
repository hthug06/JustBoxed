package fr.ht06.justBoxed;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class AdvancementManager {

    public static void grantAdvancement(Player player, NamespacedKey advancement){
        if (JustBoxed.getInstance().getConfig().getBoolean("showAllAdvancements")) {
            JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);});
        }

        else{
            JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);});
        }

        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);});
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);});

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + player.getName() +" only " + advancement);

        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, true);});
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);});

    }

    public static void revokeAdvancement(Player player, NamespacedKey advancement){
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);});
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);});

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + player.getName() +" only " + advancement);

        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, true);});
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);});
    }

    public static void revokeAllAdvancement(Player player){
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, false);});
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);});

        Bukkit.advancementIterator().forEachRemaining(advancement -> {
            if (player.getAdvancementProgress(advancement).isDone()) {
                revokeAdvancement(player, advancement.getKey());
            }
        });

        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.LOG_ADMIN_COMMANDS, true);});
        JustBoxed.getInstance().getServer().getWorlds().forEach(world -> {world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);});
    }

    public static Integer getTotalAdvancement(Player player){
        AtomicInteger total = new AtomicInteger(0);
        Bukkit.advancementIterator().forEachRemaining(advancement -> {
            if (player.getAdvancementProgress(advancement).isDone() && !advancement.getKey().toString().contains("recipe")) {
                total.addAndGet(1);
            }
        });
        return total.get();
    }

    public static Boolean hasAdvancement(Player player, String advancement){
        if (Bukkit.getAdvancement(new NamespacedKey("minecraft", advancement)) != null) {
            return (player.getAdvancementProgress(Bukkit.getAdvancement(new NamespacedKey("minecraft", advancement))).isDone());
        }
        else return false;
    }

}
