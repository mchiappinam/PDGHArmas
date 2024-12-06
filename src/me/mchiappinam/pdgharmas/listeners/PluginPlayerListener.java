package me.mchiappinam.pdgharmas.listeners;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.mchiappinam.pdgharmas.PVPGunPlus;
import me.mchiappinam.pdgharmas.PermissionInterface;
import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

public class PluginPlayerListener implements Listener
{
    private PVPGunPlus plugin;
    
    public PluginPlayerListener(final PVPGunPlus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        this.plugin.onJoin(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.plugin.onQuit(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Item dropped = event.getItemDrop();
        final Player dropper = event.getPlayer();
        final GunPlayer gp = this.plugin.getGunPlayer(dropper);
        if (gp != null) {
            final ItemStack lastHold = gp.getLastItemHeld();
            if (lastHold != null) {
                final Gun gun = gp.getGun(dropped.getItemStack().getTypeId());
                if (gun != null && lastHold.equals((Object)dropped.getItemStack()) && gun.hasClip && gun.changed && gun.reloadGunOnDrop) {
                    gun.reloadGun();
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Action action = event.getAction();
        final Player player = event.getPlayer();
        final ItemStack itm1 = player.getItemInHand();
        if (itm1 != null && (action.equals((Object)Action.LEFT_CLICK_AIR) || action.equals((Object)Action.LEFT_CLICK_BLOCK) || action.equals((Object)Action.RIGHT_CLICK_AIR) || action.equals((Object)Action.RIGHT_CLICK_BLOCK))) {
            String clickType = "left";
            if (action.equals((Object)Action.RIGHT_CLICK_AIR) || action.equals((Object)Action.RIGHT_CLICK_BLOCK)) {
                clickType = "right";
            }
            final GunPlayer gp = this.plugin.getGunPlayer(player);
            if (gp != null) {
            	
            	
            	int itemID = itm1.getType().getId();
            	if(plugin.armasLimite.containsKey(itemID)) {
            		int slot = player.getInventory().getHeldItemSlot();
            		int grupo = plugin.armasLimite.get(itemID);
            		if(slot>0) {
            			if(clickType.contains("right"))
		            		for(int a=0;slot>a;a++) {
		            			if(player.getInventory().getItem(a)!=null) {
		            				int aID = player.getInventory().getItem(a).getType().getId();
			            			if(plugin.armasLimite.containsKey(aID)) {
			            				if(plugin.armasLimite.get(aID)==grupo) {
			            					player.sendMessage(plugin.getConfig().getString("grupos."+grupo+".erro").replaceAll("&", "§"));
			            					return;
			            				}
			            			}
		            			}
	            			}
            		}
            	}
            	
            	
                gp.onClick(clickType);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String[] split = event.getMessage().split(" ");
        split[0] = split[0].substring(1);
        final String label = split[0];
        final String[] args = new String[split.length - 1];
        for (int i = 1; i < split.length; ++i) {
            args[i - 1] = split[i];
        }
        if (label.equalsIgnoreCase("pdgharmas8498749849") && args.length == 0) {
            player.sendMessage(ChatColor.DARK_GRAY + "----" + ChatColor.GRAY + "[" + ChatColor.YELLOW + "PVPGUNPLUS" + ChatColor.GRAY + "]" + ChatColor.DARK_GRAY + "----");
            player.sendMessage(ChatColor.GRAY + "/pdgharmas8498749849 " + ChatColor.GREEN + "reload" + ChatColor.WHITE + " to reload the server");
            player.sendMessage(ChatColor.GRAY + "/pdgharmas8498749849 " + ChatColor.GREEN + "list" + ChatColor.WHITE + " to list the guns loaded into the server");
            player.sendMessage(ChatColor.GRAY + "/pdgharmas8498749849 " + ChatColor.GREEN + "toggle" + ChatColor.WHITE + " to toggle whether or not you can fire");
        }
        try {
            if (label.equalsIgnoreCase("pdgharmas8498749849") && args[0].equals("reload") && PermissionInterface.checkPermission(player, "pvpgunplus.admin")) {
                this.plugin.reload(true);
                player.sendMessage("RELOADED PVPGUN");
            }
            if (label.equalsIgnoreCase("pdgharmas8498749849") && args[0].equals("toggle") && PermissionInterface.checkPermission(player, "pvpgunplus.user")) {
                final GunPlayer gp = this.plugin.getGunPlayer(player);
                if (gp != null) {
                    gp.enabled = !gp.enabled;
                    final String on = ChatColor.GREEN + "ON";
                    final String off = ChatColor.RED + "OFF";
                    if (gp.enabled) {
                        player.sendMessage(ChatColor.GRAY + "You have turned guns " + on);
                    }
                    else {
                        player.sendMessage(ChatColor.GRAY + "You have turned guns " + off);
                    }
                }
            }
            if (label.equalsIgnoreCase("pdgharmas8498749849") && args[0].equals("list")) {
                player.sendMessage("-------PVPGUNS-------");
                final ArrayList<Gun> loadedGuns = this.plugin.getLoadedGuns();
                for (int j = 0; j < loadedGuns.size(); ++j) {
                    final Gun g = loadedGuns.get(j);
                    player.sendMessage(" -" + g.getName() + ChatColor.YELLOW + "(" + Integer.toString(g.getGunType()) + ")" + ChatColor.GRAY + " AMMO: " + ChatColor.RED + g.getAmmoMaterial().toString() + ChatColor.GRAY + "  amt# " + ChatColor.RED + Integer.toString(g.getAmmoAmtNeeded()));
                }
                player.sendMessage("---------------------");
            }
        }
        catch (Exception ex) {}
    }
}
