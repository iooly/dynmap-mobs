package org.dynmap.mobs.task;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;
import org.dynmap.mobs.*;

public class MobUpdateTask extends UpdateTask {

    public MobUpdateTask(DynmapMobsPlugin plugin, MobGroup mobGroup) {
        super(plugin, mobGroup);
    }

    @Override
    protected MarkerData getMarkerData(LivingEntity le, int baseIndex) {
        String label = null;
        int i = baseIndex;
        int size = mobGroup.getSize();
        MobMapping mob = mobGroup.getMobAt(i);
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
            return null;
        }
        mob = mobGroup.getMobAt(i);
        if (label == null) {
            label = mob.label;
        }
        MarkerData data = Utils.obtainMarkerData();
        data.label = label;
        data.icon = mob.icon;
        return data;
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
