package com.example.swing.dialog;

import com.example.dao.LeaveDAO;
import com.example.model.Employee;
import com.example.model.LeaveRecord;
import com.example.util.ComboAutoComplete;
import com.example.util.UserSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LeaveDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Employee> employeeBox = new JComboBox<>();
    private final JComboBox<String> leaveTypeBox;
    private final JFormattedTextField startDateField = makeDateField();
    private final JFormattedTextField endDateField   = makeDateField();
    private final JLabel remainingLabel = new JLabel();

    private final LeaveDAO dao;
    private final boolean isAdmin;
    private final int myId;
    private final LeaveRecord originalRecord;
    private boolean saved = false;
    private final boolean isEdit;

    public LeaveDialog(JFrame parent, LeaveRecord record, LeaveDAO dao) {
        super(parent, record == null ? "휴가 추가" : "휴가 수정", true);
        this.dao = dao;
        this.isEdit = record != null;
        this.originalRecord = record;
        this.isAdmin = UserSession.getInstance().isAdmin();
        this.myId = UserSession.getInstance().getEmployeeId();

        leaveTypeBox = isAdmin
            ? new JComboBox<>(new String[]{"연가", "공가"})
            : new JComboBox<>(new String[]{"연가"});

        try {
            List<Employee> employees = dao.getAllEmployees();
            for (Employee e : employees) employeeBox.addItem(e);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "직원 목록 로드 실패: " + ex.getMessage());
        }

        if (!isAdmin) {
            for (int i = 0; i < employeeBox.getItemCount(); i++) {
                if (employeeBox.getItemAt(i).getId() == myId) { employeeBox.setSelectedIndex(i); break; }
            }
            employeeBox.setEnabled(false);
        } else {
            ComboAutoComplete.apply(employeeBox);
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
        lc.gridy = 4; fc.gridy = 4; form.add(new JLabel("시작일:"), lc); form.add(startDateField, fc);
        lc.gridy = 5; fc.gridy = 5; form.add(new JLabel("종료일:"), lc); form.add(endDateField, fc);

        idField.setEditable(false);
        if (isEdit) {
            idField.setText(String.valueOf(record.getId()));
            for (int i = 0; i < employeeBox.getItemCount(); i++) {
                if (employeeBox.getItemAt(i).getId() == record.getEmployeeId()) { employeeBox.setSelectedIndex(i); break; }
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
        Employee emp = getSelectedEmployee();
        if (!"연가".equals(leaveTypeBox.getSelectedItem()) || emp == null) {
            remainingLabel.setText("");
            return;
        }
        try {
            int remaining = dao.getRemainingLeaveDays(emp.getId());
            remainingLabel.setText("잔여 연차: " + remaining + "일");
        } catch (Exception ex) {
            remainingLabel.setText("잔여 연차 조회 실패");
        }
    }

    private Employee getSelectedEmployee() {
        Object sel = employeeBox.getSelectedItem();
        return sel instanceof Employee ? (Employee) sel : null;
    }

    private static boolean isValidDate(String date) {
        return date != null && date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private boolean validateDates() {
        String start = startDateField.getText().trim();
        String end   = endDateField.getText().trim();
        if (isValidDate(start) && isValidDate(end) && end.compareTo(start) < 0) {
            endDateField.setBackground(new Color(255, 200, 200));
            return false;
        }
        endDateField.setBackground(UIManager.getColor("TextField.background"));
        return true;
    }

    private void save() {
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();
        Employee emp = getSelectedEmployee();

        if (emp == null) { JOptionPane.showMessageDialog(this, "직원을 선택하세요."); return; }
        if (!isValidDate(startDate)) { JOptionPane.showMessageDialog(this, "시작일을 올바르게 입력하세요. (YYYY-MM-DD)", "날짜 오류", JOptionPane.ERROR_MESSAGE); return; }
        if (!isValidDate(endDate))   { JOptionPane.showMessageDialog(this, "종료일을 올바르게 입력하세요. (YYYY-MM-DD)", "날짜 오류", JOptionPane.ERROR_MESSAGE); return; }
        if (!validateDates()) {
            JOptionPane.showMessageDialog(this, "종료일이 시작일보다 이전일 수 없습니다.", "날짜 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isAdmin) {
            try {
                if (LocalDate.parse(startDate).isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "시작일은 오늘 이후여야 합니다.", "날짜 오류", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "시작일 형식이 올바르지 않습니다. (YYYY-MM-DD)", "날짜 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String leaveType = (String) leaveTypeBox.getSelectedItem();
        if ("연가".equals(leaveType)) {
            try {
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                long netAdditional;

                if (!isEdit) {
                    List<LeaveRecord> overlaps = dao.getOverlappingRecords(emp.getId(), "연가", startDate, endDate);
                    if (overlaps.isEmpty()) {
                        netAdditional = ChronoUnit.DAYS.between(start, end) + 1;
                    } else {
                        LocalDate mergedStart = start;
                        LocalDate mergedEnd = end;
                        long existingDays = 0;
                        for (LeaveRecord o : overlaps) {
                            LocalDate oStart = LocalDate.parse(o.getStartDate());
                            LocalDate oEnd = LocalDate.parse(o.getEndDate());
                            if (oStart.isBefore(mergedStart)) mergedStart = oStart;
                            if (oEnd.isAfter(mergedEnd)) mergedEnd = oEnd;
                            existingDays += ChronoUnit.DAYS.between(oStart, oEnd) + 1;
                        }
                        netAdditional = ChronoUnit.DAYS.between(mergedStart, mergedEnd) + 1 - existingDays;
                    }
                } else {
                    long originalDays = ChronoUnit.DAYS.between(
                        LocalDate.parse(originalRecord.getStartDate()),
                        LocalDate.parse(originalRecord.getEndDate())) + 1;
                    long newDays = ChronoUnit.DAYS.between(start, end) + 1;
                    netAdditional = newDays - originalDays;
                }

                if (netAdditional > 0) {
                    int remaining = dao.getRemainingLeaveDays(emp.getId());
                    if (remaining < netAdditional) {
                        JOptionPane.showMessageDialog(this,
                            "잔여 연차가 부족합니다. (잔여: " + remaining + "일, 추가 필요: " + netAdditional + "일)\n"
                            + "마이너스 연차는 사용 불가합니다. 다시 입력하여 등록하세요.",
                            "연차 부족", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)", "날짜 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            LeaveRecord lr = new LeaveRecord(id, emp.getId(), emp.getEmployeeName(), leaveType, startDate, endDate);
            if (isEdit) dao.update(lr); else dao.insertOrMerge(lr);
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID 값이 올바르지 않습니다.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }

    private static JFormattedTextField makeDateField() {
        try {
            javax.swing.text.MaskFormatter fmt = new javax.swing.text.MaskFormatter("####-##-##");
            fmt.setPlaceholderCharacter('_');
            fmt.setAllowsInvalid(false);
            JFormattedTextField f = new JFormattedTextField(fmt);
            f.setColumns(10);
            f.setFocusLostBehavior(JFormattedTextField.PERSIST);
            return f;
        } catch (java.text.ParseException ex) {
            return new JFormattedTextField();
        }
    }
}
