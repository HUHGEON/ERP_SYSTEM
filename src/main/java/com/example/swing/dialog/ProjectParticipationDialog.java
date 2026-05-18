package com.example.swing.dialog;

import com.example.dao.DeveloperDAO;
import com.example.dao.ProjectDAO;
import com.example.dao.ProjectParticipationDAO;
import com.example.model.Developer;
import com.example.model.Project;
import com.example.model.ProjectParticipation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProjectParticipationDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Project> projectBox = new JComboBox<>();
    private final JComboBox<Developer> developerBox = new JComboBox<>();
    private final JTextField roleField = new JTextField(15);
    private final JTextField startField = new JTextField(12);
    private final JTextField endField = new JTextField(12);

    private final ProjectParticipationDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public ProjectParticipationDialog(JFrame parent, ProjectParticipation pp, ProjectParticipationDAO dao) {
        super(parent, pp == null ? "프로젝트 투입 추가" : "프로젝트 투입 수정", true);
        this.dao = dao;
        this.isEdit = pp != null;

        try {
            List<Project> projects = new ProjectDAO().search("", "");
            for (Project p : projects) projectBox.addItem(p);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "프로젝트 목록 로드 실패: " + ex.getMessage()); }

        try {
            List<Developer> devs = new DeveloperDAO().getAllDevelopers();
            for (Developer d : devs) developerBox.addItem(d);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "개발자 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        idField.setEditable(false);
        lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("ID:"), lc); form.add(idField, fc);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("프로젝트:"), lc); form.add(projectBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("개발자:"), lc); form.add(developerBox, fc);
        lc.gridy = 3; fc.gridy = 3; form.add(new JLabel("직무:"), lc); form.add(roleField, fc);
        lc.gridy = 4; fc.gridy = 4; form.add(new JLabel("투입일 (YYYY-MM-DD):"), lc); form.add(startField, fc);
        lc.gridy = 5; fc.gridy = 5; form.add(new JLabel("종료일 (YYYY-MM-DD, 선택):"), lc); form.add(endField, fc);

        if (isEdit) {
            idField.setText(String.valueOf(pp.getId()));
            for (int i = 0; i < projectBox.getItemCount(); i++) {
                if (projectBox.getItemAt(i).getId() == pp.getProjectId()) { projectBox.setSelectedIndex(i); break; }
            }
            for (int i = 0; i < developerBox.getItemCount(); i++) {
                if (developerBox.getItemAt(i).getId() == pp.getDeveloperId()) { developerBox.setSelectedIndex(i); break; }
            }
            roleField.setText(pp.getProjectRole() != null ? pp.getProjectRole() : "");
            startField.setText(pp.getStartDate() != null ? pp.getStartDate() : "");
            endField.setText(pp.getEndDate() != null ? pp.getEndDate() : "");
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
        String start = startField.getText().trim();
        if (start.isEmpty()) { JOptionPane.showMessageDialog(this, "투입일을 입력하세요."); return; }
        if (projectBox.getSelectedItem() == null || developerBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "프로젝트와 개발자를 선택하세요."); return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Project p = (Project) projectBox.getSelectedItem();
            Developer d = (Developer) developerBox.getSelectedItem();
            String end = endField.getText().trim();
            ProjectParticipation pp = new ProjectParticipation(id, p.getId(), p.getProjectName(),
                d.getId(), d.getEmployeeName(), roleField.getText().trim(), start, end.isEmpty() ? null : end);
            if (isEdit) dao.update(pp); else dao.insert(pp);
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
