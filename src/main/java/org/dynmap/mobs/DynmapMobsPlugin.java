package org.dynmap.mobs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.mobs.task.MobUpdateTask;
import org.dynmap.mobs.task.VehicleUpdateTask;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DynmapMobsPlugin extends JavaPlugin {
    private static Logger log;
    Plugin dynmap;
    DynmapAPI api;
    MarkerAPI markerapi;

    MobGroup mMobsGroup;
    MobGroup mVehiclesGroupl;

    double resolution; /* Position resolution */

    boolean stop;
    boolean reload = false;

    Method gethandle;
    int hideifundercover;
    int hideifshadow;

    @Override
    public void onLoad() {
        log = this.getLogger();
    }

    public static void info(String msg) {
        log.log(Level.INFO, msg);
    }

    public static void severe(String msg) {
        log.log(Level.SEVERE, msg);
    }

    public synchronized boolean isStop() {
        return stop;
    }

    private synchronized void setStop(boolean stop) {
        this.stop = stop;
    }

    private class OurServerListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            Plugin p = event.getPlugin();
            String name = p.getDescription().getName();
            if (name.equals("dynmap")) {
                activate();
            }
        }
    }

    @Override
    public void onEnable() {
        info("initializing");
        PluginManager pm = getServer().getPluginManager();
        /* Get dynmap */
        dynmap = pm.getPlugin("dynmap");
        if (dynmap == null) {
            severe("Cannot find dynmap!");
            return;
        }
        api = (DynmapAPI) dynmap; /* Get API */

        getServer().getPluginManager().registerEvents(new OurServerListener(), this);        

        /* If enabled, activate */
        if (dynmap.isEnabled()) {
            activate();
        }
    }


    private void activate() {
        /* look up the getHandle method for CraftEntity */
        try {
            Class<?> cls = Class.forName(Utils.mapClassName("org.bukkit.craftbukkit.entity.CraftEntity"));
            gethandle = cls.getMethod("getHandle");
        } catch (ClassNotFoundException cnfx) {
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
        if (gethandle == null) {
            severe("Unable to locate CraftEntity.getHandle() - cannot process most Mo'Creatures mobs");
        }
        
        /* Now, get markers API */
        markerapi = api.getMarkerAPI();
        if (markerapi == null) {
            severe("Error loading Dynmap marker API!");
            return;
        }
        /* Load configuration */
        if (reload) {
            reloadConfig();
            reset();
        } else {
            reload = true;
        }
        this.saveDefaultConfig();
        FileConfiguration cfg = getConfig();
        cfg.options().copyDefaults(true);   /* Load defaults, if needed */
        this.saveConfig();  /* Save updates, if needed */

        if (mMobsGroup == null) {
            mMobsGroup = Utils.newMobGroup(Utils.TYPE_MOB, cfg, markerapi);
        }
        if (mVehiclesGroupl == null) {
            mVehiclesGroupl = Utils.newMobGroup(Utils.TYPE_VEHICLE, cfg, markerapi);
        }
        hideifshadow = cfg.getInt(Keys.HIDE_IF_SHADOW, 15);
        hideifundercover = cfg.getInt(Keys.HIDE_IF_UNDER_COVER, 15);
        resolution = cfg.getDouble(Keys.UPDATE_RESOLUTION, 1.0);
        setStop(false);

        mMobsGroup.update();
        mVehiclesGroupl.update();

        getServer().getScheduler()
                .scheduleSyncDelayedTask(this, new MobUpdateTask(this, mMobsGroup), mMobsGroup.getUpdatePeriod());
        getServer().getScheduler()
                .scheduleSyncDelayedTask(this, new VehicleUpdateTask(this, mMobsGroup), mMobsGroup.getUpdatePeriod());

        info("version " + this.getDescription().getVersion() + " is activated");
    }

    @Override
    public void onDisable() {
        reset();
    }

    public Method getGethandle() {
        return gethandle;
    }

    public int getHideifundercover() {
        return hideifundercover;
    }

    public int getHideifshadow() {
        return hideifshadow;
    }

    public double getResolution() {
        return resolution;
    }

    private void reset() {
        if (mMobsGroup != null) {
            mMobsGroup.deleteMarkerSet();
        }
        if (mVehiclesGroupl != null) {
            mVehiclesGroupl.deleteMarkerSet();
        }
//        lookup_cache.clear();
        setStop(true);

    }

}
