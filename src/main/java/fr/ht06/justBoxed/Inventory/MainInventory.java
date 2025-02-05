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
        //BOX
        int amountBox = JustBoxed.boxManager.getBoxes().size();
        if (amountBox == 0) amountBox = 1;
        ItemStack itemBox = new ItemStack(Material.CHEST, amountBox);
        ItemMeta itemBoxMeta = itemBox.getItemMeta();

        itemBoxMeta.displayName(Component.text("Boxes Information").decoration(TextDecoration.ITALIC, false));
        itemBoxMeta.lore(List.of(Component.text("See information about boxes", NamedTextColor.GRAY)));
        itemBox.setItemMeta(itemBoxMeta);
        inventory.setItem(11, itemBox);


        //world
        int amountWorldLoaded = JustBoxed.worldManager.getWorldsRunnables().size();
        if (amountWorldLoaded == 0) amountWorldLoaded = 1;
        ItemStack itemWorld = new ItemStack(Material.GRASS_BLOCK, amountWorldLoaded);
        ItemMeta itemWorldMeta = itemWorld.getItemMeta();

        itemWorldMeta.displayName(Component.text("Loaded worlds information").decoration(TextDecoration.ITALIC, false));
        itemWorldMeta.lore(List.of(Component.text("See information about all the loaded worlds", NamedTextColor.GRAY)));
        itemWorld.setItemMeta(itemWorldMeta);
        inventory.setItem(15, itemWorld);

    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (event.getClickedInventory() == null) return;

        if (event.getClickedInventory().getHolder() instanceof MainInventory) {
            event.setCancelled(true);
            //boxInfo
            if (event.getSlot() == 11) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(new AllBoxInfoInventory(1).getInventory());
            }

            if (event.getSlot() == 15) {
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(new WorldInfoInventory(1).getInventory());
            }
        }
    }
}
