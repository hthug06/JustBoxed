package fr.ht06.justBoxed.Inventory;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Utils.CreateItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class AllBoxInfoInventory implements InventoryHolder, Listener {

    private Inventory inventory;
    int page;
    private MiniMessage miniMessage = MiniMessage.miniMessage();

    public AllBoxInfoInventory(int page) {
        this.page = page;
        inventory = Bukkit.createInventory(this, 54, Component.text("Boxes Information"));
        init(page);
    }

    private void init(int page) {

        ItemStack itemBack = CreateItem.createItem(Component.text("Go back to the main page", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false), 1, Material.ARROW);
        inventory.setItem(45, itemBack);

        if (JustBoxed.boxManager.getBoxes().isEmpty()) {
            Component name =  Component.text("No boxes found", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
            inventory.setItem(22, CreateItem.createItem(name, 1, Material.BARRIER, List.of(Component.text("This is so calm for the moment", NamedTextColor.GRAY))));
        }

        for (int slot = 0; slot < 44; slot++) {
            if (44*(page-1)+slot >= JustBoxed.boxManager.getBoxes().size()) break;
            Box box = JustBoxed.boxManager.getBoxes().get(44*(page-1)+slot);
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            meta.setRarity(ItemRarity.COMMON);
            meta.displayName(box.getName().decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text(box.getUuid().toString(), NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, true)));
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

        if (page > 1) {
            ItemStack itemStack = new ItemStack(Material.ARROW);
            ItemMeta meta = itemStack.getItemMeta();
            meta.displayName(Component.text("Go to page " + (page - 1)).decoration(TextDecoration.ITALIC, false));
            itemStack.setItemMeta(meta);
            inventory.setItem(45, itemStack);
        }

        if (inventory.getStorageContents()[53] != null) {
            ItemStack itemStack = new ItemStack(Material.ARROW);
            ItemMeta meta = itemStack.getItemMeta();
            meta.displayName(Component.text("Go to page " + (page + 1)).decoration(TextDecoration.ITALIC, false));
            itemStack.setItemMeta(meta);
            inventory.setItem(53, itemStack);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof AllBoxInfoInventory) {
            if (event.getCurrentItem() == null) return;

            if (event.getSlot() == 45 && event.getCurrentItem().getType().equals(Material.ARROW)) {
                event.getWhoClicked().openInventory(new MainInventory().getInventory());
                return;
            }

            //page -1
            event.setCancelled(true);
            if (event.getSlot() == 45 && event.getCurrentItem().getType().equals(Material.ARROW)) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(new AllBoxInfoInventory(page-1).getInventory());
                return;
            }

            //page +1
            if (event.getSlot() == 53 && event.getCurrentItem().getType().equals(Material.ARROW)) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(new AllBoxInfoInventory(page+1).getInventory());
                return;
            }
            if (event.getInventory().isEmpty()) return;

            //get the box
            Component boxComp = event.getCurrentItem().lore().getFirst();
            String boxUUID = PlainTextComponentSerializer.plainText().serialize(boxComp);
            UUID uuid = UUID.fromString(boxUUID);
            Box box = JustBoxed.boxManager.getBoxByUUID(uuid);
            event.getWhoClicked().openInventory(new BoxInfoInventory(box).getInventory());
        }
    }

}
