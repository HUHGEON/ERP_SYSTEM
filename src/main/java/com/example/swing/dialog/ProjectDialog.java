package com.example.swing.dialog;

import com.example.dao.ProjectDAO;
import com.example.model.Customer;
import com.example.model.Project;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProjectDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(20);
    private final JComboBox<Customer> customerBox = new JComboBox<>();
    private final JTextField startDateField = new JTextField(12);
    private final JTextField endDateField = new JTextField(12);

    private final ProjectDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public ProjectDialog(JFrame parent, Project project, ProjectDAO dao) {
        super(parent, project == null ? "프로젝트 추가" : "프로젝트 수정", true);
        this.dao = dao;
        this.isEdit = project != null;

        try {
            List<Customer> customers = dao.getAllCustomers();
            for (Customer c : customers) customerBox.addItem(c);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "발주처 목록 로드 실패: " + ex.getMessage());
        }

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

        addRow(form, lc, fc, 0, "프로젝트명:", nameField);
        addRow(form, lc, fc, 1, "발주처:", customerBox);
        addRow(form, lc, fc, 2, "시작일 (YYYY-MM-DD):", startDateField);
        addRow(form, lc, fc, 3, "종료일 (YYYY-MM-DD, 미입력=진행중):", endDateField);

        idField.setEditable(false);
        if (isEdit) {
            idField.setText(String.valueOf(project.getId()));
            nameField.setText(project.getProjectName());
            for (int i = 0; i < customerBox.getItemCount(); i++) {
                if (customerBox.getItemAt(i).getId() == project.getCustomerId()) {
                    customerBox.setSelectedIndex(i);
                    break;
                }
            }
            startDateField.setText(project.getStartDate() != null ? project.getStartDate() : "");
            endDateField.setText(project.getEndDate() != null ? project.getEndDate() : "");
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
        String startDate = startDateField.getText().trim();

        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "프로젝트명을 입력하세요."); return; }
        if (startDate.isEmpty()) { JOptionPane.showMessageDialog(this, "시작일을 입력하세요."); return; }
        if (customerBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "발주처를 선택하세요."); return; }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            Customer customer = (Customer) customerBox.getSelectedItem();
            String endDate = endDateField.getText().trim();

            Project p = new Project(id, customer.getId(), customer.getCustomerName(),
                name, startDate, endDate.isEmpty() ? null : endDate);
            if (isEdit) dao.update(p); else dao.insert(p);
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
