package org.dynmap.mobs;

import org.bukkit.entity.Entity;
import org.dynmap.markers.MarkerIcon;

/* Mapping of mobs to icons */
public class MobMapping {
    public String mobid;
    public boolean enabled;
    public Class<Entity> mobclass;
    public Class<?> entclass;
    public String cls_id;
    public String entclsid;
    public String label;
    public MarkerIcon icon;

    /*package*/ MobMapping(String id, String clsid, String lbl) {
        this(id, clsid, lbl, null);
    }

    @SuppressWarnings("unchecked")
    MobMapping(String id, String clsid, String lbl, String entclsid) {
        mobid = id;
        label = lbl;
        cls_id = clsid;
        this.entclsid = entclsid;
    }

    public void init() {
        try {
            mobclass = (Class<Entity>) Class.forName(Utils.mapClassName(cls_id));
        } catch (ClassNotFoundException cnfx) {
            mobclass = null;
        }
        try {
            if (entclsid != null) {
                entclass = Class.forName(Utils.mapClassName(entclsid));
            }
        } catch (ClassNotFoundException cnfx) {
            entclass = null;
        }
    }
}