package fr.ht06.justBoxed;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class AdvancementManager {

    public static void grantAdvancement(Player player, String advancement){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + player.getName() +" only " + advancement);
    }

    public static void revokeAdvancement(Player player, String advancement){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "advancement revoke " + player.getName() +" only " + advancement);
    }

    public static void revokeAllAdvancement(Player player){
        Bukkit.getWorld(player.getWorld().getName()).setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
        Bukkit.advancementIterator().forEachRemaining(advancement -> {
            if (player.getAdvancementProgress(advancement).isDone()) {
                revokeAdvancement(player, advancement.getKey().toString());
            }
        });
        Bukkit.getWorld(player.getWorld().getName()).setGameRule(GameRule.SEND_COMMAND_FEEDBACK, true);
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
