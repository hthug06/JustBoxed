package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.Config.DataConfig;
import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.*;

public class SaveBoxData {

    //I think I'll try to implement SQL one day... 😅

    public static void save() {

        if (new File(Bukkit.getServer().getPluginManager().getPlugin("JustBoxed").getDataFolder(), "data.yml").exists()){
            JustBoxed.getInstance().getComponentLogger().info("§cThe data already exist which is not normal, the data don't change for security");
            return;
        }

        //Init data.yml
        DataConfig.setup();
        DataConfig.get().options().copyDefaults(true);
        DataConfig.get().options().parseComments(true);
        DataConfig.get().addDefault("warning", "This is the data file, it's stock data while the server is offline");
        DataConfig.get().addDefault("another warning", "Don't touch anything in this file if you don't want to have corruption");
        DataConfig.get().addDefault("tip", "If you want to reset the plugin data, delete this file :)");
        DataConfig.get().createSection("Box");

        //save it
        DataConfig.save();

        //get the box section (they might be some more later on like player info or something)
        ConfigurationSection section = DataConfig.get().getConfigurationSection("Box");

        //verification
        if (section == null) {
            JustBoxed.getInstance()
                    .getComponentLogger()
                    .info(Component.text("Can't save the data, please make sure that the data file is correct", TextColor.color(0xC70039) ));
            return;
        }

        //Now save box y box
        for (Box box : JustBoxed.boxManager.getBoxes()){
            //create a section for the box (actually, this is the name, but maybe I gave them a UUID later
            section.createSection(box.getUuid().toString());

            //get the box section
            ConfigurationSection boxSection = DataConfig.get().getConfigurationSection("Box." + box.getUuid().toString());

            //save almost everything (don't save the invitation)
            if (boxSection == null) {
                JustBoxed.getInstance()
                        .getComponentLogger()
                        .info(Component.text("Can't save the data, please make sure that the data file is correct", TextColor.color(0xC70039) ));
                return;
            }

            //change in 1.0.3
            boxSection.setRichMessage("name", box.getName());
            boxSection.set("ownerUUID", box.getOwner().toString());

            if (!box.getMembers().isEmpty()) {
                List<String> members = new ArrayList<>(box.getMembers().stream().map(UUID::toString).toList());
                boxSection.set("membersUUID", members);
            }
            Location spawn = box.getSpawn();
            boxSection.set("x", spawn.getX());
            boxSection.set("y", spawn.getY());
            boxSection.set("z", spawn.getZ());
            boxSection.set("worldName", box.getWorldName());
            boxSection.set("boxSize", box.getSize());


            if (!box.getCompletedAdvancements().isEmpty()) {
                List<String> nameSpacedKeys = new ArrayList<>();
                //Put in a map, then in the config
                for (NamespacedKey key : box.getCompletedAdvancements()) {
                    nameSpacedKeys.add(key.toString());
                }
                boxSection.set("Advancement", nameSpacedKeys);
            }

        }
        //notify then save
        JustBoxed.getInstance().getComponentLogger().info(Component.text("Data Saved !", TextColor.color(0x56C749) ));
        DataConfig.save();
    }
}
