package com.example.swing.dialog;

import com.example.dao.SeminarDAO;
import com.example.model.Seminar;

import javax.swing.*;
import java.awt.*;

public class SeminarDialog extends JDialog {

    private final JTextField idField       = new JTextField(10);
    private final JTextField nameField     = new JTextField(20);
    private final JTextField topicField    = new JTextField(20);
    private final JTextField dateTimeField = new JTextField(18);

    private final SeminarDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public SeminarDialog(JFrame parent, Seminar seminar, SeminarDAO dao) {
        super(parent, seminar == null ? "세미나 추가" : "세미나 수정", true);
        this.dao = dao;
        this.isEdit = seminar != null;

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("세미나명:"), lc); form.add(nameField, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("주제:"), lc); form.add(topicField, fc);
        lc.gridy = 3; fc.gridy = 3; form.add(new JLabel("일시 (YYYY-MM-DD HH:MM):"), lc); form.add(dateTimeField, fc);

        if (isEdit) {
            idField.setText(String.valueOf(seminar.getId()));
            nameField.setText(seminar.getSeminarName());
            topicField.setText(seminar.getTopic() != null ? seminar.getTopic() : "");
            dateTimeField.setText(seminar.getDateTime() != null ? seminar.getDateTime() : "");
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
        String name = nameField.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "세미나명을 입력하세요."); return; }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            Seminar s = new Seminar(id, name, topicField.getText().trim(), dateTimeField.getText().trim());
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
    private GridBagConstraints lc() { GridBagConstraints c = new GridBagConstraints(); c.anchor = GridBagConstraints.EAST; c.insets = new Insets(5,5,5,8); return c; }
    private GridBagConstraints fc() { GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0; c.insets = new Insets(5,0,5,5); c.gridwidth = GridBagConstraints.REMAINDER; return c; }
}
