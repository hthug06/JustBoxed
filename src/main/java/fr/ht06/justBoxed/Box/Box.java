package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.AdvancementManager;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.InviteRunnable;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Box {
    private String name;
    private UUID owner;
    private List<UUID> members = new ArrayList<>();
    private org.bukkit.Location spawn;
    private String worldName;
    private int size = JustBoxed.getInstance().getConfig().getInt("size");
    private List<NamespacedKey> completedAdvancements = new ArrayList<>();
    private List<InviteRunnable> invitedPlayers = new ArrayList<>();

    public Box(String name, UUID owner, org.bukkit.Location spawn, String worldName, boolean created) {
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        this.worldName = worldName;

        Player player = Bukkit.getPlayer(owner);
//        WorldBorderManager.setWorldBorder(this);

        //revoke all player's advancement cause this is the goal of this gamemode lol
        AdvancementManager.revokeAllAdvancement(player);
    }

    public Box(String name, UUID owner, org.bukkit.Location spawn, String worldName) {
        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        this.worldName = worldName;
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
//        WorldBorderManager.setWorldBorder(this, 2);
    }

    public String getWorldName() {
        return worldName;
    }

    public Location getSpawn() {
        return new Location(Bukkit.getWorld(worldName), spawn.getX(), spawn.getY(), spawn.getZ());
    }

    public Location getSpawnForJoin(Player player) {
        if (Bukkit.getWorld(worldName) == null) {
            Bukkit.getScheduler().runTaskAsynchronously(JustBoxed.getInstance(), () -> {
                player.sendMessage("Your world is loading, try to join it in a few seconds.");
                try {
                    new WorldCreator(worldName).createWorld();
                }catch (IllegalStateException ignored) {}
            });
            return null;
        }
        return new Location(Bukkit.getWorld(worldName), spawn.getX(), spawn.getY(), spawn.getZ());
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public void addDoneAdvancementOnStart(@NotNull NamespacedKey advancement) {
        completedAdvancements.add(advancement);
    }

    public void addDoneAdvancement(@NotNull NamespacedKey advancement) {
        completedAdvancements.add(advancement);
        //don't work if any player are offline
        AdvancementManager.grantAdvancement(Bukkit.getPlayer(owner), advancement);
        members.forEach(uuid -> {
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                AdvancementManager.grantAdvancement(Bukkit.getPlayer(uuid), advancement);
            }
        });
    }

    public List<InviteRunnable> getInvitation() {
        return invitedPlayers;
    }


    //All about invitation
    public InviteRunnable getPlayerInvitation(UUID uuid) {
        List<InviteRunnable> invit = invitedPlayers.stream()
                .filter(inviteRunnable -> inviteRunnable.getInvitedUUID().equals(uuid))
                .toList();
        return invit.getFirst();
    }

    public void addInvitation(InviteRunnable runnable) {
        invitedPlayers.add(runnable);
    }

    public void removeInvitation(InviteRunnable runnable) {
        invitedPlayers.remove(runnable);
    }

    public void removeInvitation(UUID uuid) {
        invitedPlayers.removeIf(inviteRunnable -> inviteRunnable.getInvitedUUID().equals(uuid));
    }

    public boolean isInvited(UUID uuid) {
        List<InviteRunnable> invit = invitedPlayers.stream()
                .filter(inviteRunnable -> inviteRunnable.getInvitedUUID().equals(uuid))
                .toList();
        return !invit.isEmpty();
    }

    public void broadcastMessage(Component message) {

        Player player = Bukkit.getPlayer(owner);

        if (player != null && player.isOnline()) {
            player.sendMessage(message);
        }

        members.forEach(uuid -> {
            Player member = Bukkit.getPlayer(uuid);
            if (member != null && member.isOnline()) {
                member.sendMessage(message);
            }
        });
    }

    public boolean isOnline(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid).isOnline();
    }

    public List<NamespacedKey> getCompletedAdvancements() {
        return completedAdvancements;
    }
}
