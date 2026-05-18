package com.example.swing;

import com.example.dao.AuthDAO;
import com.example.util.MaskingUtil;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;

public class LoginDialog extends JDialog {

    private static final Color NAVY       = new Color(25,  50, 120);
    private static final Color NAVY_DARK  = new Color(18,  38,  95);
    private static final Color BORDER_DEF = new Color(210, 215, 228);
    private static final Color PH_FG      = new Color(175, 180, 198);

    private boolean authenticated = false;

    private final JTextField     nameField     = makePlaceholderField("이름");
    private final JPasswordField residentField = makePlaceholderPassword("주민번호");
    private final AuthDAO        authDAO       = new AuthDAO();

    public LoginDialog() {
        super((Frame) null, "ERP 시스템 로그인", true);

        MaskingUtil.installResidentFilter(residentField);
        residentField.addActionListener(this::onLogin);

        styleField(nameField);
        styleField(residentField);

        // 타이틀
        JLabel title = new JLabel("인사관리 시스템");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // 버튼
        JButton loginBtn = buildNavyBtn("로그인");
        loginBtn.addActionListener(this::onLogin);

        // 카드
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(52, 44, 52, 44));

        card.add(title);
        card.add(Box.createVerticalStrut(36));
        card.add(nameField);
        card.add(Box.createVerticalStrut(12));
        card.add(residentField);
        card.add(Box.createVerticalStrut(20));
        card.add(loginBtn);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Color.WHITE);
        root.add(card);

        setContentPane(root);
        getRootPane().setDefaultButton(loginBtn);
        setSize(520, 400);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void styleField(JComponent field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 15));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            roundedBorder(BORDER_DEF),
            BorderFactory.createEmptyBorder(0, 4, 0, 4)
        ));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    roundedBorder(NAVY),
                    BorderFactory.createEmptyBorder(0, 4, 0, 4)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    roundedBorder(BORDER_DEF),
                    BorderFactory.createEmptyBorder(0, 4, 0, 4)
                ));
            }
        });
    }

    private JButton buildNavyBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? NAVY_DARK : NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private AbstractBorder roundedBorder(Color color) {
        return new AbstractBorder() {
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.6f));
                g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 12, 12);
                g2.dispose();
            }
            @Override public Insets getBorderInsets(Component c) { return new Insets(14, 16, 14, 16); }
            @Override public Insets getBorderInsets(Component c, Insets i) { i.set(14, 16, 14, 16); return i; }
        };
    }

    private JTextField makePlaceholderField(String ph) {
        return new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(PH_FG);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    Insets ins = getInsets();
                    g2.drawString(ph, ins.left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            }
        };
    }

    private JPasswordField makePlaceholderPassword(String ph) {
        return new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(PH_FG);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
                    FontMetrics fm = g2.getFontMetrics();
                    Insets ins = getInsets();
                    g2.drawString(ph, ins.left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            }
        };
    }

    private void onLogin(ActionEvent e) {
        String name     = nameField.getText().trim();
        String resident = new String(residentField.getPassword()).trim();

        if (name.isEmpty() || resident.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이름과 주민번호를 모두 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (authDAO.login(name, resident)) {
                authenticated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "이름 또는 주민번호가 올바르지 않습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
                residentField.setText("");
                residentField.requestFocus();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAuthenticated() { return authenticated; }
}
