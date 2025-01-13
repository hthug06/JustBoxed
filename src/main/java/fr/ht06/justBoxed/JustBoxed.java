package fr.ht06.justBoxed;


import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.Commands.BoxedCommand;
import fr.ht06.justBoxed.Events.PlayerListeners;
import fr.ht06.justBoxed.TabCompleter.BoxedTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class JustBoxed extends JavaPlugin {

    public static BoxManager manager = new BoxManager();

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this, 24371);

        if (getConfig().getBoolean("showAllAdvancements")) {
            Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false));
        }

        saveDefaultConfig();
        //Commands
        getCommand("boxed").setExecutor(new BoxedCommand());
        getCommand("boxreload").setExecutor(this);

        //TabCompleter
        getCommand("boxed").setTabCompleter(new BoxedTabCompleter());

        //Events
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("boxreload")) {
            sender.sendMessage("§cConfig reloaded");
            reloadConfig();

        }
        return true;
    }

    public static JustBoxed getInstance() {
        return getPlugin(JustBoxed.class);
    }

}
