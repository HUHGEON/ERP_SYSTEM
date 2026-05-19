package com.example.swing.panel;

import com.example.dao.OutputDAO;
import com.example.dao.ProjectDAO;
import com.example.dao.ProjectParticipationDAO;
import com.example.model.Output;
import com.example.model.Project;
import com.example.model.ProjectParticipation;
import com.example.swing.dialog.OutputDialog;
import com.example.swing.dialog.ProjectDialog;
import com.example.swing.dialog.ProjectParticipationDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectPanel extends JPanel {

    private static final String[] STATUSES   = {"", "진행중", "완료"};
    private static final String[] PROJ_COLS  = {"ID", "프로젝트명", "발주처", "시작일", "종료일", "상태"};
    private static final String[] OUT_COLS   = {"ID", "산출물 유형", "산출물명"};
    private static final String[] MEM_COLS   = {"이름", "역할", "투입일", "종료일"};

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final OutputDAO  outputDAO  = new OutputDAO();
    private final ProjectParticipationDAO participationDAO = new ProjectParticipationDAO();

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int myId = UserSession.getInstance().getEmployeeId();

    private final DefaultTableModel projectModel = new DefaultTableModel(PROJ_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel outputModel = new DefaultTableModel(OUT_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel memberModel = new DefaultTableModel(MEM_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable projectTable = new JTable(projectModel);
    private final JTable outputTable  = new JTable(outputModel);
    private final JTable memberTable  = new JTable(memberModel);

    private final JTextField nameField   = new JTextField(15);
    private final JComboBox<String> statusBox = new JComboBox<>(STATUSES);
    private final JLabel memberLabel = new JLabel("투입 팀원 — 프로젝트를 선택하세요");
    private final JLabel outputLabel = new JLabel("산출물 — 프로젝트를 선택하세요");

    private List<Project>              projectList;
    private List<Output>               outputList;
    private List<ProjectParticipation> memberList;
    private int selectedProjectId = -1;

    public ProjectPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── 상단: 프로젝트 검색 ──
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("프로젝트명:"));
        searchPanel.add(nameField);
        searchPanel.add(new JLabel("상태:"));
        searchPanel.add(statusBox);
        JButton searchBtn = new JButton("검색");
        JButton resetBtn  = new JButton("초기화");
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        projectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectTable.getTableHeader().setReorderingAllowed(false);
        projectTable.setRowHeight(24);

        JPanel projBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addProjBtn    = new JButton("추가");
        JButton editProjBtn   = new JButton("수정");
        JButton deleteProjBtn = new JButton("삭제");
        projBtnPanel.add(addProjBtn);
        projBtnPanel.add(editProjBtn);
        projBtnPanel.add(deleteProjBtn);

        JPanel topPanel = new JPanel(new BorderLayout(3, 3));
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(new JScrollPane(projectTable), BorderLayout.CENTER);
        topPanel.add(projBtnPanel, BorderLayout.SOUTH);

        // ── 하단 왼쪽: 투입 팀원 ──
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getTableHeader().setReorderingAllowed(false);
        memberTable.setRowHeight(24);

        memberLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 0));
        memberLabel.setFont(memberLabel.getFont().deriveFont(Font.BOLD));

        JPanel memBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addMemBtn    = new JButton("투입 추가");
        JButton editMemBtn   = new JButton("투입 수정");
        JButton deleteMemBtn = new JButton("투입 삭제");
        memBtnPanel.add(addMemBtn);
        memBtnPanel.add(editMemBtn);
        memBtnPanel.add(deleteMemBtn);

        JPanel memberPanel = new JPanel(new BorderLayout(3, 3));
        memberPanel.add(memberLabel, BorderLayout.NORTH);
        memberPanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);
        memberPanel.add(memBtnPanel, BorderLayout.SOUTH);

        // ── 하단 오른쪽: 산출물 ──
        outputTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outputTable.getTableHeader().setReorderingAllowed(false);
        outputTable.setRowHeight(24);

        JPanel outBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addOutBtn    = new JButton("산출물 추가");
        JButton editOutBtn   = new JButton("산출물 수정");
        JButton deleteOutBtn = new JButton("산출물 삭제");
        outBtnPanel.add(addOutBtn);
        outBtnPanel.add(editOutBtn);
        outBtnPanel.add(deleteOutBtn);

        outputLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 0));
        outputLabel.setFont(outputLabel.getFont().deriveFont(Font.BOLD));

        JPanel outputPanel = new JPanel(new BorderLayout(3, 3));
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(outputTable), BorderLayout.CENTER);
        outputPanel.add(outBtnPanel, BorderLayout.SOUTH);

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, memberPanel, outputPanel);
        bottomSplit.setResizeWeight(0.5);
        bottomSplit.setDividerSize(5);

        JPanel bottomPanel = new JPanel(new BorderLayout(3, 3));
        bottomPanel.add(bottomSplit, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        split.setResizeWeight(0.55);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        // 비관리자는 검색/추가/수정/삭제 숨김 (본인 참여 프로젝트만 조회)
        if (!isAdmin) {
            searchPanel.setVisible(false);
            addProjBtn.setVisible(false);
            editProjBtn.setVisible(false);
            deleteProjBtn.setVisible(false);
            addMemBtn.setVisible(false);
            editMemBtn.setVisible(false);
            deleteMemBtn.setVisible(false);
            addOutBtn.setVisible(false);
            editOutBtn.setVisible(false);
            deleteOutBtn.setVisible(false);
        }

        // ── 이벤트 ──
        searchBtn.addActionListener(e -> loadProjects());
        nameField.addActionListener(e -> loadProjects());
        resetBtn.addActionListener(e -> { nameField.setText(""); statusBox.setSelectedIndex(0); loadProjects(); });

        addProjBtn.addActionListener(e -> openProjectDialog(null));
        editProjBtn.addActionListener(e -> {
            int row = projectTable.getSelectedRow();
            if (row < 0) { info("수정할 프로젝트를 선택하세요."); return; }
            openProjectDialog(projectList.get(row));
        });
        deleteProjBtn.addActionListener(e -> deleteProject());

        projectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = projectTable.getSelectedRow();
                if (row >= 0) {
                    Project p = projectList.get(row);
                    selectedProjectId = p.getId();
                    memberLabel.setText("투입 팀원 — " + p.getProjectName());
                    outputLabel.setText("산출물 — " + p.getProjectName());
                    loadMembers();
                    loadOutputs();
                }
            }
        });

        addMemBtn.addActionListener(e -> {
            if (selectedProjectId < 0) { info("프로젝트를 먼저 선택하세요."); return; }
            openParticipationDialog(null);
        });
        editMemBtn.addActionListener(e -> {
            int row = memberTable.getSelectedRow();
            if (row < 0) { info("수정할 투입 인원을 선택하세요."); return; }
            openParticipationDialog(memberList.get(row));
        });
        deleteMemBtn.addActionListener(e -> deleteMember());

        addOutBtn.addActionListener(e -> {
            if (selectedProjectId < 0) { info("프로젝트를 먼저 선택하세요."); return; }
            openOutputDialog(null);
        });
        editOutBtn.addActionListener(e -> {
            int row = outputTable.getSelectedRow();
            if (row < 0) { info("수정할 산출물을 선택하세요."); return; }
            openOutputDialog(outputList.get(row));
        });
        deleteOutBtn.addActionListener(e -> deleteOutput());

        loadProjects();
    }

    private void loadProjects() {
        try {
            projectList = projectDAO.search(nameField.getText().trim(), (String) statusBox.getSelectedItem());
            if (!isAdmin) {
                Set<Integer> myProjectIds = participationDAO.getByDeveloperId(myId).stream()
                    .map(ProjectParticipation::getProjectId)
                    .collect(Collectors.toCollection(HashSet::new));
                projectList = projectList.stream()
                    .filter(p -> myProjectIds.contains(p.getId()))
                    .collect(Collectors.toList());
            }
            projectModel.setRowCount(0);
            for (Project p : projectList) {
                projectModel.addRow(new Object[]{
                    p.getId(), p.getProjectName(), p.getCustomerName(),
                    p.getStartDate(), p.getEndDate() != null ? p.getEndDate() : "-", p.getStatus()
                });
            }
            selectedProjectId = -1;
            memberModel.setRowCount(0);
            outputModel.setRowCount(0);
            memberLabel.setText("투입 팀원 — 프로젝트를 선택하세요");
            outputLabel.setText("산출물 — 프로젝트를 선택하세요");
        } catch (Exception ex) { error("데이터 로드 오류: " + ex.getMessage()); }
    }

    private void loadMembers() {
        if (selectedProjectId < 0) return;
        try {
            memberList = participationDAO.getByProjectId(selectedProjectId);
            memberModel.setRowCount(0);
            for (ProjectParticipation pp : memberList) {
                String end = (pp.getEndDate() != null && !pp.getEndDate().isEmpty()) ? pp.getEndDate() : "진행중";
                memberModel.addRow(new Object[]{pp.getDeveloperName(), pp.getProjectRole(), pp.getStartDate(), end});
            }
        } catch (Exception ex) { error("팀원 로드 오류: " + ex.getMessage()); }
    }

    private void loadOutputs() {
        if (selectedProjectId < 0) return;
        try {
            outputList = outputDAO.searchByProjectId(selectedProjectId);
            outputModel.setRowCount(0);
            for (Output o : outputList)
                outputModel.addRow(new Object[]{o.getId(), o.getOutputType(), o.getOutputName()});
        } catch (Exception ex) { error("산출물 로드 오류: " + ex.getMessage()); }
    }

    private void openProjectDialog(Project p) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ProjectDialog dialog = new ProjectDialog(frame, p, projectDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadProjects();
    }

    private void openParticipationDialog(ProjectParticipation pp) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ProjectParticipationDialog dialog = new ProjectParticipationDialog(frame, pp, participationDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadMembers();
    }

    private void openOutputDialog(Output o) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        OutputDialog dialog = new OutputDialog(frame, o, outputDAO, selectedProjectId);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadOutputs();
    }

    private void deleteProject() {
        int row = projectTable.getSelectedRow();
        if (row < 0) { info("삭제할 프로젝트를 선택하세요."); return; }
        Project p = projectList.get(row);
        if (JOptionPane.showConfirmDialog(this, "'" + p.getProjectName() + "' 프로젝트를 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { projectDAO.delete(p.getId()); loadProjects(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    private void deleteMember() {
        int row = memberTable.getSelectedRow();
        if (row < 0) { info("삭제할 투입 인원을 선택하세요."); return; }
        ProjectParticipation pp = memberList.get(row);
        if (JOptionPane.showConfirmDialog(this, "'" + pp.getDeveloperName() + "' 투입 기록을 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { participationDAO.delete(pp.getId()); loadMembers(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    private void deleteOutput() {
        int row = outputTable.getSelectedRow();
        if (row < 0) { info("삭제할 산출물을 선택하세요."); return; }
        Output o = outputList.get(row);
        if (JOptionPane.showConfirmDialog(this, "'" + o.getOutputName() + "' 산출물을 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { outputDAO.delete(o.getId()); loadOutputs(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    /** 외부 호출: 프로젝트명으로 필터 후 해당 행 선택. */
    public void filterByProjectName(String projectName, int projectId) {
        nameField.setText(projectName);
        statusBox.setSelectedIndex(0);
        loadProjects();
        selectRow(projectId);
    }

    /** 외부에서 호출: 해당 프로젝트로 이동하여 행 선택. 필터에 가려져 있으면 필터 초기화 후 재시도. */
    public void selectProjectById(int projectId) {
        if (!selectRow(projectId)) {
            nameField.setText("");
            statusBox.setSelectedIndex(0);
            loadProjects();
            selectRow(projectId);
        }
    }

    private boolean selectRow(int projectId) {
        if (projectList == null) return false;
        for (int i = 0; i < projectList.size(); i++) {
            if (projectList.get(i).getId() == projectId) {
                projectTable.setRowSelectionInterval(i, i);
                projectTable.scrollRectToVisible(projectTable.getCellRect(i, 0, true));
                return true;
            }
        }
        return false;
    }

    private void info(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }
}
