package com.example.swing.dialog;

import com.example.dao.CustomerEvaluationDAO;
import com.example.dao.EvaluationDAO;
import com.example.dao.ProjectDAO;
import com.example.model.Customer;
import com.example.model.CustomerEvaluation;
import com.example.model.Evaluation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CustomerEvaluationDialog extends JDialog {

    private final JComboBox<Evaluation> evaluationBox = new JComboBox<>();
    private final JComboBox<Customer> customerBox = new JComboBox<>();

    private final CustomerEvaluationDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public CustomerEvaluationDialog(JFrame parent, CustomerEvaluation ce, CustomerEvaluationDAO dao) {
        super(parent, ce == null ? "고객 평가 추가" : "고객 평가 수정", true);
        this.dao = dao;
        this.isEdit = ce != null;

        try {
            List<Evaluation> evals = new EvaluationDAO().getAllEvaluations();
            for (Evaluation e : evals) evaluationBox.addItem(e);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "평가 목록 로드 실패: " + ex.getMessage()); }

        try {
            List<Customer> customers = new ProjectDAO().getAllCustomers();
            for (Customer c : customers) customerBox.addItem(c);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "발주처 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("평가 선택:"), lc); form.add(evaluationBox, fc);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("발주처:"), lc); form.add(customerBox, fc);

        if (isEdit) {
            for (int i = 0; i < evaluationBox.getItemCount(); i++) {
                if (evaluationBox.getItemAt(i).getId() == ce.getId()) { evaluationBox.setSelectedIndex(i); break; }
            }
            evaluationBox.setEnabled(false);
            for (int i = 0; i < customerBox.getItemCount(); i++) {
                if (customerBox.getItemAt(i).getId() == ce.getCustomerId()) { customerBox.setSelectedIndex(i); break; }
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
        if (evaluationBox.getSelectedItem() == null || customerBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "모든 항목을 선택하세요."); return;
        }
        try {
            Evaluation eval = (Evaluation) evaluationBox.getSelectedItem();
            Customer c = (Customer) customerBox.getSelectedItem();
            CustomerEvaluation ce = new CustomerEvaluation(eval.getId(), c.getId(), c.getCustomerName(), eval.getParticipationCategory());
            if (isEdit) dao.update(ce); else dao.insert(ce);
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
