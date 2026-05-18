package com.example.swing.dialog;

import com.example.dao.StudyDAO;
import com.example.model.Study;

import javax.swing.*;
import java.awt.*;

public class StudyDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(20);
    private final JTextField categoryField = new JTextField(15);

    private final StudyDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public StudyDialog(JFrame parent, Study study, StudyDAO dao) {
        super(parent, study == null ? "스터디 추가" : "스터디 수정", true);
        this.dao = dao;
        this.isEdit = study != null;

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

        lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("ID:"), lc); form.add(idField, fc);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("스터디명:"), lc); form.add(nameField, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("카테고리:"), lc); form.add(categoryField, fc);

        idField.setEditable(false);
        if (isEdit) {
            idField.setText(String.valueOf(study.getId()));
            nameField.setText(study.getStudyName());
            categoryField.setText(study.getCategory() != null ? study.getCategory() : "");
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

    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "스터디명을 입력하세요."); return; }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            Study s = new Study(id, name, categoryField.getText().trim());
            if (isEdit) dao.update(s); else dao.insert(s);
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
