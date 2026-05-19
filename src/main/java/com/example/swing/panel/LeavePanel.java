package com.example.swing.panel;

import com.example.dao.LeaveDAO;
import com.example.model.LeaveRecord;
import com.example.swing.dialog.LeaveDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LeavePanel extends JPanel {

    private static final String[] LEAVE_TYPES = {"", "연가", "공가"};
    private static final String[] COLUMNS = {"ID", "직원 이름", "휴가 종류", "시작일", "종료일"};

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int myId = UserSession.getInstance().getEmployeeId();

    private final LeaveDAO dao = new LeaveDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField(12);
    private final JComboBox<String> leaveTypeBox = new JComboBox<>(LEAVE_TYPES);

    private List<LeaveRecord> currentList;

    public LeavePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("직원 이름:"));
        searchPanel.add(nameField);
        searchPanel.add(new JLabel("휴가 종류:"));
        searchPanel.add(leaveTypeBox);
        JButton searchBtn = new JButton("검색");
        JButton resetBtn = new JButton("초기화");
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);

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

        if (!isAdmin) {
            searchPanel.setVisible(false);
            addBtn.setVisible(false);
            editBtn.setVisible(false);
            deleteBtn.setVisible(false);
        }

        searchBtn.addActionListener(e -> loadData());
        resetBtn.addActionListener(e -> {
            nameField.setText("");
            leaveTypeBox.setSelectedIndex(0);
            loadData();
        });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("수정할 휴가 기록을 선택하세요."); return; }
            openDialog(currentList.get(row));
        });
        deleteBtn.addActionListener(e -> deleteSelected());

        loadData();
    }

    private void loadData() {
        try {
            currentList = isAdmin
                ? dao.search(nameField.getText().trim(), (String) leaveTypeBox.getSelectedItem())
                : dao.getByEmployeeId(myId);
            tableModel.setRowCount(0);
            for (LeaveRecord lr : currentList) {
                tableModel.addRow(new Object[]{
                    lr.getId(), lr.getEmployeeName(), lr.getLeaveType(),
                    lr.getStartDate(), lr.getEndDate()
                });
            }
        } catch (Exception ex) {
            showError("데이터 로드 오류: " + ex.getMessage());
        }
    }

    private void openDialog(LeaveRecord record) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        LeaveDialog dialog = new LeaveDialog(frame, record, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("삭제할 휴가 기록을 선택하세요."); return; }
        LeaveRecord lr = currentList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
            lr.getEmployeeName() + "의 휴가 기록을 삭제하시겠습니까?",
            "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dao.delete(lr.getId());
                loadData();
            } catch (Exception ex) {
                showError("삭제 오류: " + ex.getMessage());
            }
        }
    }

    /** 외부 호출: 휴가 id로 행 선택. 필터/권한으로 안 보이면 필터 초기화 후 재시도. */
    public void selectLeaveById(int leaveId) {
        if (!selectRow(leaveId)) {
            if (isAdmin) {
                nameField.setText("");
                leaveTypeBox.setSelectedIndex(0);
            }
            loadData();
            selectRow(leaveId);
        }
    }

    private boolean selectRow(int leaveId) {
        if (currentList == null) return false;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getId() == leaveId) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return true;
            }
        }
        return false;
    }

    private void showInfo(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }
}
