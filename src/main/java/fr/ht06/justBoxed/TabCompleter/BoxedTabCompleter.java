package fr.ht06.justBoxed.TabCompleter;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.JustBoxed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BoxedTabCompleter implements TabCompleter {

    Box box;
    BoxManager boxManager = JustBoxed.boxManager;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> players = JustBoxed.getInstance().getServer().getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .toList();

        Player player = (Player) sender;

        if (boxManager.hasBox(player.getUniqueId())) {
            box = boxManager.getBoxByPlayer(player.getUniqueId());
        }

        //No box
        else {
            if (args.length == 1) {
                return List.of("create", "join", "visit");
            }
            if (args.length == 2) {
                return players;
            }
            else {
                return List.of();
            }
        }

        //Owner
        if (boxManager.isOwner(box, player)) {
            if(args.length == 1) {
                return Stream.of("delete", "invite", "join", "spawn", "setowner", "kick", "team", "setspawn", "visit")
                        .sorted()
                        .collect(Collectors.toList());
            }
            else if(args.length == 2) {
                if(args[0].equalsIgnoreCase("invite")
                        || args[0].equalsIgnoreCase("join")
                        || args[0].equalsIgnoreCase("setowner")
                        || args[0].equalsIgnoreCase("visit")
                        || args[0].equalsIgnoreCase("kick")) {
                    return players;
                }
            }
        }

        //Member
        else{
            if(args.length == 1) {
                return Stream.of("spawn", "leave", "team", "visit").sorted().collect(Collectors.toList());
            }
            if(args.length == 2) {
                if(args[0].equalsIgnoreCase("visit")){
                    return players;
                }
            }
        }
        return List.of();
    }
}
