package me.mchiappinam.pdgharmas.events;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

import org.bukkit.entity.Entity;

public class PVPGunPlusGunDamageEntityEvent extends PVPGunPlusEvent
{
    private Gun gun;
    private GunPlayer shooter;
    private Entity shot;
    private boolean isHeadshot;
    private int damage;
    private int taserTime; //Adicionado 19/07/2017
    private boolean isTaser; //Adicionado 19/07/2017
    private boolean isMiraAbobora; //Adicionado 20/01/2018
    private EntityDamageByEntityEvent event;
    
    public PVPGunPlusGunDamageEntityEvent(final EntityDamageByEntityEvent event, final GunPlayer shooter, final Gun gun, final Entity shot, final boolean headshot) {
        this.gun = gun;
        this.shooter = shooter;
        this.shot = shot;
        this.isHeadshot = headshot;
        this.damage = gun.getGunDamage();
        this.isTaser = gun.isTaser();
        this.taserTime = gun.getTaserTime();
        this.isMiraAbobora = gun.isMiraAbobora();
    }
    
    public EntityDamageByEntityEvent getEntityDamageEntityEvent() {
        return this.event;
    }
    
    public boolean isHeadshot() {
        return this.isHeadshot;
    }
    
    public boolean isTaser() {
        return this.isTaser;
    }
    
    public boolean isMiraAbobora() {
        return this.isMiraAbobora;
    }
    
    public void setHeadshot(final boolean b) {
        this.isHeadshot = b;
    }
    
    public GunPlayer getShooter() {
        return this.shooter;
    }
    
    public Entity getEntityDamaged() {
        return this.shot;
    }
    
    public Player getKillerAsPlayer() {
        return this.shooter.getPlayer();
    }
    
    public Gun getGun() {
        return this.gun;
    }
    
    public int getDamage() {
        return this.damage;
    }
    
    public int getTaserTime() {
        return this.taserTime;
    }
}
