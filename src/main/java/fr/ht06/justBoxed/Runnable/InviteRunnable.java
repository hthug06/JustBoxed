package fr.ht06.justBoxed.Runnable;

import fr.ht06.justBoxed.Box.Box;
import fr.ht06.justBoxed.JustBoxed;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class InviteRunnable extends BukkitRunnable {

    int inviteTime = JustBoxed.getInstance().getConfig().getInt("inviteTime");
    Box box;
    UUID invitedUUID;

    public InviteRunnable(Box box,  UUID invitedUUID) {
        this.invitedUUID = invitedUUID;
        this.box = box;
        this.runTaskTimer(JustBoxed.getInstance(), 0, 20);
    }

    @Override
    public void run() {

        if (this.inviteTime == 0) {
            cancel();
            box.removeInvitation(this);
        }
        inviteTime--;

    }

    public int getInviteRemainingTime() {
        return this.inviteTime;
    }

    public UUID getInvitedUUID() {
        return this.invitedUUID;
    }
}
