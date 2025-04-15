package com.udasecurity.security.application;

import com.udasecurity.security.data.Sensor;
import com.udasecurity.security.data.SensorType;
import com.udasecurity.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

/**
 * Panel that allows users to manage sensors in their security system.
 * Users can add new sensors, activate/deactivate existing sensors,
 * and remove sensors as needed.
 */
public class SensorPanel extends JPanel {
    private static final int MAX_FREE_SENSORS = 4;
    private static final String PREMIUM_MESSAGE = "To add more than 4 sensors, please subscribe to our Premium Membership!";
    private static final String PANEL_TITLE = "Sensor Management";

    private final SecurityService securityService;
    private final JPanel sensorListPanel;

    private final JTextField sensorNameField;
    private final JComboBox<SensorType> sensorTypeDropdown;

    /**
     * Constructs a new SensorPanel with the provided security service.
     *
     * @param securityService The security service to manage sensors
     */
    public SensorPanel(SecurityService securityService) {
        super();
        if (securityService == null) {
            throw new IllegalArgumentException("Security service cannot be null");
        }

        this.securityService = securityService;
        setLayout(new MigLayout());

        // Initialize fields
        sensorNameField = new JTextField();
        sensorTypeDropdown = new JComboBox<>(SensorType.values());

        // Create components
        JLabel titleLabel = createTitleLabel();
        JPanel addSensorPanel = createAddSensorPanel();
        sensorListPanel = createSensorListPanel();

        // Layout components
        add(titleLabel, "wrap");
        add(addSensorPanel, "span, wrap");
        add(sensorListPanel, "span, grow");

        // Initial sensor list population
        refreshSensorList();
    }

    /**
     * Creates and configures the panel title label.
     *
     * @return The configured title label
     */
    private JLabel createTitleLabel() {
        JLabel label = new JLabel(PANEL_TITLE);
        label.setFont(StyleService.HEADING_FONT);
        return label;
    }

    /**
     * Creates the panel for adding new sensors to the system.
     *
     * @return The configured add sensor panel
     */
    private JPanel createAddSensorPanel() {
        JPanel panel = new JPanel(new MigLayout());

        JLabel nameLabel = new JLabel("Name:");
        JLabel typeLabel = new JLabel("Sensor Type:");
        JButton addButton = new JButton("Add New Sensor");

        // Configure field sizes
        sensorNameField.setPreferredSize(new Dimension(150, sensorNameField.getPreferredSize().height));

        // Configure add button action
        addButton.addActionListener(e -> handleAddSensor());

        // Layout components
        panel.add(nameLabel);
        panel.add(sensorNameField, "width 50:100:200");
        panel.add(typeLabel);
        panel.add(sensorTypeDropdown, "wrap");
        panel.add(addButton, "span, center");

        return panel;
    }

    /**
     * Creates the panel that will display the list of sensors.
     *
     * @return The configured sensor list panel
     */
    private JPanel createSensorListPanel() {
        return new JPanel(new MigLayout("fillx"));
    }

    /**
     * Handles the addition of a new sensor to the system.
     */
    private void handleAddSensor() {
        String sensorName = sensorNameField.getText().trim();

        // Validate sensor name
        if (sensorName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Sensor name cannot be empty",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check sensor limit
        if (securityService.getSensors().size() >= MAX_FREE_SENSORS) {
            JOptionPane.showMessageDialog(this,
                    PREMIUM_MESSAGE,
                    "Sensor Limit Reached",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create and add the sensor
        SensorType selectedType = (SensorType) sensorTypeDropdown.getSelectedItem();
        Sensor newSensor = new Sensor(sensorName, selectedType);
        securityService.addSensor(newSensor);

        // Reset the form and refresh the display
        sensorNameField.setText("");
        refreshSensorList();
    }

    /**
     * Refreshes the sensor list display with current data.
     */
    private void refreshSensorList() {
        sensorListPanel.removeAll();

        securityService.getSensors().stream()
                .sorted(Comparator.comparing(Sensor::getName))
                .forEach(sensor -> addSensorToPanel(sensor, sensorListPanel));

        // Ensure the panel updates visually
        sensorListPanel.revalidate();
        sensorListPanel.repaint();
    }

    /**
     * Adds a single sensor representation to the provided panel.
     *
     * @param sensor The sensor to display
     * @param panel The panel to add the sensor to
     */
    private void addSensorToPanel(Sensor sensor, JPanel panel) {
        // Create sensor display components
        String labelText = String.format("%s (%s): %s",
                sensor.getName(),
                sensor.getSensorType(),
                sensor.getActive() ? "Active" : "Inactive");

        JLabel sensorLabel = new JLabel(labelText);

        JButton toggleButton = new JButton(sensor.getActive() ? "Deactivate" : "Activate");
        toggleButton.addActionListener(e -> toggleSensorStatus(sensor));

        JButton removeButton = new JButton("Remove Sensor");
        removeButton.addActionListener(e -> removeSensor(sensor));

        // Add components to panel with consistent sizing
        panel.add(sensorLabel, "width 300:300:300");
        panel.add(toggleButton, "width 100:100:100");
        panel.add(removeButton, "wrap");
    }

    /**
     * Toggles a sensor's active status.
     *
     * @param sensor The sensor to toggle
     */
    private void toggleSensorStatus(Sensor sensor) {
        securityService.changeSensorActivationStatus(sensor, !sensor.getActive());
        refreshSensorList();
    }

    /**
     * Removes a sensor from the system.
     *
     * @param sensor The sensor to remove
     */
    private void removeSensor(Sensor sensor) {
        securityService.removeSensor(sensor);
        refreshSensorList();
    }
}