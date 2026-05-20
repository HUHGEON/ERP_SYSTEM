package com.example.swing.panel;

import com.example.dao.StudyParticipationDAO;
import com.example.model.StudyParticipation;
import com.example.swing.dialog.StudyParticipationDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudyParticipationPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "스터디명", "직원명"};

    private final StudyParticipationDAO dao = new StudyParticipationDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField studyField = new JTextField(12);
    private final JTextField empField = new JTextField(10);
    private List<StudyParticipation> currentList;

    public StudyParticipationPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("스터디명:")); searchPanel.add(studyField);
        searchPanel.add(new JLabel("직원명:")); searchPanel.add(empField);
        JButton searchBtn = new JButton("검색"); JButton resetBtn = new JButton("초기화");
        searchPanel.add(searchBtn); searchPanel.add(resetBtn);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // 스터디명
        table.getColumnModel().getColumn(2).setPreferredWidth(90);   // 직원명

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("추가"); JButton editBtn = new JButton("수정"); JButton deleteBtn = new JButton("삭제");
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn);

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> loadData());
        resetBtn.addActionListener(e -> { studyField.setText(""); empField.setText(""); loadData(); });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "수정할 항목을 선택하세요."); return; }
            openDialog(currentList.get(table.convertRowIndexToModel(row)));
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        loadData();
    }

    private void loadData() {
        try {
            currentList = dao.search(studyField.getText().trim(), empField.getText().trim());
            tableModel.setRowCount(0);
            for (StudyParticipation sp : currentList)
                tableModel.addRow(new Object[]{sp.getId(), sp.getStudyName(), sp.getEmployeeName()});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE); }
    }

    private void openDialog(StudyParticipation sp) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        StudyParticipationDialog dialog = new StudyParticipationDialog(frame, sp, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요."); return; }
        StudyParticipation sp = currentList.get(table.convertRowIndexToModel(row));
        if (JOptionPane.showConfirmDialog(this, "해당 스터디 참여 기록을 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.delete(sp.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "삭제 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); }
        }
    }
}
