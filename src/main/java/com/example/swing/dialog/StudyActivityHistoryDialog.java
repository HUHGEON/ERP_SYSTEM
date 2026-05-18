package com.example.swing.dialog;

import com.example.dao.StudyActivityHistoryDAO;
import com.example.dao.StudyDAO;
import com.example.model.Study;
import com.example.model.StudyActivityHistory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudyActivityHistoryDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Study> studyBox = new JComboBox<>();
    private final JTextField dateField = new JTextField(12);
    private final JTextArea contentArea = new JTextArea(4, 25);

    private final StudyActivityHistoryDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public StudyActivityHistoryDialog(JFrame parent, StudyActivityHistory h, StudyActivityHistoryDAO dao) {
        super(parent, h == null ? "스터디 활동 추가" : "스터디 활동 수정", true);
        this.dao = dao;
        this.isEdit = h != null;

        try {
            List<Study> studies = new StudyDAO().search("", "");
            for (Study s : studies) studyBox.addItem(s);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "스터디 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        contentArea.setLineWrap(true); contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);

        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("스터디:"), lc); form.add(studyBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("활동일자 (YYYY-MM-DD):"), lc); form.add(dateField, fc);
        lc.gridy = 3; fc.gridy = 3; lc.anchor = GridBagConstraints.NORTHEAST;
        form.add(new JLabel("내용:"), lc); form.add(contentScroll, fc);

        if (isEdit) {
            idField.setText(String.valueOf(h.getId()));
            for (int i = 0; i < studyBox.getItemCount(); i++) {
                if (studyBox.getItemAt(i).getId() == h.getStudyId()) { studyBox.setSelectedIndex(i); break; }
            }
            dateField.setText(h.getActivityDate() != null ? h.getActivityDate() : "");
            contentArea.setText(h.getContent() != null ? h.getContent() : "");
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
        pack(); setResizable(false); setLocationRelativeTo(parent);
    }

    private void save() {
        if (studyBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "스터디를 선택하세요."); return; }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Study s = (Study) studyBox.getSelectedItem();
            String date = dateField.getText().trim();
            StudyActivityHistory h = new StudyActivityHistory(id, s.getId(), s.getStudyName(),
                date.isEmpty() ? null : date, contentArea.getText().trim());
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
