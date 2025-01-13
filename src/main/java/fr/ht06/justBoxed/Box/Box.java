package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.AdvancementManager;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.InviteRunnable;
import fr.ht06.justBoxed.WorldBorderManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Box {
    String name;
    UUID owner;
    List<UUID> members;
    org.bukkit.Location spawn;
    String worldName;
    int size = JustBoxed.getInstance().getConfig().getInt("size");
    List<Advancement> completedAdvancements = new ArrayList<>();
    List<InviteRunnable> invitedPlayers = new ArrayList<>();

    public Box(String name, UUID owner, org.bukkit.Location spawn) {
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;

        Player player = Bukkit.getPlayer(owner);
        this.worldName = player.getName() + "_boxed";
        WorldBorderManager.setWorldBorder(this);

        //revoke all player's advancement cause this is the goal of this gamemode lol
        AdvancementManager.revokeAllAdvancement(player);

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

    public Location getSpawn() {
        return spawn;
    }

    public void addDoneAdvancement(Advancement advancement) {
        completedAdvancements.add(advancement);
        //don't work if any player are offline
        AdvancementManager.grantAdvancement(Bukkit.getPlayer(owner), advancement.getKey().getKey());
        members.forEach(uuid -> {
            AdvancementManager.grantAdvancement(Bukkit.getPlayer(uuid), advancement.getKey().getKey());
        });
    }

    public List<InviteRunnable> getInvitation() {
        return invitedPlayers;
    }


    //All about invitation
    public InviteRunnable getPlayerInvitation(UUID uuid) {
        invitedPlayers.stream()
                .filter(inviteRunnable -> inviteRunnable.getInvitedUUID().equals(uuid))
                .toList();
        return invitedPlayers.getFirst();
    }

    public void addInvitation(InviteRunnable runnable) {
        invitedPlayers.add(runnable);
    }

    public void removeInvitation(InviteRunnable runnable) {
        invitedPlayers.remove(runnable);
    }

    public boolean isinvited(UUID uuid) {
        invitedPlayers.stream()
                .filter(inviteRunnable -> inviteRunnable.getInvitedUUID().equals(uuid))
                .toList();
        return invitedPlayers.isEmpty();
    }
}
