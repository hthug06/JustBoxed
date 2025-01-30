package fr.ht06.justBoxed;


import fr.ht06.justBoxed.Box.BoxManager;
import fr.ht06.justBoxed.Box.LoadBoxData;
import fr.ht06.justBoxed.Box.SaveBoxData;
import fr.ht06.justBoxed.Commands.ABoxedCommand;
import fr.ht06.justBoxed.Inventory.AllBoxInfoInventory;
import fr.ht06.justBoxed.Inventory.BoxInfoInventory;
import fr.ht06.justBoxed.Inventory.MainInventory;
import fr.ht06.justBoxed.Runnable.WorldRunnable;
import fr.ht06.justBoxed.Commands.BoxedCommand;
import fr.ht06.justBoxed.Events.PlayerListeners;
import fr.ht06.justBoxed.TabCompleter.ABoxedTabCompleter;
import fr.ht06.justBoxed.TabCompleter.BoxedTabCompleter;
import fr.ht06.justBoxed.World.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JustBoxed extends JavaPlugin {

    public static BoxManager boxManager = new BoxManager();
    public static WorldManager worldManager = new WorldManager();
    public static List<UUID> creatingWorld = new ArrayList<>();

    @Override
    public void onEnable() {
        //bstats
        Metrics metrics = new Metrics(this, 24371);

        //might change later on
        Bukkit.getWorld("world").setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, getConfig().getBoolean("showAllAdvancements"));
        Bukkit.getWorld("world_nether").setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, getConfig().getBoolean("showAllAdvancements"));
        Bukkit.getWorld("world_the_end").setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, getConfig().getBoolean("showAllAdvancements"));

        //config.yml
        saveDefaultConfig();

        //Commands
        getCommand("boxed").setExecutor(new BoxedCommand());
        getCommand("aboxed").setExecutor(new ABoxedCommand());
        getCommand("boxreload").setExecutor(this);

        //TabCompleter
        getCommand("boxed").setTabCompleter(new BoxedTabCompleter());
        getCommand("aboxed").setTabCompleter(new ABoxedTabCompleter());

        //Events
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        loadInventoryEvents();


        //data.yml
        LoadBoxData.load();
        try {
            FileUtils.deleteDirectory(new File(Bukkit.getServer().getPluginManager().getPlugin("JustBoxed").getDataFolder(), "data.yml"));
        } catch (IOException e) {
            getComponentLogger().info(Component.text("Can't save the data, please make sure that the data file is correct", TextColor.color(0xC70039) ));
        }

        //get all the world we cannot /don't want to unload
        List<String> untouchableWorld = new ArrayList<>(List.of("world", "world_nether", "world_the_end"));
        untouchableWorld.addAll(JustBoxed.getInstance().getConfig().getStringList("alwaysLoadedWorld"));

        //create a new worldRunnable for every already existing world (box)
        Bukkit.getWorlds().forEach(world -> {
            //if not untouchable and online -> worldRunnable
            if (!untouchableWorld.contains(world.getName()) && world.getName().contains("_box")) {
                JustBoxed.worldManager.add(new WorldRunnable(Bukkit.getWorld(world.getName())));
            }
        });

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


    private void loadInventoryEvents() {
        getServer().getPluginManager().registerEvents(new MainInventory(), this);
        getServer().getPluginManager().registerEvents(new AllBoxInfoInventory(1), this);
        getServer().getPluginManager().registerEvents(new BoxInfoInventory(), this);
    }

}
