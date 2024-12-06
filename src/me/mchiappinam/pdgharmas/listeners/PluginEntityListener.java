package me.mchiappinam.pdgharmas.listeners;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import me.mchiappinam.pdgharmas.PVPGunPlus;
import me.mchiappinam.pdgharmas.events.PVPGunPlusBulletCollideEvent;
import me.mchiappinam.pdgharmas.events.PVPGunPlusGunDamageEntityEvent;
import me.mchiappinam.pdgharmas.events.PVPGunPlusGunKillEntityEvent;
import me.mchiappinam.pdgharmas.gun.Bullet;
import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.Listener;

public class PluginEntityListener implements Listener
{
    PVPGunPlus plugin;
	List<String> freezados = new ArrayList<String>();
	List<String> freezadosMsgDelay = new ArrayList<String>();
    
    public PluginEntityListener(final PVPGunPlus plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHit(final ProjectileHitEvent event) {
        final Projectile check = event.getEntity();
        final Bullet bullet = PVPGunPlus.getPlugin().getBullet((Entity)check);
        if (bullet != null) {
            bullet.onHit();
            bullet.setNextTickDestroy();
            final Projectile p = event.getEntity();
            Block b = p.getLocation().getBlock();
            int id = b.getTypeId();
            for (double i = 0.2; i < 4.0; i += 0.2) {
                if (id == 0) {
                    b = p.getLocation().add(p.getVelocity().normalize().multiply(i)).getBlock();
                    id = b.getTypeId();
                }
            }
            if (id > 0) {
                p.getLocation().getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, id);
            }
            final PVPGunPlusBulletCollideEvent evv = new PVPGunPlusBulletCollideEvent(bullet.getShooter(), bullet.getGun(), b);
            this.plugin.getServer().getPluginManager().callEvent((Event)evv);
        }
        event.getEntity().remove();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(final EntityDeathEvent event) {
        final Entity dead = (Entity)event.getEntity();
        if (dead.getLastDamageCause() != null) {
            final EntityDamageEvent e = dead.getLastDamageCause();
            if (e instanceof EntityDamageByEntityEvent) {
                final EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent)e;
                final Entity damager = ede.getDamager();
                if (damager instanceof Projectile) {
                    final Projectile proj = (Projectile)damager;
                    final Bullet bullet = PVPGunPlus.getPlugin().getBullet((Entity)proj);
                    if (bullet != null) {
                        final Gun used = bullet.getGun();
                        final GunPlayer shooter = bullet.getShooter();
                        final PVPGunPlusGunKillEntityEvent pvpgunkill = new PVPGunPlusGunKillEntityEvent(shooter, used, dead);
                        this.plugin.getServer().getPluginManager().callEvent((Event)pvpgunkill);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Entity damager = event.getDamager();
        if (event.getEntity() instanceof LivingEntity) {
            final LivingEntity hurt = (LivingEntity)event.getEntity();
            if (damager instanceof Projectile) {
                final Projectile proj = (Projectile)damager;
                final Bullet bullet = PVPGunPlus.getPlugin().getBullet((Entity)proj);
                if (bullet != null) {
                    boolean headshot = false;
                    if (this.isNear(proj.getLocation(), hurt.getEyeLocation(), 0.26) && bullet.getGun().canHeadShot()) {
                        headshot = true;
                    }
                    final PVPGunPlusGunDamageEntityEvent pvpgundmg = new PVPGunPlusGunDamageEntityEvent(event, bullet.getShooter(), bullet.getGun(), event.getEntity(), headshot);
                    this.plugin.getServer().getPluginManager().callEvent((Event)pvpgundmg);
                    if (!pvpgundmg.isCancelled()) {
                        final double damage = pvpgundmg.getDamage();
                        double mult = 1.0;
                        if (pvpgundmg.isHeadshot()) {
                            PVPGunPlus.playEffect(Effect.ZOMBIE_DESTROY_DOOR, hurt.getLocation(), 3);
                            mult = 0.8;
                        }
                        if(pvpgundmg.isTaser()) {
	                        if(!freezados.contains(((Player)hurt).getName().toLowerCase())) {plugin.getServer().broadcastMessage("§2§l[§c§lX§2§l]§a O §e"+((Player)hurt).getName()+"§a está sendo eletrocutado!");
	                        	freeze(hurt,pvpgundmg.getTaserTime());
	                        }
                        }
                        hurt.setLastDamage(0);
                        event.setDamage((int)Math.ceil(damage * mult));
                        final int armorPenetration = bullet.getGun().getArmorPenetration();
                        if (armorPenetration > 0) {
                            Damageable hh = hurt;
                            Double health = hh.getHealth();
                            int newHealth = health.intValue() - armorPenetration;
                            if (newHealth < 0) {
                                newHealth = 0;
                            }
                            if (newHealth > 20) {
                                newHealth = 20;
                            }
                            hurt.setHealth(newHealth);
                        }
                        bullet.getGun().doKnockback(hurt, bullet.getVelocity());
                        bullet.remove();
                        hurt.setMaximumNoDamageTicks(0);
                	    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                	      public void run() {
                	    	  if(hurt!=null) {
	                              hurt.setMaximumNoDamageTicks(8);
                	    	  }
                	      }
                	    }, 5L);
                    }
                    else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    
    private boolean isNear(final Location location, final Location eyeLocation, final double d) {
        return Math.abs(location.getY() - eyeLocation.getY()) <= d;
    }
    
    public void freeze(LivingEntity hurt, int t) {
    	freezados.add(((Player)hurt).getName().toLowerCase());
    	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() {
        	freezados.remove(((Player)hurt).getName().toLowerCase()); } }, t+0L);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if(freezados.contains(e.getPlayer().getName().toLowerCase()))
        	if(((e.getFrom().getBlockX()!=e.getTo().getBlockX())||(e.getFrom().getBlockZ()!=e.getTo().getBlockZ()))) {
        		e.setCancelled(true);
	            
	            if(!freezadosMsgDelay.contains(e.getPlayer().getName().toLowerCase())) {
	            	e.getPlayer().sendMessage("§2§l[§c§lX§2§l]§a Choqueee§czz tzz §atzz §ctzz§a...");
	                freezadosMsgDelay.add(e.getPlayer().getName().toLowerCase());
	                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() { public void run() {
	                	freezadosMsgDelay.remove(e.getPlayer().getName().toLowerCase()); } }, 10L);
	           }
        }
    }
}
