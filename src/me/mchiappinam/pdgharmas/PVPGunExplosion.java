package me.mchiappinam.pdgharmas;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;

import java.util.ArrayList;
import org.bukkit.World;
import org.bukkit.FireworkEffect;
import java.util.Random;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.entity.Firework;
import org.bukkit.Location;

public class PVPGunExplosion
{
    private Location location;
    
    public PVPGunExplosion(final Location location) {
        this.location = location;
    }
    
    public void explode() {
        World world = location.getWorld();
        world.playEffect(location , Effect.FIREWORKS_SPARK, 2);
        world.playEffect(location , Effect.MOBSPAWNER_FLAMES, 2);
        final ArrayList<Color> c = new ArrayList<Color>();
        c.add(Color.RED);
        c.add(Color.RED);
        c.add(Color.RED);
        c.add(Color.ORANGE);
        c.add(Color.ORANGE);
        c.add(Color.ORANGE);
        c.add(Color.BLACK);
        c.add(Color.GRAY);
        FireworkEffect effect = FireworkEffect.builder().trail(false).flicker(false).withColor((Iterable)c).withFade((Iterable)c).with(FireworkEffect.Type.BALL_LARGE).build();
        Firework fw = world.spawn(location, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.clearEffects();
        meta.addEffect(effect);
        meta.setPower(1);
        fw.setFireworkMeta(meta);
        //fw.detonate();
        fw.playEffect(EntityEffect.FIREWORK_EXPLODE);
    }
    
    /**public ItemStack getFirework() {
        final Random rand = new Random();
        FireworkEffect.Type type = FireworkEffect.Type.BALL_LARGE;
        if (rand.nextInt(2) == 0) {
            type = FireworkEffect.Type.BURST;
        }
        final ItemStack i = new ItemStack(Material.FIREWORK, 1);
        final FireworkMeta fm = (FireworkMeta)i.getItemMeta();
        final ArrayList<Color> c = new ArrayList<Color>();
        c.add(Color.RED);
        c.add(Color.RED);
        c.add(Color.RED);
        c.add(Color.ORANGE);
        c.add(Color.ORANGE);
        c.add(Color.ORANGE);
        c.add(Color.BLACK);
        c.add(Color.GRAY);
        final FireworkEffect e = FireworkEffect.builder().flicker(true).withColor((Iterable)c).withFade((Iterable)c).with(type).trail(true).build();
        fm.addEffect(e);
        fm.setPower(3);
        i.setItemMeta((ItemMeta)fm);
        return i;
    }*/
}
