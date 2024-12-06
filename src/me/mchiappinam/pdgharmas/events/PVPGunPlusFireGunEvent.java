package me.mchiappinam.pdgharmas.events;

import org.bukkit.entity.Player;

import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

public class PVPGunPlusFireGunEvent extends PVPGunPlusEvent
{
    private Gun gun;
    private GunPlayer shooter;
    private int amountAmmoNeeded;
    private double accuracy;
    
    public PVPGunPlusFireGunEvent(final GunPlayer shooter, final Gun gun) {
        this.gun = gun;
        this.shooter = shooter;
        this.amountAmmoNeeded = gun.getAmmoAmtNeeded();
        this.accuracy = gun.getAccuracy();
        if (shooter.getPlayer().isSneaking() && gun.getAccuracy_crouched() > -1.0) {
            this.accuracy = gun.getAccuracy_crouched();
        }
        if (shooter.isAimedIn() && gun.getAccuracy_aimed() > -1.0) {
            this.accuracy = gun.getAccuracy_aimed();
        }
    }
    
    public PVPGunPlusEvent setAmountAmmoNeeded(final int i) {
        this.amountAmmoNeeded = i;
        return this;
    }
    
    public int getAmountAmmoNeeded() {
        return this.amountAmmoNeeded;
    }
    
    public double getGunAccuracy() {
        return this.accuracy;
    }
    
    public Gun getGun() {
        return this.gun;
    }
    
    public GunPlayer getShooter() {
        return this.shooter;
    }
    
    public Player getShooterAsPlayer() {
        return this.shooter.getPlayer();
    }
    
    public void setGunAccuracy(final double d) {
        this.accuracy = d;
    }
}
