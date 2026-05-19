package com.example.swing.panel;

import com.example.dao.*;
import com.example.model.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeDetailPanel extends JPanel {

    private final HrRecordDAO hrRecordDAO = new HrRecordDAO();
    private final DeveloperDAO developerDAO = new DeveloperDAO();
    private final ManagementDAO managementDAO = new ManagementDAO();
    private final CareerDAO careerDAO = new CareerDAO();
    private final LeaveDAO leaveDAO = new LeaveDAO();
    private final ProjectParticipationDAO projectDAO = new ProjectParticipationDAO();
    private final StudyParticipationDAO studyDAO = new StudyParticipationDAO();

    // 헤더 라벨
    private final JLabel nameLabel = new JLabel();
    private final JLabel gradeLabel = new JLabel();
    private final JLabel deptLabel = new JLabel();
    private final JLabel hireDateLabel = new JLabel();
    private final JLabel promotionLabel = new JLabel();
    private final JLabel techLabel = new JLabel();
    private final JPanel techRow;

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
        addHeaderCell(headerPanel, gc, 0, 0, "이름", nameLabel, new Color(70, 130, 180));
        addHeaderCell(headerPanel, gc, 2, 0, "직급", gradeLabel, new Color(70, 130, 180));
        addHeaderCell(headerPanel, gc, 4, 0, "부서", deptLabel, new Color(70, 130, 180));
        addHeaderCell(headerPanel, gc, 6, 0, "입사일", hireDateLabel, new Color(34, 139, 34));
        addHeaderCell(headerPanel, gc, 8, 0, "승진일", promotionLabel, new Color(34, 139, 34));

        // 두 번째 줄: 기술스택 (개발자만 표시)
        techRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        techRow.setBackground(new Color(245, 248, 255));
        JLabel techKey = new JLabel("기술스택:");
        techKey.setForeground(new Color(160, 82, 45));
        techKey.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        techLabel.setForeground(new Color(160, 82, 45));
        techLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        techRow.add(techKey);
        techRow.add(techLabel);

        gc.gridx = 0; gc.gridy = 1;
        gc.gridwidth = 10;
        gc.fill = GridBagConstraints.HORIZONTAL;
        headerPanel.add(techRow, gc);

        // 세 번째 줄: 권한단계 (경영관리만 표시)
        permissionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        permissionRow.setBackground(new Color(245, 248, 255));
        JLabel permKey = new JLabel("권한단계:");
        permKey.setForeground(new Color(128, 0, 128));
        permKey.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        permissionLabel.setForeground(new Color(128, 0, 128));
        permissionLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        permissionRow.add(permKey);
        permissionRow.add(permissionLabel);

        gc.gridy = 2;
        headerPanel.add(permissionRow, gc);
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;

        // 탭 (프로젝트 탭도 생성자에서 미리 추가하여 MainFrame.styleAllTables가 헤더 스타일을 적용하도록 함)
        tabs.addTab("경력", new JScrollPane(makeTable(careerModel)));
        tabs.addTab("프로젝트", new JScrollPane(makeTable(projectModel)));
        tabs.addTab("휴가", new JScrollPane(makeTable(leaveModel)));
        tabs.addTab("스터디", new JScrollPane(makeTable(studyModel)));
        projectTabIndex = 1;

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

    public void loadEmployee(Employee emp) {
        // 기본 정보
        nameLabel.setText(emp.getEmployeeName());
        gradeLabel.setText(emp.getGrade());
        deptLabel.setText(emp.getDepartment());

        // 인사 기록 (입사일, 승진일)
        try {
            HrRecord hr = hrRecordDAO.getByEmployeeId(emp.getId());
            hireDateLabel.setText(hr != null && hr.getEmploymentData() != null ? hr.getEmploymentData() : "-");
            promotionLabel.setText(hr != null && hr.getPromotionDate() != null ? hr.getPromotionDate() : "-");
        } catch (Exception e) {
            hireDateLabel.setText("-");
            promotionLabel.setText("-");
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
        if (isDev) {
            try {
                for (ProjectParticipation pp : projectDAO.getByDeveloperId(emp.getId())) {
                    String end = (pp.getEndDate() != null && !pp.getEndDate().isEmpty()) ? pp.getEndDate() : "진행중";
                    projectModel.addRow(new Object[]{pp.getProjectName(), pp.getProjectRole(), pp.getStartDate(), end});
                }
            } catch (Exception ignored) {}
        }

        // 휴가
        leaveModel.setRowCount(0);
        try {
            for (LeaveRecord lr : leaveDAO.getByEmployeeId(emp.getId())) {
                leaveModel.addRow(new Object[]{lr.getLeaveType(), lr.getStartDate(), lr.getEndDate()});
            }
        } catch (Exception ignored) {}

        // 스터디
        studyModel.setRowCount(0);
        try {
            for (StudyParticipation sp : studyDAO.getByEmployeeId(emp.getId())) {
                studyModel.addRow(new Object[]{sp.getStudyName()});
            }
        } catch (Exception ignored) {}
    }
}
