package me.mchiappinam.pdgharmas;

import org.bukkit.entity.Player;

public class PermissionInterface
{
    public static boolean checkPermission(final Player player, final String command) {
        try {
            return player.isOp() || player.hasPermission(command);
        }
        catch (Exception ex) {
            return true;
        }
    }
}
