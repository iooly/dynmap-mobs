package org.dynmap.mobs;

import org.dynmap.markers.MarkerIcon;

public class MarkerData {

    private static final Pools.Pool<MarkerData> MARKER_DATA_POOL = new Pools.SynchronizedPool<MarkerData>(50);

    public String label;
    public MarkerIcon icon;
    public int entryId;
    public String worldName;
    public double posX;
    public double posY;
    public double posZ;

    private MarkerData() {
    }

    public static MarkerData obtain() {
        MarkerData data = MARKER_DATA_POOL.acquire();
        return data == null ? new MarkerData() : data;
    }

    public void recycle() {
        icon = null;
        label = null;
        worldName = null;
        entryId = 0;
        posX = 0;
        posY = 0;
        posZ = 0;
        MARKER_DATA_POOL.release(this);
    }
}
