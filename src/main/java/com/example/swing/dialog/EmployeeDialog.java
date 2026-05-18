package com.example.swing.dialog;

import com.example.dao.EmployeeDAO;
import com.example.model.Employee;

import javax.swing.*;
import java.awt.*;

public class EmployeeDialog extends JDialog {

    private static final String[] GRADES = {"사원", "대리", "과장", "부장", "이사"};
    private static final String[] DEPARTMENTS = {"개발자", "마케팅", "경영관리", "연구개발"};

    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(15);
    private final JComboBox<String> gradeBox = new JComboBox<>(GRADES);
    private final JComboBox<String> deptBox = new JComboBox<>(DEPARTMENTS);
    private final JTextField residentField = new JTextField(15);
    private final JTextField educationField = new JTextField(20);

    private final EmployeeDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public EmployeeDialog(JFrame parent, Employee emp, EmployeeDAO dao) {
        super(parent, emp == null ? "직원 추가" : "직원 수정", true);
        this.dao = dao;
        this.isEdit = emp != null;

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

        addRow(form, lc, fc, 0, "ID:", idField);
        addRow(form, lc, fc, 1, "이름:", nameField);
        addRow(form, lc, fc, 2, "직급:", gradeBox);
        addRow(form, lc, fc, 3, "부서:", deptBox);
        addRow(form, lc, fc, 4, "주민번호 (XXXXXX-XXXXXXX):", residentField);
        addRow(form, lc, fc, 5, "학력:", educationField);

        idField.setEditable(false);
        if (isEdit) {
            idField.setText(String.valueOf(emp.getId()));
            nameField.setText(emp.getEmployeeName());
            gradeBox.setSelectedItem(emp.getGrade());
            deptBox.setSelectedItem(emp.getDepartment());
            residentField.setText(emp.getResidentNumber());
            educationField.setText(emp.getEducation());
        } else {
            try { idField.setText(String.valueOf(dao.nextId())); } catch (Exception e) { idField.setText("1"); }
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void addRow(JPanel form, GridBagConstraints lc, GridBagConstraints fc, int row, String label, JComponent field) {
        lc.gridy = row;
        fc.gridy = row;
        form.add(new JLabel(label), lc);
        form.add(field, fc);
    }

    private void save() {
        String name = nameField.getText().trim();
        String resident = residentField.getText().trim();
        String education = educationField.getText().trim();

        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "이름을 입력하세요."); return; }
        if (resident.isEmpty()) { JOptionPane.showMessageDialog(this, "주민번호를 입력하세요."); return; }
        if (education.isEmpty()) { JOptionPane.showMessageDialog(this, "학력을 입력하세요."); return; }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            Employee emp = new Employee(id, name,
                (String) gradeBox.getSelectedItem(), resident, education,
                (String) deptBox.getSelectedItem());
            if (isEdit) dao.update(emp); else dao.insert(emp);
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
