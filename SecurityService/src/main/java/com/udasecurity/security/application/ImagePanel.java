package com.udasecurity.security.application;

import com.udasecurity.security.data.AlarmStatus;
import com.udasecurity.security.service.SecurityService;

import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Panel containing the camera display interface.
 * Allows users to refresh the camera by uploading images and scan images for analysis.
 */
public class ImagePanel extends JPanel implements StatusListener {
    private static final Logger LOGGER = Logger.getLogger(ImagePanel.class.getName());
    private static final int IMAGE_WIDTH = 300;
    private static final int IMAGE_HEIGHT = 225;
    private static final String DEFAULT_HEADER = "Camera Feed";
    private static final String CAT_DETECTED_HEADER = "DANGER - CAT DETECTED";
    private static final String NO_CAT_HEADER = "Camera Feed - No Cats Detected";

    private final SecurityService securityService;
    private final JLabel cameraHeader;
    private final JLabel cameraLabel;
    private BufferedImage currentCameraImage;

    /**
     * Constructs an ImagePanel with camera display and control buttons.
     *
     * @param securityService The security service to process images and handle events
     */
    public ImagePanel(SecurityService securityService) {
        super();
        if (securityService == null) {
            throw new IllegalArgumentException("Security service cannot be null");
        }

        this.securityService = securityService;
        this.securityService.addStatusListener(this);

        setLayout(new MigLayout());

        // Initialize UI components
        cameraHeader = createCameraHeader();
        cameraLabel = createCameraLabel();
        JButton refreshButton = createRefreshButton();
        JButton scanButton = createScanButton();

        // Layout components
        add(cameraHeader, "span 3, wrap");
        add(cameraLabel, "span 3, wrap");
        add(refreshButton);
        add(scanButton);
    }

    private JLabel createCameraHeader() {
        JLabel header = new JLabel(DEFAULT_HEADER);
        header.setFont(StyleService.HEADING_FONT);
        return header;
    }

    private JLabel createCameraLabel() {
        JLabel label = new JLabel();
        label.setBackground(Color.WHITE);
        label.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        return label;
    }

    private JButton createRefreshButton() {
        JButton button = new JButton("Refresh Camera");
        button.addActionListener(e -> handleImageSelection());
        return button;
    }

    private JButton createScanButton() {
        JButton button = new JButton("Scan Picture");
        button.addActionListener(e -> {
            if (currentCameraImage != null) {
                securityService.processImage(currentCameraImage);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select an image first",
                        "No Image",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
        return button;
    }

    private void handleImageSelection() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select Picture");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        if (selectedFile == null) {
            return;
        }

        loadImage(selectedFile);
    }

    private void loadImage(File imageFile) {
        try {
            currentCameraImage = ImageIO.read(imageFile);
            if (currentCameraImage == null) {
                throw new IOException("Invalid image format");
            }

            updateDisplayedImage();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load image", e);
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load the selected image: " + e.getMessage(),
                    "Image Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateDisplayedImage() {
        if (currentCameraImage != null) {
            Image scaledImage = currentCameraImage.getScaledInstance(
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT,
                    Image.SCALE_SMOOTH
            );
            cameraLabel.setIcon(new ImageIcon(scaledImage));
            repaint();
        }
    }

    @Override
    public void notify(AlarmStatus status) {
        // No behavior necessary for this status change
    }

    @Override
    public void catDetected(boolean catDetected) {
        cameraHeader.setText(catDetected ? CAT_DETECTED_HEADER : NO_CAT_HEADER);
    }

    @Override
    public void sensorStatusChanged() {
        // No behavior necessary for sensor status changes
    }
}