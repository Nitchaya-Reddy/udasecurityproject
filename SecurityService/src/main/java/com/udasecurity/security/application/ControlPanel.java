package com.udasecurity.security.application;

import com.udasecurity.security.data.ArmingStatus;
import com.udasecurity.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * Interactive control panel for managing system arming states with visual feedback
 */
public class ControlPanel extends JPanel {

    private static final String LAYOUT_CONSTRAINT = "span 3, wrap";
    private static final MigLayout PANEL_LAYOUT = new MigLayout();

    private final SecurityService securityService;
    private final Map<ArmingStatus, JButton> controlButtons;

    public ControlPanel(SecurityService securityService) {
        super(PANEL_LAYOUT);
        this.securityService = securityService;
        this.controlButtons = createControlButtons();

        initializeComponents();
        setupInitialState();
    }

    private void initializeComponents() {
        JLabel header = createPanelHeader();
        add(header, LAYOUT_CONSTRAINT);
        addButtonsToPanel();
    }

    private JLabel createPanelHeader() {
        JLabel label = new JLabel("System Controls");
        label.setFont(StyleService.HEADING_FONT);
        return label;
    }

    private Map<ArmingStatus, JButton> createControlButtons() {
        Map<ArmingStatus, JButton> buttons = new EnumMap<>(ArmingStatus.class);

        for (ArmingStatus status : ArmingStatus.values()) {
            JButton btn = new JButton(status.getDescription());
            btn.addActionListener(e -> handleArmingStatusChange(status));
            buttons.put(status, btn);
        }

        return buttons;
    }

    private void addButtonsToPanel() {
        for (ArmingStatus status : ArmingStatus.values()) {
            add(controlButtons.get(status));
        }
    }

    private void handleArmingStatusChange(ArmingStatus newStatus) {
        securityService.setArmingStatus(newStatus);
        updateButtonVisuals(newStatus);
    }

    private void updateButtonVisuals(ArmingStatus activeStatus) {
        controlButtons.forEach((status, button) ->
                button.setBackground(status == activeStatus ? status.getColor() : null));
    }

    private void setupInitialState() {
        ArmingStatus currentStatus = securityService.getArmingStatus();
        controlButtons.get(currentStatus).setBackground(currentStatus.getColor());
    }
}