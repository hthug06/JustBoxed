package fr.ht06.justBoxed.Inventory;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.JustBoxed;
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
            inventory.setItem(44, itemStack);
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
//        event.getWhoClicked().closeInventory();

        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof AllBoxInfoInventory) {
            if (event.getCurrentItem() == null) return;

            //Vérification si c'estle bon inventaire
            event.setCancelled(true);
            if (event.getSlot() == 44 && event.getCurrentItem().getType().equals(Material.ARROW)) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(new AllBoxInfoInventory(page-1).getInventory());
                return;
            }

            if (event.getSlot() == 53 && event.getCurrentItem().getType().equals(Material.ARROW)) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(new AllBoxInfoInventory(page+1).getInventory());
                return;
            }
            if (event.getInventory().isEmpty()) return;
            Component boxComp = event.getCurrentItem().lore().getFirst();
            String boxUUID = PlainTextComponentSerializer.plainText().serialize(boxComp);
            UUID uuid = UUID.fromString(boxUUID);
            Box box = JustBoxed.boxManager.getBoxByUUID(uuid);
            event.getWhoClicked().openInventory(new BoxInfoInventory(box).getInventory());
        }
    }

}
