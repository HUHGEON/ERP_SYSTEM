package com.example.swing.dialog;

import com.example.dao.EmployeeDAO;
import com.example.dao.HrRecordDAO;
import com.example.model.Employee;
import com.example.model.HrRecord;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class HrRecordDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Employee> employeeBox = new JComboBox<>();
    private final JTextField employmentField = new JTextField(12);
    private final JTextField promotionField = new JTextField(12);

    private final HrRecordDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public HrRecordDialog(JFrame parent, HrRecord record, HrRecordDAO dao) {
        super(parent, record == null ? "인사기록 추가" : "인사기록 수정", true);
        this.dao = dao;
        this.isEdit = record != null;

        try {
            List<Employee> employees = new EmployeeDAO().search("", "", "");
            for (Employee e : employees) employeeBox.addItem(e);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "직원 목록 로드 실패: " + ex.getMessage());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("ID:"), lc); form.add(idField, fc);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("직원:"), lc); form.add(employeeBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("입사일 (YYYY-MM-DD):"), lc); form.add(employmentField, fc);
        lc.gridy = 3; fc.gridy = 3; form.add(new JLabel("승진일 (YYYY-MM-DD, 선택):"), lc); form.add(promotionField, fc);

        if (isEdit) {
            idField.setText(String.valueOf(record.getId()));
            for (int i = 0; i < employeeBox.getItemCount(); i++) {
                if (employeeBox.getItemAt(i).getId() == record.getEmployeeId()) { employeeBox.setSelectedIndex(i); break; }
            }
            employmentField.setText(record.getEmploymentData() != null ? record.getEmploymentData() : "");
            promotionField.setText(record.getPromotionDate() != null ? record.getPromotionDate() : "");
        } else {
            try { idField.setText(String.valueOf(dao.nextId())); } catch (Exception e) { idField.setText("1"); }
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
        String employment = employmentField.getText().trim();
        if (employment.isEmpty()) { JOptionPane.showMessageDialog(this, "입사일을 입력하세요."); return; }
        if (employeeBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "직원을 선택하세요."); return; }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Employee emp = (Employee) employeeBox.getSelectedItem();
            String promotion = promotionField.getText().trim();
            HrRecord h = new HrRecord(id, emp.getId(), emp.getEmployeeName(), employment, promotion.isEmpty() ? null : promotion);
            if (isEdit) dao.update(h); else dao.insert(h);
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
