package com.runescape.loginscreen.cinematic.camera;
import com.runescape.util.Vector3;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Camera {

    public Camera(Vector3 position, double rotation, double tilt) {
        this.position = position;
        this.rotation = rotation;
        this.tilt = tilt;
    }

    private Vector3 position;
    public double rotation, tilt;

    public Camera copy() {
        return new Camera(this.position,this.rotation,this.tilt);
    }

    public int getRotation() {
        return (int) Math.ceil(rotation);
    }

    public int getTilt() {
        return (int) Math.ceil(tilt);
    }

}
