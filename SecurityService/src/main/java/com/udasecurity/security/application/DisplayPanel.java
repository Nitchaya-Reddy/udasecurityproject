package com.udasecurity.security.application;

import com.udasecurity.security.data.AlarmStatus;
import com.udasecurity.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;

/**
 * Real-time system status display panel with visual feedback.
 * Implements observer pattern for status updates.
 */
public class DisplayPanel extends JPanel implements StatusListener {

    private static final String PANEL_TITLE = "Secure Home Defense System";
    private static final String STATUS_PREFIX = "Security Status: ";
    private static final String LAYOUT_CONSTRAINTS = "span 2, wrap";

    private final JLabel statusIndicator;

    public DisplayPanel(SecurityService securityService) {
        super(new MigLayout());
        this.statusIndicator = createStatusLabel();

        configurePanelComponents(securityService);
        registerAsObserver(securityService);
    }

    private void configurePanelComponents(SecurityService service) {
        add(createHeaderLabel(), LAYOUT_CONSTRAINTS);
        add(createStatusDescriptor(), LAYOUT_CONSTRAINTS);
        add(statusIndicator);
        updateStatusDisplay(service.getAlarmStatus());
    }

    private JLabel createHeaderLabel() {
        JLabel header = new JLabel(PANEL_TITLE);
        header.setFont(StyleService.HEADING_FONT);
        return header;
    }

    private JLabel createStatusDescriptor() {
        return new JLabel(STATUS_PREFIX);
    }

    private JLabel createStatusLabel() {
        JLabel label = new JLabel();
        label.setOpaque(true);
        return label;
    }

    private void registerAsObserver(SecurityService service) {
        service.addStatusListener(this);
    }

    @Override
    public void notify(AlarmStatus currentStatus) {
        updateStatusDisplay(currentStatus);
    }

    private void updateStatusDisplay(AlarmStatus status) {
        statusIndicator.setText(status.getDescription());
        statusIndicator.setBackground(status.getColor());
        repaint();
    }

    @Override
    public void catDetected(boolean detected) { /* Status listener contract */ }

    @Override
    public void sensorStatusChanged() { /* Status listener contract */ }
}