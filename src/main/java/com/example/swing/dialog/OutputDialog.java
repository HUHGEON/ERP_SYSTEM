package com.example.swing.dialog;

import com.example.dao.OutputDAO;
import com.example.dao.ProjectDAO;
import com.example.model.Output;
import com.example.model.Project;
import com.example.util.ComboAutoComplete;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OutputDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Project> projectBox = new JComboBox<>();
    private final JTextField typeField = new JTextField(15);
    private final JTextField nameField = new JTextField(20);

    private final OutputDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public OutputDialog(JFrame parent, Output output, OutputDAO dao) {
        this(parent, output, dao, -1);
    }

    public OutputDialog(JFrame parent, Output output, OutputDAO dao, int presetProjectId) {
        super(parent, output == null ? "산출물 추가" : "산출물 수정", true);
        this.dao = dao;
        this.isEdit = output != null;

        try {
            List<Project> projects = new ProjectDAO().search("", "");
            for (Project p : projects) projectBox.addItem(p);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "프로젝트 목록 로드 실패: " + ex.getMessage()); }

        ComboAutoComplete.apply(projectBox);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("프로젝트:"), lc); form.add(projectBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("산출물 유형:"), lc); form.add(typeField, fc);
        lc.gridy = 3; fc.gridy = 3; form.add(new JLabel("산출물명:"), lc); form.add(nameField, fc);

        if (isEdit) {
            idField.setText(String.valueOf(output.getId()));
            for (int i = 0; i < projectBox.getItemCount(); i++) {
                if (projectBox.getItemAt(i).getId() == output.getProjectId()) { projectBox.setSelectedIndex(i); break; }
            }
            typeField.setText(output.getOutputType() != null ? output.getOutputType() : "");
            nameField.setText(output.getOutputName() != null ? output.getOutputName() : "");
        } else {
            try { idField.setText(String.valueOf(dao.nextId())); } catch (Exception e) { idField.setText("1"); }
            if (presetProjectId > 0) {
                for (int i = 0; i < projectBox.getItemCount(); i++) {
                    if (projectBox.getItemAt(i).getId() == presetProjectId) { projectBox.setSelectedIndex(i); break; }
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
        getRootPane().setDefaultButton(saveBtn);
        pack(); setResizable(false); setLocationRelativeTo(parent);
    }

    private void save() {
        if (projectBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "프로젝트를 선택하세요."); return; }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Project p = (Project) projectBox.getSelectedItem();
            Output o = new Output(id, p.getId(), p.getProjectName(), typeField.getText().trim(), nameField.getText().trim());
            if (isEdit) dao.update(o); else dao.insert(o);
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
