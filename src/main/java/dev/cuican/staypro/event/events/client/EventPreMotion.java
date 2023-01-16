package dev.cuican.staypro.event.events.client;


import dev.cuican.staypro.event.StayEvent;

public class EventPreMotion extends StayEvent {

    public float getYaw() {
        return yaw;
    }

    float yaw;

    public float getPitch() {
        return pitch;
    }

    float pitch;
    public EventPreMotion(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }


}