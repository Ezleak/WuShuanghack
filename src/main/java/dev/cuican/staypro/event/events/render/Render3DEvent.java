package dev.cuican.staypro.event.events.render;


import dev.cuican.staypro.event.StayEvent;

public class Render3DEvent
        extends StayEvent {
    private final float partialTicks;

    public Render3DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}

