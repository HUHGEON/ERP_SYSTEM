package com.example.swing.panel;

import com.example.dao.*;
import com.example.model.*;
import com.example.swing.MainFrame;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDetailPanel extends JPanel {

    private static final javax.swing.Icon LOCK_ICON = new javax.swing.Icon() {
        @Override public int getIconWidth()  { return 13; }
        @Override public int getIconHeight() { return 15; }
        @Override public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x99, 0x99, 0xBB));
            g2.fillRoundRect(x + 1, y + 6, 11, 9, 3, 3);
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawArc(x + 1, y, 11, 9, 0, 180);
            g2.setColor(Color.WHITE);
            g2.fillOval(x + 4, y + 8, 4, 3);
            g2.fillRect(x + 5, y + 10, 2, 3);
            g2.dispose();
        }
    };

    private final HrRecordDAO hrRecordDAO = new HrRecordDAO();
    private final DeveloperDAO developerDAO = new DeveloperDAO();
    private final ManagementDAO managementDAO = new ManagementDAO();
    private final CareerDAO careerDAO = new CareerDAO();
    private final LeaveDAO leaveDAO = new LeaveDAO();
    private final ProjectParticipationDAO projectDAO = new ProjectParticipationDAO();
    private final StudyParticipationDAO studyDAO = new StudyParticipationDAO();

    // 헤더 라벨
    private final JLabel nameLabel      = new JLabel();
    private final JLabel gradeLabel     = new JLabel();
    private final JLabel deptLabel      = new JLabel();
    private final JLabel hireDateLabel  = new JLabel();
    private final JLabel promotionLabel = new JLabel();
    private final JLabel salaryLabel    = new JLabel();
    private final JLabel techLabel      = new JLabel();
    private final JPanel techRow;

    private final JLabel leaveRemainingLabel = new JLabel();
    private final JLabel leaveUsedLabel      = new JLabel();

    private int     currentSalary   = 0;
    private boolean salaryRevealed  = false;

    private final JLabel permissionLabel = new JLabel();
    private final JPanel permissionRow;

    // 탭별 테이블 모델
    private final DefaultTableModel careerModel = new DefaultTableModel(
        new String[]{"회사명", "시작일", "종료일"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel projectModel = new DefaultTableModel(
        new String[]{"프로젝트명", "역할", "시작일", "종료일"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel leaveModel = new DefaultTableModel(
        new String[]{"휴가 유형", "시작일", "종료일"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel studyModel = new DefaultTableModel(
        new String[]{"스터디명"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTabbedPane tabs = new JTabbedPane();
    private int projectTabIndex = -1;

    // 행 → 도메인 id 매핑 (모델에는 표시 컬럼만 두고 id는 별도 보관)
    private final List<Integer> projectIds = new ArrayList<>();
    private final List<Integer> leaveIds   = new ArrayList<>();
    private final List<Integer> studyIds   = new ArrayList<>();

    private final JTable careerTable;
    private final JTable projectTable;
    private final JTable leaveTable;
    private final JTable studyTable;

    public EmployeeDetailPanel() {
        setLayout(new BorderLayout(0, 4));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
            "직원 상세 정보",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font(Font.SANS_SERIF, Font.BOLD, 12),
            new Color(100, 149, 237)
        ));

        // 헤더: 핵심 정보 한 줄 + 기술스택 한 줄
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        headerPanel.setBackground(new Color(245, 248, 255));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 6, 2, 6);
        gc.anchor = GridBagConstraints.WEST;

        // 첫 번째 줄: 이름 | 직급 | 부서 | 입사일 | 승진일
        addHeaderCell(headerPanel, gc, 0, 0, "이름",   nameLabel,      new Color(70, 130, 180));
        addHeaderCell(headerPanel, gc, 2, 0, "직급",   gradeLabel,     new Color(70, 130, 180));
        addHeaderCell(headerPanel, gc, 4, 0, "부서",   deptLabel,      new Color(70, 130, 180));
        addHeaderCell(headerPanel, gc, 6, 0, "입사일", hireDateLabel,  new Color(34, 139, 34));
        addHeaderCell(headerPanel, gc, 8, 0, "승진일", promotionLabel, new Color(34, 139, 34));

        // 연봉: 블라킹 → 클릭 시 토글
        salaryLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        salaryLabel.setToolTipText("클릭하여 확인");
        salaryLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                salaryRevealed = !salaryRevealed;
                updateSalaryLabel();
            }
        });

        // 두 번째 줄: 연봉 | 잔여 연차 | 사용 연차
        addHeaderCell(headerPanel, gc, 0, 1, "연봉",      salaryLabel,         new Color(160, 60, 180));
        addHeaderCell(headerPanel, gc, 2, 1, "잔여 연차", leaveRemainingLabel, new Color(34, 139, 34));
        addHeaderCell(headerPanel, gc, 4, 1, "사용 연차", leaveUsedLabel,      new Color(200, 80, 0));

        // 기술스택 (개발자만 표시)
        techRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        techRow.setBackground(new Color(245, 248, 255));
        JLabel techKey = new JLabel("기술스택:");
        techKey.setForeground(new Color(160, 82, 45));
        techKey.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        techLabel.setForeground(new Color(160, 82, 45));
        techLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        techRow.add(techKey);
        techRow.add(techLabel);

        gc.gridx = 0; gc.gridy = 2;
        gc.gridwidth = 10;
        gc.fill = GridBagConstraints.HORIZONTAL;
        headerPanel.add(techRow, gc);

        // 권한단계 (경영관리만 표시)
        permissionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        permissionRow.setBackground(new Color(245, 248, 255));
        JLabel permKey = new JLabel("권한단계:");
        permKey.setForeground(new Color(128, 0, 128));
        permKey.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        permissionLabel.setForeground(new Color(128, 0, 128));
        permissionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        permissionRow.add(permKey);
        permissionRow.add(permissionLabel);

        gc.gridy = 3;
        headerPanel.add(permissionRow, gc);
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;

        // 탭 (프로젝트 탭도 생성자에서 미리 추가하여 MainFrame.styleAllTables가 헤더 스타일을 적용하도록 함)
        careerTable  = makeTable(careerModel);
        projectTable = makeTable(projectModel);
        leaveTable   = makeTable(leaveModel);
        studyTable   = makeTable(studyModel);
        tabs.addTab("프로젝트", new JScrollPane(projectTable));
        tabs.addTab("휴가",   new JScrollPane(leaveTable));
        tabs.addTab("스터디", new JScrollPane(studyTable));
        tabs.addTab("경력",   new JScrollPane(careerTable));
        projectTabIndex = 0;

        // 더블클릭으로 해당 페이지 이동 + 항목 선택
        attachDoubleClickNav(projectTable, projectIds, "프로젝트", (mf, id) ->
            ((ProjectPanel) mf.getContentPanel("프로젝트")).selectProjectById(id));
        attachDoubleClickNav(leaveTable, leaveIds, "휴가기록", (mf, id) ->
            ((LeavePanel) mf.getContentPanel("휴가기록")).selectLeaveById(id));
        attachDoubleClickNav(studyTable, studyIds, "스터디", (mf, id) ->
            ((StudyPanel) mf.getContentPanel("스터디")).selectStudyById(id));

        add(headerPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void addHeaderCell(JPanel panel, GridBagConstraints gc, int x, int y, String key, JLabel valueLabel, Color valueColor) {
        gc.gridx = x; gc.gridy = y;
        JLabel keyLabel = new JLabel(key + ":");
        keyLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        keyLabel.setForeground(Color.GRAY);
        panel.add(keyLabel, gc);

        gc.gridx = x + 1;
        valueLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        valueLabel.setForeground(valueColor);
        panel.add(valueLabel, gc);
    }

    private JTable makeTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    @FunctionalInterface
    private interface NavAction { void run(MainFrame mf, int id); }

    private void attachDoubleClickNav(JTable table, List<Integer> idList, String cardKey, NavAction action) {
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || row >= idList.size()) return;
                int id = idList.get(row);
                Window w = SwingUtilities.getWindowAncestor(EmployeeDetailPanel.this);
                if (!(w instanceof MainFrame)) return;
                MainFrame mf = (MainFrame) w;
                mf.navigateTo(cardKey);
                try { action.run(mf, id); }
                catch (Exception ex) { /* 대상 패널 미초기화 등은 무시 */ }
            }
        });
    }

    private void updateSalaryLabel() {
        if (salaryRevealed) {
            salaryLabel.setIcon(null);
            salaryLabel.setText(currentSalary > 0 ? String.format("%,d원", currentSalary) : "-");
            salaryLabel.setForeground(new Color(160, 60, 180));
            salaryLabel.setToolTipText("클릭하여 숨기기");
        } else {
            salaryLabel.setIcon(LOCK_ICON);
            salaryLabel.setText(" 보기");
            salaryLabel.setForeground(new Color(140, 140, 165));
            salaryLabel.setToolTipText("클릭하여 확인");
        }
    }

    public void loadEmployee(Employee emp) {
        // 기본 정보
        nameLabel.setText(emp.getEmployeeName());
        gradeLabel.setText(emp.getGrade());
        deptLabel.setText(emp.getDepartment());
        String hd = emp.getHireDate();
        hireDateLabel.setText(hd != null ? hd.substring(0, Math.min(10, hd.length())) : "-");
        currentSalary = emp.getSalary();
        salaryRevealed = true;
        updateSalaryLabel();

        // 인사 기록 (가장 최근 승진일)
        try {
            HrRecord hr = hrRecordDAO.getByEmployeeId(emp.getId());
            promotionLabel.setText(hr != null && hr.getPromotionDate() != null ? hr.getPromotionDate() : "-");
        } catch (Exception e) {
            promotionLabel.setText("-");
        }

        // 연차 현황
        try {
            int remaining = leaveDAO.getRemainingLeaveDays(emp.getId());
            int used      = leaveDAO.getUsedAnnualLeaveDays(emp.getId());
            leaveRemainingLabel.setText(remaining + "일");
            leaveUsedLabel.setText(used + "일");
        } catch (Exception e) {
            leaveRemainingLabel.setText("-");
            leaveUsedLabel.setText("-");
        }

        // 기술스택 (개발자만)
        boolean isDev = "개발자".equals(emp.getDepartment());
        techRow.setVisible(isDev);
        if (isDev) {
            try {
                Developer dev = developerDAO.getById(emp.getId());
                techLabel.setText(dev != null && dev.getTech() != null ? dev.getTech() : "(등록된 기술스택 없음)");
            } catch (Exception e) {
                techLabel.setText("-");
            }
        }

        // 권한단계 (경영관리만)
        boolean isMgmt = "경영관리".equals(emp.getDepartment());
        permissionRow.setVisible(isMgmt);
        if (isMgmt) {
            try {
                Management mgmt = managementDAO.getById(emp.getId());
                permissionLabel.setText(mgmt != null && mgmt.getPermissionLevel() != null ? mgmt.getPermissionLevel() : "(등록된 권한 없음)");
            } catch (Exception e) {
                permissionLabel.setText("-");
            }
        }

        // 프로젝트 투입 탭 (개발자만 활성화)
        tabs.setEnabledAt(projectTabIndex, isDev);

        // 경력
        careerModel.setRowCount(0);
        try {
            for (Career c : careerDAO.getByEmployeeId(emp.getId())) {
                careerModel.addRow(new Object[]{c.getCompanyName(), c.getStartTime(), c.getEndTime()});
            }
        } catch (Exception ignored) {}

        // 프로젝트 투입 (개발자만)
        projectModel.setRowCount(0);
        projectIds.clear();
        if (isDev) {
            try {
                for (ProjectParticipation pp : projectDAO.getByDeveloperId(emp.getId())) {
                    String end = (pp.getEndDate() != null && !pp.getEndDate().isEmpty()) ? pp.getEndDate() : "진행중";
                    projectModel.addRow(new Object[]{pp.getProjectName(), pp.getProjectRole(), pp.getStartDate(), end});
                    projectIds.add(pp.getProjectId());
                }
            } catch (Exception ignored) {}
        }

        // 휴가
        leaveModel.setRowCount(0);
        leaveIds.clear();
        try {
            for (LeaveRecord lr : leaveDAO.getByEmployeeId(emp.getId())) {
                leaveModel.addRow(new Object[]{lr.getLeaveType(), lr.getStartDate(), lr.getEndDate()});
                leaveIds.add(lr.getId());
            }
        } catch (Exception ignored) {}

        // 스터디
        studyModel.setRowCount(0);
        studyIds.clear();
        try {
            for (StudyParticipation sp : studyDAO.getByEmployeeId(emp.getId())) {
                studyModel.addRow(new Object[]{sp.getStudyName()});
                studyIds.add(sp.getStudyId());
            }
        } catch (Exception ignored) {}
    }
}
