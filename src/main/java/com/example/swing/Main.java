package com.example.swing;

import com.example.util.UserSession;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            showLogin();
        });
    }

    static void showLogin() {
        LoginDialog login = new LoginDialog();
        login.setVisible(true);
        if (login.isAuthenticated()) {
            new MainFrame().setVisible(true);
        }
    }

    static void logout(JFrame mainFrame) {
        UserSession.getInstance().clear();
        mainFrame.dispose();
        showLogin();
    }
}
