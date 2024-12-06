package me.mchiappinam.pdgharmas.gun;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import java.util.Random;
import org.bukkit.event.Event;

import me.mchiappinam.pdgharmas.PVPGunPlus;
import me.mchiappinam.pdgharmas.events.PVPGunPlusFireGunEvent;

import java.util.ArrayList;

public class Gun
{
    private boolean canHeadshot;
    private boolean isThrowable;
    private boolean hasSmokeTrail;
    private boolean localGunSound;
    private boolean canAimLeft;
    private boolean canAimRight;
    private boolean canGoPastMaxDistance;
    private byte gunByte;
    private byte ammoByte;
    private int gunType;
    private int ammoType;
    private int ammoAmtNeeded;
    private int gunDamage;
    private int explosionDamage;
    private int roundsPerBurst;
    private int reloadTime;
    private int maxDistance;
    private int bulletsPerClick;
    private int bulletsShot;
    private int bulletDelay;
    private int armorPenetration;
    private int releaseTime;
    private double bulletSpeed;
    private double accuracy;
    private double accuracy_aimed;
    private double accuracy_crouched;
    private double explodeRadius;
    private double fireRadius;
    private double flashRadius;
    private double knockback;
    private double recoil;
    private double gunVolume;
    private String gunName;
    private String fileName;
    private boolean taser; //Adicionado 19/07/2017
    private int taserTime; //Adicionado 19/07/2017
    private boolean miraAbobora; //Adicionado 20/01/2018
    public String projType;
    public ArrayList<String> gunSound;
    public String outOfAmmoMessage;
    public String permissionMessage;
    public boolean needsPermission;
    public boolean canClickRight;
    public boolean canClickLeft;
    public boolean hasClip;
    public boolean ignoreItemData;
    public boolean reloadGunOnDrop;
    public int maxClipSize;
    public int bulletDelayTime;
    public int roundsFired;
    public int gunReloadTimer;
    public int timer;
    public int lastFired;
    public int ticks;
    public int heldDownTicks;
    public boolean firing;
    public boolean reloading;
    public boolean changed;
    public GunPlayer owner;
    public String node;
    public String reloadType;
    
    public Gun(final String name) {
        this.explosionDamage = -1;
        this.bulletDelay = 2;
        this.releaseTime = -1;
        this.accuracy_aimed = -1.0;
        this.accuracy_crouched = -1.0;
        this.gunVolume = 1.0;
        this.projType = "";
        this.gunSound = new ArrayList<String>();
        this.outOfAmmoMessage = "";
        this.permissionMessage = "";
        this.hasClip = true;
        this.ignoreItemData = false;
        this.reloadGunOnDrop = true;
        this.maxClipSize = 30;
        this.bulletDelayTime = 10;
        this.firing = false;
        this.changed = false;
        this.reloadType = "NORMAL";
        this.gunName = name;
        this.fileName = name;
        this.outOfAmmoMessage = "Sem munição!";
    }
    
    public void shoot() {
        if (this.owner != null && this.owner.getPlayer().isOnline() && !this.reloading) {
            final PVPGunPlusFireGunEvent event = new PVPGunPlusFireGunEvent(this.owner, this);
            PVPGunPlus.getPlugin().getServer().getPluginManager().callEvent((Event)event);
            if (!event.isCancelled()) {
                if ((this.owner.checkAmmo(this, event.getAmountAmmoNeeded()) && event.getAmountAmmoNeeded() > 0) || event.getAmountAmmoNeeded() == 0) {
                    this.owner.removeAmmo(this, event.getAmountAmmoNeeded());
                    if (this.roundsFired >= this.maxClipSize && this.hasClip) {
                        this.reloadGun();
                        return;
                    }
                    this.doRecoil(this.owner.getPlayer());
                    this.changed = true;
                    ++this.roundsFired;
                    for (int i = 0; i < this.gunSound.size(); ++i) {
                        final Sound sound = PVPGunPlus.getSound(this.gunSound.get(i));
                        if (sound != null) {
                            if (this.localGunSound) {
                                this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), sound, (float)this.gunVolume, 2.0f);
                            }
                            else {
                                this.owner.getPlayer().getWorld().playSound(this.owner.getPlayer().getLocation(), sound, (float)this.gunVolume, 2.0f);
                            }
                        }
                    }
                    for (int i = 0; i < this.bulletsPerClick; ++i) {
                        int acc = (int)(event.getGunAccuracy() * 1000.0);
                        if (acc <= 0) {
                            acc = 1;
                        }
                        final Location ploc = this.owner.getPlayer().getLocation();
                        final Random rand = new Random();
                        final double dir = -ploc.getYaw() - 90.0f;
                        final double pitch = -ploc.getPitch();
                        final double xwep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5) / 1000.0;
                        final double ywep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5) / 1000.0;
                        final double zwep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5) / 1000.0;
                        final double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + xwep;
                        final double yd = Math.sin(Math.toRadians(pitch)) + ywep;
                        final double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch)) + zwep;
                        final Vector vec = new Vector(xd, yd, zd);
                        vec.multiply(this.bulletSpeed);
                        final Bullet bullet = new Bullet(this.owner, vec, this);
                        PVPGunPlus.getPlugin().addBullet(bullet);
                    }
                    if (this.roundsFired >= this.maxClipSize && this.hasClip) {
                        this.reloadGun();
                    }
                }
                else {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.ITEM_BREAK, 20.0f, 20.0f);
                    this.owner.getPlayer().sendMessage(this.outOfAmmoMessage);
                    this.finishShooting();
                }
            }
        }
    }
    
    public void tick() {
        ++this.ticks;
        ++this.lastFired;
        --this.timer;
        --this.gunReloadTimer;
        if (this.gunReloadTimer < 0) {
            if (this.reloading) {
                this.finishReloading();
            }
            this.reloading = false;
        }
        this.gunSounds();
        if (this.lastFired > 6) {
            this.heldDownTicks = 0;
        }
        if (((this.heldDownTicks >= 2 && this.timer <= 0) || this.firing) && !this.reloading) {
            if (this.roundsPerBurst > 1) {
                if (this.ticks % this.bulletDelay == 0) {
                    ++this.bulletsShot;
                    if (this.bulletsShot <= this.roundsPerBurst) {
                        this.shoot();
                    }
                    else {
                        this.finishShooting();
                    }
                }
            }
            else {
                this.shoot();
                this.finishShooting();
            }
        }
        if (this.reloading) {
            this.firing = false;
        }
    }
    
    public Gun copy() {
        final Gun g = new Gun(this.gunName);
        g.gunName = this.gunName;
        g.gunType = this.gunType;
        g.gunByte = this.gunByte;
        g.ammoByte = this.ammoByte;
        g.ammoAmtNeeded = this.ammoAmtNeeded;
        g.ammoType = this.ammoType;
        g.roundsPerBurst = this.roundsPerBurst;
        g.bulletsPerClick = this.bulletsPerClick;
        g.bulletSpeed = this.bulletSpeed;
        g.accuracy = this.accuracy;
        g.accuracy_aimed = this.accuracy_aimed;
        g.accuracy_crouched = this.accuracy_crouched;
        g.maxDistance = this.maxDistance;
        g.gunVolume = this.gunVolume;
        g.gunDamage = this.gunDamage;
        g.explodeRadius = this.explodeRadius;
        g.fireRadius = this.fireRadius;
        g.flashRadius = this.flashRadius;
        g.canHeadshot = this.canHeadshot;
        g.reloadTime = this.reloadTime;
        g.taser = this.taser;
        g.taserTime = this.taserTime;
        g.miraAbobora = this.miraAbobora; //Adicionado 20/01/2018
        g.canAimLeft = this.canAimLeft;
        g.canAimRight = this.canAimRight;
        g.canClickLeft = this.canClickLeft;
        g.canClickRight = this.canClickRight;
        g.hasSmokeTrail = this.hasSmokeTrail;
        g.armorPenetration = this.armorPenetration;
        g.isThrowable = this.isThrowable;
        g.ignoreItemData = this.ignoreItemData;
        g.outOfAmmoMessage = this.outOfAmmoMessage;
        g.projType = this.projType;
        g.needsPermission = this.needsPermission;
        g.node = this.node;
        g.gunSound = this.gunSound;
        g.bulletDelayTime = this.bulletDelayTime;
        g.hasClip = this.hasClip;
        g.maxClipSize = this.maxClipSize;
        g.reloadGunOnDrop = this.reloadGunOnDrop;
        g.localGunSound = this.localGunSound;
        g.fileName = this.fileName;
        g.explosionDamage = this.explosionDamage;
        g.recoil = this.recoil;
        g.knockback = this.knockback;
        g.reloadType = this.reloadType;
        g.releaseTime = this.releaseTime;
        g.canGoPastMaxDistance = this.canGoPastMaxDistance;
        g.permissionMessage = this.permissionMessage;
        return g;
    }
    
    public void reloadGun() {
        this.reloading = true;
        this.gunReloadTimer = this.reloadTime;
    }
    
    private void gunSounds() {
        if (this.reloading) {
            final int amtReload = this.reloadTime - this.gunReloadTimer;
            if (this.reloadType.equalsIgnoreCase("bolt")) {
                if (amtReload == 6) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 2.0f, 1.5f);
                }
                if (amtReload == this.reloadTime - 4) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.DOOR_CLOSE, 1.0f, 1.5f);
                }
            }
            else if (this.reloadType.equalsIgnoreCase("pump") || this.reloadType.equals("INDIVIDUAL_BULLET")) {
                final int rep = (this.reloadTime - 10) / this.maxClipSize;
                if (amtReload >= 5 && amtReload <= this.reloadTime - 5 && amtReload % rep == 0) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.NOTE_SNARE_DRUM, 1.0f, 2.0f);
                }
                if (amtReload == this.reloadTime - 3) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.PISTON_EXTEND, 1.0f, 2.0f);
                }
                if (amtReload == this.reloadTime - 1) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.PISTON_RETRACT, 1.0f, 2.0f);
                }
            }
            else {
                if (amtReload == 6) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.FIRE_IGNITE, 2.0f, 2.0f);
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 1.0f, 2.0f);
                }
                if (amtReload == this.reloadTime / 2) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.PISTON_RETRACT, 0.33f, 2.0f);
                }
                if (amtReload == this.reloadTime - 4) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.FIRE_IGNITE, 2.0f, 2.0f);
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.DOOR_CLOSE, 1.0f, 2.0f);
                }
            }
        }
        else {
            if (this.reloadType.equalsIgnoreCase("pump")) {
                if (this.timer == 8) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.PISTON_EXTEND, 1.0f, 2.0f);
                }
                if (this.timer == 6) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.PISTON_RETRACT, 1.0f, 2.0f);
                }
            }
            if (this.reloadType.equalsIgnoreCase("bolt")) {
                if (this.timer == this.bulletDelayTime - 4) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.DOOR_OPEN, 2.0f, 1.25f);
                }
                if (this.timer == 6) {
                    this.owner.getPlayer().playSound(this.owner.getPlayer().getLocation(), Sound.DOOR_CLOSE, 1.0f, 1.25f);
                }
            }
        }
    }
    
    private void doRecoil(final Player player) {
        if (this.recoil != 0.0) {
            final Location ploc = player.getLocation();
            final double dir = -ploc.getYaw() - 90.0f;
            final double pitch = -ploc.getPitch() - 180.0f;
            final double xd = Math.cos(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
            final double yd = Math.sin(Math.toRadians(pitch));
            final double zd = -Math.sin(Math.toRadians(dir)) * Math.cos(Math.toRadians(pitch));
            final Vector vec = new Vector(xd, yd, zd);
            vec.multiply(this.recoil / 2.0).setY(0);
            player.setVelocity(player.getVelocity().add(vec));
        }
    }
    
    public void doKnockback(final LivingEntity entity, final Vector speed) {
        if (this.knockback > 0.0) {
            speed.normalize().setY(0.6).multiply(this.knockback / 4.0);
            entity.setVelocity(speed);
        }
    }
    
    public void finishReloading() {
        this.bulletsShot = 0;
        this.roundsFired = 0;
        this.changed = false;
        this.gunReloadTimer = 0;
    }
    
    private void finishShooting() {
        this.bulletsShot = 0;
        this.timer = this.bulletDelayTime;
        this.firing = false;
    }
    
    public String getName() {
        return this.gunName;
    }
    
    public Material getAmmoMaterial() {
        final int id = this.getAmmoType();
        final Material mat = Material.getMaterial(id);
        if (mat != null) {
            return mat;
        }
        return null;
    }
    
    public int getAmmoType() {
        return this.ammoType;
    }
    
    public int getAmmoAmtNeeded() {
        return this.ammoAmtNeeded;
    }
    
    public Material getGunMaterial() {
        final int id = this.getGunType();
        final Material mat = Material.getMaterial(id);
        if (mat != null) {
            return mat;
        }
        System.out.println("NULL MATERIAL IN GUN: " + this.gunName + " / TYPEID: " + id);
        return null;
    }
    
    public int getGunType() {
        return this.gunType;
    }
    
    public double getExplodeRadius() {
        return this.explodeRadius;
    }
    
    public double getFireRadius() {
        return this.fireRadius;
    }
    
    public boolean isThrowable() {
        return this.isThrowable;
    }
    
    public void setName(String val) {
        val = val.replace("&", "§");
        this.gunName = val;
    }
    
    public int getValueFromString(final String str) {
        if (str.contains(":")) {
            final String news = str.substring(0, str.indexOf(":"));
            return Integer.parseInt(news);
        }
        return Integer.parseInt(str);
    }
    
    public byte getByteDataFromString(final String str) {
        if (str.contains(":")) {
            final String news = str.substring(str.indexOf(":") + 1, str.length());
            return Byte.parseByte(news);
        }
        return -1;
    }
    
    public void setGunType(final String val) {
        this.gunType = this.getValueFromString(val);
        this.gunByte = this.getByteDataFromString(val);
        if (this.gunByte == -1) {
            this.ignoreItemData = true;
            this.gunByte = 0;
        }
    }
    
    public void setAmmoType(final String val) {
        this.ammoType = this.getValueFromString(val);
        this.ammoByte = this.getByteDataFromString(val);
        if (this.ammoByte == -1) {
            this.ammoByte = 0;
        }
    }
    
    public void setAmmoAmountNeeded(final int parseInt) {
        this.ammoAmtNeeded = parseInt;
    }
    
    public void setRoundsPerBurst(final int parseInt) {
        this.roundsPerBurst = parseInt;
    }
    
    public void setBulletsPerClick(final int parseInt) {
        this.bulletsPerClick = parseInt;
    }
    
    public void setBulletSpeed(final double parseDouble) {
        this.bulletSpeed = parseDouble;
    }
    
    public void setAccuracy(final double parseDouble) {
        this.accuracy = parseDouble;
    }
    
    public void setAccuracyAimed(final double parseDouble) {
        this.accuracy_aimed = parseDouble;
    }
    
    public void setAccuracyCrouched(final double parseDouble) {
        this.accuracy_crouched = parseDouble;
    }
    
    public void setExplodeRadius(final double parseDouble) {
        this.explodeRadius = parseDouble;
    }
    
    public void setFireRadius(final double parseDouble) {
        this.fireRadius = parseDouble;
    }
    
    public void setCanHeadshot(final boolean parseBoolean) {
        this.canHeadshot = parseBoolean;
    }
    
    public void setCanClickLeft(final boolean parseBoolean) {
        this.canClickLeft = parseBoolean;
    }
    
    public void setCanClickRight(final boolean parseBoolean) {
        this.canClickRight = parseBoolean;
    }
    
    public void clear() {
        this.owner = null;
    }
    
    public void setReloadTime(final int parseInt) {
        this.reloadTime = parseInt;
    }
    
    public int getReloadTime() {
        return this.reloadTime;
    }
    
    public void setTaserTime(final int parseInt) { //Adicionado 19/07/2017
        this.taserTime = parseInt;
    }
    
    public int getTaserTime() {
        return this.taserTime;
    }
    
    public void setTaser(final boolean parseBoolean) { //Adicionado 19/07/2017
        this.taser = parseBoolean;
    }
    
    public void setMiraAbobora(final boolean parseBoolean) { //Adicionado 20/01/2018
        this.miraAbobora = parseBoolean;
    }
    
    public boolean isTaser() {
        return this.taser;
    }
    
    public boolean isMiraAbobora() {
        return this.miraAbobora;
    }
    
    public int getGunDamage() {
        return this.gunDamage;
    }
    
    public void setGunDamage(final int parseInt) {
        this.gunDamage = parseInt;
    }
    
    public double getMaxDistance() {
        return this.maxDistance;
    }
    
    public void setMaxDistance(final int i) {
        this.maxDistance = i;
    }
    
    public boolean canAimLeft() {
        return this.canAimLeft;
    }
    
    public boolean canAimRight() {
        return this.canAimRight;
    }
    
    public void setCanAimLeft(final boolean parseBoolean) {
        this.canAimLeft = parseBoolean;
    }
    
    public void setCanAimRight(final boolean parseBoolean) {
        this.canAimRight = parseBoolean;
    }
    
    public void setOutOfAmmoMessage(String val) {
        val = val.replace("&", "§");
        this.outOfAmmoMessage = val;
    }
    
    public void setPermissionMessage(String val) {
        val = val.replace("&", "§");
        this.permissionMessage = val;
    }
    
    public void setFlashRadius(final double parseDouble) {
        this.flashRadius = parseDouble;
    }
    
    public double getFlashRadius() {
        return this.flashRadius;
    }
    
    public void setIsThrowable(final boolean b) {
        this.isThrowable = b;
    }
    
    public boolean canHeadShot() {
        return this.canHeadshot;
    }
    
    public boolean hasSmokeTrail() {
        return this.hasSmokeTrail;
    }
    
    public void setSmokeTrail(final boolean b) {
        this.hasSmokeTrail = b;
    }
    
    public boolean isLocalGunSound() {
        return this.localGunSound;
    }
    
    public void setLocalGunSound(final boolean b) {
        this.localGunSound = b;
    }
    
    public void setArmorPenetration(final int parseInt) {
        this.armorPenetration = parseInt;
    }
    
    public int getArmorPenetration() {
        return this.armorPenetration;
    }
    
    public void setExplosionDamage(final int i) {
        this.explosionDamage = i;
    }
    
    public int getExplosionDamage() {
        return this.explosionDamage;
    }
    
    public String getFilename() {
        return this.fileName;
    }
    
    public void setFilename(final String string) {
        this.fileName = string;
    }
    
    public void setGunTypeByte(final byte b) {
        this.gunByte = b;
    }
    
    public byte getGunTypeByte() {
        return this.gunByte;
    }
    
    public void setAmmoTypeByte(final byte b) {
        this.ammoByte = b;
    }
    
    public byte getAmmoTypeByte() {
        return this.ammoByte;
    }
    
    public void setRecoil(final double d) {
        this.recoil = d;
    }
    
    public double getRecoil() {
        return this.recoil;
    }
    
    public void setKnockback(final double d) {
        this.knockback = d;
    }
    
    public double getKnockback() {
        return this.knockback;
    }
    
    public void addGunSounds(final String val) {
        final String[] sounds = val.split(",");
        for (int i = 0; i < sounds.length; ++i) {
            this.gunSound.add(sounds[i]);
        }
    }
    
    public int getReleaseTime() {
        return this.releaseTime;
    }
    
    public void setReleaseTime(final int v) {
        this.releaseTime = v;
    }
    
    public void setCanGoPastMaxDistance(final boolean parseBoolean) {
        this.canGoPastMaxDistance = parseBoolean;
    }
    
    public boolean canGoPastMaxDistance() {
        return this.canGoPastMaxDistance;
    }
    
    public void setGunVolume(final double parseDouble) {
        this.gunVolume = parseDouble;
    }
    
    public double getGunVolume() {
        return this.gunVolume;
    }
    
    public double getAccuracy() {
        return this.accuracy;
    }
    
    public double getAccuracy_aimed() {
        return this.accuracy_aimed;
    }
    
    public double getAccuracy_crouched() {
        return this.accuracy_crouched;
    }
}
