package me.mchiappinam.pdgharmas.gun;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.Event;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Sound;
import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import me.mchiappinam.pdgharmas.PVPGunExplosion;
import me.mchiappinam.pdgharmas.PVPGunPlus;

import org.bukkit.entity.Entity;

public class Bullet
{
    private int ticks;
    private int releaseTime;
    private boolean dead;
    private boolean active;
    private boolean destroyNextTick;
    private boolean released;
    private Entity projectile;
    private Vector velocity;
    private Location lastLocation;
    private Location startLocation;
    private GunPlayer shooter;
    private Gun shotFrom;
    
    public Bullet(final GunPlayer owner, final Vector vec, final Gun gun) {
        this.dead = false;
        this.active = true;
        this.destroyNextTick = false;
        this.released = false;
        this.shotFrom = gun;
        this.shooter = owner;
        this.velocity = vec;
        if (gun.isThrowable()) {
            final ItemStack thrown = new ItemStack(gun.getGunType(), 1, (short)gun.getGunTypeByte());
            this.projectile = (Entity)owner.getPlayer().getWorld().dropItem(owner.getPlayer().getEyeLocation(), thrown);
            ((Item)this.projectile).setPickupDelay(9999999);
            this.startLocation = this.projectile.getLocation();
        }
        else {
            Class<? extends Projectile> mclass = (Class<? extends Projectile>)Snowball.class;
            final String check = gun.projType.replace(" ", "").replace("_", "");
            if (check.equalsIgnoreCase("egg")) {
                mclass = (Class<? extends Projectile>)Egg.class;
            }
            if (check.equalsIgnoreCase("arrow")) {
                mclass = (Class<? extends Projectile>)Arrow.class;
            }
            this.projectile = (Entity)owner.getPlayer().launchProjectile((Class)mclass);
            ((Projectile)this.projectile).setShooter((LivingEntity)owner.getPlayer());
            this.startLocation = this.projectile.getLocation();
        }
        if (this.shotFrom.getReleaseTime() == -1) {
            this.releaseTime = 80 + (gun.isThrowable() ? 0 : 1) * 400;
        }
        else {
            this.releaseTime = this.shotFrom.getReleaseTime();
        }
    }
    
    public void tick() {
        if (!this.dead) {
            ++this.ticks;
            if (this.projectile != null) {
                this.lastLocation = this.projectile.getLocation();
                if (this.ticks > this.releaseTime) {
                    this.dead = true;
                    return;
                }
                if (this.shotFrom.hasSmokeTrail()) {
                    this.lastLocation.getWorld().playEffect(this.lastLocation, Effect.SMOKE, 0);
                }
                if (this.shotFrom.isThrowable() && this.ticks == 90) {
                    this.remove();
                    return;
                }
                if (this.active) {
                    if (this.lastLocation.getWorld().equals(this.startLocation.getWorld())) {
                        final double dis = this.lastLocation.distance(this.startLocation);
                        if (dis > this.shotFrom.getMaxDistance()) {
                            this.active = false;
                            if (!this.shotFrom.isThrowable() && !this.shotFrom.canGoPastMaxDistance()) {
                                this.velocity.multiply(0.25);
                            }
                        }
                    }
                    this.projectile.setVelocity(this.velocity);
                }
            }
            else {
                this.dead = true;
            }
            if (this.ticks > 200) {
                this.dead = true;
            }
        }
        else {
            this.remove();
        }
        if (this.destroyNextTick) {
            this.dead = true;
        }
    }
    
    public Gun getGun() {
        return this.shotFrom;
    }
    
    public GunPlayer getShooter() {
        return this.shooter;
    }
    
    public Vector getVelocity() {
        return this.velocity;
    }
    
    public void remove() {
        this.dead = true;
        PVPGunPlus.getPlugin().removeBullet(this);
        this.projectile.remove();
        this.onHit();
        this.destroy();
    }
    
    public void onHit() {
        if (this.released) {
            return;
        }
        this.released = true;
        if (this.projectile != null) {
            this.lastLocation = this.projectile.getLocation();
            if (this.shotFrom != null) {
                int rad2;
                int rad = rad2 = (int)this.shotFrom.getExplodeRadius();
                if (this.shotFrom.getFireRadius() > rad) {
                    rad = (int)this.shotFrom.getFireRadius();
                    rad2 = 2;
                    for (int i = -rad; i <= rad; ++i) {
                        for (int ii = -rad2 / 2; ii <= rad2 / 2; ++ii) {
                            for (int iii = -rad; iii <= rad; ++iii) {
                                final Location nloc = this.lastLocation.clone().add((double)i, (double)ii, (double)iii);
                                if (nloc.distance(this.lastLocation) <= rad && PVPGunPlus.getPlugin().random.nextInt(5) == 1) {
                                    this.lastLocation.getWorld().playEffect(nloc, Effect.MOBSPAWNER_FLAMES, 2);
                                }
                            }
                        }
                    }
                }
                else if (rad > 0) {
                    for (int i = -rad; i <= rad; ++i) {
                        for (int ii = -rad2 / 2; ii <= rad2 / 2; ++ii) {
                            for (int iii = -rad; iii <= rad; ++iii) {
                                final Location nloc = this.lastLocation.clone().add((double)i, (double)ii, (double)iii);
                                if (nloc.distance(this.lastLocation) <= rad && PVPGunPlus.getPlugin().random.nextInt(10) == 1) {
                                    //new PVPGunExplosion(nloc).explode();
                                }
                            }
                        }
                    }
                    new PVPGunExplosion(this.lastLocation).explode();
                }
                this.explode();
                this.fireSpread();
                this.flash();
            }
        }
    }
    
    public void explode() {
        if (this.shotFrom.getExplodeRadius() > 0.0) {
            this.lastLocation.getWorld().createExplosion(this.lastLocation, 0.0f);
            if (this.shotFrom.isThrowable()) {
                this.projectile.teleport(this.projectile.getLocation().add(0.0, 1.0, 0.0));
            }
            final int c = (int)this.shotFrom.getExplodeRadius();
            final ArrayList<Entity> entities = (ArrayList<Entity>)this.projectile.getNearbyEntities((double)c, (double)c, (double)c);
            for (int i = 0; i < entities.size(); ++i) {
                if (entities.get(i) instanceof LivingEntity && ((LivingEntity)entities.get(i)).hasLineOfSight(this.projectile)) {
                    int dmg = this.shotFrom.getExplosionDamage();
                    if (dmg == -1) {
                        dmg = this.shotFrom.getGunDamage();
                    }
                    ((LivingEntity)entities.get(i)).setLastDamage(0);
                    ((LivingEntity)entities.get(i)).damage(dmg, (Entity)this.shooter.getPlayer());
                    ((LivingEntity)entities.get(i)).setLastDamage(0);
                }
            }
        }
    }
    
    public void fireSpread() {
        if (this.shotFrom.getFireRadius() > 0.0) {
            this.lastLocation.getWorld().playSound(this.lastLocation, Sound.GLASS, 20.0f, 20.0f);
            final int c = (int)this.shotFrom.getFireRadius();
            final ArrayList<Entity> entities = (ArrayList<Entity>)this.projectile.getNearbyEntities((double)c, (double)c, (double)c);
            for (int i = 0; i < entities.size(); ++i) {
                if (entities.get(i) instanceof LivingEntity) {
                    final EntityDamageByEntityEvent e = new EntityDamageByEntityEvent((Entity)this.shooter.getPlayer(), (Entity)entities.get(i), EntityDamageEvent.DamageCause.CUSTOM, 0);
                    Bukkit.getServer().getPluginManager().callEvent((Event)e);
                    if (!e.isCancelled() && ((LivingEntity)entities.get(i)).hasLineOfSight(this.projectile)) {
                        ((LivingEntity)entities.get(i)).setFireTicks(140);
                        ((LivingEntity)entities.get(i)).setLastDamage(0);
                        ((LivingEntity)entities.get(i)).damage(1, (Entity)this.shooter.getPlayer());
                    }
                }
            }
        }
    }
    
    public void flash() {
        if (this.shotFrom.getFlashRadius() > 0.0) {
            this.lastLocation.getWorld().playSound(this.lastLocation, Sound.SPLASH, 20.0f, 20.0f);
            final int c = (int)this.shotFrom.getFlashRadius();
            final ArrayList<Entity> entities = (ArrayList<Entity>)this.projectile.getNearbyEntities((double)c, (double)c, (double)c);
            for (int i = 0; i < entities.size(); ++i) {
                if (entities.get(i) instanceof LivingEntity) {
                    final EntityDamageByEntityEvent e = new EntityDamageByEntityEvent((Entity)this.shooter.getPlayer(), (Entity)entities.get(i), EntityDamageEvent.DamageCause.CUSTOM, 0);
                    Bukkit.getServer().getPluginManager().callEvent((Event)e);
                    if (!e.isCancelled() && ((LivingEntity)entities.get(i)).hasLineOfSight(this.projectile)) {
                        ((LivingEntity)entities.get(i)).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 140, 1));
                    }
                }
            }
        }
    }
    
    public void destroy() {
        this.projectile = null;
        this.velocity = null;
        this.shotFrom = null;
        this.shooter = null;
    }
    
    public Entity getProjectile() {
        return this.projectile;
    }
    
    public void setNextTickDestroy() {
        this.destroyNextTick = true;
    }
}
