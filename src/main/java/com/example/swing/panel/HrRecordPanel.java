package com.example.swing.panel;

import com.example.dao.HrRecordDAO;
import com.example.model.HrRecord;
import com.example.swing.dialog.HrRecordDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HrRecordPanel extends JPanel implements Refreshable {

    private static final String[] COLUMNS = {"ID", "직원명", "직급", "승진일"};

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int myId = UserSession.getInstance().getEmployeeId();

    private final HrRecordDAO dao = new HrRecordDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField(15);
    private List<HrRecord> currentList;

    public HrRecordPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("직원명:"));
        searchPanel.add(nameField);
        JButton searchBtn = new JButton("검색"); JButton resetBtn = new JButton("초기화");
        searchPanel.add(searchBtn); searchPanel.add(resetBtn);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(90);   // 직원명
        table.getColumnModel().getColumn(2).setPreferredWidth(65);   // 직급
        table.getColumnModel().getColumn(3).setPreferredWidth(90);   // 승진일

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("추가"); JButton editBtn = new JButton("수정"); JButton deleteBtn = new JButton("삭제");
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn);

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        if (!isAdmin) {
            addBtn.setVisible(false);
            editBtn.setVisible(false);
            deleteBtn.setVisible(false);
        }

        searchBtn.addActionListener(e -> loadData());
        resetBtn.addActionListener(e -> { nameField.setText(""); loadData(); });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "수정할 인사기록을 선택하세요."); return; }
            openDialog(currentList.get(table.convertRowIndexToModel(row)));
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        loadData();
    }

    @Override public void refresh() { loadData(); }

    private void loadData() {
        try {
            if (!isAdmin) {
                currentList = new ArrayList<>();
                HrRecord r = dao.getByEmployeeId(myId);
                if (r != null) currentList.add(r);
            } else {
                currentList = dao.search(nameField.getText().trim());
            }
            tableModel.setRowCount(0);
            for (HrRecord h : currentList)
                tableModel.addRow(new Object[]{h.getId(), h.getEmployeeName(), h.getPositionName(), h.getPromotionDate()});
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE); }
    }

    private void openDialog(HrRecord record) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        HrRecordDialog dialog = new HrRecordDialog(frame, record, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "삭제할 인사기록을 선택하세요."); return; }
        HrRecord h = currentList.get(table.convertRowIndexToModel(row));
        if (JOptionPane.showConfirmDialog(this, "해당 인사기록을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.delete(h.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "삭제 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); }
        }
    }
}
