package com.udasecurity.security.service;

import com.udasecurity.image.service.ImageService;
import com.udasecurity.security.application.StatusListener;
import com.udasecurity.security.data.AlarmStatus;
import com.udasecurity.security.data.ArmingStatus;
import com.udasecurity.security.data.SecurityRepository;
import com.udasecurity.security.data.Sensor;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Core security system controller managing alarm states, sensor status,
 * and image processing. Orchestrates system behavior based on inputs
 * from sensors and user commands.
 */
public class SecurityService {

    private static final float CAT_DETECTION_THRESHOLD = 50.0f;

    private final SecurityRepository securityRepository;
    private final ImageService imageService;
    private final Set<StatusListener> statusListeners = new HashSet<>();
    private boolean catDetected;

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Updates system arming status and handles related state transitions
     * @param newStatus Target arming status for the system
     */
    public void setArmingStatus(ArmingStatus newStatus) {
        if (newStatus == ArmingStatus.DISARMED) {
            handleSystemDisarming();
        } else {
            handleSystemArming(newStatus);
        }
        securityRepository.setArmingStatus(newStatus);
        notifyStatusListeners();
    }

    private void handleSystemDisarming() {
        setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    private void handleSystemArming(ArmingStatus status) {
        deactivateAllSensors();
        evaluateAlarmStateOnArming(status);
    }

    private void deactivateAllSensors() {
        getSensors().forEach(sensor -> {
            sensor.setActive(false);
            securityRepository.updateSensor(sensor);
        });
    }

    private void evaluateAlarmStateOnArming(ArmingStatus status) {
        if (status == ArmingStatus.ARMED_HOME && catDetected) {
            setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    /**
     * Processes sensor activation state changes and updates system status
     * @param sensor Affected sensor
     * @param active New activation state
     */
    public void changeSensorActivationStatus(Sensor sensor, boolean active) {
        if (sensor.getActive() == active) return;

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);

        if (active) {
            handleSensorActivation();
        } else {
            handleSensorDeactivation();
        }
    }

    private void handleSensorActivation() {
        switch (getAlarmStatus()) {
            case NO_ALARM:
                setAlarmStatus(AlarmStatus.PENDING_ALARM);
                break;
            case PENDING_ALARM:
                evaluatePendingAlarmState();
                break;
        }
    }

    private void evaluatePendingAlarmState() {
        if (anyActiveSensors()) {
            setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    private void handleSensorDeactivation() {
        if (getAlarmStatus() == AlarmStatus.PENDING_ALARM && noActiveSensors()) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }

    /**
     * Analyzes camera image for feline presence and updates system state
     * @param cameraImage Image frame from security camera
     */
    public void processImage(BufferedImage cameraImage) {
        boolean felineDetected = imageService.imageContainsCat(cameraImage, CAT_DETECTION_THRESHOLD);
        updateFelineDetectionState(felineDetected);
    }

    private void updateFelineDetectionState(boolean detected) {
        catDetected = detected;

        if (detected && armedHome()) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else if (noActiveSensors()) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }

        notifyFelineDetection(detected);
    }

    // Status listener management
    public void addStatusListener(StatusListener listener) {
        statusListeners.add(listener);
    }

    public void removeStatusListener(StatusListener listener) {
        statusListeners.remove(listener);
    }

    // Alarm state management
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        notifyAlarmStateChange(status);
    }

    // Notification methods
    private void notifyStatusListeners() {
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    private void notifyAlarmStateChange(AlarmStatus status) {
        statusListeners.forEach(listener -> listener.notify(status));
    }

    private void notifyFelineDetection(boolean detected) {
        statusListeners.forEach(listener -> listener.catDetected(detected));
    }

    // Utility methods
    private boolean armedHome() {
        return getArmingStatus() == ArmingStatus.ARMED_HOME;
    }

    private boolean anyActiveSensors() {
        return getSensors().stream().anyMatch(Sensor::getActive);
    }

    private boolean noActiveSensors() {
        return !anyActiveSensors();
    }

    // Repository delegates
    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return Collections.unmodifiableSet(securityRepository.getSensors());
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}