package com.example.swing;

import com.example.dao.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {

    private boolean authenticated = false;
    private final JPasswordField passwordField = new JPasswordField();

    public LoginDialog() {
        super((Frame) null, "ERP 시스템 로그인", true);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Color.WHITE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(44, 48, 44, 48));

        JLabel titleLabel = new JLabel("ERP 시스템");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(33, 37, 41));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("비밀번호를 입력하세요");
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subLabel.setForeground(new Color(130, 135, 145));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            new EmptyBorder(4, 12, 4, 12)
        ));
        passwordField.addActionListener(this::onLogin);

        JButton loginBtn = new JButton("로그인");
        styleFilledButton(loginBtn, new Color(66, 133, 244));
        loginBtn.addActionListener(this::onLogin);

        JButton cancelBtn = new JButton("취소");
        cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        cancelBtn.setForeground(new Color(130, 135, 145));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setOpaque(true);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> System.exit(0));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(subLabel);
        card.add(Box.createVerticalStrut(32));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(cancelBtn);

        root.add(card);
        setContentPane(root);
        setSize(380, 340);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void styleFilledButton(JButton btn, Color bg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void onLogin(ActionEvent e) {
        String input = new String(passwordField.getPassword());
        if (input.equals(DatabaseConnection.getErpPassword())) {
            authenticated = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "비밀번호가 올바르지 않습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
