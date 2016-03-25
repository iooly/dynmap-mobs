package org.dynmap.mobs;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.dynmap.markers.Marker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateTask implements Runnable {

    private final MobGroup mobGroup;
    private final DynmapMobsPlugin plugin;

    private Map<Integer, Marker> newmap = new HashMap<Integer, Marker>(); /* Build new map */
    private ArrayList<World> worldsToDo = null;
    private List<LivingEntity> mobsToDo = null;
    private int mobIndex = 0;
    private World curWorld = null;
    private Map<Integer, Marker> mobicons = new HashMap<Integer, Marker>();
    private Method gethandle;

    HashMap<String, Integer> lookup_cache = new HashMap<String, Integer>();

    UpdateTask(DynmapMobsPlugin plugin, MobGroup mobGroup) {
        this.mobGroup = mobGroup;
        this.plugin = plugin;
        gethandle = plugin.getGethandle();
    }

    @Override
    public void run() {
        if (plugin.isStop() | mobGroup.getSize() <= 0 || mobGroup.getMarkerSet() == null) {
            return;
        }
        // If needed, prime world list
        if (worldsToDo == null) {
            worldsToDo = new ArrayList<World>(plugin.getServer().getWorlds());
        }
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
                if ((mobsToDo != null) && mobsToDo.isEmpty()) {
                    mobsToDo = null;
                }
            }
        }

        int hideifshadow = plugin.getHideifshadow();
        int hideifundercover = plugin.getHideifundercover();
        double res = plugin.getResolution();

        // Process up to limit per tick
        for (int cnt = 0; cnt < mobGroup.getUpdatesPerTick(); cnt++) {
            if (mobIndex >= mobsToDo.size()) {
                mobsToDo = null;
                break;
            }
            // Get next entity
            LivingEntity le = mobsToDo.get(mobIndex);
            mobIndex++;

            int i;

                /* See if entity is mob we care about */
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
            Integer idx = lookup_cache.get(clsid);
            MobMapping mob;
            int size = mobGroup.getSize();
            if (idx == null) {

                for (i = 0; i < size; i++) {
                    mob = mobGroup.getMobAt(i);
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
                lookup_cache.put(clsid, i);
            } else {
                i = idx;
            }
            if (i >= size) {
                continue;
            }
            mob = mobGroup.getMobAt(i);

            String label = null;
            if (mob.mobid.equals("spider")) {    /* Check for jockey */
                if (le.getPassenger() != null) { /* Has passenger? */
                    i = findNext(i, "spiderjockey");    /* Make jockey */
                }
            } else if (mob.mobid.equals("wolf")) { /* Check for tamed wolf */
                Wolf wolf = (Wolf) le;
                if (wolf.isTamed()) {
                    i = findNext(i, "tamedwolf");
                    AnimalTamer t = wolf.getOwner();
                    if ((t != null) && (t instanceof OfflinePlayer)) {
                        label = "Wolf (" + ((OfflinePlayer) t).getName() + ")";
                    }
                }
            } else if (mob.mobid.equals("ocelot")) { /* Check for tamed ocelot */
                Ocelot cat = (Ocelot) le;
                if (cat.isTamed()) {
                    i = findNext(i, "cat");
                    AnimalTamer t = cat.getOwner();
                    if ((t != null) && (t instanceof OfflinePlayer)) {
                        label = "Cat (" + ((OfflinePlayer) t).getName() + ")";
                    }
                }
            } else if (mob.mobid.equals("zombie")) {
                Zombie zom = (Zombie) le;
                if (zom.isVillager()) {
                    i = findNext(i, "zombievilager");   /* Make in to zombie villager */
                }
            } else if (mob.mobid.equals("skeleton")) {
                Skeleton sk = (Skeleton) le;
                if (sk.getSkeletonType() == Skeleton.SkeletonType.WITHER) {
                    i = findNext(i, "witherskeleton");    /* Make in to wither skeleton */
                }
            } else if (mob.mobid.equals("villager")) {
                Villager v = (Villager) le;
                Villager.Profession p = v.getProfession();
                if (p != null) {
                    switch (p) {
                        case BLACKSMITH:
                            label = "Blacksmith";
                            break;
                        case BUTCHER:
                            label = "Butcher";
                            break;
                        case FARMER:
                            label = "Farmer";
                            break;
                        case LIBRARIAN:
                            label = "Librarian";
                            break;
                        case PRIEST:
                            label = "Priest";
                            break;
                    }
                }
            } else if (mob.mobid.equals("vanillahorse")) {    /* Check for rider */
                Horse h = (Horse) le;
                Horse.Variant hv = h.getVariant();
                switch (hv) {
                    case DONKEY:
                        label = "Donkey";
                        break;
                    case MULE:
                        label = "Mule";
                        break;
                    case UNDEAD_HORSE:
                        label = "Undead Horse";
                        break;
                    case SKELETON_HORSE:
                        label = "Skeleton Horse";
                        break;
                    default:
                        break;
                }
                if (le.getPassenger() != null) { /* Has passenger? */
                    Entity e = le.getPassenger();
                    if (e instanceof Player) {
                        label = label + " (" + ((Player) e).getName() + ")";
                    }
                }
            }
            if (i >= size) {
                continue;
            }
            mob = mobGroup.getMobAt(i);

            if (label == null) {
                label = mob.label;
            }
            Location loc = le.getLocation();
            Block blk = null;


            if (hideifshadow < 15) {
                blk = loc.getBlock();
                if (blk.getLightLevel() <= hideifshadow) {
                    continue;
                }
            }
            if (hideifundercover < 15) {
                if (blk == null) blk = loc.getBlock();
                if (blk.getLightFromSky() <= hideifundercover) {
                    continue;
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
                m = mobGroup.getMarkerSet().createMarker("mob" + le.getEntityId(), label, curWorld.getName(), x, y, z, mob.icon, false);
            } else {  /* Else, update position if needed */
                m.setLocation(curWorld.getName(), x, y, z);
                m.setLabel(label);
                m.setMarkerIcon(mob.icon);
            }
            if (m != null) {
                newmap.put(le.getEntityId(), m);    /* Add to new map */
            }
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 1);
    }

    private int findNext(int idx, String mobid) {
        idx++;
        int size = mobGroup.getSize();
        if ((idx < size) && mobGroup.getMobAt(idx).mobid.equals(mobid)) {
            return idx;
        } else {
            return size;
        }
    }
}
