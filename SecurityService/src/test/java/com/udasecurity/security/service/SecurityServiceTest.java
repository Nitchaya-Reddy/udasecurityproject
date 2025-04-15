package com.udasecurity.security.service;

import com.udasecurity.image.service.ImageService;
import com.udasecurity.security.data.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private SecurityRepository repository;

    @Mock
    private ImageService imageProcessor;

    @InjectMocks
    private SecurityService systemUnderTest;

    private Sensor mockSensor;

    @BeforeEach
    void initialize() {
        mockSensor = new Sensor("Main Door Sensor", SensorType.DOOR);
    }

    @Test
    void givenSystemIsArmed_whenSensorActivated_thenAlarmStatusBecomesPending() {
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        systemUnderTest.changeSensorActivationStatus(mockSensor, true);

        verify(repository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void givenSensorActivatedAndAlarmPending_whenSensorTriggeredAgain_thenAlarmIsTriggered() {
        Sensor entrySensor = new Sensor("Front Door", SensorType.DOOR);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(repository.getSensors()).thenReturn(Set.of(entrySensor));

        systemUnderTest.changeSensorActivationStatus(entrySensor, true);

        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void givenAlarmPending_whenAllSensorsDeactivated_thenAlarmIsCleared() {
        mockSensor.setActive(true);
        Set<Sensor> activeSensors = new HashSet<>();
        activeSensors.add(mockSensor);

        when(repository.getSensors()).thenReturn(activeSensors);
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        systemUnderTest.changeSensorActivationStatus(mockSensor, false);

        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void givenAlarmIsActive_whenSensorChangesState_thenAlarmRemainsUnchanged() {
        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        systemUnderTest.changeSensorActivationStatus(mockSensor, true);

        verify(repository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    void givenSensorAlreadyActive_whenActivatedAgainWhileAlarmPending_thenAlarmIsTriggered() {
        Sensor sensor = new Sensor("Window Sensor", SensorType.WINDOW);

        when(repository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(repository.getSensors()).thenReturn(Set.of(sensor));

        systemUnderTest.changeSensorActivationStatus(sensor, true);

        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void givenSensorAlreadyInactive_whenDeactivated_thenNoAlarmStateChange() {
        mockSensor.setActive(false);

        systemUnderTest.changeSensorActivationStatus(mockSensor, false);

        verify(repository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    void givenCatDetectedAndSystemArmedHome_thenAlarmIsTriggered() {
        when(repository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageProcessor.imageContainsCat(any(), anyFloat())).thenReturn(true);

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        systemUnderTest.processImage(image);

        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void givenNoCatDetectedAndAllSensorsInactive_thenAlarmIsCleared() {
        when(imageProcessor.imageContainsCat(any(), anyFloat())).thenReturn(false);
        when(repository.getSensors()).thenReturn(Collections.singleton(mockSensor));

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        systemUnderTest.processImage(image);

        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"ALARM", "PENDING_ALARM"})
    void givenSystemDisarmed_thenAlarmIsResetToNoAlarm(AlarmStatus currentStatus) {
        systemUnderTest.setArmingStatus(ArmingStatus.DISARMED);

        verify(repository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void whenSystemArmed_thenAllSensorsAreDeactivated(ArmingStatus newStatus) {
        mockSensor.setActive(true);
        Set<Sensor> sensorList = new HashSet<>();
        sensorList.add(mockSensor);

        when(repository.getSensors()).thenReturn(sensorList);

        systemUnderTest.setArmingStatus(newStatus);

        assertFalse(mockSensor.getActive());
        verify(repository).updateSensor(mockSensor);
    }

    @Test
    void givenCatDetected_whenSystemArmedAfterward_thenAlarmIsRaised() {
        when(repository.getArmingStatus())
                .thenReturn(ArmingStatus.DISARMED)
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(imageProcessor.imageContainsCat(any(), anyFloat())).thenReturn(true);

        BufferedImage catImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        systemUnderTest.processImage(catImage);

        systemUnderTest.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(repository).setAlarmStatus(AlarmStatus.ALARM);
    }
}
