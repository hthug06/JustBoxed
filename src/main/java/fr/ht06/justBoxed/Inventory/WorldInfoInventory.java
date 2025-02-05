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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorldInfoInventory implements InventoryHolder, Listener {

    Inventory inventory;

    //to register the listener
    public WorldInfoInventory() {}

    public WorldInfoInventory(int page) {
        inventory = Bukkit.createInventory(this, 54, Component.text("World Info"));
        init(page);
    }

    private void init(int page) {

        ItemStack itemBack = CreateItem.createItem(Component.text("Go back to the main page", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), 1, Material.ARROW);
        inventory.setItem(45, itemBack);

        List<WorldRunnable> worldRunnables = JustBoxed.worldManager.getWorldsRunnables()
                .stream()
                .filter(worldRunnable -> worldRunnable.getWorld() != null)
                .toList();

        if (worldRunnables.isEmpty()) {
            Component name =  Component.text("No world loaded", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
            inventory.setItem(22, CreateItem.createItem(name, 1, Material.BARRIER, List.of(Component.text("This is so calm for the moment", NamedTextColor.GRAY))));
            return;
        }

        for (int slot = 0; slot < 44; slot++) {
            if (44 * (page - 1) + slot >= JustBoxed.worldManager.getWorldsRunnables().size()) break;
            Box box = JustBoxed.boxManager.getBoxByWorldName(worldRunnables.get(slot).getWorld().getName());
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            meta.setRarity(ItemRarity.COMMON);
            meta.displayName(box.getName().decoration(TextDecoration.ITALIC, false));
            World world = worldRunnables.get(slot).getWorld();
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("World name:    " + box.getWorldName(), NamedTextColor.GRAY));
            lore.add(Component.text("Player in the world: " + world.getPlayerCount(), NamedTextColor.GRAY));
            lore.add(Component.text("Uptime: " + BoxInfoInventory.timeSerializer(worldRunnables.get(slot).getActiveTime()), NamedTextColor.GRAY));
            lore.add(Component.text("Chunk loaded: " + world.getChunkCount(), NamedTextColor.GRAY));
            lore.add(Component.text(""));
            lore.add(Component.text("Right Click to open the box Menu", NamedTextColor.GRAY));
            lore.add(Component.text(""));
            lore.add(Component.text("Box UUID: " + box.getUuid().toString(), NamedTextColor.GRAY));
            meta.lore(lore);
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(box.getOwner()));
            itemStack.setItemMeta(meta);
            inventory.setItem(slot, itemStack);
//            inventory.getViewers().stream().forEach(humanEntity -> Bukkit.getPlayer(humanEntity.getName()).updateInventory());

            int finalSlot = slot;
            new BukkitRunnable() {
                int count = 0;

                @Override
                public void run() {
                    //2.5 second
                    if (count == 25) {
                        cancel();
                        return;
                    }
                    inventory.setItem(finalSlot, itemStack);
                    count++;
                }
            }.runTaskTimerAsynchronously(JustBoxed.getInstance(), 2L, 2L);
        }

        //need to reload the inv every second

    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof WorldInfoInventory) {//Vérification si c'estle bon inventaire
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            if (event.getSlot() == 45) {
                event.getWhoClicked().openInventory(new MainInventory().getInventory());
                return;
            }

            //get the box
            ItemStack item = event.getCurrentItem();
            Component name = item.lore().getLast();
            String uuidStr = PlainTextComponentSerializer.plainText().serialize(name).replace("Box UUID: ", "");
            Box box = JustBoxed.boxManager.getBoxByUUID(UUID.fromString(uuidStr));

            if (event.getCurrentItem().getType() == null) return;

            //right click = go to the box menu
            if (event.isRightClick()) {
                event.getWhoClicked().openInventory(new BoxInfoInventory(box).getInventory());
            }
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
