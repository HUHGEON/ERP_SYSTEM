package com.example.swing.panel;

import com.example.dao.ProjectParticipationDAO;
import com.example.model.ProjectParticipation;
import com.example.swing.dialog.ProjectParticipationDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProjectParticipationPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "프로젝트명", "개발자명", "역할", "투입일", "종료일"};

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int myId = UserSession.getInstance().getEmployeeId();

    private final ProjectParticipationDAO dao = new ProjectParticipationDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField projectField = new JTextField(12);
    private final JTextField devField = new JTextField(10);
    private List<ProjectParticipation> currentList;

    public ProjectParticipationPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("프로젝트명:")); searchPanel.add(projectField);
        searchPanel.add(new JLabel("개발자명:")); searchPanel.add(devField);
        JButton searchBtn = new JButton("검색"); JButton resetBtn = new JButton("초기화");
        searchPanel.add(searchBtn); searchPanel.add(resetBtn);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("추가"); JButton editBtn = new JButton("수정"); JButton deleteBtn = new JButton("삭제");
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn);

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        if (!isAdmin) {
            searchPanel.setVisible(false);
            addBtn.setVisible(false);
            editBtn.setVisible(false);
            deleteBtn.setVisible(false);
        }

        searchBtn.addActionListener(e -> loadData());
        resetBtn.addActionListener(e -> { projectField.setText(""); devField.setText(""); loadData(); });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "수정할 항목을 선택하세요."); return; }
            openDialog(currentList.get(row));
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        loadData();
    }

    private void loadData() {
        try {
            currentList = isAdmin
                ? dao.search(projectField.getText().trim(), devField.getText().trim())
                : dao.getByDeveloperId(myId);
            tableModel.setRowCount(0);
            for (ProjectParticipation pp : currentList)
                tableModel.addRow(new Object[]{pp.getId(), pp.getProjectName(), pp.getDeveloperName(),
                    pp.getProjectRole(), pp.getStartDate(), pp.getEndDate() != null ? pp.getEndDate() : "-"});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE); }
    }

    private void openDialog(ProjectParticipation pp) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        ProjectParticipationDialog dialog = new ProjectParticipationDialog(frame, pp, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요."); return; }
        ProjectParticipation pp = currentList.get(row);
        if (JOptionPane.showConfirmDialog(this, "해당 프로젝트 투입 기록을 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.delete(pp.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "삭제 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); }
        }
    }
}
