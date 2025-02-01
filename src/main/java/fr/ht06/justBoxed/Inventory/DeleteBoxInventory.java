package fr.ht06.justBoxed.Inventory;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.WorldRunnable;
import fr.ht06.justBoxed.Utils.CreateItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeleteBoxInventory implements InventoryHolder, Listener {

    private Inventory inventory;

    //Need it for register the event
    public DeleteBoxInventory() {}

    public DeleteBoxInventory(Box box) {
        inventory = Bukkit.createInventory(this, 27, Component.text("Delete this box?"));
        init(box);
    }

    private void init(Box box) {
        Component name = Component.text("Do you really want to delete this box?", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("- This action is irreversible", NamedTextColor.RED));
        lore.add(Component.text("- Members of this box will lose everything", NamedTextColor.RED));
        lore.add(Component.text("", NamedTextColor.RED));
        lore.add(Component.text("- Click here to confirm", NamedTextColor.RED));
        lore.add(Component.text("", NamedTextColor.RED));
        lore.add(Component.text("Box uuid: " + box.getUuid().toString(), NamedTextColor.GRAY));
        ItemStack itemConfirm = CreateItem.createItem(name, 1, Material.RED_TERRACOTTA, lore);
        inventory.setItem(13, itemConfirm);


        inventory.setItem(22, CreateItem.createItem(Component.text("Cancel this action", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false),
                1,
                Material.BARRIER));
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
//        event.getWhoClicked().closeInventory();

        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof DeleteBoxInventory) {
            if (event.getCurrentItem() == null) return;

            if (event.getSlot() == 22){
                event.getWhoClicked().closeInventory();
                return;
            }

            //Vérification if this is the wanted inventory
            event.setCancelled(true);
            if (event.getInventory().isEmpty()) return;

            //get the box with the uuid
            ItemStack itemGrass = event.getInventory().getItem(13);
            Component name = itemGrass.lore().getLast();
            String uuidStr = PlainTextComponentSerializer.plainText().serialize(name).replace("Box uuid: ", "");
            Box box = JustBoxed.boxManager.getBoxByUUID(UUID.fromString(uuidStr));

            //delete the box
            deleteBox(box);

            //send a message and close the inv
            event.getViewers().forEach(humanEntity -> {
                humanEntity.sendMessage(box.getName().append(Component.text(" is deleted", NamedTextColor.RED)));
                humanEntity.closeInventory();
            });
        }
    }

    public void deleteBox(Box box) {
        box.broadcastMessage(Component.text("An admin deleted your box", NamedTextColor.RED));

        //Get the world
        World w = Bukkit.getWorld(box.getWorldName());

        WorldRunnable runnable = JustBoxed.worldManager.get(w);
        JustBoxed.worldManager.remove(runnable);
        JustBoxed.boxManager.removeBox(box);

        //if the world is null, the world is already unloaded so we just delete the files
        if (w != null){
            //teleport everyone to another place (for nom 0 0 in world)
            for (Player player : w.getPlayers()) {
                player.teleport(Bukkit.getWorld("world").getSpawnLocation());
            }

            //Unload the world (without saving cause the world going to be deleted)
            Bukkit.getServer().unloadWorld(w, false);
        }

        try {
            FileUtils.deleteDirectory(new File(JustBoxed.getInstance().getServer().getWorldContainer(), box.getWorldName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
