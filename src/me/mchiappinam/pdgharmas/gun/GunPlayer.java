package me.mchiappinam.pdgharmas.gun;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.mchiappinam.pdgharmas.InventoryHelper;
import me.mchiappinam.pdgharmas.PVPGunPlus;
import me.mchiappinam.pdgharmas.PermissionInterface;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;

public class GunPlayer
{
    private int ticks;
    private Player controller;
    private ItemStack lastHeldItem;
    private ArrayList<Gun> guns;
    private Gun currentlyFiring;
    public boolean enabled;
    private List<String> mirando=new ArrayList<String>();
    
    public GunPlayer(final PVPGunPlus plugin, final Player player) {
        this.enabled = true;
        this.controller = player;
        this.guns = plugin.getLoadedGuns();
        for (int i = 0; i < this.guns.size(); ++i) {
            this.guns.get(i).owner = this;
        }
    }
    
    public boolean isAimedIn() {
        return this.controller != null && this.controller.isOnline() && this.controller.hasPotionEffect(PotionEffectType.SLOW) && mirando.contains(this.controller.getName().toLowerCase());
    }
    
    public boolean onClick(final String clickType) {
        if (!this.enabled) {
            return false;
        }
        Gun holding = null;
        final ItemStack hand = this.controller.getItemInHand();
        if (hand != null) {
            final ArrayList<Gun> tempgun = this.getGunsByType(hand);
            final ArrayList<Gun> canFire = new ArrayList<Gun>();
            for (int i = 0; i < tempgun.size(); ++i) {
                if (PermissionInterface.checkPermission(this.controller, tempgun.get(i).node) || !tempgun.get(i).needsPermission) {
                    canFire.add(tempgun.get(i));
                }
            }
            if (tempgun.size() > canFire.size() && canFire.size() == 0) {
                if (tempgun.get(0).permissionMessage != null && tempgun.get(0).permissionMessage.length() > 0) {
                    this.controller.sendMessage(tempgun.get(0).permissionMessage);
                }
                return false;
            }
            tempgun.clear();
            for (int i = 0; i < canFire.size(); ++i) {
                final Gun check = canFire.get(i);
                final byte gunDat = check.getGunTypeByte();
                final byte itmDat = hand.getData().getData();
                if (gunDat == itmDat || check.ignoreItemData) {
                    holding = check;
                }
            }
            canFire.clear();
        }
        if (holding != null) {
            if ((holding.canClickRight || holding.canAimRight()) && clickType.equals("right")) {
                if (!holding.canAimRight()) {
                    final Gun gun = holding;
                    ++gun.heldDownTicks;
                    holding.lastFired = 0;
                    if (this.currentlyFiring == null) {
                        this.fireGun(holding);
                    }
                }
                else {
                    this.checkAim(holding);
                }
            }
            else if ((holding.canClickLeft || holding.canAimLeft()) && clickType.equals("left")) {
                if (!holding.canAimLeft()) {
                    holding.heldDownTicks = 0;
                    if (this.currentlyFiring == null) {
                        this.fireGun(holding);
                    }
                }else{
                    this.checkAim(holding);
                }
            }
        }
        return true;
    }
    
    public void checkAim(Gun holding) {
        if (this.isAimedIn()) {
            if((holding.isMiraAbobora())||(mirando.contains(this.controller.getName().toLowerCase()))) {
            	ItemStack elmo = controller.getPlayer().getInventory().getHelmet();
            	PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.controller.getEntityId(), 4, CraftItemStack.asNMSCopy(elmo));
                ((CraftPlayer)this.controller).getHandle().playerConnection.sendPacket(packet);
            }
            this.controller.removePotionEffect(PotionEffectType.SLOW);
            mirando.remove(this.controller.getName().toLowerCase());
        }
        else {
            if(holding.isMiraAbobora()) {
            	PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.controller.getEntityId(), 4, CraftItemStack.asNMSCopy(new ItemStack(Material.PUMPKIN, 1)));
                ((CraftPlayer)this.controller).getHandle().playerConnection.sendPacket(packet);
            }
            this.controller.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 12000, 4));
            if(!mirando.contains(this.controller.getName().toLowerCase()))
            	mirando.add(this.controller.getName().toLowerCase());
        }
    }
    
    private void fireGun(final Gun gun) {
        if (PermissionInterface.checkPermission(this.controller, gun.node) || !gun.needsPermission) {
            if (gun.timer <= 0) {
                this.currentlyFiring = gun;
                gun.firing = true;
            }
        }
        else if (gun.permissionMessage != null && gun.permissionMessage.length() > 0) {
            this.controller.sendMessage(gun.permissionMessage);
        }
    }
    
    public void tick() {
        ++this.ticks;
        if (this.controller != null) {
            final ItemStack hand = this.controller.getItemInHand();
            this.lastHeldItem = hand;
            if (this.ticks % 10 == 0 && hand != null) {
                final Gun g = PVPGunPlus.getPlugin().getGun(hand.getTypeId());
                if (g == null) {
                	if(mirando.contains(this.controller.getName().toLowerCase())) {
	                    this.controller.removePotionEffect(PotionEffectType.SLOW);
	                	ItemStack elmo = controller.getPlayer().getInventory().getHelmet();
	                	PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.controller.getEntityId(), 4, CraftItemStack.asNMSCopy(elmo));
	                    ((CraftPlayer)this.controller).getHandle().playerConnection.sendPacket(packet);
	                    mirando.remove(this.controller.getName().toLowerCase());
                	}
                }
            }
            for (int i = this.guns.size() - 1; i >= 0; --i) {
                final Gun g2 = this.guns.get(i);
                if (g2 != null) {
                    g2.tick();
                    if (this.controller.isDead()) {
                        g2.finishReloading();
                    }
                    if (hand != null && g2.getGunType() == hand.getTypeId() && this.isAimedIn() && !g2.canAimLeft() && !g2.canAimRight()) {
                        this.controller.removePotionEffect(PotionEffectType.SLOW);
                    	ItemStack elmo = controller.getPlayer().getInventory().getHelmet();
                    	PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.controller.getEntityId(), 4, CraftItemStack.asNMSCopy(elmo));
                        ((CraftPlayer)this.controller).getHandle().playerConnection.sendPacket(packet);
                        mirando.remove(this.controller.getName().toLowerCase());
                    }
                    if (this.currentlyFiring != null && g2.timer <= 0 && this.currentlyFiring.equals(g2)) {
                        this.currentlyFiring = null;
                    }
                }
            }
        }
        this.renameGuns(this.controller);
    }
    
    public void renameGuns(final Player p) {
        final Inventory inv = (Inventory)p.getInventory();
        final ItemStack[] items = inv.getContents();
        for (int i = 0; i < items.length; ++i) {
            if (items[i] != null) {
                final String name = this.getGunName(items[i]);
                if (name != null && name.length() > 0) {
                    this.setName(items[i], name);
                }
            }
        }
    }
    
    public ArrayList<Gun> getGunsByType(final ItemStack item) {
        final ArrayList<Gun> ret = new ArrayList<Gun>();
        for (int i = 0; i < this.guns.size(); ++i) {
            if (this.guns.get(i).getGunMaterial().equals((Object)item.getType())) {
                ret.add(this.guns.get(i));
            }
        }
        return ret;
    }
    
    public String getGunName(final ItemStack item) {
        final String ret = "";
        final ArrayList<Gun> tempgun = this.getGunsByType(item);
        final int amtGun = tempgun.size();
        if (amtGun > 0) {
            for (int i = 0; i < tempgun.size(); ++i) {
                if (PermissionInterface.checkPermission(this.controller, tempgun.get(i).node) || !tempgun.get(i).needsPermission) {
                    final Gun current = tempgun.get(i);
                    if (current.getGunMaterial() != null && current.getGunMaterial().getId() == item.getTypeId()) {
                        final byte gunDat = tempgun.get(i).getGunTypeByte();
                        final byte itmDat = item.getData().getData();
                        if (gunDat == itmDat || tempgun.get(i).ignoreItemData) {
                            return this.getGunName(current);
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    private String getGunName(final Gun current) {
        String add = "";
        String refresh = "";
        if (current.hasClip) {
            int leftInClip = 0;
            int ammoLeft = 0;
            final int maxInClip = current.maxClipSize;
            final int currentAmmo = (int)Math.floor(InventoryHelper.amtItem((Inventory)this.controller.getInventory(), current.getAmmoType(), current.getAmmoTypeByte()) / current.getAmmoAmtNeeded());
            ammoLeft = currentAmmo - maxInClip + current.roundsFired;
            if (ammoLeft < 0) {
                ammoLeft = 0;
            }
            leftInClip = currentAmmo - ammoLeft;
            add = "§r    §e« §b§l" + Integer.toString(leftInClip) + "§r §f§l│§r §b§l" + Integer.toString(ammoLeft) + "§r §e»";
            if (current.reloading) {
                final int reloadSize = 4;
                final double reloadFrac = (current.getReloadTime() - current.gunReloadTimer) / current.getReloadTime();
                final int amt = (int)Math.round(reloadFrac * reloadSize);
                for (int ii = 0; ii < amt; ++ii) {
                    refresh = String.valueOf(refresh) + "▪";
                }
                for (int ii = 0; ii < reloadSize - amt; ++ii) {
                    refresh = String.valueOf(refresh) + "▫";
                }
                add = ChatColor.RED + "    " + new StringBuffer(refresh).reverse() + " RECARREGANDO " + refresh;
            }
        }
        final String name = current.getName();
        return String.valueOf(name) + add;
    }
    
    public ItemStack setName(final ItemStack item, final String name) {
        final ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        item.setItemMeta(im);
        return item;
    }
    
    public Player getPlayer() {
        return this.controller;
    }
    
    public void unload() {
        this.controller = null;
        this.currentlyFiring = null;
        for (int i = 0; i < this.guns.size(); ++i) {
            this.guns.get(i).clear();
        }
    }
    
    public void reloadAllGuns() {
        for (int i = this.guns.size() - 1; i >= 0; --i) {
            final Gun g = this.guns.get(i);
            if (g != null) {
                g.reloadGun();
                g.finishReloading();
            }
        }
    }
    
    public boolean checkAmmo(final Gun gun, final int amount) {
        return InventoryHelper.amtItem((Inventory)this.controller.getInventory(), gun.getAmmoType(), gun.getAmmoTypeByte()) >= amount;
    }
    
    public void removeAmmo(final Gun gun, final int amount) {
        if (amount == 0) {
            return;
        }
        InventoryHelper.removeItem((Inventory)this.controller.getInventory(), gun.getAmmoType(), gun.getAmmoTypeByte(), amount);
    }
    
    public ItemStack getLastItemHeld() {
        return this.lastHeldItem;
    }
    
    public Gun getGun(final int typeId) {
        for (int i = this.guns.size() - 1; i >= 0; --i) {
            final Gun check = this.guns.get(i);
            if (check.getGunType() == typeId) {
                return check;
            }
        }
        return null;
    }
}
