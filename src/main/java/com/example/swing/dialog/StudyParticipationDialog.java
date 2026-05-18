package com.example.swing.dialog;

import com.example.dao.EmployeeDAO;
import com.example.dao.StudyDAO;
import com.example.dao.StudyParticipationDAO;
import com.example.model.Employee;
import com.example.model.Study;
import com.example.model.StudyParticipation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudyParticipationDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Study> studyBox = new JComboBox<>();
    private final JComboBox<Employee> employeeBox = new JComboBox<>();

    private final StudyParticipationDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public StudyParticipationDialog(JFrame parent, StudyParticipation sp, StudyParticipationDAO dao) {
        super(parent, sp == null ? "스터디 참여 추가" : "스터디 참여 수정", true);
        this.dao = dao;
        this.isEdit = sp != null;

        try {
            List<Study> studies = new StudyDAO().search("", "");
            for (Study s : studies) studyBox.addItem(s);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "스터디 목록 로드 실패: " + ex.getMessage()); }

        try {
            List<Employee> employees = new EmployeeDAO().search("", "", "");
            for (Employee e : employees) employeeBox.addItem(e);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "직원 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("ID:"), lc); form.add(idField, fc);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("스터디:"), lc); form.add(studyBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("직원:"), lc); form.add(employeeBox, fc);

        if (isEdit) {
            idField.setText(String.valueOf(sp.getId()));
            for (int i = 0; i < studyBox.getItemCount(); i++) {
                if (studyBox.getItemAt(i).getId() == sp.getStudyId()) { studyBox.setSelectedIndex(i); break; }
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
        if (studyBox.getSelectedItem() == null || employeeBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "스터디와 직원을 선택하세요."); return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Study s = (Study) studyBox.getSelectedItem();
            Employee e = (Employee) employeeBox.getSelectedItem();
            StudyParticipation sp = new StudyParticipation(id, s.getId(), s.getStudyName(), e.getId(), e.getEmployeeName());
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
