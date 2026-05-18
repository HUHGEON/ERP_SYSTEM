package com.example.swing.panel;

import com.example.dao.OutputDAO;
import com.example.dao.ProjectDAO;
import com.example.model.Output;
import com.example.model.Project;
import com.example.swing.dialog.OutputDialog;
import com.example.swing.dialog.ProjectDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProjectPanel extends JPanel {

    private static final String[] STATUSES = {"", "진행중", "완료"};
    private static final String[] PROJ_COLS = {"ID", "프로젝트명", "발주처", "시작일", "종료일", "상태"};
    private static final String[] OUT_COLS  = {"ID", "산출물 유형", "산출물명"};

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final OutputDAO  outputDAO  = new OutputDAO();

    private final DefaultTableModel projectModel = new DefaultTableModel(PROJ_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel outputModel = new DefaultTableModel(OUT_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable projectTable = new JTable(projectModel);
    private final JTable outputTable  = new JTable(outputModel);

    private final JTextField nameField   = new JTextField(15);
    private final JComboBox<String> statusBox = new JComboBox<>(STATUSES);
    private final JLabel outputLabel = new JLabel("산출물 — 프로젝트를 선택하세요");

    private List<Project> projectList;
    private List<Output>  outputList;
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

        // ── 하단: 산출물 ──
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

        JPanel bottomPanel = new JPanel(new BorderLayout(3, 3));
        bottomPanel.add(outputLabel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(outputTable), BorderLayout.CENTER);
        bottomPanel.add(outBtnPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        split.setResizeWeight(0.55);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        // ── 이벤트 ──
        searchBtn.addActionListener(e -> loadProjects());
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
                    outputLabel.setText("산출물 — " + p.getProjectName());
                    loadOutputs();
                }
            }
        });

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
            projectModel.setRowCount(0);
            for (Project p : projectList) {
                projectModel.addRow(new Object[]{
                    p.getId(), p.getProjectName(), p.getCustomerName(),
                    p.getStartDate(), p.getEndDate() != null ? p.getEndDate() : "-", p.getStatus()
                });
            }
            selectedProjectId = -1;
            outputModel.setRowCount(0);
            outputLabel.setText("산출물 — 프로젝트를 선택하세요");
        } catch (Exception ex) { error("데이터 로드 오류: " + ex.getMessage()); }
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

    private void info(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }
}
