package com.runescape.loginscreen.cinematic.camera;
import com.runescape.util.Vector3;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Camera {

    private Vector3 position;
    public double rotation, tilt;

    public Camera copy() {
        return Camera.builder()
           .position(this.position)
           .tilt(this.tilt)
           .rotation(this.rotation)
        .build();
    }

    public int getRotation() {
        return (int) Math.ceil(rotation);
    }

    public int getTilt() {
        return (int) Math.ceil(tilt);
    }

}
