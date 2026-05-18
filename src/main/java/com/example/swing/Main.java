package com.example.swing;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            LoginDialog login = new LoginDialog();
            login.setVisible(true);

            if (login.isAuthenticated()) {
                new MainFrame().setVisible(true);
            }
        });
    }
}
