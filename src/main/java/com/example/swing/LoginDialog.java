package com.example.swing;

import com.example.dao.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {

    private boolean authenticated = false;
    private final JPasswordField passwordField = new JPasswordField(16);

    public LoginDialog() {
        super((Frame) null, "ERP 시스템 로그인", true);

        JLabel label = new JLabel("비밀번호를 입력하세요");
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JButton loginBtn = new JButton("확인");
        JButton cancelBtn = new JButton("취소");

        loginBtn.addActionListener(this::onLogin);
        cancelBtn.addActionListener(e -> System.exit(0));
        passwordField.addActionListener(this::onLogin);

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        inputRow.add(passwordField);
        inputRow.add(loginBtn);
        inputRow.add(cancelBtn);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        content.add(label);
        content.add(Box.createVerticalStrut(16));
        content.add(inputRow);

        setContentPane(content);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
