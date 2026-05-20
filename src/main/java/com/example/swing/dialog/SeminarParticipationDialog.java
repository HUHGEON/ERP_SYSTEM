package com.example.swing.dialog;

import com.example.dao.EmployeeDAO;
import com.example.dao.SeminarDAO;
import com.example.dao.SeminarParticipationDAO;
import com.example.model.Employee;
import com.example.model.Seminar;
import com.example.model.SeminarParticipation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SeminarParticipationDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Seminar> seminarBox = new JComboBox<>();
    private final JComboBox<Employee> employeeBox = new JComboBox<>();

    private final SeminarParticipationDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public SeminarParticipationDialog(JFrame parent, SeminarParticipation sp, SeminarParticipationDAO dao) {
        super(parent, sp == null ? "세미나 참여 추가" : "세미나 참여 수정", true);
        this.dao = dao;
        this.isEdit = sp != null;

        try {
            List<Seminar> seminars = new SeminarDAO().search("", "");
            for (Seminar s : seminars) seminarBox.addItem(s);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "세미나 목록 로드 실패: " + ex.getMessage()); }

        try {
            List<Employee> employees = new EmployeeDAO().search("", "", "");
            for (Employee e : employees) employeeBox.addItem(e);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "직원 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("세미나:"), lc); form.add(seminarBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("직원:"), lc); form.add(employeeBox, fc);

        if (isEdit) {
            idField.setText(String.valueOf(sp.getId()));
            for (int i = 0; i < seminarBox.getItemCount(); i++) {
                if (seminarBox.getItemAt(i).getId() == sp.getSeminarId()) { seminarBox.setSelectedIndex(i); break; }
            }
            for (int i = 0; i < employeeBox.getItemCount(); i++) {
                if (employeeBox.getItemAt(i).getId() == sp.getEmployeeId()) { employeeBox.setSelectedIndex(i); break; }
            }
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
        if (seminarBox.getSelectedItem() == null || employeeBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "세미나와 직원을 선택하세요."); return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Seminar s = (Seminar) seminarBox.getSelectedItem();
            Employee e = (Employee) employeeBox.getSelectedItem();
            SeminarParticipation sp = new SeminarParticipation(id, s.getId(), s.getSeminarName(), e.getId(), e.getEmployeeName());
            if (isEdit) dao.update(sp); else dao.insert(sp);
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
