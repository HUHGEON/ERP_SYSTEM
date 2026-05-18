package com.example.swing.panel;

import com.example.dao.CareerDAO;
import com.example.model.Career;
import com.example.swing.dialog.CareerDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CareerPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "직원명", "회사명", "입사일", "퇴사일"};

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int myId = UserSession.getInstance().getEmployeeId();

    private final CareerDAO dao = new CareerDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField(15);
    private List<Career> currentList;

    public CareerPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("직원명:"));
        searchPanel.add(nameField);
        JButton searchBtn = new JButton("검색");
        JButton resetBtn = new JButton("초기화");
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
        resetBtn.addActionListener(e -> { nameField.setText(""); loadData(); });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "수정할 경력을 선택하세요."); return; }
            openDialog(currentList.get(row));
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        loadData();
    }

    private void loadData() {
        try {
            currentList = isAdmin
                ? dao.search(nameField.getText().trim())
                : dao.getByEmployeeId(myId);
            tableModel.setRowCount(0);
            for (Career c : currentList)
                tableModel.addRow(new Object[]{c.getId(), c.getEmployeeName(), c.getCompanyName(), c.getStartTime(), c.getEndTime()});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE); }
    }

    private void openDialog(Career career) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        CareerDialog dialog = new CareerDialog(frame, career, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "삭제할 경력을 선택하세요."); return; }
        Career c = currentList.get(row);
        if (JOptionPane.showConfirmDialog(this, "해당 경력을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.delete(c.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "삭제 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); }
        }
    }
}
