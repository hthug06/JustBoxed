package fr.ht06.justBoxed.Box;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoxManager {

    List<Box> boxes = new ArrayList<>();

    public BoxManager() {}

    public void add(Box box) {
        boxes.add(box);
    }

    public Box getBoxByName(String name) {
        return boxes.stream().filter(box -> box.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Box getBoxByPlayer(UUID uuid) {
        for (Box box : boxes) {
            if (box.getOwner().equals(uuid) ) {
                return box;
            }
        }
        return null;
    }

    public boolean hasBox(Player player) {
        for (Box box : boxes) {
            if (box.getOwner().equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void removeBox(Box box) {
        boxes.remove(box);
    }

}
