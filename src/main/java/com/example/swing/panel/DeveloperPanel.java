package com.example.swing.panel;

import com.example.dao.DeveloperDAO;
import com.example.model.Developer;
import com.example.swing.dialog.DeveloperDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeveloperPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "직원명", "보유기술"};

    private final DeveloperDAO dao = new DeveloperDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField(15);
    private List<Developer> currentList;

    public DeveloperPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("직원명:"));
        searchPanel.add(nameField);
        JButton searchBtn = new JButton("검색");
        JButton resetBtn = new JButton("초기화");
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // 직원명
        table.getColumnModel().getColumn(2).setPreferredWidth(200);  // 보유기술

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("추가");
        JButton editBtn = new JButton("수정");
        JButton deleteBtn = new JButton("삭제");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> loadData());
        resetBtn.addActionListener(e -> { nameField.setText(""); loadData(); });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "수정할 개발자를 선택하세요."); return; }
            openDialog(currentList.get(table.convertRowIndexToModel(row)));
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        loadData();
    }

    private void loadData() {
        try {
            currentList = dao.search(nameField.getText().trim());
            tableModel.setRowCount(0);
            for (Developer d : currentList) tableModel.addRow(new Object[]{d.getId(), d.getEmployeeName(), d.getTech()});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE); }
    }

    private void openDialog(Developer dev) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        DeveloperDialog dialog = new DeveloperDialog(frame, dev, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "삭제할 개발자를 선택하세요."); return; }
        Developer d = currentList.get(table.convertRowIndexToModel(row));
        if (JOptionPane.showConfirmDialog(this, "'" + d.getEmployeeName() + "' 개발자를 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.delete(d.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "삭제 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); }
        }
    }
}
