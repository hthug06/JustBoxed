package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.AdvancementManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoxManager {

    private List<Box> boxes = new ArrayList<>();

    public BoxManager() {}

    public void add(Box box) {
        boxes.add(box);
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public Box getBoxByName(Component name) {
        return boxes.stream().filter(box -> box.getName().equals(name)).findFirst().orElse(null);
    }

    public Box getBoxByPlainName(String name) {
        return boxes.stream().filter(box -> PlainTextComponentSerializer.plainText().serialize(box.getName()).equals(name)).findFirst().orElse(null);
    }

    public Box getBoxByWorldName(String worldname) {
        return boxes.stream().filter(box -> box.getWorldName().equalsIgnoreCase(worldname)).findFirst().orElse(null);
    }

    public Box getBoxByPlayer(UUID uuid) {
        for (Box box : boxes) {
            if (box.getOwner().equals(uuid) || box.getMembers().contains(uuid)) {
                return box;
            }
        }
        return null;
    }

    public Box getBoxByUUID(UUID uuid) {
        return boxes.stream().filter(box -> box.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public boolean hasBox(UUID uuid) {
        for (Box box : boxes) {
            if (box.getOwner().equals(uuid) || box.getMembers().contains(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(Box box, Player player) {
        return box.getOwner().equals(player.getUniqueId());
    }

    public boolean isMember(Box box, Player player) {
        return box.getMembers().contains(player.getUniqueId());
    }

    public void removeBox(Box box) {

        //For revoke all advancement
        if (Bukkit.getOfflinePlayer(box.getOwner()).getPlayer() != null){
            AdvancementManager.revokeAllAdvancement(Bukkit.getOfflinePlayer(box.getOwner()).getPlayer());
        }

        box.getMembers().forEach(member -> {
            if (Bukkit.getOfflinePlayer(member).isOnline()){
                AdvancementManager.revokeAllAdvancement(Bukkit.getOfflinePlayer(member).getPlayer());
            }
        });

        boxes.remove(box);
    }

    public boolean nameTaken(Component name){
        String namestr = PlainTextComponentSerializer.plainText().serialize(name);
        for (Box box: boxes){
            if (PlainTextComponentSerializer.plainText().serialize(box.getName()).equalsIgnoreCase(namestr)){
                return true;
            }
        }
        return false;
    }

    public boolean UUIDTaken(UUID uuid) {
        for (Box box : boxes) {
            if(box.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

}
