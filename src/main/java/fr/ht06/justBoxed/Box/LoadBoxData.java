package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.Config.DataConfig;
import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.util.UUID;

public class LoadBoxData {

    //I think I'll try to implement SQL one day... 😅
    public static void load(){

        //if there is no file, there is no data
        if (!new File(Bukkit.getServer().getPluginManager().getPlugin("JustBoxed").getDataFolder(), "data.yml").exists()) {
            JustBoxed.getInstance().getComponentLogger().info(Component.text("No data to load: inexistant file...", TextColor.color(0x5dade2)));
            return;
        }

        //file exists, load it
        DataConfig.setup();
        DataConfig.save();

        //if there is no box saved, do nothing
        if (!DataConfig.get().contains("Box")){
            JustBoxed.getInstance().getComponentLogger().info(Component.text("No data to load: no box created...", TextColor.color(0x5dade2)));
            return;
        }
        else {
            JustBoxed.getInstance().getComponentLogger().info(Component.text("Data found! Loading...", TextColor.color(0xCA7F30)));
        }

        //load box 1 by 1
        for (String boxUUID : DataConfig.get().getConfigurationSection("Box").getKeys(false)) {

            //load everything related to the boxUUID
            String ownerName = DataConfig.get().getString("Box."+ boxUUID +".ownerUUID");

            int x = DataConfig.get().getInt("Box."+ boxUUID +".x");
            int y = DataConfig.get().getInt("Box."+ boxUUID +".y");
            int z = DataConfig.get().getInt("Box."+ boxUUID +".z");
            String worldName = DataConfig.get().getString("Box."+ boxUUID +".worldName");
            Location spawn = new Location(Bukkit.getWorld(worldName), x, y, z);

            //create the box base
            Box newBox;
            //Need to check because before 1.0.3, the name of the box was a String, and after this is a Component
            if(DataConfig.get().isString("Box."+ boxUUID +".name")){
                String name = DataConfig.get().getString("Box."+ boxUUID +".name");
                Component nameComponent = MiniMessage.miniMessage().deserialize(name);
                newBox = new Box(nameComponent, UUID.fromString(ownerName), spawn, worldName);

            }
            else {
                Component name = DataConfig.get().getRichMessage("Box."+ boxUUID +".name");
                newBox = new Box(name, UUID.fromString(ownerName), spawn, worldName);
            }


            //if the plugin is in version 1.0, the save file uses box name instead of uuid, need tpo check this
            try {
                newBox.setUuid(UUID.fromString(boxUUID));
            }catch (Exception ignored){}


            //then add the members
           if (DataConfig.get().getList("Box."+ boxUUID +".membersUUID") != null) {
               DataConfig.get().getList("Box."+ boxUUID +".membersUUID")
                       .forEach(member -> newBox.addMember(UUID.fromString((String) member)));
           }

           //and the box size
           newBox.setSize(DataConfig.get().getInt("Box."+ boxUUID +".boxSize"));


           //And the advancement
           DataConfig.get().getStringList("Box."+ boxUUID +".Advancement")
                   .forEach(nameSpacedKey ->{
                       String namespace = nameSpacedKey.split(":")[0];
                       String key = nameSpacedKey.split(":")[1];
                       NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
                       newBox.addDoneAdvancementOnStart(namespacedKey);
                   });

           //and add it to the boxUUID manager
           JustBoxed.boxManager.add(newBox);
        }
        JustBoxed.getInstance().getComponentLogger().info(Component.text("Data loaded!", TextColor.color(0x48CA4A)));
    }
}
