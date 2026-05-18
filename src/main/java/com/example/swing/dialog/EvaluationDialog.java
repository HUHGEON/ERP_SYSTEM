package com.example.swing.dialog;

import com.example.dao.EvaluationDAO;
import com.example.dao.ProjectParticipationDAO;
import com.example.model.Evaluation;
import com.example.model.ProjectParticipation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EvaluationDialog extends JDialog {

    private static final String[] CATEGORIES = {"업무 수행", "커뮤니케이션"};

    private final JTextField idField = new JTextField(10);
    private final JComboBox<ProjectParticipation> participationBox = new JComboBox<>();
    private final JComboBox<String> categoryBox = new JComboBox<>(CATEGORIES);

    private final EvaluationDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public EvaluationDialog(JFrame parent, Evaluation eval, EvaluationDAO dao) {
        super(parent, eval == null ? "평가 추가" : "평가 수정", true);
        this.dao = dao;
        this.isEdit = eval != null;

        try {
            List<ProjectParticipation> pps = new ProjectParticipationDAO().getAllParticipations();
            for (ProjectParticipation pp : pps) participationBox.addItem(pp);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "프로젝트 투입 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("프로젝트 투입:"), lc); form.add(participationBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("평가 유형:"), lc); form.add(categoryBox, fc);

        if (isEdit) {
            idField.setText(String.valueOf(eval.getId()));
            for (int i = 0; i < participationBox.getItemCount(); i++) {
                if (participationBox.getItemAt(i).getId() == eval.getParticipationId()) { participationBox.setSelectedIndex(i); break; }
            }
            categoryBox.setSelectedItem(eval.getParticipationCategory());
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
        if (participationBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "프로젝트 투입을 선택하세요."); return; }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            ProjectParticipation pp = (ProjectParticipation) participationBox.getSelectedItem();
            Evaluation e = new Evaluation(id, pp.getId(), (String) categoryBox.getSelectedItem());
            if (isEdit) dao.update(e); else dao.insert(e);
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
