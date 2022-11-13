package com.runescape.loginscreen.cinematic.camera;

import com.runescape.util.Vector3;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CameraFrameOLD {

    public CameraFrameOLD(Vector3 targetLocation, int rotation, int tilt, int frames,boolean moveScene) {
        this.moveScene = moveScene;
        this.targetLocation = targetLocation;
        this.rotation = rotation;
        this.tilt = tilt;
        this.frames = frames;
    }

    public CameraFrameOLD(Vector3 targetLocation, int rotation, int tilt, int frames) {
        this.targetLocation = targetLocation;
        this.rotation = rotation;
        this.tilt = tilt;
        this.frames = frames;
    }

    @Setter
    private boolean moveScene;

    private Vector3 targetLocation;
    private Vector3 jumpLocation;
    public int rotation = -1;
    public int tilt = -1;
    private int frames = 500;

    @Setter
    private int start;

    public boolean hasFrame(int frameNum) {
        return frameNum >= start && frameNum <= getEnd();
    }

    public int getEnd() {
        return start + frames;
    }

}