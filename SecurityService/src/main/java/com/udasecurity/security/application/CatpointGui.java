package com.udasecurity.security.application;

import com.udasecurity.image.service.FakeImageService;
import com.udasecurity.image.service.ImageService;
import com.udasecurity.security.data.PretendDatabaseSecurityRepositoryImpl;
import com.udasecurity.security.data.SecurityRepository;
import com.udasecurity.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;

public class CatpointGui extends JFrame {
    private static final String APP_TITLE = "Secure Monitoring System";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 850;
    private static final int WINDOW_X = 100;
    private static final int WINDOW_Y = 100;

    // Initialize dependencies first
    private final SecurityRepository securityRepository = new PretendDatabaseSecurityRepositoryImpl();
    private final ImageService imageService = new FakeImageService();
    private final SecurityService securityService = new SecurityService(securityRepository, imageService);

    // UI components initialized after dependencies
    private final DisplayPanel displayPanel = new DisplayPanel(securityService);
    private final ImagePanel imagePanel = new ImagePanel(securityService);
    private final ControlPanel controlPanel = new ControlPanel(securityService);
    private final SensorPanel sensorPanel = new SensorPanel(securityService);

    public CatpointGui() {
        configureWindowSettings();
        initializeGuiComponents();
        setVisible(true);
    }

    private void configureWindowSettings() {
        setTitle(APP_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocation(WINDOW_X, WINDOW_Y);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void initializeGuiComponents() {
        JPanel mainPanel = new JPanel(new MigLayout());
        mainPanel.add(displayPanel, "wrap");
        mainPanel.add(imagePanel, "wrap");
        mainPanel.add(controlPanel, "wrap");
        mainPanel.add(sensorPanel);
        getContentPane().add(mainPanel);
    }


}