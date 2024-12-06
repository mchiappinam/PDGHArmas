package me.mchiappinam.pdgharmas.events;

import org.bukkit.entity.Player;

import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;

import org.bukkit.block.Block;

public class PVPGunPlusBulletCollideEvent extends PVPGunPlusEvent
{
    private Gun gun;
    private GunPlayer shooter;
    private Block blockHit;
    
    public PVPGunPlusBulletCollideEvent(final GunPlayer shooter, final Gun gun, final Block block) {
        this.gun = gun;
        this.shooter = shooter;
        this.blockHit = block;
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
    
    public Block getBlockHit() {
        return this.blockHit;
    }
}
