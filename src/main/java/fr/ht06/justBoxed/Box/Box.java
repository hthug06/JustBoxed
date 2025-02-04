package fr.ht06.justBoxed.Box;

import fr.ht06.justBoxed.AdvancementManager;
import fr.ht06.justBoxed.JustBoxed;
import fr.ht06.justBoxed.Runnable.InviteRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Box {
    private UUID uuid;
    private Component name;
    private UUID owner;
    private List<UUID> members = new ArrayList<>();
    private org.bukkit.Location spawn;
    private String worldName;
    private int size = JustBoxed.getInstance().getConfig().getInt("size");
    private List<NamespacedKey> completedAdvancements = new ArrayList<>();
    private List<InviteRunnable> invitedPlayers = new ArrayList<>();

    public Box(Component name, UUID owner, org.bukkit.Location spawn, String worldName) {
        //create a box uuid to save it in the data.yml
        do {
            this.uuid = UUID.randomUUID();
        }while (JustBoxed.boxManager.UUIDTaken(uuid));

        if  (Bukkit.getOfflinePlayer(owner).isOnline()) {
            AdvancementManager.revokeAllAdvancement(Bukkit.getOfflinePlayer(owner).getPlayer());
        }


        this.name = name;
        this.owner = owner;
        this.spawn = spawn;
        this.worldName = worldName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setName(Component name) {
        this.name = name;
    }

    public Component getName() {
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

        //Get the advancement
        Advancement advancementReel = Bukkit.getAdvancement(advancement);

        //don't work if any player are offline
        if (Bukkit.getOfflinePlayer(owner).isOnline()){

            //don't give the advancement if he already has it
            if (!Bukkit.getOfflinePlayer(owner).getPlayer().getAdvancementProgress(advancementReel).isDone()) {
                AdvancementManager.grantAdvancement(Bukkit.getPlayer(owner), advancement);
            }
        }

        members.forEach(uuid -> {
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                //don't give the advancement if he already has it
                if (!Bukkit.getPlayer(uuid).getAdvancementProgress(advancementReel).isDone()) {
                    AdvancementManager.grantAdvancement(Bukkit.getPlayer(uuid), advancement);
                }
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
