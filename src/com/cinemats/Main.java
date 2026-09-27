package com.cinemats;

import com.cinemats.ui.auth.LoginFrame;

import javax.swing.*;

// Application entry point for Cinema Express
public class Main {
    public static void main(String[] args) {
        // Set Look and Feel for clean modern native rendering
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception alsoIgnored) {}
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
