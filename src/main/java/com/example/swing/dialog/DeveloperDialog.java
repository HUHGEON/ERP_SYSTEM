package com.example.swing.dialog;

import com.example.dao.DeveloperDAO;
import com.example.model.Developer;
import com.example.model.Employee;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DeveloperDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Employee> employeeBox = new JComboBox<>();
    private final JTextField techField = new JTextField(20);

    private final DeveloperDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public DeveloperDialog(JFrame parent, Developer dev, DeveloperDAO dao) {
        super(parent, dev == null ? "개발자 추가" : "개발자 수정", true);
        this.dao = dao;
        this.isEdit = dev != null;

        if (!isEdit) {
            try {
                List<Employee> employees = dao.getAvailableEmployees();
                for (Employee e : employees) employeeBox.addItem(e);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "직원 목록 로드 실패: " + ex.getMessage());
            }
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = labelConstraints();
        GridBagConstraints fc = fieldConstraints();

        idField.setEditable(false);
        if (isEdit) {
            idField.setText(String.valueOf(dev.getId()));
            JTextField nameField = new JTextField(dev.getEmployeeName());
            nameField.setEditable(false);
            techField.setText(dev.getTech() != null ? dev.getTech() : "");
            lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("ID:"), lc); form.add(idField, fc);
            lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("직원명:"), lc); form.add(nameField, fc);
            lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("보유기술:"), lc); form.add(techField, fc);
        } else {
            lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("직원 선택:"), lc); form.add(employeeBox, fc);
            lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("보유기술:"), lc); form.add(techField, fc);
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
        try {
            if (isEdit) {
                int id = Integer.parseInt(idField.getText().trim());
                dao.update(new Developer(id, null, techField.getText().trim()));
            } else {
                if (employeeBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "직원을 선택하세요."); return; }
                Employee emp = (Employee) employeeBox.getSelectedItem();
                dao.insert(new Developer(emp.getId(), emp.getEmployeeName(), techField.getText().trim()));
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }

    private GridBagConstraints labelConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST; c.insets = new Insets(5, 5, 5, 8); return c;
    }
    private GridBagConstraints fieldConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0;
        c.insets = new Insets(5, 0, 5, 5); c.gridwidth = GridBagConstraints.REMAINDER; return c;
    }
}
