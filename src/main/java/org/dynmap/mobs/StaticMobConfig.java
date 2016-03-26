package org.dynmap.mobs;

public class StaticMobConfig {

    public final String tinyIconsKey;
    public final String markerSetKey;
    public final String layerNameKey;
    public final String defaultLayerName;
    public final String layerPriorityKey;
    public final String hideByDefaultKey;
    public final String minZoomKey;
    public final String noLabelsKey;
    public final String incCoordKey;
    public final String updatePeriodKey;
    public final String updatesPerTickKey;

    public StaticMobConfig(String tinyIconsKey,
                           String markerSetKey,
                           String layerNameKey,
                           String defaultLayerName,
                           String layerPriorityKey,
                           String hideByDefaultKey,
                           String minZoomKey,
                           String noLabelsKey,
                           String incCoordKey,
                           String updatePeriodKey,
                           String updatesPerTickKey) {
        this.tinyIconsKey = tinyIconsKey;
        this.markerSetKey = markerSetKey;
        this.layerNameKey = layerNameKey;
        this.defaultLayerName = defaultLayerName;
        this.layerPriorityKey = layerPriorityKey;
        this.hideByDefaultKey = hideByDefaultKey;
        this.minZoomKey = minZoomKey;
        this.noLabelsKey = noLabelsKey;
        this.incCoordKey = incCoordKey;
        this.updatePeriodKey = updatePeriodKey;
        this.updatesPerTickKey = updatesPerTickKey;
    }


}
