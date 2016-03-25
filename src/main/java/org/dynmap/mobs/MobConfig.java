package org.dynmap.mobs;

import org.dynmap.markers.MarkerIcon;
import org.dynmap.mobs.exception.ConfigIndexOutOfBoundsException;

public class MobConfig {
    private final String prefix;
    private final MobMapping[] config;
    private final int size;

    public final StaticMobConfig staticConfig;

    public MobConfig(String prefix, MobMapping[] config, StaticMobConfig staticConfig) {
        this.prefix = prefix;
        this.config = config;
        this.size = config.length;
        this.staticConfig = staticConfig;
    }

    public int size() {
        return size;
    }

    public String fullId(int index) {
        checkIndex(index);
        return prefix + config[index].mobid;
    }

    public String mobId(int index) {
        return config[index].mobid;
    }

    public void init(int index) {
        checkIndex(index);
        config[index].init();
    }

    public void enabled(int index, boolean enabled) {
        checkIndex(index);
        config[index].enabled = enabled;
    }

    public boolean enabled(int index) {
        checkIndex(index);
        return config[index].enabled;
    }

    public void icon(int index, MarkerIcon icon) {
        checkIndex(index);
        config[index].icon = icon;
    }

    public String label(int index) {
        checkIndex(index);
        return config[index].label;
    }

    public MobMapping get(int index) {
        checkIndex(index);
        return config[index];
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new ConfigIndexOutOfBoundsException("size is " + size + ", index is: " + index);
        }
    }
}
