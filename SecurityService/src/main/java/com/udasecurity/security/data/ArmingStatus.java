package com.udasecurity.security.data;

import java.awt.*;

/**
 * List of potential states the security system can use to describe how the system is armed.
 * Also contains metadata about what text and color is associated with the arming status.
 */
public enum ArmingStatus {
    DISARMED("Disarmed", new Color(102,204,0)),
    ARMED_HOME("Armed - At Home", new Color(51,153,255)),
    ARMED_AWAY("Armed - Away", new Color(255,102,178));

    private final String description;
    private final Color color;

    ArmingStatus(String description, Color color) {
        this.description = description;
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }
}
