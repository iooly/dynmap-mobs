package org.dynmap.mobs.task;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.dynmap.markers.Marker;
import org.dynmap.mobs.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateTask implements Runnable {
    protected final MobGroup mobGroup;
    protected final DynmapMobsPlugin plugin;
    private final String singleMakerPrefix;

    private Map<Integer, Marker> newmap = new HashMap<Integer, Marker>(); /* Build new map */
    private ArrayList<World> worldsToDo = null;
    private List<LivingEntity> mobsToDo = null;
    private int mobIndex = 0;
    private World curWorld = null;
    private Map<Integer, Marker> mobicons = new HashMap<Integer, Marker>();
    private Method gethandle;

    protected int hideifshadow;
    protected int hideifundercover;
    protected double res;

    HashMap<String, Integer> lookupCache = new HashMap<String, Integer>();

    public UpdateTask(DynmapMobsPlugin plugin, MobGroup mobGroup) {
        this.mobGroup = mobGroup;
        this.plugin = plugin;
        gethandle = plugin.getGethandle();
        singleMakerPrefix = mobGroup.getSingleMakerPrefix();
    }

    @Override
    public final void run() {
        if (plugin.isStop() | mobGroup.getSize() <= 0 || mobGroup.getMarkerSet() == null) {
            return;
        }
        if (worldsToDo == null) {
            worldsToDo = new ArrayList<World>(plugin.getServer().getWorlds());
        }
        updateMobsToDo();

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
            try {
                putOnTheMap(le, data);
            } finally {
                Utils.recycleMarkData(data);
            }
        }
    }

    protected MarkerData getMarkerData(LivingEntity le, int baseIndex) {
        MobMapping mob = mobGroup.getMobAt(baseIndex);
        MarkerData data = Utils.obtainMarkerData();
        data.label = mob.label;
        data.icon = mob.icon;
        return data;
    }

    private boolean putOnTheMap(LivingEntity le, MarkerData data) {
        String label = data.label;

        Location loc = le.getLocation();
        Block blk = null;

        if (hideifshadow < 15) {
            blk = loc.getBlock();
            if (blk.getLightLevel() <= hideifshadow) {
                return false;
            }
        }
        if (hideifundercover < 15) {
            if (blk == null) blk = loc.getBlock();
            if (blk.getLightFromSky() <= hideifundercover) {
                return false;
            }
        }
        /* See if we already have marker */
        double x = Math.round(loc.getX() / res) * res;
        double y = Math.round(loc.getY() / res) * res;
        double z = Math.round(loc.getZ() / res) * res;
        Marker m = mobicons.remove(le.getEntityId());
        if (mobGroup.isNoLabels()) {
            label = "";
        } else if (mobGroup.isIncCoord()) {
            label = label + " [" + (int) x + "," + (int) y + "," + (int) z + "]";
        }
        if (m == null) { /* Not found?  Need new one */
            m = mobGroup.getMarkerSet().createMarker(singleMakerPrefix + le.getEntityId(), label, curWorld.getName(), x, y, z, data.icon, false);
        } else {  /* Else, update position if needed */
            m.setLocation(curWorld.getName(), x, y, z);
            m.setLabel(label);
            m.setMarkerIcon(data.icon);
        }
        if (m != null) {
            newmap.put(le.getEntityId(), m);    /* Add to new map */
        }
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

    private void updateMobsToDo() {
        while (mobsToDo == null) {
            if (worldsToDo.isEmpty()) {
                // Now, review old map - anything left is gone
                for (Marker oldm : mobicons.values()) {
                    oldm.deleteMarker();
                }
                // And replace with new map
                mobicons = newmap;
                // Schedule next run
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdateTask(plugin, mobGroup), mobGroup.getUpdatePeriod());
                return;
            } else {
                curWorld = worldsToDo.remove(0); // Get next world
                mobsToDo = curWorld.getLivingEntities();     // Get living entities
                mobIndex = 0;
                if (mobsToDo != null && mobsToDo.isEmpty()) {
                    mobsToDo = null;
                }
            }
        }
    }


}
