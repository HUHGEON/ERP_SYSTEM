package com.example.swing.dialog;

import com.example.dao.DeveloperDAO;
import com.example.dao.ProjectDAO;
import com.example.dao.ProjectParticipationDAO;
import com.example.model.Developer;
import com.example.model.Project;
import com.example.model.ProjectParticipation;
import com.example.util.ComboAutoComplete;
import com.example.util.MaskingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
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
        this(parent, pp, dao, -1);
    }

    public ProjectParticipationDialog(JFrame parent, ProjectParticipation pp, ProjectParticipationDAO dao, int preselectedProjectId) {
        super(parent, pp == null ? "프로젝트 투입 추가" : "프로젝트 투입 수정", true);
        this.dao = dao;
        this.isEdit = pp != null;

        try {
            String statusFilter = (preselectedProjectId < 0) ? "진행중" : "";
            List<Project> projects = new ProjectDAO().search("", statusFilter);
            for (Project p : projects) projectBox.addItem(p);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "프로젝트 목록 로드 실패: " + ex.getMessage()); }

        try {
            List<Developer> devs = new DeveloperDAO().getAllDevelopers();
            for (Developer d : devs) developerBox.addItem(d);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "개발자 목록 로드 실패: " + ex.getMessage()); }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        MaskingUtil.installDateFilter(startField);
        MaskingUtil.installDateFilter(endField);
        idField.setEditable(false);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("프로젝트:"), lc); form.add(projectBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("개발자:"), lc); form.add(developerBox, fc);
        lc.gridy = 3; fc.gridy = 3; form.add(new JLabel("직무:"), lc); form.add(roleField, fc);
        lc.gridy = 4; fc.gridy = 4; form.add(new JLabel("투입일 (YYYY-MM-DD):"), lc); form.add(startField, fc);
        lc.gridy = 5; fc.gridy = 5; form.add(new JLabel("종료일 (YYYY-MM-DD, 선택):"), lc); form.add(endField, fc);

        // 컨텍스트에서 열린 경우 프로젝트 고정, 아니면 자동완성 적용
        int lockProjectId = isEdit ? pp.getProjectId() : preselectedProjectId;
        if (lockProjectId != -1) {
            for (int i = 0; i < projectBox.getItemCount(); i++) {
                if (projectBox.getItemAt(i).getId() == lockProjectId) { projectBox.setSelectedIndex(i); break; }
            }
            projectBox.setEnabled(false);
        } else {
            ComboAutoComplete.apply(projectBox);
        }
        ComboAutoComplete.apply(developerBox);

        if (isEdit) {
            idField.setText(String.valueOf(pp.getId()));
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

        endField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validateDates(); }
        });
        startField.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validateDates(); }
        });

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);
        pack(); setResizable(false); setLocationRelativeTo(parent);
    }

    private boolean validateDates() {
        String start = startField.getText().trim();
        String end = endField.getText().trim();
        if (!start.isEmpty() && !end.isEmpty() && end.compareTo(start) < 0) {
            endField.setBackground(new Color(255, 200, 200));
            return false;
        }
        endField.setBackground(UIManager.getColor("TextField.background"));
        return true;
    }

    private void save() {
        String start = startField.getText().trim();
        if (start.isEmpty()) { JOptionPane.showMessageDialog(this, "투입일을 입력하세요."); return; }
        if (projectBox.getSelectedItem() == null || developerBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "프로젝트와 개발자를 선택하세요."); return;
        }
        String endRaw = endField.getText().trim();
        try {
            LocalDate.parse(start);
            if (!endRaw.isEmpty()) LocalDate.parse(endRaw);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)", "날짜 오류", JOptionPane.ERROR_MESSAGE); return; }
        if (!validateDates()) {
            JOptionPane.showMessageDialog(this, "종료일이 투입일보다 이전일 수 없습니다.", "날짜 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Project p = (Project) projectBox.getSelectedItem();
            Developer d = (Developer) developerBox.getSelectedItem();
            String end = endField.getText().trim();

            // 동일 프로젝트 중복 투입 차단
            ProjectParticipation existing = dao.getByProjectAndEmployee(p.getId(), d.getId());
            if (existing != null && (!isEdit || existing.getId() != id)) {
                JOptionPane.showMessageDialog(this,
                    d.getEmployeeName() + " 직원은 이미 해당 프로젝트에 투입되어 있습니다.",
                    "중복 투입", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 완료된 프로젝트에는 투입 불가
            if (p.getEndDate() != null && !p.getEndDate().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "'" + p.getProjectName() + "'은 이미 완료된 프로젝트입니다.\n완료된 프로젝트에는 인원을 투입할 수 없습니다.",
                    "투입 제한", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 종료일 없이 투입 시 이미 진행 중인 프로젝트가 있으면 차단
            if (end.isEmpty() && dao.isActivelyParticipating(d.getId(), isEdit ? id : -1)) {
                JOptionPane.showMessageDialog(this,
                    d.getEmployeeName() + " 직원은 이미 다른 프로젝트에 투입 중입니다.\n기존 프로젝트를 먼저 완료하세요.",
                    "투입 제한", JOptionPane.WARNING_MESSAGE);
                return;
            }

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
