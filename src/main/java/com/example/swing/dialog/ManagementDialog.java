package com.example.swing.dialog;

import com.example.dao.ManagementDAO;
import com.example.model.Employee;
import com.example.model.Management;
import com.example.util.ComboAutoComplete;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ManagementDialog extends JDialog {

    private final JComboBox<Employee> employeeBox = new JComboBox<>();
    private final JTextField permissionField = new JTextField(15);
    private final JTextField idField = new JTextField(10);

    private final ManagementDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public ManagementDialog(JFrame parent, Management mgmt, ManagementDAO dao) {
        super(parent, mgmt == null ? "경영관리 추가" : "경영관리 수정", true);
        this.dao = dao;
        this.isEdit = mgmt != null;

        if (!isEdit) {
            try {
                List<Employee> employees = dao.getAvailableEmployees();
                for (Employee e : employees) employeeBox.addItem(e);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "직원 목록 로드 실패: " + ex.getMessage());
            }
            ComboAutoComplete.apply(employeeBox);
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        if (isEdit) {
            idField.setEditable(false);
            idField.setText(String.valueOf(mgmt.getId()));
            JTextField nameField = new JTextField(mgmt.getEmployeeName());
            nameField.setEditable(false);
            permissionField.setText(mgmt.getPermissionLevel());
            lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("직원명:"), lc); form.add(nameField, fc);
            lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("권한단계:"), lc); form.add(permissionField, fc);
        } else {
            lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("직원 선택:"), lc); form.add(employeeBox, fc);
            lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("권한단계:"), lc); form.add(permissionField, fc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("저장"); JButton cancelBtn = new JButton("취소");
        btnPanel.add(saveBtn); btnPanel.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);
        pack(); setResizable(false); setLocationRelativeTo(parent);
    }

    private void save() {
        String perm = permissionField.getText().trim();
        if (perm.isEmpty()) { JOptionPane.showMessageDialog(this, "권한단계를 입력하세요."); return; }
        try {
            if (isEdit) {
                int id = Integer.parseInt(idField.getText().trim());
                dao.update(new Management(id, null, perm));
            } else {
                if (employeeBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "직원을 선택하세요."); return; }
                Employee emp = (Employee) employeeBox.getSelectedItem();
                dao.insert(new Management(emp.getId(), emp.getEmployeeName(), perm));
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    private GridBagConstraints lc() { GridBagConstraints c = new GridBagConstraints(); c.anchor = GridBagConstraints.EAST; c.insets = new Insets(5,5,5,8); return c; }
    private GridBagConstraints fc() { GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0; c.insets = new Insets(5,0,5,5); c.gridwidth = GridBagConstraints.REMAINDER; return c; }
}
