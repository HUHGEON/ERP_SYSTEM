package com.example.swing.panel;

import com.example.dao.OutputDAO;
import com.example.model.Output;
import com.example.swing.dialog.OutputDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OutputPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "프로젝트명", "산출물 유형", "산출물명"};

    private final OutputDAO dao = new OutputDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField projectField = new JTextField(12);
    private final JTextField typeField = new JTextField(10);
    private List<Output> currentList;

    public OutputPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("프로젝트명:")); searchPanel.add(projectField);
        searchPanel.add(new JLabel("산출물 유형:")); searchPanel.add(typeField);
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

        searchBtn.addActionListener(e -> loadData());
        resetBtn.addActionListener(e -> { projectField.setText(""); typeField.setText(""); loadData(); });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "수정할 산출물을 선택하세요."); return; }
            openDialog(currentList.get(row));
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        loadData();
    }

    private void loadData() {
        try {
            currentList = dao.search(projectField.getText().trim(), typeField.getText().trim());
            tableModel.setRowCount(0);
            for (Output o : currentList)
                tableModel.addRow(new Object[]{o.getId(), o.getProjectName(), o.getOutputType(), o.getOutputName()});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE); }
    }

    private void openDialog(Output output) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        OutputDialog dialog = new OutputDialog(frame, output, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "삭제할 산출물을 선택하세요."); return; }
        Output o = currentList.get(row);
        if (JOptionPane.showConfirmDialog(this, "'" + o.getOutputName() + "' 산출물을 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.delete(o.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "삭제 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); }
        }
    }
}
