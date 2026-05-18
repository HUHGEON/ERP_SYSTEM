package com.example.swing.dialog;

import com.example.dao.DeveloperDAO;
import com.example.dao.EvaluationDAO;
import com.example.dao.PartnerEvaluationDAO;
import com.example.model.Developer;
import com.example.model.Evaluation;
import com.example.model.PartnerEvaluation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PartnerEvaluationDialog extends JDialog {

    private final JComboBox<Evaluation> evaluationBox = new JComboBox<>();
    private final JComboBox<Developer> partnerBox = new JComboBox<>();

    private final PartnerEvaluationDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public PartnerEvaluationDialog(JFrame parent, PartnerEvaluation pe, PartnerEvaluationDAO dao) {
        super(parent, pe == null ? "동료 평가 추가" : "동료 평가 수정", true);
        this.dao = dao;
        this.isEdit = pe != null;

        try {
            List<Evaluation> evals = new EvaluationDAO().getAllEvaluations();
            for (Evaluation e : evals) evaluationBox.addItem(e);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "평가 목록 로드 실패: " + ex.getMessage()); }

        try {
            List<Developer> devs = new DeveloperDAO().getAllDevelopers();
            for (Developer d : devs) partnerBox.addItem(d);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "개발자 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("평가 선택:"), lc); form.add(evaluationBox, fc);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("동료 개발자:"), lc); form.add(partnerBox, fc);

        if (isEdit) {
            for (int i = 0; i < evaluationBox.getItemCount(); i++) {
                if (evaluationBox.getItemAt(i).getId() == pe.getId()) { evaluationBox.setSelectedIndex(i); break; }
            }
            evaluationBox.setEnabled(false);
            for (int i = 0; i < partnerBox.getItemCount(); i++) {
                if (partnerBox.getItemAt(i).getId() == pe.getPartnerId()) { partnerBox.setSelectedIndex(i); break; }
            }
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
        if (evaluationBox.getSelectedItem() == null || partnerBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "모든 항목을 선택하세요."); return;
        }
        try {
            Evaluation eval = (Evaluation) evaluationBox.getSelectedItem();
            Developer partner = (Developer) partnerBox.getSelectedItem();
            PartnerEvaluation pe = new PartnerEvaluation(eval.getId(), partner.getId(), partner.getEmployeeName(), eval.getParticipationCategory());
            if (isEdit) dao.update(pe); else dao.insert(pe);
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
