package com.udasecurity.security.application;

import com.udasecurity.security.data.AlarmStatus;

/**
 * Interface for components that need to be notified about system status changes.
 * Implementing classes will receive notifications when alarm status changes,
 * when cats are detected in images, or when sensor status changes.
 */
public interface StatusListener {

    /**
     * Called when the system's alarm status changes.
     *
     * @param status The new alarm status
     */
    void notify(AlarmStatus status);

    /**
     * Called when a cat detection status changes in the system.
     *
     * @param catDetected true if a cat was detected, false otherwise
     */
    void catDetected(boolean catDetected);

    /**
     * Called when any sensor's status changes in the system.
     */
    void sensorStatusChanged();
}