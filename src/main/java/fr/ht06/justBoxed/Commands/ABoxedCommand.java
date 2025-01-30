package fr.ht06.justBoxed.Commands;

import fr.ht06.justBoxed.JustBoxed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ABoxedCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cUsage: /aboxed <args>");
            return true;
        }
        else{
            if (args[0].equalsIgnoreCase("worldinfo")) {
                if (JustBoxed.worldManager.getWorldsRunnables().isEmpty()) {
                    player.sendMessage("Empty");
                    return true;
                }
                JustBoxed.worldManager.getWorldsRunnables().forEach(worldRunnable -> {
                    player.sendMessage(worldRunnable.getWorld().getName()+ ": " + worldRunnable.getActiveTime()+ "s");
                });
            }
        }
        return true;
    }
}
