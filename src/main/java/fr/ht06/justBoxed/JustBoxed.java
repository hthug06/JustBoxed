package fr.ht06.justBoxed;


import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.Box.LoadBoxData;
import fr.ht06.justBoxed.Box.SaveBoxData;
import fr.ht06.justBoxed.Box.UnloadInactiveBox;
import fr.ht06.justBoxed.Commands.BoxedCommand;
import fr.ht06.justBoxed.Events.PlayerListeners;
import fr.ht06.justBoxed.TabCompleter.BoxedTabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JustBoxed extends JavaPlugin {

    public static BoxManager manager = new BoxManager();
    public static List<UUID> creatingWorld = new ArrayList<>();

    @Override
    public void onEnable() {
        //bstats
        Metrics metrics = new Metrics(this, 24371);

        //might change later on
        if (getConfig().getBoolean("showAllAdvancements")) {
            Bukkit.getWorld("world").setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);
        }
        else {
            Bukkit.getWorld("world").setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }

        //config.yml
        saveDefaultConfig();

        //Commands
        getCommand("boxed").setExecutor(new BoxedCommand());
        getCommand("boxreload").setExecutor(this);

        //TabCompleter
        getCommand("boxed").setTabCompleter(new BoxedTabCompleter());

        //Events
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);


        //data.yml
        LoadBoxData.load();
        try {
            FileUtils.deleteDirectory(new File(Bukkit.getServer().getPluginManager().getPlugin("JustBoxed").getDataFolder(), "data.yml"));
        } catch (IOException e) {
            getComponentLogger().info(Component.text("Can't save the data, please make sure that the data file is correct", TextColor.color(0xC70039) ));
        }

        //unload every inactive world every x second after 5 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                UnloadInactiveBox.unloadAll();
            }
        }.runTaskTimer(this, 300, JustBoxed.getInstance().getConfig().getInt("inactivityUnload")* 20L);

    }

    @Override
    public void onDisable() {

        //save everything in data.yml
        SaveBoxData.save();
    }

    //boxreload | bxrl command
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("boxreload")) {
            sender.sendMessage("§cConfig reloaded");
            reloadConfig();

        }
        return true;
    }

    //get the plugin instance
    public static JustBoxed getInstance() {
        return getPlugin(JustBoxed.class);
    }

}
