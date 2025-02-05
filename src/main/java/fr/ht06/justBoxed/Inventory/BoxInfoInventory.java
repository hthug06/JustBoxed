package fr.ht06.justBoxed.Inventory;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.WorldRunnable;
import fr.ht06.justBoxed.Utils.CreateItem;
import fr.ht06.justBoxed.World.LoadWorld;
import fr.ht06.justBoxed.WorldBorderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoxInfoInventory implements InventoryHolder, Listener {

    Inventory inventory;

    public BoxInfoInventory(Box box) {
        this.inventory = Bukkit.createInventory(this, 36, box.getName());
        init(box);
    }

    public BoxInfoInventory(){}

    private void init(Box box) {

        //World info
        setItemWorldInfo(box);

        //delete the box under the world info
        setItemDeleteBox();

        //Box info
        setItemBoxInfo(box);

        //Member info
        setItemMemberInfo(box);

        //go back to the allboxInfoInv
        inventory.setItem(27, CreateItem.createItem(Component.text("Go back to Boxes information Menu").decoration(TextDecoration.ITALIC, false),
                1,
                Material.ARROW));

        Bukkit.getScheduler().runTaskLater(JustBoxed.getInstance(), () -> {
            if (!inventory.getViewers().isEmpty()) {
                inventory.getViewers().getFirst().openInventory(new BoxInfoInventory(box).getInventory());
            }
        }, 20);

    }



    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof BoxInfoInventory) {//Vérification si c'estle bon inventaire
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;
            //get the box
            ItemStack itemGrass = event.getInventory().getItem(12);
            Component name = itemGrass.lore().getLast();
            String uuidStr = PlainTextComponentSerializer.plainText().serialize(name).replace("Box UUID: ", "");
            Box box = JustBoxed.boxManager.getBoxByUUID(UUID.fromString(uuidStr));

            if (event.getCurrentItem().getType() == null) return;

            //World info
            if (event.getCurrentItem().getType().equals(Material.GRASS_BLOCK)) {

                //World offline
                if (event.getCurrentItem().lore().size() == 4){
                    //load and teleport
                    if (event.isRightClick()) {
                        //close then load the box
                        event.getWhoClicked().closeInventory();

                        LoadWorld.load(box.getWorldName());

                        //And wait for the world to load
                        new BukkitRunnable() {
                            Box box = JustBoxed.boxManager.getBoxByUUID(UUID.fromString(uuidStr));
                            @Override
                            public void run() {
                                event.getWhoClicked().sendActionBar(Component.text("Loading the world...", NamedTextColor.GOLD));
                                if (Bukkit.getWorld(box.getWorldName()) != null){
                                    event.getWhoClicked().teleport(box.getSpawn());
                                    event.getWhoClicked().sendActionBar(Component.text("World Loaded!", NamedTextColor.GREEN));
                                    cancel();
                                }
                            }
                        }.runTaskTimer(JustBoxed.getInstance(), 0, 20);
                    }
                    //only load
                    else if (event.isLeftClick()) {
                        LoadWorld.load(box.getWorldName());
                        event.getWhoClicked().sendMessage(Component.text("Loading the world...", NamedTextColor.GOLD));
                        new BukkitRunnable() {
                            Box box = JustBoxed.boxManager.getBoxByUUID(UUID.fromString(uuidStr));
                            @Override
                            public void run() {
                                event.getWhoClicked().sendActionBar(Component.text("Loading the world...", NamedTextColor.GOLD));
                                if (Bukkit.getWorld(box.getWorldName()) != null){
                                    event.getWhoClicked().sendMessage(Component.text("World Loaded!", NamedTextColor.GREEN));
                                    cancel();
                                }
                            }
                        }.runTaskTimer(JustBoxed.getInstance(), 0, 20);
                    }

                    event.getWhoClicked().openInventory(new BoxInfoInventory(box).getInventory());
                }

                //world online / loaded
                else{
                    if (event.isRightClick()) {
                        WorldBorderManager.setWorldBorder(box);
                        event.getWhoClicked().teleport(box.getSpawn());
                        event.getWhoClicked().sendActionBar(Component.text("Teleported to the box...", NamedTextColor.GOLD));
                        event.getWhoClicked().closeInventory();
                    }

                    //simple leftClick
                    else if (event.isLeftClick() && !event.isShiftClick()) {
                        if(!Bukkit.getWorld(box.getWorldName()).getPlayers().isEmpty()){
                            event.getWhoClicked().sendMessage(Component.text("This world cannot be unloaded because there are player inside...", NamedTextColor.RED));
                            return;
                        }

                        JustBoxed.worldManager.remove(JustBoxed.worldManager.get(Bukkit.getWorld(box.getWorldName())));
                        Bukkit.unloadWorld(box.getWorldName(), true);
                        event.getWhoClicked().sendMessage(Component.text(box.getWorldName() + " is unloaded!", NamedTextColor.GREEN));
                    }

                    //Shift + left click (force unload)
                    else if (event.isLeftClick() && event.isShiftClick()) {
                        if(!Bukkit.getWorld(box.getWorldName()).getPlayers().isEmpty()){
                            for (Player player : Bukkit.getWorld(box.getWorldName()).getPlayers()) {
                                player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                            }
                        }

                        JustBoxed.worldManager.remove(JustBoxed.worldManager.get(Bukkit.getWorld(box.getWorldName())));
                        Bukkit.unloadWorld(box.getWorldName(), true);
                        event.getWhoClicked().sendMessage(Component.text(box.getWorldName() + " is unloaded!", NamedTextColor.GREEN));
                    }
                }
            }

            else if (event.getCurrentItem().getType() == Material.RED_TERRACOTTA) {
                event.getWhoClicked().openInventory(new DeleteBoxInventory(box).getInventory());
            }

            else if (event.getCurrentItem().getType() == Material.ARROW) {
                event.getWhoClicked().openInventory(new AllBoxInfoInventory(1).getInventory());
            }
        }
    }

    private void setItemWorldInfo(Box box){
        World world = Bukkit.getWorld(box.getWorldName());
        WorldRunnable worldRunnable = JustBoxed.worldManager.get(world);

        ItemStack worldInfo = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta worldInfoItemMeta = worldInfo.getItemMeta();
        worldInfoItemMeta.displayName(Component.text("World name: "+ box.getWorldName()).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        if (world == null){
            lore.add(Component.text("World offline", NamedTextColor.RED));
            lore.add(Component.text(""));
            lore.add(Component.text("Right Click to load the world and teleport to the box", NamedTextColor.GRAY));
            lore.add(Component.text("Left Click to only load the world", NamedTextColor.GRAY));
        }
        else {
            lore.add(Component.text("Player in the world: " + world.getPlayerCount(), NamedTextColor.GRAY));
            lore.add(Component.text("Uptime: " + timeSerializer(worldRunnable.getActiveTime()), NamedTextColor.GRAY));
            lore.add(Component.text("Chunk loaded: " + world.getChunkCount(), NamedTextColor.GRAY));
            lore.add(Component.text(""));
            lore.add(Component.text("Right Click to teleport to the box", NamedTextColor.GRAY));
            lore.add(Component.text("Left Click to unload the world", NamedTextColor.GRAY));
            lore.add(Component.text("Shift + Left Click to force-unload the world", NamedTextColor.GRAY));
        }

        worldInfoItemMeta.lore(lore);
        worldInfo.setItemMeta(worldInfoItemMeta);
        inventory.setItem(11, worldInfo);
    }

    private void setItemDeleteBox() {
        ItemStack itemDelete = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta meta = itemDelete.getItemMeta();
        meta.displayName(Component.text("Delete this box?", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        itemDelete.setItemMeta(meta);
        inventory.setItem(20, itemDelete);
    }

    private void setItemBoxInfo(Box box) {
        ItemStack boxItem = new ItemStack(Material.CHEST);
        ItemMeta boxMeta = boxItem.getItemMeta();

        boxMeta.displayName(box.getName().decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Box size: " + box.getSize(), TextColor.color(0x85c1e9)));
        lore.add(Component.text("Completed advancement: " + box.getCompletedAdvancements().size(), TextColor.color(0xf1c40f)));
        lore.add(Component.text(""));
        lore.add(Component.text("Box UUID: " + box.getUuid().toString(), TextColor.color(0x5f6a6a)));
        boxMeta.lore(lore);
        boxItem.setItemMeta(boxMeta);
        inventory.setItem(12, boxItem);
    }

    private void setItemMemberInfo(Box box) {
        ItemStack playerItem = new ItemStack(Material.ARMOR_STAND);
        ItemMeta playerMeta = playerItem.getItemMeta();
        playerMeta.displayName(Component.text("Player info: ").decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        //owner
        lore.add(Component.text("Owner: " + Bukkit.getOfflinePlayer(box.getOwner()).getName(), TextColor.color(0xC7392B)));
        //members
        if(!box.getMembers().isEmpty()){
            lore.add(Component.text("Member(s): ", TextColor.color(0x558CD2)));
            box.getMembers().forEach(member -> lore.add(Component.text("- "+Bukkit.getOfflinePlayer(member).getName(), TextColor.color(0x558CD2))));
        }
        playerMeta.lore(lore);
        playerItem.setItemMeta(playerMeta);
        inventory.setItem(13, playerItem);
    }

    public static String timeSerializer(int time) {
        int hours = 0;
        while (time > 3600) {
            time -= 3600;
            hours++;
        }
        int minutes = 0;
        while (time > 60) {
            time -= 60;
            minutes++;
        }
        return hours + "h " + minutes + "min " + time + "sec";
    }
}
