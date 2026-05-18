package com.example.swing;

import com.example.swing.panel.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private static final Color NAV_BG         = new Color(33,  37,  41);
    private static final Color NAV_BTN_FG     = new Color(173, 181, 189);
    private static final Color NAV_BTN_FG_ACT = Color.WHITE;
    private static final Color NAV_BTN_ACT_BG = new Color(66, 133, 244);
    private static final Color SUB_BG         = new Color(248, 249, 250);
    private static final Color SUB_BTN_FG     = new Color(73,  80,  87);
    private static final Color SUB_BTN_ACT_BG = new Color(66, 133, 244);
    private static final Color SUB_BTN_ACT_FG = Color.WHITE;

    private final CardLayout mainCardLayout = new CardLayout();
    private final JPanel mainCardPanel = new JPanel(mainCardLayout);
    private JButton[] mainBtns;

    public MainFrame() {
        setTitle("인사관리 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(mainCardPanel, BorderLayout.CENTER);

        mainCardPanel.add(buildCard(
            new String[]{"직원", "인사기록", "평가"},
            new JPanel[]{new EmployeePanel(), new HrRecordPanel(), new EvaluationPanel()}
        ), "인사관리");

        mainCardPanel.add(buildCard(
            new String[]{"경력", "프로젝트", "프로젝트투입"},
            new JPanel[]{new CareerPanel(), new ProjectPanel(), new ProjectParticipationPanel()}
        ), "업무이력");

        mainCardPanel.add(buildCard(
            new String[]{"스터디", "스터디활동"},
            new JPanel[]{new StudyPanel(), new StudyActivityHistoryPanel()}
        ), "역량개발");

        mainCardPanel.add(new LeavePanel(), "근태관리");

        mainCardLayout.show(mainCardPanel, "인사관리");
        setActiveMainBtn(0);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setBackground(NAV_BG);
        topBar.setPreferredSize(new Dimension(0, 48));

        String[][] entries = {
            {"인사 관리", "인사관리"},
            {"업무 이력", "업무이력"},
            {"역량 개발", "역량개발"},
            {"근태 관리", "근태관리"}
        };

        mainBtns = new JButton[entries.length];
        for (int i = 0; i < entries.length; i++) {
            JButton btn = createNavBtn(entries[i][0]);
            final int idx = i;
            final String key = entries[i][1];
            btn.addActionListener(e -> {
                mainCardLayout.show(mainCardPanel, key);
                setActiveMainBtn(idx);
            });
            topBar.add(btn);
            mainBtns[i] = btn;
        }
        return topBar;
    }

    private JButton createNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(NAV_BTN_FG);
        btn.setBackground(NAV_BG);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 44, 48));
        return btn;
    }

    private void setActiveMainBtn(int activeIdx) {
        for (int i = 0; i < mainBtns.length; i++) {
            boolean active = (i == activeIdx);
            mainBtns[i].setBackground(active ? NAV_BTN_ACT_BG : NAV_BG);
            mainBtns[i].setForeground(active ? NAV_BTN_FG_ACT : NAV_BTN_FG);
        }
    }

    private JPanel buildCard(String[] names, JPanel[] panels) {
        CardLayout subLayout = new CardLayout();
        JPanel subCardPanel = new JPanel(subLayout);

        JPanel subMenuBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        subMenuBar.setBackground(SUB_BG);
        subMenuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)));

        JButton[] subBtns = new JButton[names.length];

        for (int i = 0; i < names.length; i++) {
            subCardPanel.add(panels[i], names[i]);
            JButton btn = createSubBtn(names[i]);
            final int idx = i;
            final String name = names[i];
            btn.addActionListener(e -> {
                subLayout.show(subCardPanel, name);
                setActiveSubBtn(subBtns, idx);
            });
            subMenuBar.add(btn);
            subBtns[i] = btn;
        }

        subLayout.show(subCardPanel, names[0]);
        setActiveSubBtn(subBtns, 0);

        JPanel card = new JPanel(new BorderLayout());
        card.add(subMenuBar, BorderLayout.NORTH);
        card.add(subCardPanel, BorderLayout.CENTER);
        return card;
    }

    private JButton createSubBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(SUB_BTN_FG);
        btn.setBackground(SUB_BG);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setActiveSubBtn(JButton[] btns, int activeIdx) {
        for (int i = 0; i < btns.length; i++) {
            boolean active = (i == activeIdx);
            btns[i].setBackground(active ? SUB_BTN_ACT_BG : SUB_BG);
            btns[i].setForeground(active ? SUB_BTN_ACT_FG : SUB_BTN_FG);
        }
    }
}
