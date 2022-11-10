package com.runescape.loginscreen.cinematic;

import com.runescape.cache.ResourceProvider;
import com.runescape.scene.MapRegion;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MapRegionData {

    private int regionX, regionY, hash;
    private int landscape, objects;
    private byte[] landscapeData, objectsData;

    public void requestFiles(ResourceProvider resourceProvider) {
        resourceProvider.provide(3, objects);
        resourceProvider.provide(3, landscape);

    }

    public void setObjectsData(byte[] objectsData, ResourceProvider rp) {
        this.objectsData = objectsData;
        MapRegion.requestModelPreload(objectsData, rp);
    }
}