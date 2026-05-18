package com.example.swing.panel;

import com.example.dao.EmployeeDAO;
import com.example.model.Employee;
import com.example.swing.dialog.EmployeeDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeePanel extends JPanel {

    private static final String[] GRADES = {"", "사원", "대리", "과장", "부장", "이사"};
    private static final String[] DEPARTMENTS = {"", "개발자", "마케팅", "경영관리", "연구개발"};
    private static final String[] COLUMNS = {"ID", "이름", "직급", "부서", "주민번호", "학력"};

    private final EmployeeDAO dao = new EmployeeDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField(12);
    private final JComboBox<String> gradeBox = new JComboBox<>(GRADES);
    private final JComboBox<String> deptBox = new JComboBox<>(DEPARTMENTS);

    private List<Employee> currentList;

    public EmployeePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("이름:"));
        searchPanel.add(nameField);
        searchPanel.add(new JLabel("직급:"));
        searchPanel.add(gradeBox);
        searchPanel.add(new JLabel("부서:"));
        searchPanel.add(deptBox);
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

        searchBtn.addActionListener(e -> loadData());
        resetBtn.addActionListener(e -> {
            nameField.setText("");
            gradeBox.setSelectedIndex(0);
            deptBox.setSelectedIndex(0);
            loadData();
        });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showInfo("수정할 직원을 선택하세요."); return; }
            openDialog(currentList.get(row));
        });
        deleteBtn.addActionListener(e -> deleteSelected());

        loadData();
    }

    private void loadData() {
        try {
            currentList = dao.search(
                nameField.getText().trim(),
                (String) gradeBox.getSelectedItem(),
                (String) deptBox.getSelectedItem()
            );
            tableModel.setRowCount(0);
            for (Employee e : currentList) {
                tableModel.addRow(new Object[]{
                    e.getId(), e.getEmployeeName(), e.getGrade(),
                    e.getDepartment(), e.getResidentNumber(), e.getEducation()
                });
            }
        } catch (Exception ex) {
            showError("데이터 로드 오류: " + ex.getMessage());
        }
    }

    private void openDialog(Employee emp) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EmployeeDialog dialog = new EmployeeDialog(frame, emp, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { showInfo("삭제할 직원을 선택하세요."); return; }
        Employee emp = currentList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
            "'" + emp.getEmployeeName() + "' 직원을 삭제하시겠습니까?",
            "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dao.delete(emp.getId());
                loadData();
            } catch (Exception ex) {
                showError("삭제 오류: " + ex.getMessage());
            }
        }
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE);
    }
}
