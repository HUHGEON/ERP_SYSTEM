package com.example.swing.dialog;

import com.example.dao.LeaveDAO;
import com.example.model.Employee;
import com.example.model.LeaveRecord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class LeaveDialog extends JDialog {

    private static final String[] LEAVE_TYPES = {"연가", "공가"};

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Employee> employeeBox = new JComboBox<>();
    private final JComboBox<String> leaveTypeBox = new JComboBox<>(LEAVE_TYPES);
    private final JTextField startDateField = new JTextField(12);
    private final JTextField endDateField = new JTextField(12);
    private final JLabel remainingLabel = new JLabel();

    private final LeaveDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public LeaveDialog(JFrame parent, LeaveRecord record, LeaveDAO dao) {
        super(parent, record == null ? "휴가 추가" : "휴가 수정", true);
        this.dao = dao;
        this.isEdit = record != null;

        try {
            List<Employee> employees = dao.getAllEmployees();
            for (Employee e : employees) employeeBox.addItem(e);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "직원 목록 로드 실패: " + ex.getMessage());
        }

        remainingLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        remainingLabel.setForeground(new Color(34, 120, 34));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets = new Insets(5, 5, 5, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(5, 0, 5, 5);
        fc.gridwidth = GridBagConstraints.REMAINDER;

        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("직원:"), lc); form.add(employeeBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("휴가 종류:"), lc); form.add(leaveTypeBox, fc);
        lc.gridy = 3; fc.gridy = 3; form.add(new JLabel(""), lc); form.add(remainingLabel, fc);
        lc.gridy = 4; fc.gridy = 4; form.add(new JLabel("시작일 (YYYY-MM-DD):"), lc); form.add(startDateField, fc);
        lc.gridy = 5; fc.gridy = 5; form.add(new JLabel("종료일 (YYYY-MM-DD):"), lc); form.add(endDateField, fc);

        idField.setEditable(false);
        if (isEdit) {
            idField.setText(String.valueOf(record.getId()));
            for (int i = 0; i < employeeBox.getItemCount(); i++) {
                if (employeeBox.getItemAt(i).getId() == record.getEmployeeId()) {
                    employeeBox.setSelectedIndex(i);
                    break;
                }
            }
            leaveTypeBox.setSelectedItem(record.getLeaveType());
            startDateField.setText(record.getStartDate() != null ? record.getStartDate() : "");
            endDateField.setText(record.getEndDate() != null ? record.getEndDate() : "");
        } else {
            try { idField.setText(String.valueOf(dao.nextId())); } catch (Exception e) { idField.setText("1"); }
        }

        updateRemainingLabel();
        employeeBox.addActionListener(e -> updateRemainingLabel());
        leaveTypeBox.addActionListener(e -> updateRemainingLabel());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        endDateField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validateDates(); }
        });
        startDateField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validateDates(); }
        });

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void updateRemainingLabel() {
        boolean isYeonga = "연가".equals(leaveTypeBox.getSelectedItem());
        if (!isYeonga || employeeBox.getSelectedItem() == null) {
            remainingLabel.setText("");
            return;
        }
        try {
            Employee emp = (Employee) employeeBox.getSelectedItem();
            int remaining = dao.getRemainingLeaveDays(emp.getId());
            remainingLabel.setText("잔여 연차: " + remaining + "일");
        } catch (Exception ex) {
            remainingLabel.setText("잔여 연차 조회 실패");
        }
    }

    private boolean validateDates() {
        String start = startDateField.getText().trim();
        String end = endDateField.getText().trim();
        if (!start.isEmpty() && !end.isEmpty() && end.compareTo(start) < 0) {
            endDateField.setBackground(new Color(255, 200, 200));
            return false;
        }
        endDateField.setBackground(UIManager.getColor("TextField.background"));
        return true;
    }

    private void save() {
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();

        if (employeeBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "직원을 선택하세요."); return; }
        if (startDate.isEmpty()) { JOptionPane.showMessageDialog(this, "시작일을 입력하세요."); return; }
        if (endDate.isEmpty()) { JOptionPane.showMessageDialog(this, "종료일을 입력하세요."); return; }
        if (!validateDates()) {
            JOptionPane.showMessageDialog(this, "종료일이 시작일보다 이전일 수 없습니다.", "날짜 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            Employee emp = (Employee) employeeBox.getSelectedItem();
            LeaveRecord lr = new LeaveRecord(id, emp.getId(), emp.getEmployeeName(),
                (String) leaveTypeBox.getSelectedItem(), startDate, endDate);
            if (isEdit) dao.update(lr); else dao.insert(lr);
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID 값이 올바르지 않습니다.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
