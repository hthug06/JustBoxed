package fr.ht06.justBoxed;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class BoxedTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> players = JustBoxed.getInstance().getServer().getOnlinePlayers().stream().map(Player::getName).toList();
        if(args.length == 1) {
            return List.of("create", "delete", "invite", "join", "tp");
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("join")) {
                return players;
            }
        }
        return List.of();
    }
}
