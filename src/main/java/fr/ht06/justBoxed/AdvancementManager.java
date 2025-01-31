package fr.ht06.justBoxed;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class AdvancementManager {

    public static void grantAdvancement(Player player, NamespacedKey advancement){
        Advancement adv = Bukkit.getAdvancement(advancement);
        for(String criteria: player.getAdvancementProgress(adv).getRemainingCriteria()){
            if (!player.getAdvancementProgress(adv).isDone()){
                player.getAdvancementProgress(adv).awardCriteria(criteria);
            }
        }
    }

    public static void revokeAdvancement(Player player, NamespacedKey advancement){
        Advancement adv = Bukkit.getAdvancement(advancement);
        if (player.getAdvancementProgress(adv).isDone()){
            adv.getCriteria().forEach(s -> player.getAdvancementProgress(adv).revokeCriteria(s));
        }
    }

    public static void revokeAllAdvancement(Player player){
        Bukkit.advancementIterator().forEachRemaining(advancement -> {

            AdvancementProgress progress = player.getAdvancementProgress(advancement);

            //if he has the advancement, revoke it
            if (progress.isDone()){
                for (String criteria : progress.getAwardedCriteria()){
                    progress.revokeCriteria(criteria);
                }
            }

        });
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
