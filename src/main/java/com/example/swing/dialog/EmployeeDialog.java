package com.example.swing.dialog;

import com.example.dao.DeveloperDAO;
import com.example.dao.EmployeeDAO;
import com.example.model.Developer;
import com.example.model.Employee;
import com.example.model.Position;
import com.example.util.MaskingUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EmployeeDialog extends JDialog {

    private static final String[] DEPARTMENTS = {"개발자", "마케팅", "경영관리", "연구개발"};

    private final JTextField idField          = new JTextField(10);
    private final JTextField nameField        = new JTextField(15);
    private final JLabel     positionLabel    = new JLabel("-");
    private final JComboBox<String> deptBox   = new JComboBox<>(DEPARTMENTS);
    private final JTextField residentField    = new JTextField(15);
    private final JTextField educationField   = new JTextField(20);
    private final JTextField phoneField       = new JTextField(15);
    private final JTextField emailLocalField  = new JTextField(10);
    private final JTextField emailDomainField = new JTextField(12);
    private final JTextField hireDateField    = new JTextField(12);
    private final JLabel     techLabel        = new JLabel("기술스택 (쉼표 구분):");
    private final JTextField techField        = new JTextField(20);

    private final EmployeeDAO dao;
    private final DeveloperDAO developerDAO = new DeveloperDAO();
    private List<Position> allPositions;
    private boolean saved = false;
    private final boolean isEdit;
    private String originalResidentNumber = null;

    public EmployeeDialog(JFrame parent, Employee emp, EmployeeDAO dao) {
        super(parent, emp == null ? "직원 추가" : "직원 수정", true);
        this.dao = dao;
        this.isEdit = emp != null;

        try {
            allPositions = dao.getAllPositions();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "직급 목록 로드 실패: " + ex.getMessage());
        }

        positionLabel.setFont(positionLabel.getFont().deriveFont(Font.BOLD));
        positionLabel.setForeground(new Color(25, 50, 120));

        // 전화번호 포맷 필터 (두 모드 공통 — 주민번호 필터는 값 세팅 후 설치)
        ((AbstractDocument) phoneField.getDocument()).setDocumentFilter(new PhoneFilter());

        // 이메일 패널 ([local] @ [domain])
        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        emailPanel.setOpaque(false);
        emailLocalField.setPreferredSize(new Dimension(110, emailLocalField.getPreferredSize().height));
        emailDomainField.setPreferredSize(new Dimension(130, emailDomainField.getPreferredSize().height));
        JLabel atLabel = new JLabel("  @  ");
        emailPanel.add(emailLocalField);
        emailPanel.add(atLabel);
        emailPanel.add(emailDomainField);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.EAST;
        lc.insets = new Insets(5, 5, 5, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(5, 0, 5, 5);
        fc.gridwidth = GridBagConstraints.REMAINDER;

        addRow(form, lc, fc, 0, "이름:", nameField);
        addRow(form, lc, fc, 1, "직급 (자동):", positionLabel);
        addRow(form, lc, fc, 2, "부서:", deptBox);
        addRow(form, lc, fc, 3, "주민번호:", residentField);
        addRow(form, lc, fc, 4, "학력:", educationField);
        addRow(form, lc, fc, 5, "전화번호:", phoneField);
        addRow(form, lc, fc, 6, "이메일:", emailPanel);
        addRow(form, lc, fc, 7, "입사일 (YYYY-MM-DD):", hireDateField);
        lc.gridy = 8; fc.gridy = 8;
        form.add(techLabel, lc);
        form.add(techField, fc);

        idField.setEditable(false);
        hireDateField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { updatePositionLabel(); }
            public void removeUpdate(DocumentEvent e)  { updatePositionLabel(); }
            public void changedUpdate(DocumentEvent e) { updatePositionLabel(); }
        });

        if (!isEdit) {
            hireDateField.setText(LocalDate.now().toString());
            try { idField.setText(String.valueOf(dao.nextId())); } catch (Exception e) { idField.setText("1"); }
        }

        if (isEdit) {
            idField.setText(String.valueOf(emp.getId()));
            nameField.setText(emp.getEmployeeName());
            deptBox.setSelectedItem(emp.getDepartment());
            originalResidentNumber = emp.getResidentNumber();
            residentField.setText(MaskingUtil.maskResidentNumber(emp.getResidentNumber()));
            educationField.setText(emp.getEducation());
            phoneField.setText(emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "");
            // 이메일 split
            String mail = emp.getEmail() != null ? emp.getEmail() : "";
            int at = mail.indexOf('@');
            if (at >= 0) {
                emailLocalField.setText(mail.substring(0, at));
                emailDomainField.setText(mail.substring(at + 1));
            } else {
                emailLocalField.setText(mail);
            }
            hireDateField.setText(emp.getHireDate() != null ? emp.getHireDate() : "");
            if ("개발자".equals(emp.getDepartment())) {
                try {
                    Developer dev = developerDAO.getById(emp.getId());
                    if (dev != null) techField.setText(dev.getTech());
                } catch (Exception ignored) {}
            }
        }

        // 주민번호 필터: 값 세팅 완료 후 설치 (기존 텍스트는 그대로 유지됨)
        MaskingUtil.installResidentFilter(residentField);

        updateTechFieldState();
        deptBox.addActionListener(e -> updateTechFieldState());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void updatePositionLabel() {
        Position p = calcPositionByHireDate(hireDateField.getText().trim());
        positionLabel.setText(p != null ? p.getPositionName() : "-");
    }

    private Position calcPositionByHireDate(String hireDateStr) {
        if (allPositions == null || hireDateStr.isEmpty()) return null;
        try {
            LocalDate hireDate = LocalDate.parse(hireDateStr);
            long years = ChronoUnit.YEARS.between(hireDate, LocalDate.now());
            String name;
            if (years < 2)      name = "사원";
            else if (years < 4) name = "대리";
            else if (years < 6) name = "과장";
            else if (years < 8) name = "부장";
            else                name = "이사";
            for (Position p : allPositions) {
                if (p.getPositionName().equals(name)) return p;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void updateTechFieldState() {
        boolean isDev = "개발자".equals(deptBox.getSelectedItem());
        techLabel.setEnabled(isDev);
        techField.setEnabled(isDev);
        techField.setBackground(isDev ? Color.WHITE : new Color(230, 230, 230));
        if (!isDev) techField.setText("");
    }

    private void addRow(JPanel form, GridBagConstraints lc, GridBagConstraints fc, int row, String label, JComponent field) {
        lc.gridy = row;
        fc.gridy = row;
        form.add(new JLabel(label), lc);
        form.add(field, fc);
    }

    private void save() {
        String name      = nameField.getText().trim();
        String resident  = residentField.getText().trim();
        if (isEdit && resident.contains("*")) resident = originalResidentNumber;
        String education = educationField.getText().trim();
        String dept      = (String) deptBox.getSelectedItem();
        String phone     = phoneField.getText().trim();
        String emailLocal  = emailLocalField.getText().trim();
        String emailDomain = emailDomainField.getText().trim();
        String hireDate  = hireDateField.getText().trim();

        if (name.isEmpty())        { JOptionPane.showMessageDialog(this, "이름을 입력하세요."); return; }
        if (resident.isEmpty())    { JOptionPane.showMessageDialog(this, "주민번호를 입력하세요."); return; }
        if (education.isEmpty())   { JOptionPane.showMessageDialog(this, "학력을 입력하세요."); return; }
        if (hireDate.isEmpty())    { JOptionPane.showMessageDialog(this, "입사일을 입력하세요."); return; }
        if (emailLocal.isEmpty() || emailDomain.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이메일을 올바르게 입력하세요."); return;
        }

        Position pos = calcPositionByHireDate(hireDate);
        if (pos == null) { JOptionPane.showMessageDialog(this, "입사일 형식이 올바르지 않습니다. (YYYY-MM-DD)"); return; }

        String email = emailLocal + "@" + emailDomain;

        try {
            int id = Integer.parseInt(idField.getText().trim());

            Employee emp = new Employee();
            emp.setId(id);
            emp.setPositionId(pos.getId());
            emp.setEmployeeName(name);
            emp.setResidentNumber(resident);
            emp.setEducation(education);
            emp.setDepartment(dept);
            emp.setPhoneNumber(phone);
            emp.setEmail(email);
            emp.setHireDate(hireDate);

            if (isEdit) {
                dao.update(emp);
                if ("개발자".equals(dept)) {
                    String tech = techField.getText().trim();
                    Developer existing = developerDAO.getById(id);
                    if (existing == null) developerDAO.insert(new Developer(id, name, tech));
                    else                  developerDAO.update(new Developer(id, name, tech));
                }
            } else {
                dao.insert(emp);
                if ("개발자".equals(dept)) {
                    developerDAO.insert(new Developer(id, name, techField.getText().trim()));
                }
            }

            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID 값이 올바르지 않습니다.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }

    // 전화번호 자동 포맷 필터 (숫자만 허용, XXX-XXXX-XXXX)
    private static class PhoneFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = cur.substring(0, offset) + string + cur.substring(offset);
            applyFormatted(fb, newText);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = cur.substring(0, offset) + (text == null ? "" : text) + cur.substring(offset + length);
            applyFormatted(fb, newText);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = cur.substring(0, offset) + cur.substring(offset + length);
            applyFormatted(fb, newText);
        }

        private void applyFormatted(FilterBypass fb, String raw) throws BadLocationException {
            String digits = raw.replaceAll("[^0-9]", "");
            if (digits.length() > 11) digits = digits.substring(0, 11);
            String formatted = format(digits);
            super.replace(fb, 0, fb.getDocument().getLength(), formatted, null);
        }

        private String format(String digits) {
            if (digits.length() <= 3) return digits;
            if (digits.length() <= 7) return digits.substring(0, 3) + "-" + digits.substring(3);
            return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
    }
}
