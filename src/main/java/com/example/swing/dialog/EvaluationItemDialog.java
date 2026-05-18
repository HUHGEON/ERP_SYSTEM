package com.example.swing.dialog;

import com.example.dao.EvaluationDAO;
import com.example.dao.EvaluationItemDAO;
import com.example.model.Evaluation;
import com.example.model.EvaluationItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EvaluationItemDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Evaluation> evaluationBox = new JComboBox<>();
    private final JTextField rateField = new JTextField(8);
    private final JTextArea contentArea = new JTextArea(4, 25);

    private final EvaluationItemDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public EvaluationItemDialog(JFrame parent, EvaluationItem item, EvaluationItemDAO dao) {
        this(parent, item, dao, -1);
    }

    public EvaluationItemDialog(JFrame parent, EvaluationItem item, EvaluationItemDAO dao, int presetEvalId) {
        super(parent, item == null ? "평가항목 추가" : "평가항목 수정", true);
        this.dao = dao;
        this.isEdit = item != null;

        try {
            List<Evaluation> evals = new EvaluationDAO().getAllEvaluations();
            for (Evaluation e : evals) evaluationBox.addItem(e);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "평가 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        contentArea.setLineWrap(true); contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);

        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("평가:"), lc); form.add(evaluationBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("평점 (0.00~9.99):"), lc); form.add(rateField, fc);
        lc.gridy = 3; fc.gridy = 3; lc.anchor = GridBagConstraints.NORTHEAST;
        form.add(new JLabel("평가내용:"), lc); form.add(contentScroll, fc);

        if (isEdit) {
            idField.setText(String.valueOf(item.getId()));
            for (int i = 0; i < evaluationBox.getItemCount(); i++) {
                if (evaluationBox.getItemAt(i).getId() == item.getEvaluationId()) { evaluationBox.setSelectedIndex(i); break; }
            }
            rateField.setText(String.valueOf(item.getRate()));
            contentArea.setText(item.getContent() != null ? item.getContent() : "");
        } else {
            try { idField.setText(String.valueOf(dao.nextId())); } catch (Exception e) { idField.setText("1"); }
            if (presetEvalId > 0) {
                for (int i = 0; i < evaluationBox.getItemCount(); i++) {
                    if (evaluationBox.getItemAt(i).getId() == presetEvalId) { evaluationBox.setSelectedIndex(i); break; }
                }
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
        if (evaluationBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "평가를 선택하세요."); return; }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            double rate = Double.parseDouble(rateField.getText().trim());
            Evaluation eval = (Evaluation) evaluationBox.getSelectedItem();
            EvaluationItem item = new EvaluationItem(id, eval.getId(), rate, contentArea.getText().trim());
            if (isEdit) dao.update(item); else dao.insert(item);
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "평점은 숫자로 입력하세요.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
    private GridBagConstraints lc() { GridBagConstraints c = new GridBagConstraints(); c.anchor = GridBagConstraints.EAST; c.insets = new Insets(5,5,5,8); return c; }
    private GridBagConstraints fc() { GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1.0; c.insets = new Insets(5,0,5,5); c.gridwidth = GridBagConstraints.REMAINDER; return c; }
}
