package com.example.swing.dialog;

import com.example.dao.CareerDAO;
import com.example.dao.DeveloperDAO;
import com.example.model.Career;
import com.example.model.Developer;
import com.example.util.ComboAutoComplete;
import com.example.util.MaskingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.util.List;

public class CareerDialog extends JDialog {

    private final JTextField idField = new JTextField(10);
    private final JComboBox<Developer> developerBox = new JComboBox<>();
    private final JTextField companyField = new JTextField(20);
    private final JTextField startField = new JTextField(12);
    private final JTextField endField = new JTextField(12);

    private final CareerDAO dao;
    private boolean saved = false;
    private final boolean isEdit;

    public CareerDialog(JFrame parent, Career career, CareerDAO dao) {
        super(parent, career == null ? "경력 추가" : "경력 수정", true);
        this.dao = dao;
        this.isEdit = career != null;

        try {
            List<Developer> devs = new DeveloperDAO().getAllDevelopers();
            for (Developer d : devs) developerBox.addItem(d);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "개발자 목록 로드 실패: " + ex.getMessage());
        }
        ComboAutoComplete.apply(developerBox);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = lc(); GridBagConstraints fc = fc();

        MaskingUtil.installDateFilter(startField);
        MaskingUtil.installDateFilter(endField);
        idField.setEditable(false);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("개발자:"), lc); form.add(developerBox, fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("회사명:"), lc); form.add(companyField, fc);
        lc.gridy = 3; fc.gridy = 3; form.add(new JLabel("입사일 (YYYY-MM-DD):"), lc); form.add(startField, fc);
        lc.gridy = 4; fc.gridy = 4; form.add(new JLabel("퇴사일 (YYYY-MM-DD):"), lc); form.add(endField, fc);

        if (isEdit) {
            idField.setText(String.valueOf(career.getId()));
            for (int i = 0; i < developerBox.getItemCount(); i++) {
                if (developerBox.getItemAt(i).getId() == career.getEmployeeId()) { developerBox.setSelectedIndex(i); break; }
            }
            companyField.setText(career.getCompanyName());
            startField.setText(career.getStartTime() != null ? career.getStartTime() : "");
            endField.setText(career.getEndTime() != null ? career.getEndTime() : "");
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
        String company = companyField.getText().trim();
        String start = startField.getText().trim();
        String end = endField.getText().trim();
        if (company.isEmpty()) { JOptionPane.showMessageDialog(this, "회사명을 입력하세요."); return; }
        if (start.isEmpty()) { JOptionPane.showMessageDialog(this, "입사일을 입력하세요."); return; }
        if (end.isEmpty()) { JOptionPane.showMessageDialog(this, "퇴사일을 입력하세요."); return; }
        try { LocalDate.parse(start); LocalDate.parse(end); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)", "날짜 오류", JOptionPane.ERROR_MESSAGE); return; }
        if (!validateDates()) {
            JOptionPane.showMessageDialog(this, "퇴사일이 입사일보다 이전일 수 없습니다.", "날짜 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (developerBox.getSelectedItem() == null) { JOptionPane.showMessageDialog(this, "개발자를 선택하세요."); return; }
        try {
            int id = Integer.parseInt(idField.getText().trim());
            Developer dev = (Developer) developerBox.getSelectedItem();
            Career c = new Career(id, dev.getId(), dev.getEmployeeName(), company, start, end);
            if (isEdit) dao.update(c); else dao.insert(c);
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
