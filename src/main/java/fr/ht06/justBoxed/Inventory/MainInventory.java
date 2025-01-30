package fr.ht06.justBoxed.Inventory;

import fr.ht06.justBoxed.JustBoxed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainInventory implements InventoryHolder, Listener {

    private Inventory inventory;

    public MainInventory() {
        inventory = Bukkit.createInventory(this, 27, Component.text("Main Inventory"));
        init();
    }

    private void init() {
        int amountWorld = JustBoxed.worldManager.getWorldsRunnables().size();
        if (amountWorld == 0) amountWorld = 1;
        ItemStack itemWorld = new ItemStack(Material.GRASS_BLOCK, amountWorld);
        ItemMeta itemWorldMeta = itemWorld.getItemMeta();

        itemWorldMeta.displayName(Component.text("Worlds Information").decoration(TextDecoration.ITALIC, false));
        itemWorldMeta.lore(List.of(Component.text("See information about all boxes worlds", NamedTextColor.GRAY)));
        itemWorld.setItemMeta(itemWorldMeta);
        inventory.setItem(13, itemWorld);

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
//        event.getWhoClicked().closeInventory();

        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof MainInventory) {//Vérification si c'estle bon inventaire
            event.setCancelled(true);
            if (event.getSlot() == 13) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(new AllBoxInfoInventory(1).getInventory());
            }
        }
    }
}
