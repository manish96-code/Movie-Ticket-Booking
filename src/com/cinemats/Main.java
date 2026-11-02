package com.cinemats;

import com.cinemats.model.User;
import com.cinemats.ui.admin.AdminDashboard;
import com.cinemats.util.Theme;

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

        Theme.initGlobalTheme();

        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard((User) null);
            dashboard.setVisible(true);
        });
    }
}
