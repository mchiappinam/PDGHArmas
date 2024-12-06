package me.mchiappinam.pdgharmas;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Effect;

import me.mchiappinam.pdgharmas.gun.Bullet;
import me.mchiappinam.pdgharmas.gun.Gun;
import me.mchiappinam.pdgharmas.gun.GunPlayer;
import me.mchiappinam.pdgharmas.gun.WeaponReader;
import me.mchiappinam.pdgharmas.listeners.PluginEntityListener;
import me.mchiappinam.pdgharmas.listeners.PluginPlayerListener;

import java.io.File;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

public class PVPGunPlus extends JavaPlugin
{

	public HashMap<Integer, Integer> armasLimite = new HashMap<Integer, Integer>();
    private PluginPlayerListener playerListener;
    private PluginEntityListener entityListener;
    private ArrayList<Bullet> bullets;
    private ArrayList<Gun> loadedGuns;
    private ArrayList<GunPlayer> players;
    private String pluginName;
    public int UpdateTimer;
    public Random random;
    public static PVPGunPlus plugin;
    
    public PVPGunPlus() {
        this.playerListener = new PluginPlayerListener(this);
        this.entityListener = new PluginEntityListener(this);
        this.bullets = new ArrayList<Bullet>();
        this.loadedGuns = new ArrayList<Gun>();
        this.players = new ArrayList<GunPlayer>();
        this.pluginName = "PDGHArmas";
    }
    
    public void onDisable() {
        System.out.println(this.pluginName + " desativado");
        this.clearMemory(true);
    }
    
    public void onEnable() {
        System.out.println(this.pluginName + " ativado");
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents((Listener)this.playerListener, (Plugin)this);
        pm.registerEvents((Listener)this.entityListener, (Plugin)this);
        this.startup(true);
    }
    
    public void clearMemory(final boolean init) {
        this.getServer().getScheduler().cancelTask(this.UpdateTimer);
        for (int i = this.bullets.size() - 1; i >= 0; --i) {
            this.bullets.get(i).destroy();
        }
        for (int i = this.players.size() - 1; i >= 0; --i) {
            this.players.get(i).unload();
        }
        if (init) {
            this.loadedGuns.clear();
        }
        this.bullets.clear();
        this.players.clear();
    }
    
    public void startup(final boolean init) {
        this.UpdateTimer = this.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this, (Runnable)new UpdateTimer(), 20L, 1L);
        this.random = new Random();
        PVPGunPlus.plugin = this;
        final File dir = new File(this.getPluginFolder());
        if (!dir.exists()) {
            dir.mkdir();
        }
        File dir2 = new File(String.valueOf(this.getPluginFolder()) + "/guns");
        if (!dir2.exists()) {
            dir2.mkdir();
        }
        dir2 = new File(String.valueOf(this.getPluginFolder()) + "/projectile");
        if (!dir2.exists()) {
            dir2.mkdir();
        }
        if (init) {
            this.loadGuns();
            this.loadProjectile();
        }
        this.getOnlinePlayers();
		getServer().getConsoleSender().sendMessage("§3[PDGHArmasLimite] §2iniciando...");
		File file = new File(getDataFolder(),"config.yml");
		getServer().getConsoleSender().sendMessage("§3[PDGHArmasLimite] §2verificando se a config existe...");
		if(!file.exists()) {
			try {
				getServer().getConsoleSender().sendMessage("§3[PDGHArmasLimite] §2config inexistente, criando config...");
				saveResource("config_template.yml",false);
				File file2 = new File(getDataFolder(),"config_template.yml");
				file2.renameTo(new File(getDataFolder(),"config.yml"));
				getServer().getConsoleSender().sendMessage("§3[PDGHArmasLimite] §2config criada");
			}catch(Exception e) {getServer().getConsoleSender().sendMessage("§c[PDGHArmasLimite] §cERRO AO CRIAR CONFIG");}
		}
		int classe=1;
		for(String grupo : getConfig().getConfigurationSection("grupos").getKeys(false)) {
			for(Integer id : getConfig().getIntegerList("grupos."+grupo+".armas")) {
				armasLimite.put(id,classe);
			}
			classe++;
		}
		getServer().getConsoleSender().sendMessage("§3[PDGHArmasLimite] §2ativado - Developed by mchiappinam");
		getServer().getConsoleSender().sendMessage("§3[PDGHArmasLimite] §2Acesse: http://pdgh.com.br/");
		getServer().getConsoleSender().sendMessage("§3[PDGHArmasLimite] §2Acesse: https://hostload.com.br/");
    }
    
    private String getPluginFolder() {
        return this.getDataFolder().getAbsolutePath();
    }
    
    private void loadProjectile() {
        final String path = String.valueOf(this.getPluginFolder()) + "/projectile";
        final File dir = new File(path);
        final String[] children = dir.list();
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                final String filename = children[i];
                final WeaponReader f = new WeaponReader(this, new File(String.valueOf(path) + "/" + filename), "gun");
                if (f.loaded) {
                    f.ret.node = "pdgharmas." + filename.toLowerCase();
                    this.loadedGuns.add(f.ret);
                    f.ret.setIsThrowable(true);
                    System.out.println("Arma arremessável carregada - " + f.ret.getName());
                } else {
                  System.out.println("Falha ao carregar a arma arremessável - " + f.ret.getName());
                }
            }
        }
    }
    
    private void loadGuns() {
        final String path = String.valueOf(this.getPluginFolder()) + "/guns";
        final File dir = new File(path);
        final String[] children = dir.list();
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                final String filename = children[i];
                final WeaponReader f = new WeaponReader(this, new File(String.valueOf(path) + "/" + filename), "gun");
                if (f.loaded) {
                    f.ret.node = "pdgharmas." + filename.toLowerCase();
                    this.loadedGuns.add(f.ret);
                    System.out.println("Arma carregada - " + f.ret.getName());
                } else {
                  System.out.println("Falha ao carregar a arma " + f.ret.getName());
                }
            }
        }
    }
    
    public void reload(final boolean b) {
        this.clearMemory(b);
        this.startup(b);
    }
    
    public void reload() {
        this.reload(false);
    }
    
    public static void playEffect(final Effect e, final Location l, final int num) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            p.playEffect(l, e, num);
        }
    }
    
    public void getOnlinePlayers() {
	    for (Player p : Bukkit.getOnlinePlayers()) {
	        final GunPlayer g = new GunPlayer(this, p);
	        this.players.add(g);
	    }
    	/*Player[] plist = Bukkit.getOnlinePlayers();
        for (int i = 0; i < plist.length; ++i) {
            final GunPlayer g = new GunPlayer(this, plist[i]);
            this.players.add(g);
        }*/
    }
    
    public static PVPGunPlus getPlugin() {
        return PVPGunPlus.plugin;
    }
    
    public GunPlayer getGunPlayer(final Player player) {
        for (int i = this.players.size() - 1; i >= 0; --i) {
            if (this.players.get(i).getPlayer().equals(player)) {
                return this.players.get(i);
            }
        }
        return null;
    }
    
    public Gun getGun(final int typeId) {
        for (int i = this.loadedGuns.size() - 1; i >= 0; --i) {
            if (this.loadedGuns.get(i).getGunMaterial() != null && this.loadedGuns.get(i).getGunMaterial().getId() == typeId) {
                return this.loadedGuns.get(i);
            }
        }
        return null;
    }
    
    public Gun getGun(final String gunName) {
        for (int i = this.loadedGuns.size() - 1; i >= 0; --i) {
            if (this.loadedGuns.get(i).getName().toLowerCase().equals(gunName) || this.loadedGuns.get(i).getFilename().toLowerCase().equals(gunName)) {
                return this.loadedGuns.get(i);
            }
        }
        return null;
    }
    
    public void onJoin(final Player player) {
        if (this.getGunPlayer(player) == null) {
            final GunPlayer gp = new GunPlayer(this, player);
            this.players.add(gp);
        }
    }
    
    public void onQuit(final Player player) {
        for (int i = this.players.size() - 1; i >= 0; --i) {
            if (this.players.get(i).getPlayer().getName().equals(player.getName())) {
                this.players.remove(i);
            }
        }
    }
    
    public ArrayList<Gun> getLoadedGuns() {
        final ArrayList<Gun> ret = new ArrayList<Gun>();
        for (int i = this.loadedGuns.size() - 1; i >= 0; --i) {
            ret.add(this.loadedGuns.get(i).copy());
        }
        return ret;
    }
    
    public void removeBullet(final Bullet bullet) {
        this.bullets.remove(bullet);
    }
    
    public void addBullet(final Bullet bullet) {
        this.bullets.add(bullet);
    }
    
    public Bullet getBullet(final Entity proj) {
        for (int i = this.bullets.size() - 1; i >= 0; --i) {
            if (this.bullets.get(i).getProjectile().getEntityId() == proj.getEntityId()) {
                return this.bullets.get(i);
            }
        }
        return null;
    }
    
    public static Sound getSound(final String gunSound) {
        final String snd = gunSound.toUpperCase().replace(" ", "_");
        final Sound sound = Sound.valueOf(snd);
        return sound;
    }
    
    public ArrayList<Gun> getGunsByType(final ItemStack item) {
        final ArrayList<Gun> ret = new ArrayList<Gun>();
        for (int i = 0; i < this.loadedGuns.size(); ++i) {
            if (this.loadedGuns.get(i).getGunMaterial().equals((Object)item.getType())) {
                ret.add(this.loadedGuns.get(i));
            }
        }
        return ret;
    }
    
    class UpdateTimer implements Runnable
    {
        @Override
        public void run() {
            for (int i = PVPGunPlus.this.players.size() - 1; i >= 0; --i) {
                final GunPlayer gp = PVPGunPlus.this.players.get(i);
                if (gp != null) {
                    gp.tick();
                }
            }
            for (int i = PVPGunPlus.this.bullets.size() - 1; i >= 0; --i) {
                final Bullet t = PVPGunPlus.this.bullets.get(i);
                if (t != null) {
                    t.tick();
                }
            }
        }
    }
}
