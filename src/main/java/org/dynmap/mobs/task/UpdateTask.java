package org.dynmap.mobs.task;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.dynmap.markers.Marker;
import org.dynmap.mobs.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UpdateTask implements Runnable {

    protected final MobGroup mobGroup;
    protected final DynmapMobsPlugin plugin;
    private final String singleMakerPrefix;
    private final Server server;
    private final Logger logger;

    private ArrayList<World> worldsToDo = null;
    private List<LivingEntity> mobsToDo = null;
    private int mobIndex = 0;
    private World curWorld = null;
    private final SparseArray<Marker> markerCache = new SparseArray<Marker>();
    private Method gethandle;
    protected int hideifshadow;
    protected int hideifundercover;
    protected double res;

    HashMap<String, Integer> lookupCache = new HashMap<String, Integer>();

    public UpdateTask(DynmapMobsPlugin plugin, MobGroup mobGroup) {
        this.mobGroup = mobGroup;
        this.plugin = plugin;
        server = plugin.getServer();
        gethandle = plugin.getGethandle();
        singleMakerPrefix = mobGroup.getSingleMakerPrefix();
        logger = plugin.getLogger();
    }

    @Override
    public final void run() {
        if (Utils.DEBUG) {
            logger.log(Level.INFO, "start run | Thread: [" + Thread.currentThread().getName() + "] id: " + Thread.currentThread().getId());
        }
        while (true) {
            boolean result = process();
            if (Utils.DEBUG) {
                logger.log(Level.INFO, "process result: " + result);
            }
            if (!result) {
                if (Utils.DEBUG) {
                    logger.log(Level.INFO, "plugin.isStop() | mobGroup.getSize() <= 0 || mobGroup.getMarkerSet() == nul, return");
                }
                return;
            }

            if (Utils.DEBUG) {
                logger.log(Level.INFO, "Task complete, wait for next Task, waiting time is: " + mobGroup.getUpdatePeriod());
            }

            try {
                Thread.sleep(mobGroup.getUpdatePeriod());
            } catch (InterruptedException e) {
            }
        }
    }

    private boolean process() {
        while (true) {
            if (plugin.isStop() | mobGroup.getSize() <= 0 || mobGroup.getMarkerSet() == null) {
                return false;
            }
            if (worldsToDo == null) {
                worldsToDo = new ArrayList<World>(server.getWorlds());
            }

            boolean result = updateMobsToDo();
            if (!result) {
                return true;
            }

            if (Utils.DEBUG) {
                logger.log(Level.INFO, "update mobs todo complete: " + mobsToDo);
            }

            hideifshadow = plugin.getHideifshadow();
            hideifundercover = plugin.getHideifundercover();
            res = plugin.getResolution();
            int updatesPerTick = mobGroup.getUpdatesPerTick();
            // Process up to limit per tick

            final int size = mobGroup.getSize();
            for (int cnt = 0; cnt < updatesPerTick; cnt++) {
                if (mobIndex >= mobsToDo.size()) {
                    mobsToDo = null;
                    break;
                }
                // Get next entity
                LivingEntity le = mobsToDo.get(mobIndex);
                mobIndex++;
                int index = getBaseEntryIndex(le);
                if (index >= size) {
                    continue;
                }
                MarkerData data = getMarkerData(le, index);
                if (data == null) {
                    continue;
                }

                result = fillMarkData(le, data);
                if (!result) {
                    if (Utils.DEBUG) {
                        logger.log(Level.INFO, "fill marker data fail, continue....");
                    }

                    continue;
                }
                server.getScheduler().scheduleSyncDelayedTask(plugin, new PutOnMapTask(data), 0);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    protected MarkerData getMarkerData(LivingEntity le, int baseIndex) {
        MobMapping mob = mobGroup.getMobAt(baseIndex);
        MarkerData data = MarkerData.obtain();
        data.label = mob.label;
        data.icon = mob.icon;
        return data;
    }

    private void putOnTheMap(MarkerData data) {

        final int entryId = data.entryId;
        final String label = data.label;
        final String worldName = data.worldName;

        Marker m = getCachedMarker(entryId);
        if (m == null) { /* Not found?  Need new one */
            m = mobGroup.getMarkerSet().createMarker(singleMakerPrefix + entryId, label, worldName, data.posX, data.posY, data.posZ, data.icon, false);
            if (Utils.DEBUG) {
                logger.log(Level.INFO, "not in the cache, create new one: " + m);
            }
        } else {  /* Else, update position if needed */
            if (Utils.DEBUG) {
                logger.log(Level.INFO, "in the cache, use cached: " + m);
            }
            m.setLocation(worldName, data.posX, data.posY, data.posZ);
            m.setLabel(label);
            m.setMarkerIcon(data.icon);
        }
        if (m != null) {
            cacheMarker(entryId, m);
        }
    }

    private boolean fillMarkData(LivingEntity le, MarkerData data) {
        data.worldName = curWorld.getName();
        data.entryId = le.getEntityId();


//        Block blk = null;

//        if (hideifshadow < 15) {
//            blk = loc.getBlock();
//            if (blk.getLightLevel() <= hideifshadow) {
//                return false;
//            }
//        }
//        if (hideifundercover < 15) {
//            if (blk == null) blk = loc.getBlock();
//            if (blk.getLightFromSky() <= hideifundercover) {
//                return false;
//            }
//        }
        /* See if we already have marker */
        Location loc = le.getLocation();
        double x = Math.round(loc.getX() / res) * res;
        double y = Math.round(loc.getY() / res) * res;
        double z = Math.round(loc.getZ() / res) * res;

        if (mobGroup.isNoLabels()) {
            data.label = "";
        } else if (mobGroup.isIncCoord()) {
            data.label = data.label + " [" + (int) x + "," + (int) y + "," + (int) z + "]";
        }
        data.posX = x;
        data.posY = y;
        data.posZ = z;
        return true;
    }

    private int getBaseEntryIndex(LivingEntity le) {
        String clsid = null;
        if (gethandle != null) {
            try {
                clsid = gethandle.invoke(le).getClass().getName();
            } catch (Exception x) {
            }
        }
        if (clsid == null) {
            clsid = le.getClass().getName();
        }
        Integer idx = lookupCache.get(clsid);
        MobMapping mob;
        int size = mobGroup.getSize();
        if (idx == null) {
            for (idx = 0; idx < size; idx++) {
                mob = mobGroup.getMobAt(idx);
                if ((mob.mobclass != null) && mob.mobclass.isInstance(le)) {
                    if (mob.entclsid == null) {
                        break;
                    } else if (gethandle != null) {
                        Object obcentity = null;
                        try {
                            obcentity = gethandle.invoke(le);
                        } catch (Exception x) {
                        }
                        if ((mob.entclass != null) && (obcentity != null) && (mob.entclass.isInstance(obcentity))) {
                            break;
                        }
                    }
                }
            }
            lookupCache.put(clsid, idx);
        }
        return idx;
    }

    private boolean updateMobsToDo() {
        while (mobsToDo == null) {
            if (worldsToDo.isEmpty()) {
                // And replace with new map
                worldsToDo = null;
                clearMarkerCacheIfNeeded();
                // Schedule next run
                return false;
            } else {
                curWorld = worldsToDo.remove(0); // Get next world
                mobsToDo = curWorld.getLivingEntities();     // Get living entities
                mobIndex = 0;
                if (mobsToDo != null && mobsToDo.isEmpty()) {
                    mobsToDo = null;
                }
            }
        }
        return true;
    }

    private void cacheMarker(int id, Marker marker) {
        if (Utils.DEBUG) {
            logger.log(Level.INFO, "cache marker: [" + id + "] " + marker );
        }
        synchronized (markerCache) {
            Marker older = markerCache.get(id);
            if (older != null && older != marker) {
                older.deleteMarker();
            }
            markerCache.put(id, marker);
        }
    }

    private Marker getCachedMarker(int id) {
        synchronized (markerCache) {
            return markerCache.get(id);
        }
    }

    private void clearMarkerCacheIfNeeded() {
        if (Utils.DEBUG) {
            logger.log(Level.INFO,"clearMarkerCache .......");
        }
        synchronized (markerCache) {
            int size = markerCache.size();
            if (size > 200) {
                Marker marker;
                for (int i = 0; i < size; i++) {
                    marker = markerCache.valueAt(i);
                    if (marker != null) {
                        marker.deleteMarker();
                    }
                }
                markerCache.clear();
            }
        }
    }

    private class PutOnMapTask implements Runnable {
        private final MarkerData data;

        private PutOnMapTask(MarkerData data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                putOnTheMap(data);
            } finally {
                if (data != null) {
                    data.recycle();
                }
            }
        }
    }


}
