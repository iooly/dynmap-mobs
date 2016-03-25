package org.dynmap.mobs;

import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.Configuration;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.dynmap.mobs.exception.ConfigIndexOutOfBoundsException;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MobGroup {
    private static Logger LOGGER = Logger.getLogger(MobGroup.class.getName());

    private MobMapping[] mobs;
    private int size;
    private final int type;
    private MarkerSet markerSet;

    private boolean tinyIcons;
    private boolean noLabels;
    private boolean incCoord;
    private long updatePeriod;
    private int updatesPerTick;

    private final MobConfig mobConfig;
    private final Configuration config;
    private final MarkerAPI markerapi;

    public MobGroup(int type, MobConfig mobConfig, Configuration config, MarkerAPI markerapi) {
        this.type = type;
        this.mobConfig = mobConfig;
        this.config = config;
        this.markerapi = markerapi;
    }

    public void update() {
        StaticMobConfig staticConfig = mobConfig.staticConfig;
        boolean tinyIcons = config.getBoolean(staticConfig.tinyIconsKey, false);
        int enabledSize = 0;
        int allSize = mobConfig.size();
        MarkerIcon icon;

        for (int i = 0; i < allSize; i++) {
            mobConfig.init(i);
            mobConfig.enabled(i, config.getBoolean(mobConfig.fullId(i), false));
            icon = markerapi.getMarkerIcon(mobConfig.fullId(i));

            InputStream in = null;
            try {
                in = getClass().getResourceAsStream((tinyIcons ? "/8x8/" : "/") + mobConfig.mobId(i) + ".png");
                if (in != null) {
                    if (icon == null) {
                        icon = markerapi.createMarkerIcon(mobConfig.fullId(i), mobConfig.label(i), in);
                    } else {
                        icon.setMarkerIconImage(in);
                    }
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
            if (icon == null) {
                icon = markerapi.getMarkerIcon(MarkerIcon.DEFAULT);
            }
            mobConfig.icon(i, icon);
            if (mobConfig.enabled(i)) {
                enabledSize++;
            }
        }
        size = enabledSize;
        mobs = new MobMapping[enabledSize];
        for (int i = 0, j = 0; i < allSize; i++) {
            if (mobConfig.enabled(i)) {
                mobs[j] = mobConfig.get(i);
                j++;
            }
        }
        if (mobs.length > 0) {
            MarkerSet set = markerapi.getMarkerSet("mobs.markerset");
            if (set == null)
                set = markerapi.createMarkerSet("mobs.markerset", config.getString("layer.name", "Mobs"), null, false);
            else
                set.setMarkerSetLabel(config.getString("layer.name", "Mobs"));
            if (set == null) {
                severe("Error creating marker set");
                return;
            }
            set.setLayerPriority(config.getInt("layer.layerprio", 10));
            set.setHideByDefault(config.getBoolean("layer.hidebydefault", false));
            int minzoom = config.getInt("layer.minzoom", 0);
            if (minzoom > 0) { /* Don't call if non-default - lets us work with pre-0.28 dynmap */
                set.setMinZoom(minzoom);
            }
            markerSet = set;
            tinyIcons = config.getBoolean("layer.tinyicons", false);
            noLabels = config.getBoolean("layer.nolabels", false);
            incCoord = config.getBoolean("layer.inc-coord", false);
            /* Set up update job - based on period */
            double per = config.getDouble("update.period", 5.0);
            if (per < 2.0) per = 2.0;
            updatePeriod = (long) (per * 20.0);
            updatesPerTick = config.getInt("update.mobs-per-tick", 20);
            info("Enable layer for mobs");
        } else {
            info("Layer for mobs disabled");
        }
    }

    private static void severe(String msg) {
        LOGGER.log(Level.SEVERE, msg);
    }

    private static void info(String msg) {
        LOGGER.log(Level.INFO, msg);
    }

    public void deleteMarkerSet() {
        markerSet.deleteMarkerSet();
        markerSet = null;
    }

    public int getSize() {
        return size;
    }

    public int getType() {
        return type;
    }

    public MarkerSet getMarkerSet() {
        return markerSet;
    }

    public boolean isTinyIcons() {
        return tinyIcons;
    }

    public boolean isNoLabels() {
        return noLabels;
    }

    public boolean isIncCoord() {
        return incCoord;
    }

    public long getUpdatePeriod() {
        return updatePeriod;
    }

    public int getUpdatesPerTick() {
        return updatesPerTick;
    }

    public MobMapping getMobAt(int index) {
        checkIndex(index);
        return mobs[index];
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new ConfigIndexOutOfBoundsException("size is " + size + ", index is: " + index);
        }
    }
}
