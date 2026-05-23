package com.example.swing.panel;

import com.example.dao.EmployeeDAO;
import com.example.model.Employee;
import com.example.swing.dialog.EmployeeDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

public class EmployeePanel extends JPanel implements Refreshable {

    // ── Theme ──────────────────────────────────────────────────────
    private static final Color BG           = new Color(0xF7, 0xF7, 0xF5);
    private static final Color SURFACE      = Color.WHITE;
    private static final Color BORDER       = new Color(0xEC, 0xEC, 0xEA);
    private static final Color BORDER_STRONG= new Color(0xD8, 0xD8, 0xD4);
    private static final Color TEXT         = new Color(0x1A, 0x1A, 0x1A);
    private static final Color TEXT_MUTED   = new Color(0x66, 0x66, 0x61);
    private static final Color ROW_HOVER    = new Color(0xF7, 0xF7, 0xF3);
    private static final Color ROW_SELECTED = new Color(0xEE, 0xF0, 0xFB);
    private static final Color ACCENT       = new Color(0x4B, 0x5E, 0xAA);
    private static final Color ACCENT_SOFT  = new Color(0xEE, 0xF0, 0xFB);
    private static final Color ACCENT_TEXT  = new Color(0x3A, 0x4A, 0x8C);

    private static final Icon LOCK_ICON = new Icon() {
        @Override public int getIconWidth()  { return 14; }
        @Override public int getIconHeight() { return 16; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x99, 0x99, 0xBB));
            g2.fillRoundRect(x + 1, y + 7, 12, 9, 3, 3);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawArc(x + 2, y + 1, 10, 9, 0, 180);
            g2.setColor(Color.WHITE);
            g2.fillOval(x + 5, y + 9, 4, 3);
            g2.fillRect(x + 6, y + 11, 2, 3);
            g2.dispose();
        }
    };

    private static final String[] GRADES      = {"", "사원", "대리", "과장", "부장", "이사"};
    private static final String[] DEPARTMENTS = {"", "개발자", "마케팅", "경영관리", "연구개발"};
    private static final String[] COLUMNS     = {"ID", "이름", "직급", "부서", "연봉", "전화번호", "이메일", "주민번호", "학력"};

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int     myId   = UserSession.getInstance().getEmployeeId();

    private final EmployeeDAO       dao        = new EmployeeDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable            table      = new JTable(tableModel);
    private final JTextField        nameField  = new JTextField(14);
    private final JComboBox<String> gradeBox   = new JComboBox<>(GRADES);
    private final JComboBox<String> deptBox    = new JComboBox<>(DEPARTMENTS);
    private final JLabel            countLbl   = new JLabel("0명");

    // Stats labels (관리자 전용)
    private final JLabel statTotal = new JLabel("–");
    private final JLabel statDev   = new JLabel("–");
    private final JLabel statMgmt  = new JLabel("–");

    private final EmployeeDetailPanel detailPanel = new EmployeeDetailPanel();
    private final JSplitPane          splitPane;

    private List<Employee> currentList   = new java.util.ArrayList<>();
    private boolean        detailShowing     = false;
    private int            hoveredRow        = -1;
    private boolean        viewMode          = false;   // 조회모드: true면 잠금 없이 전체 표시
    private int            lastSelectedEmpId = -1;
    // 주민번호: 0=숨김, 1=마스킹, 2=전체 공개
    private final java.util.Map<Integer, Integer> residentRevealState = new java.util.HashMap<>();
    private final java.util.Set<Integer>          revealedSalaries    = new java.util.HashSet<>();

    public EmployeePanel() {
        setLayout(new BorderLayout());
        setBackground(BG);

        // ── 상단 콘텐츠 영역 ──────────────────────────────────────
        JPanel mainContent = new JPanel(new BorderLayout(0, 0));
        mainContent.setBackground(BG);
        mainContent.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        if (isAdmin) {
            topSection.add(buildStatsRow());
            topSection.add(Box.createVerticalStrut(14));
        }
        topSection.add(buildFilterBar());
        topSection.add(Box.createVerticalStrut(10));

        mainContent.add(topSection, BorderLayout.NORTH);
        mainContent.add(buildTableArea(), BorderLayout.CENTER);

        // ── 하단 상세 패널 ────────────────────────────────────────
        JPanel detailWrapper = new JPanel(new BorderLayout());
        detailWrapper.setBackground(BG);
        detailWrapper.setBorder(BorderFactory.createEmptyBorder(0, 16, 10, 16));
        detailWrapper.add(detailPanel, BorderLayout.CENTER);

        // ── 분할 패인 ─────────────────────────────────────────────
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainContent, detailWrapper);
        splitPane.setResizeWeight(1.0);
        splitPane.setDividerSize(5);
        splitPane.setOneTouchExpandable(false);
        splitPane.setBorder(null);

        // 구분선 스타일
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override public void paint(Graphics g) {
                        g.setColor(BORDER);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });

        add(splitPane, BorderLayout.CENTER);

        attachEvents();
        loadData();
    }

    // ── Stats 행 ──────────────────────────────────────────────────
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(statCard("전체 직원", statTotal, "명"));
        row.add(statCard("개발자",    statDev,   "명"));
        row.add(statCard("경영관리",  statMgmt,  "명"));
        return row;
    }

    private JPanel statCard(String label, JLabel valueLbl, String unit) {
        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLbl.setForeground(TEXT);

        JLabel unitLbl = new JLabel(" " + unit);
        unitLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        unitLbl.setForeground(TEXT_MUTED);

        JPanel valueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valueRow.setOpaque(false);
        valueRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueRow.add(valueLbl);
        valueRow.add(unitLbl);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER),
            BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueRow);
        return card;
    }

    // ── 필터 바 ───────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        styleField(nameField);
        styleCombo(gradeBox);
        styleCombo(deptBox);

        left.add(nameField);
        left.add(gradeBox);
        left.add(deptBox);

        JButton resetBtn = buildOutlineBtn("초기화");
        resetBtn.addActionListener(e -> {
            nameField.setText("");
            gradeBox.setSelectedIndex(0);
            deptBox.setSelectedIndex(0);
        });
        left.add(resetBtn);
        left.add(buildViewModeToggle());

        // 결과 개수 배지
        countLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        countLbl.setForeground(ACCENT_TEXT);
        countLbl.setOpaque(true);
        countLbl.setBackground(ACCENT_SOFT);
        countLbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        left.add(Box.createHorizontalStrut(4));
        left.add(countLbl);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        if (isAdmin) {
            JButton addBtn    = buildAccentBtn("+ 직원 추가");
            JButton editBtn   = buildOutlineBtn("수정");
            JButton deleteBtn = buildOutlineBtn("삭제");

            addBtn.addActionListener(e -> openDialog(null));
            editBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { showInfo("수정할 직원을 선택하세요."); return; }
                openDialog(currentList.get(table.convertRowIndexToModel(row)));
            });
            deleteBtn.addActionListener(e -> deleteSelected());

            right.add(editBtn);
            right.add(deleteBtn);
            right.add(addBtn);
        }

        bar.add(left,  BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── 테이블 영역 ───────────────────────────────────────────────
    private JPanel buildTableArea() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(BORDER);
        table.setBackground(SURFACE);
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(ACCENT_TEXT);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        table.setFillsViewportHeight(true);

        int[] widths = {45, 80, 65, 80, 95, 110, 160, 110, 70};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // 행 호버 추적
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) { hoveredRow = row; table.repaint(); }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoveredRow = -1; table.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;
                int empId = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
                if (col == 7) {  // 주민번호: 0→1(마스킹)→2(전체)→0(숨김)
                    int state = residentRevealState.getOrDefault(empId, 0);
                    if (state < 2) residentRevealState.put(empId, state + 1);
                    else residentRevealState.remove(empId);
                    table.repaint();
                } else if (col == 4) {  // 연봉
                    if (!revealedSalaries.remove(empId)) revealedSalaries.add(empId);
                    table.repaint();
                }
            }
        });

        // 연봉 컬럼: 잠금 아이콘, 클릭 시 토글
        table.getColumnModel().getColumn(4).setCellRenderer(
            buildBlockingRenderer(revealedSalaries,
                v -> v instanceof Integer && (Integer) v > 0 ? String.format("%,d원", (Integer) v) : "-"));

        // 주민번호 컬럼: 3단계 토글 (숨김 → 마스킹 → 전체)
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                int empId = (int) tableModel.getValueAt(t.convertRowIndexToModel(row), 0);
                int state = viewMode ? 2 : residentRevealState.getOrDefault(empId, 0);
                String raw = value instanceof String ? (String) value : "";
                String display;
                Icon icon = null;
                String tooltip;
                if (state == 0) {
                    display = " 보기";
                    icon = LOCK_ICON;
                    tooltip = "클릭하여 마스킹 확인";
                } else if (state == 1) {
                    display = raw.length() >= 8 ? raw.substring(0, 8) + "******" : raw;
                    tooltip = "클릭하여 전체 확인";
                } else {
                    display = raw.isEmpty() ? "-" : raw;
                    tooltip = viewMode ? null : "클릭하여 숨기기";
                }
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, display, sel, focus, row, col);
                lbl.setIcon(icon);
                lbl.setIconTextGap(4);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                lbl.setOpaque(true);
                Color fg = state == 2 ? new Color(0xCC, 0x44, 0x44) : (state == 1 ? TEXT : TEXT_MUTED);
                if (sel) {
                    lbl.setBackground(ROW_SELECTED); lbl.setForeground(state == 0 ? TEXT_MUTED : ACCENT_TEXT);
                } else if (row == hoveredRow) {
                    lbl.setBackground(ROW_HOVER); lbl.setForeground(fg);
                } else {
                    lbl.setBackground(row % 2 == 0 ? SURFACE : new Color(0xFB, 0xFB, 0xF9));
                    lbl.setForeground(fg);
                }
                lbl.setToolTipText(tooltip);
                return lbl;
            }
        });

        // 호버 + 얼룩말 줄무늬 렌더러
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, value, sel, focus, row, col);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (sel) {
                    lbl.setBackground(ROW_SELECTED);
                    lbl.setForeground(ACCENT_TEXT);
                } else if (row == hoveredRow) {
                    lbl.setBackground(ROW_HOVER);
                    lbl.setForeground(TEXT);
                } else {
                    lbl.setBackground(row % 2 == 0 ? SURFACE : new Color(0xFB, 0xFB, 0xF9));
                    lbl.setForeground(TEXT);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(SURFACE);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    // ── 이벤트 연결 ───────────────────────────────────────────────
    private void attachEvents() {
        // 라이브 검색 (입력 즉시 필터)
        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(EmployeePanel.this::loadData);
            }
            @Override public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(EmployeePanel.this::loadData);
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        };
        nameField.getDocument().addDocumentListener(dl);
        gradeBox.addActionListener(e -> loadData());
        deptBox.addActionListener(e -> loadData());

        // 행 선택 → 하단 상세 패널
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int empId = (int) tableModel.getValueAt(table.convertRowIndexToModel(row), 0);
                    if (empId != lastSelectedEmpId && !viewMode) {
                        residentRevealState.clear();
                        revealedSalaries.clear();
                        table.repaint();
                    }
                    lastSelectedEmpId = empId;
                    detailPanel.loadEmployee(currentList.get(table.convertRowIndexToModel(row)));
                    detailShowing = true;
                    splitPane.setDividerLocation(0.55);
                } else {
                    detailShowing = false;
                    SwingUtilities.invokeLater(() ->
                        splitPane.setDividerLocation(splitPane.getMaximumDividerLocation()));
                }
            }
        });

        splitPane.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && splitPane.isShowing() && !detailShowing) {
                SwingUtilities.invokeLater(() ->
                    splitPane.setDividerLocation(splitPane.getMaximumDividerLocation()));
            }
        });
        splitPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                if (!detailShowing)
                    SwingUtilities.invokeLater(() ->
                        splitPane.setDividerLocation(splitPane.getMaximumDividerLocation()));
            }
        });
    }

    // ── 데이터 ────────────────────────────────────────────────────
    @Override public void refresh() { loadData(); }

    private void loadData() {
        residentRevealState.clear();
        revealedSalaries.clear();
        try {
            currentList = isAdmin
                ? dao.search(nameField.getText().trim(),
                             (String) gradeBox.getSelectedItem(),
                             (String) deptBox.getSelectedItem())
                : dao.getByEmployeeId(myId);
            tableModel.setRowCount(0);
            for (Employee e : currentList) {
                tableModel.addRow(new Object[]{
                    e.getId(), e.getEmployeeName(), e.getGrade(),
                    e.getDepartment(),
                    e.getSalary(),
                    e.getPhoneNumber() != null ? e.getPhoneNumber() : "",
                    e.getEmail()       != null ? e.getEmail()       : "",
                    e.getResidentNumber() != null ? e.getResidentNumber() : "",
                    e.getEducation()
                });
            }
            countLbl.setText(currentList.size() + "명");
            if (isAdmin) updateStats();
        } catch (Exception ex) {
            showError("데이터 로드 오류: " + ex.getMessage());
        }
    }

    private void updateStats() {
        try {
            List<Employee> all = dao.search("", "", "");
            statTotal.setText(String.valueOf(all.size()));
            long dev  = all.stream().filter(e -> "개발자".equals(e.getDepartment())).count();
            long mgmt = all.stream().filter(e -> "경영관리".equals(e.getDepartment())).count();
            statDev.setText(String.valueOf(dev));
            statMgmt.setText(String.valueOf(mgmt));
        } catch (Exception ignored) {}
    }

    // ── UI 헬퍼 ───────────────────────────────────────────────────
    private void styleField(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(140, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_STRONG),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        f.setBackground(SURFACE);
        f.setForeground(TEXT);
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setPreferredSize(new Dimension(cb.getPreferredSize().width + 10, 32));
        cb.setBackground(SURFACE);
    }

    private JButton buildAccentBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? ACCENT_TEXT : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 32));
        return btn;
    }

    private JButton buildOutlineBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(SURFACE);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_STRONG),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0xF7, 0xF7, 0xF5));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(SURFACE);
            }
        });
        return btn;
    }

    private JToggleButton buildViewModeToggle() {
        JToggleButton btn = new JToggleButton("조회모드");
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setBackground(SURFACE);
        btn.setForeground(TEXT_MUTED);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_STRONG),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addItemListener(ev -> {
            viewMode = btn.isSelected();
            if (!viewMode) {
                residentRevealState.clear();
                revealedSalaries.clear();
            }
            btn.setBackground(viewMode ? ACCENT_SOFT : SURFACE);
            btn.setForeground(viewMode ? ACCENT_TEXT : TEXT_MUTED);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(viewMode ? ACCENT : BORDER_STRONG),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
            ));
            table.repaint();
        });
        return btn;
    }

    private DefaultTableCellRenderer buildBlockingRenderer(
            java.util.Set<Integer> revealed,
            java.util.function.Function<Object, String> fmt) {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                int empId = (int) tableModel.getValueAt(t.convertRowIndexToModel(row), 0);
                boolean rev = viewMode || revealed.contains(empId);
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, rev ? fmt.apply(value) : " 보기", sel, focus, row, col);
                lbl.setIcon(rev ? null : LOCK_ICON);
                lbl.setIconTextGap(4);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                lbl.setOpaque(true);
                if (sel) {
                    lbl.setBackground(ROW_SELECTED);
                    lbl.setForeground(rev ? ACCENT_TEXT : TEXT_MUTED);
                } else if (row == hoveredRow) {
                    lbl.setBackground(ROW_HOVER);
                    lbl.setForeground(rev ? TEXT : TEXT_MUTED);
                } else {
                    lbl.setBackground(row % 2 == 0 ? SURFACE : new Color(0xFB, 0xFB, 0xF9));
                    lbl.setForeground(rev ? TEXT : TEXT_MUTED);
                }
                lbl.setToolTipText(viewMode ? null : (rev ? "클릭하여 숨기기" : "클릭하여 확인"));
                return lbl;
            }
        };
    }

    private void openDialog(Employee emp) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EmployeeDialog dialog = new EmployeeDialog(frame, emp, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("삭제할 직원을 선택하세요."); return; }
        Employee emp = currentList.get(table.convertRowIndexToModel(row));
        int confirm = JOptionPane.showConfirmDialog(this,
            "'" + emp.getEmployeeName() + "' 직원을 삭제하시겠습니까?",
            "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dao.delete(emp.getId());
                loadData();
            } catch (Exception ex) {
                showError("삭제 오류: " + ex.getMessage());
            }
        }
    }

    private void showInfo(String msg)  { JOptionPane.showMessageDialog(this, msg); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }
}
