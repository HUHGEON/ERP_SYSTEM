package com.example.swing;

import com.example.swing.panel.*;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    // ── Palette ──────────────────────────────────────────────────
    private static final Color SB_BG      = new Color(18,  22,  40);
    private static final Color SB_HOVER   = new Color(28,  33,  56);
    private static final Color SB_HDR_FG  = new Color(100, 115, 158);
    private static final Color SB_FG      = new Color(195, 205, 228);
    private static final Color SB_ACT_BG  = new Color(34,  40,  68);
    private static final Color SB_ACT_FG  = Color.WHITE;
    private static final Color ACCENT     = new Color(66, 133, 244);
    private static final Color CONTENT_BG = new Color(244, 246, 251);

    // 테이블 헤더용
    private static final Color TH_BG      = new Color(28,  34,  58);
    private static final Color TH_BORDER  = new Color(50,  60,  100);

    private final CardLayout    contentCards = new CardLayout();
    private final JPanel        contentPanel = new JPanel(contentCards);
    private final JPanel        contentWrap  = new JPanel(new BorderLayout());
    private final JLabel        pageTitleLbl = new JLabel();
    private final List<JButton>              menuBtns     = new ArrayList<>();
    private JButton                          activeBtn;
    private final java.util.Map<String, List<JButton>> sectionItems = new java.util.LinkedHashMap<>();
    private String                           currentSectionKey = null;
    private JPanel                           sidebar;

    public MainFrame() {
        setTitle("인사관리 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 800);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        buildContentWrap();
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(contentWrap,    BorderLayout.CENTER);

        reg("직원",         "직원 관리",     new EmployeePanel());
        reg("인사기록",     "인사 기록",     new HrRecordPanel());
        reg("평가",         "프로젝트 평가", new EvaluationPanel());
        reg("경력",         "경력 관리",     new CareerPanel());
        reg("프로젝트",     "프로젝트",      new ProjectPanel());
        reg("프로젝트투입", "프로젝트 투입", new ProjectParticipationPanel());
        reg("스터디",       "스터디",        new StudyPanel());
        reg("휴가기록",     "휴가 기록",     new LeavePanel());

        if (!menuBtns.isEmpty()) activateBtn(menuBtns.get(0), "직원 관리");

        // 모든 JTable 헤더를 일괄 스타일링 (패널 생성 이후 실행)
        SwingUtilities.invokeLater(() -> styleAllTables(contentPanel));
    }

    private void reg(String key, String title, JPanel panel) {
        panel.putClientProperty("pageTitle", title);
        contentPanel.add(panel, key);
    }

    // ── 콘텐츠 래퍼 ─────────────────────────────────────────────
    private void buildContentWrap() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Color.WHITE);
        titleBar.setPreferredSize(new Dimension(0, 48));
        titleBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 230, 244)));

        pageTitleLbl.setFont(new Font("SansSerif", Font.BOLD, 17));
        pageTitleLbl.setForeground(new Color(22, 28, 52));
        pageTitleLbl.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        titleBar.add(pageTitleLbl, BorderLayout.CENTER);

        contentPanel.setBackground(CONTENT_BG);
        contentWrap.setBackground(CONTENT_BG);
        contentWrap.add(titleBar,     BorderLayout.NORTH);
        contentWrap.add(contentPanel, BorderLayout.CENTER);
    }

    // ── Top Bar ──────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(SB_BG);
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 36, 65)));

        // 좌: 벡터 로고
        final int BAR_H  = 48;
        final int LOGO_H = 40;
        bar.setPreferredSize(new Dimension(0, BAR_H));

        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, (BAR_H - LOGO_H) / 2));
        logoArea.setBackground(SB_BG);
        logoArea.add(buildLogoPanel(LOGO_H));

        // 우: 유저 정보
        UserSession s = UserSession.getInstance();
        String role = s.isAdmin() ? "관리자" : "일반";

        JLabel userLbl = new JLabel(s.getName() + "   " + s.getDepartment());
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        userLbl.setForeground(new Color(205, 215, 235));

        JLabel roleBadge = buildRoundBadge(role, s.isAdmin() ? ACCENT : new Color(80, 95, 140));

        JButton logoutBtn = buildOutlineBtn("로그아웃");
        logoutBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "로그아웃 하시겠습니까?",
                    "로그아웃", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                Main.logout(this);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setBackground(SB_BG);
        right.add(userLbl);
        right.add(roleBadge);
        right.add(logoutBtn);

        bar.add(logoArea, BorderLayout.WEST);
        bar.add(right,    BorderLayout.EAST);
        return bar;
    }

    private JLabel buildRoundBadge(String text, Color bg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        return lbl;
    }

    private JButton buildOutlineBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(new Color(190, 200, 225));
        btn.setBackground(new Color(30, 36, 62));
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(58, 68, 105), 1),
            BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
        return btn;
    }

    // ── 벡터 로고 패널 ───────────────────────────────────────────
    private JPanel buildLogoPanel(int h) {
        final Color FG  = new Color(205, 215, 232);
        final Color SUB = new Color(130, 148, 175);

        JPanel p = new JPanel() {
            @Override public Dimension getPreferredSize() { return new Dimension(300, h); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // ── 기어 ──────────────────────────────────────
                int cx = h / 2, cy = h / 2;
                int outerR = h / 2 - 1;
                int nTeeth = 11;
                double toothH = outerR * 0.20;
                int bodyR  = (int)(outerR - toothH);
                int holeR  = (int)(bodyR  * 0.52);
                int ringR  = (int)(holeR  * 0.88);
                int hubR   = (int)(holeR  * 0.52);

                // 기어 외곽 + 이빨
                java.awt.geom.Path2D.Double gear = new java.awt.geom.Path2D.Double();
                boolean first = true;
                for (int i = 0; i < nTeeth; i++) {
                    double base = 2 * Math.PI * i / nTeeth - Math.PI / 2;
                    double half = Math.PI / nTeeth;
                    double span = 0.30;
                    double[][] pts = {
                        {bodyR,  base - half * (1 - span)},
                        {outerR, base - half * span},
                        {outerR, base + half * span},
                        {bodyR,  base + half * (1 - span)},
                        {bodyR,  2 * Math.PI * (i + 1) / nTeeth - Math.PI / 2 - half * (1 - span)}
                    };
                    for (double[] pt : pts) {
                        double x = cx + pt[0] * Math.cos(pt[1]);
                        double y = cy + pt[0] * Math.sin(pt[1]);
                        if (first) { gear.moveTo(x, y); first = false; }
                        else gear.lineTo(x, y);
                    }
                }
                gear.closePath();

                java.awt.geom.Area gearArea = new java.awt.geom.Area(gear);
                gearArea.subtract(new java.awt.geom.Area(
                    new java.awt.geom.Ellipse2D.Double(cx - holeR, cy - holeR, holeR * 2, holeR * 2)));
                g2.setColor(FG);
                g2.fill(gearArea);

                // 내부 링 (도넛)
                java.awt.geom.Area ring = new java.awt.geom.Area(
                    new java.awt.geom.Ellipse2D.Double(cx - ringR, cy - ringR, ringR * 2, ringR * 2));
                ring.subtract(new java.awt.geom.Area(
                    new java.awt.geom.Ellipse2D.Double(cx - hubR, cy - hubR, hubR * 2, hubR * 2)));
                g2.fill(ring);

                // 중앙 "e"
                g2.setFont(new Font("SansSerif", Font.BOLD, (int)(hubR * 1.5)));
                FontMetrics fem = g2.getFontMetrics();
                g2.drawString("e",
                    cx - fem.stringWidth("e") / 2,
                    cy + (fem.getAscent() - fem.getDescent()) / 2);

                // ── 텍스트 ─────────────────────────────────────
                int tx = h + 12;

                // "ERP SYSTEM"
                g2.setFont(new Font("SansSerif", Font.BOLD, (int)(h * 0.44)));
                g2.setColor(FG);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("ERP SYSTEM", tx, (int)(h * 0.52));

                // 구분선
                int lineY = (int)(h * 0.62);
                g2.setStroke(new BasicStroke(0.8f));
                g2.setColor(new Color(90, 110, 145));
                g2.drawLine(tx, lineY, getWidth() - 4, lineY);

                // 서브타이틀
                g2.setFont(new Font("SansSerif", Font.PLAIN, (int)(h * 0.17)));
                g2.setColor(SUB);
                g2.drawString("INTEGRATED  |  STREAMLINED  |  COMPLETE", tx, (int)(h * 0.84));

                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    // ── Sidebar ──────────────────────────────────────────────────
    private JPanel buildSidebar() {
        // 사이드바 콘텐츠
        JPanel sb = new JPanel();
        sb.setBackground(SB_BG);
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setPreferredSize(new Dimension(210, 0));
        sidebar = sb;

        sb.add(Box.createVerticalStrut(8));

        addSection(sb, "인사 관리");
        addItem(sb, "직원",         "직원 관리");
        addItem(sb, "인사기록",     "인사 기록");
        addItem(sb, "평가",         "프로젝트 평가");

        addSection(sb, "업무 이력");
        addItem(sb, "경력",         "경력 관리");
        addItem(sb, "프로젝트",     "프로젝트");
        addItem(sb, "프로젝트투입", "프로젝트 투입");

        addSection(sb, "역량 개발");
        addItem(sb, "스터디",       "스터디");

        addSection(sb, "근태 관리");
        addItem(sb, "휴가기록",     "휴가 기록");

        sb.add(Box.createVerticalGlue());

        // 오른쪽 끝 토글 스트립
        JButton arrowBtn = new JButton("◀");
        arrowBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        arrowBtn.setForeground(new Color(120, 140, 180));
        arrowBtn.setBackground(new Color(22, 27, 48));
        arrowBtn.setOpaque(true);
        arrowBtn.setBorderPainted(false);
        arrowBtn.setFocusPainted(false);
        arrowBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        arrowBtn.setToolTipText("사이드바 접기/펼치기");
        arrowBtn.addActionListener(e -> {
            boolean nowVisible = sb.isVisible();
            sb.setVisible(!nowVisible);
            arrowBtn.setText(nowVisible ? "▶" : "◀");
            revalidate();
            repaint();
        });

        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(new Color(22, 27, 48));
        strip.setPreferredSize(new Dimension(16, 0));
        strip.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(38, 46, 80)));
        strip.add(arrowBtn, BorderLayout.CENTER);

        // 래퍼: 콘텐츠 + 스트립
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(sb,    BorderLayout.CENTER);
        wrapper.add(strip, BorderLayout.EAST);
        return wrapper;
    }

    private void addSection(JPanel parent, String title) {
        currentSectionKey = title;
        sectionItems.put(title, new ArrayList<>());

        if (sectionItems.size() > 1) {
            parent.add(Box.createVerticalStrut(4));
        }

        JButton sBtn = new JButton("▾  " + title);
        sBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        sBtn.setForeground(Color.WHITE);
        sBtn.setBackground(new Color(24, 29, 52));
        sBtn.setOpaque(true);
        sBtn.setContentAreaFilled(true);
        sBtn.setBorderPainted(false);
        sBtn.setFocusPainted(false);
        sBtn.setHorizontalAlignment(SwingConstants.LEFT);
        sBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        sBtn.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
        sBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sBtn.addActionListener(e -> {
            List<JButton> items = sectionItems.get(title);
            boolean visible = !items.isEmpty() && items.get(0).isVisible();
            sBtn.setText((visible ? "▸  " : "▾  ") + title);
            items.forEach(b -> b.setVisible(!visible));
            parent.revalidate();
            parent.repaint();
        });

        parent.add(sBtn);
    }

    private void addItem(JPanel parent, String cardKey, String pageTitle) {
        JButton btn = new JButton(cardKey);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(SB_FG);
        btn.setBackground(SB_BG);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(SB_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(SB_BG);
            }
        });
        btn.addActionListener(e -> {
            contentCards.show(contentPanel, cardKey);
            activateBtn(btn, pageTitle);
        });

        menuBtns.add(btn);
        if (currentSectionKey != null) sectionItems.get(currentSectionKey).add(btn);
        parent.add(btn);
    }

    private void activateBtn(JButton target, String pageTitle) {
        for (JButton b : menuBtns) {
            b.setBackground(SB_BG);
            b.setForeground(SB_FG);
            b.setFont(new Font("SansSerif", Font.BOLD, 13));
            b.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 8));
        }
        target.setBackground(SB_ACT_BG);
        target.setForeground(SB_ACT_FG);
        target.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT),
            BorderFactory.createEmptyBorder(0, 23, 0, 8)
        ));
        activeBtn = target;
        pageTitleLbl.setText(pageTitle);
    }

    // ── 전체 JTable 헤더 일괄 스타일링 ───────────────────────────
    private void styleAllTables(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTable) {
                applyTableStyle((JTable) c);
            }
            if (c instanceof JScrollPane) {
                Component view = ((JScrollPane) c).getViewport().getView();
                if (view instanceof JTable) applyTableStyle((JTable) view);
            }
            if (c instanceof Container) {
                styleAllTables((Container) c);
            }
        }
    }

    private void applyTableStyle(JTable table) {
        // 헤더 렌더러
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, value, sel, focus, row, col);
                lbl.setBackground(TH_BG);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, TH_BORDER),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)
                ));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        table.getTableHeader().setBackground(TH_BG);

        // 바디
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(new Color(228, 232, 245));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(210, 225, 255));
        table.setSelectionForeground(new Color(20, 30, 70));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }
}
