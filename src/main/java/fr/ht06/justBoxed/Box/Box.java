package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.WorldBorderManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class Box {
    String name;
    UUID owner;
    List<UUID> members;
    org.bukkit.Location location;
    String worldName;
    Location spawn;
    int size = JustBoxed.getInstance().getConfig().getInt("size");

    public Box(String name, UUID owner, org.bukkit.Location location) {
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.spawn = location;
        this.worldName = Bukkit.getPlayer(owner).getName() + "_boxed";
        WorldBorderManager.setWorldBorder(this);

    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        WorldBorderManager.setWorldBorder(this, 5);
    }

    public String getWorldName() {
        return worldName;
    }

    public Location getLocation() {
        return location;
    }

}
