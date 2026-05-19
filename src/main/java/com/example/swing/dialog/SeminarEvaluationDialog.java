package com.example.swing.dialog;

import com.example.dao.EmployeeDAO;
import com.example.dao.SeminarEvaluationDAO;
import com.example.model.Employee;
import com.example.model.SeminarEvaluation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SeminarEvaluationDialog extends JDialog {

    private final JTextField idField      = new JTextField(10);
    private final JComboBox<Employee> employeeBox = new JComboBox<>();
    private final JTextField ratingField  = new JTextField(6);
    private final JTextArea commentArea   = new JTextArea(4, 25);

    private final SeminarEvaluationDAO dao;
    private final int seminarId;
    private boolean saved = false;
    private final boolean isEdit;

    public SeminarEvaluationDialog(JFrame parent, SeminarEvaluation eval, int seminarId, SeminarEvaluationDAO dao) {
        super(parent, eval == null ? "평가 추가" : "평가 수정", true);
        this.dao = dao;
        this.seminarId = seminarId;
        this.isEdit = eval != null;

        try {
            List<Employee> employees = new EmployeeDAO().search("", "", "");
            for (Employee e : employees) employeeBox.addItem(e);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "직원 목록 로드 실패: " + ex.getMessage());
        }

        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("직원:"), lc); form.add(employeeBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("평점 (0.00~5.00):"), lc); form.add(ratingField, fc);

        lc.gridy = 3; lc.anchor = GridBagConstraints.NORTHEAST;
        fc.gridy = 3;
        form.add(new JLabel("코멘트:"), lc);
        form.add(new JScrollPane(commentArea), fc);
        lc.anchor = GridBagConstraints.EAST;

        if (isEdit) {
            idField.setText(String.valueOf(eval.getId()));
            for (int i = 0; i < employeeBox.getItemCount(); i++) {
                if (employeeBox.getItemAt(i).getId() == eval.getEmployeeId()) { employeeBox.setSelectedIndex(i); break; }
            }
            ratingField.setText(String.valueOf(eval.getRating()));
            commentArea.setText(eval.getComment() != null ? eval.getComment() : "");
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
        if (employeeBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "직원을 선택하세요."); return; }
        String ratingStr = ratingField.getText().trim();
        if (ratingStr.isEmpty()) { JOptionPane.showMessageDialog(this, "평점을 입력하세요."); return; }

        double rating;
        try {
            rating = Double.parseDouble(ratingStr);
            if (rating < 0 || rating > 5) { JOptionPane.showMessageDialog(this, "평점은 0.00~5.00 사이여야 합니다."); return; }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "평점은 숫자로 입력하세요."); return;
        }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            Employee emp = (Employee) employeeBox.getSelectedItem();
            SeminarEvaluation se = new SeminarEvaluation(id, seminarId, "", emp.getId(),
                emp.getEmployeeName(), rating, commentArea.getText().trim());
            if (isEdit) dao.update(se); else dao.insert(se);
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
