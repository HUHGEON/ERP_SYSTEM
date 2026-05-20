package com.example.swing.dialog;

import com.example.dao.CareerDAO;
import com.example.dao.DeveloperDAO;
import com.example.dao.EmployeeDAO;
import com.example.dao.ManagementDAO;
import com.example.model.Career;
import com.example.model.Developer;
import com.example.model.Employee;
import com.example.model.Management;
import com.example.model.Position;
import com.example.util.MaskingUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDialog extends JDialog {

    private static final String[] DEPARTMENTS = {"개발자", "마케팅", "경영관리", "연구개발"};
    private static final String[] CAREER_COLS = {"회사명", "입사일 (YYYY-MM-DD)", "퇴사일 (YYYY-MM-DD)"};

    // ── 신입 추가 ──
    private final JTextField sinipName     = new JTextField();
    private final JTextField sinipResident = new JTextField();
    private final JTextField sinipEduc     = new JTextField();
    private final JTextField sinipPhone       = new JTextField();
    private final JTextField sinipEmailLocal  = new JTextField();
    private final JTextField sinipEmailDomain = new JTextField();
    private final JTextField sinipHireDate    = new JTextField();
    private final JLabel     sinipPosLabel = new JLabel("—");
    private final JComboBox<String> sinipDeptBox  = new JComboBox<>(DEPARTMENTS);
    private final JTextField sinipTechField = new JTextField();
    private final JTextField sinipPermField = new JTextField();
    private CardLayout sinipExtraCards;
    private JPanel     sinipExtraPanel;
    private final DefaultTableModel sinipCareerModel = new DefaultTableModel(CAREER_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return true; }
    };
    private final JTable sinipCareerTable = new JTable(sinipCareerModel);

    // ── 경력 추가 ──
    private final JTextField gyeongName     = new JTextField();
    private final JTextField gyeongResident = new JTextField();
    private final JTextField gyeongEduc     = new JTextField();
    private final JTextField gyeongPhone       = new JTextField();
    private final JTextField gyeongEmailLocal  = new JTextField();
    private final JTextField gyeongEmailDomain = new JTextField();
    private final JTextField gyeongHireDate    = new JTextField();
    private final JLabel     gyeongPosLabel = new JLabel("—");
    private final JTextField gyeongTechField = new JTextField();
    private final DefaultTableModel careerModel = new DefaultTableModel(CAREER_COLS, 1) {
        @Override public boolean isCellEditable(int r, int c) { return true; }
    };
    private final JTable  careerTable      = new JTable(careerModel);
    private final JLabel  careerErrorLabel = new JLabel(" ");

    // ── 수정 ──
    private final JTextField          idField        = new JTextField(10);
    private final JTextField          nameField      = new JTextField();
    private final JComboBox<Position> positionBox    = new JComboBox<>();
    private final JComboBox<String>   deptBox        = new JComboBox<>(DEPARTMENTS);
    private final JTextField          residentField  = new JTextField();
    private final JTextField          educationField = new JTextField();
    private final JTextField          phoneField        = new JTextField();
    private final JTextField          emailLocalField   = new JTextField();
    private final JTextField          emailDomainField  = new JTextField();
    private final JTextField          hireDateField     = new JTextField();
    private final JTextField          editTechField  = new JTextField();
    private final JTextField          editPermField  = new JTextField();
    private CardLayout editExtraCards;
    private JPanel     editExtraPanel;

    private List<Position>   positions     = new ArrayList<>();
    private final EmployeeDAO   dao;
    private final DeveloperDAO  developerDAO  = new DeveloperDAO();
    private final CareerDAO     careerDAO     = new CareerDAO();
    private final ManagementDAO managementDAO = new ManagementDAO();
    private boolean saved  = false;
    private final boolean isEdit;

    public EmployeeDialog(JFrame parent, Employee emp, EmployeeDAO dao) {
        super(parent, emp == null ? "직원 추가" : "직원 수정", true);
        this.dao    = dao;
        this.isEdit = emp != null;

        try { positions = dao.getAllPositions(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "직급 목록 로드 실패: " + ex.getMessage()); }

        if (isEdit) buildEditPanel(emp);
        else        buildAddPanel();

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    // ════════════════════════  ADD MODE  ════════════════════════

    private void buildAddPanel() {
        MaskingUtil.installResidentFilter(sinipResident);
        MaskingUtil.installPhoneFilter(sinipPhone);
        MaskingUtil.installResidentFilter(gyeongResident);
        MaskingUtil.installPhoneFilter(gyeongPhone);
        MaskingUtil.installDateFilter(sinipHireDate);
        MaskingUtil.installDateFilter(gyeongHireDate);
        sinipHireDate.setText(LocalDate.now().toString());
        gyeongHireDate.setText(LocalDate.now().toString());

        // ── 모드 토글 ──
        JToggleButton sinipBtn  = new JToggleButton("신입", true);
        JToggleButton gyeongBtn = new JToggleButton("경력");
        ButtonGroup bg = new ButtonGroup();
        bg.add(sinipBtn);
        bg.add(gyeongBtn);
        sinipBtn.putClientProperty("JButton.buttonType", "segmented");
        sinipBtn.putClientProperty("JButton.segmentPosition", "first");
        gyeongBtn.putClientProperty("JButton.buttonType", "segmented");
        gyeongBtn.putClientProperty("JButton.segmentPosition", "last");
        sinipBtn.setFocusPainted(false);
        gyeongBtn.setFocusPainted(false);
        sinipBtn.setPreferredSize(new Dimension(100, 28));
        gyeongBtn.setPreferredSize(new Dimension(100, 28));

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        togglePanel.add(sinipBtn);
        togglePanel.add(gyeongBtn);

        // ── 모드별 패널 (동적 높이 CardLayout) ──
        final JPanel sinipContent  = buildSinipPanel();
        final JPanel gyeongContent = buildGyeongPanel();
        CardLayout modeCards = new CardLayout();
        JPanel modePanel = new JPanel(modeCards) {
            @Override
            public Dimension getPreferredSize() {
                int w = Math.max(sinipContent.getPreferredSize().width,
                                 gyeongContent.getPreferredSize().width);
                for (Component c : getComponents()) {
                    if (c.isVisible()) return new Dimension(w, c.getPreferredSize().height);
                }
                return super.getPreferredSize();
            }
            @Override public Dimension getMinimumSize() { return getPreferredSize(); }
        };
        modePanel.add(sinipContent,  "신입");
        modePanel.add(gyeongContent, "경력");

        sinipBtn.addActionListener(e  -> { modeCards.show(modePanel, "신입"); EmployeeDialog.this.pack(); });
        gyeongBtn.addActionListener(e -> { modeCards.show(modePanel, "경력"); EmployeeDialog.this.pack(); });

        sinipDeptBox.addActionListener(e -> updateSinipExtra());
        updateSinipExtra();
        updateSinipPosition();
        sinipCareerModel.addTableModelListener(e -> updateSinipPosition());
        careerModel.addTableModelListener(e -> { updateGyeongPosition(); refreshCareerValidation(); });

        // ── 버튼 ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        saveBtn.addActionListener(e -> { if (sinipBtn.isSelected()) saveSinip(); else saveGyeong(); });
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(togglePanel);
        content.add(modePanel);

        setLayout(new BorderLayout());
        add(content,  BorderLayout.NORTH);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // ── 신입 패널: 기본 정보 + 선택적 경력 이력 ──
    private JPanel buildSinipPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 24, 12, 24));

        JPanel info = new JPanel(new GridBagLayout());
        GridBagConstraints lc = lc(), fc = fc();
        int r = 0;
        row(info, lc, fc, r++, "이름:",                 sinipName);
        row(info, lc, fc, r++, "주민번호:",             sinipResident);
        row(info, lc, fc, r++, "학력:",                 sinipEduc);
        row(info, lc, fc, r++, "전화번호:",             sinipPhone);
        row(info, lc, fc, r++, "이메일:",               emailPanel(sinipEmailLocal, sinipEmailDomain));
        row(info, lc, fc, r++, "입사일 (YYYY-MM-DD):", sinipHireDate);

        sinipPosLabel.setForeground(new Color(0, 100, 180));
        sinipPosLabel.setFont(sinipPosLabel.getFont().deriveFont(Font.BOLD));
        row(info, lc, fc, r++, "직급 (자동):", sinipPosLabel);
        row(info, lc, fc, r++, "부서:",        sinipDeptBox);

        // 입사일 변경 시 직급 자동 갱신
        sinipHireDate.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSinipPosition(); }
            public void removeUpdate(DocumentEvent e) { updateSinipPosition(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        // 부서별 추가 입력 (CardLayout)
        sinipExtraCards = new CardLayout();
        sinipExtraPanel = new JPanel(sinipExtraCards);
        sinipExtraPanel.add(new JPanel(), "none");
        sinipExtraPanel.add(extraRow("기술스택 (쉼표 구분):", sinipTechField), "dev");
        sinipExtraPanel.add(extraRow("권한 단계:",            sinipPermField), "mgmt");
        sinipExtraCards.show(sinipExtraPanel, "none");

        GridBagConstraints ec = new GridBagConstraints();
        ec.gridy = r; ec.fill = GridBagConstraints.HORIZONTAL;
        ec.weightx = 1.0; ec.gridwidth = GridBagConstraints.REMAINDER;
        info.add(sinipExtraPanel, ec);

        // 경력 테이블 (선택사항)
        sinipCareerTable.setRowHeight(24);
        sinipCareerTable.getTableHeader().setReorderingAllowed(false);
        sinipCareerTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        sinipCareerTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        sinipCareerTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        JTextField scStartField = new JTextField();
        MaskingUtil.installDateFilter(scStartField);
        DefaultCellEditor scStartEditor = new DefaultCellEditor(scStartField);
        scStartEditor.setClickCountToStart(1);
        sinipCareerTable.getColumnModel().getColumn(1).setCellEditor(scStartEditor);

        JTextField scEndField = new JTextField();
        MaskingUtil.installDateFilter(scEndField);
        DefaultCellEditor scEndEditor = new DefaultCellEditor(scEndField);
        scEndEditor.setClickCountToStart(1);
        sinipCareerTable.getColumnModel().getColumn(2).setCellEditor(scEndEditor);

        JScrollPane scroll = new JScrollPane(sinipCareerTable);
        scroll.setPreferredSize(new Dimension(440, 90));

        JButton addRowBtn = new JButton("+ 경력 추가");
        JButton delRowBtn = new JButton("- 경력 삭제");
        addRowBtn.addActionListener(e -> sinipCareerModel.addRow(new Object[]{"", "", ""}));
        delRowBtn.addActionListener(e -> {
            int sel = sinipCareerTable.getSelectedRow();
            if (sel >= 0) { sinipCareerModel.removeRow(sel); updateSinipPosition(); }
        });
        JPanel careerBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        careerBtns.add(addRowBtn);
        careerBtns.add(delRowBtn);

        JPanel careerPanel = new JPanel(new BorderLayout(3, 3));
        careerPanel.setBorder(BorderFactory.createTitledBorder("경력 이력 (선택사항 — 호봉 반영)"));
        careerPanel.add(scroll,     BorderLayout.CENTER);
        careerPanel.add(careerBtns, BorderLayout.SOUTH);

        p.add(info,        BorderLayout.NORTH);
        p.add(careerPanel, BorderLayout.CENTER);
        return p;
    }

    private void updateSinipPosition() {
        try {
            long days = calcTotalSinipDays();
            if (days < 0) days = 0;
            Position pos = calcPosition(days);
            sinipPosLabel.setText(pos != null
                ? pos.getPositionName() + "  (" + (days / 365) + "년 경력)"
                : "—");
        } catch (Exception ignored) {
            sinipPosLabel.setText("—");
        }
    }

    private long calcTotalSinipDays() {
        long careerDays = calcSinipCareerDays();
        try {
            long sinceDays = ChronoUnit.DAYS.between(
                LocalDate.parse(sinipHireDate.getText().trim()), LocalDate.now());
            if (sinceDays > 0) careerDays += sinceDays;
        } catch (Exception ignored) {}
        return careerDays;
    }

    private long calcSinipCareerDays() {
        long total = 0;
        for (int r = 0; r < sinipCareerModel.getRowCount(); r++) {
            String s = str(sinipCareerModel.getValueAt(r, 1));
            String e = str(sinipCareerModel.getValueAt(r, 2));
            if (s.isEmpty() || e.isEmpty()) continue;
            try {
                LocalDate start = LocalDate.parse(s);
                LocalDate end   = LocalDate.parse(e);
                if (!end.isBefore(start)) total += ChronoUnit.DAYS.between(start, end);
            } catch (Exception ignored) {}
        }
        return total;
    }

    private void updateSinipExtra() {
        String dept = (String) sinipDeptBox.getSelectedItem();
        if ("개발자".equals(dept)) {
            sinipExtraCards.show(sinipExtraPanel, "dev");
            sinipPermField.setText("");
        } else if ("경영관리".equals(dept)) {
            sinipExtraCards.show(sinipExtraPanel, "mgmt");
            sinipTechField.setText("");
        } else {
            sinipExtraCards.show(sinipExtraPanel, "none");
            sinipTechField.setText(""); sinipPermField.setText("");
        }
    }

    // ── 경력 패널: 공통 + 경력 전용 필드 전부 하나의 패널 ──
    private JPanel buildGyeongPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(8, 24, 12, 24));

        JPanel info = new JPanel(new GridBagLayout());
        GridBagConstraints lc = lc(), fc = fc();
        JLabel deptFixed = new JLabel("개발자");
        deptFixed.setForeground(Color.GRAY);
        gyeongPosLabel.setForeground(new Color(0, 100, 180));
        gyeongPosLabel.setFont(gyeongPosLabel.getFont().deriveFont(Font.BOLD));
        int r = 0;
        row(info, lc, fc, r++, "이름:",                 gyeongName);
        row(info, lc, fc, r++, "주민번호:",             gyeongResident);
        row(info, lc, fc, r++, "학력:",                 gyeongEduc);
        row(info, lc, fc, r++, "전화번호:",             gyeongPhone);
        row(info, lc, fc, r++, "이메일:",               emailPanel(gyeongEmailLocal, gyeongEmailDomain));
        row(info, lc, fc, r++, "입사일 (YYYY-MM-DD):", gyeongHireDate);
        gyeongHireDate.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateGyeongPosition(); refreshCareerValidation(); }
            public void removeUpdate(DocumentEvent e) { updateGyeongPosition(); refreshCareerValidation(); }
            public void changedUpdate(DocumentEvent e) {}
        });
        row(info, lc, fc, r++, "부서:",                 deptFixed);
        row(info, lc, fc, r++, "직급 (자동 계산):",    gyeongPosLabel);
        row(info, lc, fc, r,   "기술스택 (쉼표 구분):", gyeongTechField);

        careerTable.setRowHeight(24);
        careerTable.getTableHeader().setReorderingAllowed(false);
        careerTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        careerTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        careerTable.getColumnModel().getColumn(2).setPreferredWidth(130);

        JTextField startEditorField = new JTextField();
        MaskingUtil.installDateFilter(startEditorField);
        DefaultCellEditor startCellEditor = new DefaultCellEditor(startEditorField);
        startCellEditor.setClickCountToStart(1);
        careerTable.getColumnModel().getColumn(1).setCellEditor(startCellEditor);

        JTextField endEditorField = new JTextField();
        MaskingUtil.installDateFilter(endEditorField);
        DefaultCellEditor endCellEditor = new DefaultCellEditor(endEditorField);
        endCellEditor.setClickCountToStart(1);
        careerTable.getColumnModel().getColumn(2).setCellEditor(endCellEditor);

        // 날짜 셀: 오류 종류에 따라 빨간 배경 + 툴팁
        javax.swing.table.DefaultTableCellRenderer dateRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) {
                    String errMsg = getCareerRowError(row, gyeongHireDate.getText().trim());
                    c.setBackground(errMsg != null ? new Color(255, 200, 200) : Color.WHITE);
                    ((JLabel) c).setToolTipText(errMsg);
                }
                return c;
            }
        };
        careerTable.getColumnModel().getColumn(1).setCellRenderer(dateRenderer);
        careerTable.getColumnModel().getColumn(2).setCellRenderer(dateRenderer);

        JScrollPane scroll = new JScrollPane(careerTable);
        scroll.setPreferredSize(new Dimension(440, 110));

        JButton addRowBtn = new JButton("+ 경력 추가");
        JButton delRowBtn = new JButton("- 경력 삭제");
        addRowBtn.addActionListener(e -> careerModel.addRow(new Object[]{"", "", ""}));
        delRowBtn.addActionListener(e -> {
            int sel = careerTable.getSelectedRow();
            if (sel >= 0) { careerModel.removeRow(sel); updateGyeongPosition(); refreshCareerValidation(); }
        });
        JPanel careerBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        careerBtns.add(addRowBtn);
        careerBtns.add(delRowBtn);

        careerErrorLabel.setForeground(new Color(180, 0, 0));
        careerErrorLabel.setFont(careerErrorLabel.getFont().deriveFont(Font.PLAIN, 11f));
        careerErrorLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(careerBtns);
        southPanel.add(careerErrorLabel);

        JPanel careerPanel = new JPanel(new BorderLayout(3, 3));
        careerPanel.setBorder(BorderFactory.createTitledBorder("경력 이력 (최소 1건)"));
        careerPanel.add(scroll,      BorderLayout.CENTER);
        careerPanel.add(southPanel,  BorderLayout.SOUTH);

        p.add(info,        BorderLayout.NORTH);
        p.add(careerPanel, BorderLayout.CENTER);
        return p;
    }

    private void updateGyeongPosition() {
        long days = calcTotalGyeongDays();
        Position pos = calcPosition(days);
        gyeongPosLabel.setText(pos != null
            ? pos.getPositionName() + "  (" + (days / 365) + "년 경력)"
            : "—");
    }

    /** 총 경력 일수 = 이전 경력 이력 합산 + (오늘 - 현재 회사 입사일) */
    private long calcTotalGyeongDays() {
        long careerDays = calcCareerDays();
        try {
            long sinceDays = ChronoUnit.DAYS.between(
                LocalDate.parse(gyeongHireDate.getText().trim()), LocalDate.now());
            if (sinceDays > 0) careerDays += sinceDays;
        } catch (Exception ignored) {}
        return careerDays;
    }

    private long calcCareerDays() {
        long total = 0;
        for (int r = 0; r < careerModel.getRowCount(); r++) {
            String s = str(careerModel.getValueAt(r, 1));
            String e = str(careerModel.getValueAt(r, 2));
            if (s.isEmpty() || e.isEmpty()) continue;
            try {
                LocalDate start = LocalDate.parse(s);
                LocalDate end   = LocalDate.parse(e);
                if (!end.isBefore(start)) total += ChronoUnit.DAYS.between(start, end);
            } catch (Exception ignored) {}
        }
        return total;
    }

    private Position calcPosition(long careerDays) {
        if (positions.isEmpty()) return null;
        long years = careerDays / 365;
        int idx;
        if      (years < 2) idx = 0;
        else if (years < 4) idx = 1;
        else if (years < 6) idx = 2;
        else if (years < 8) idx = 3;
        else                idx = positions.size() - 1;
        return positions.get(Math.min(idx, positions.size() - 1));
    }

    private void saveSinip() {
        String name     = sinipName.getText().trim();
        String resident = sinipResident.getText().trim();
        String educ     = sinipEduc.getText().trim();
        String phone    = sinipPhone.getText().trim();
        String email    = combineEmail(sinipEmailLocal, sinipEmailDomain);
        String hireDate = sinipHireDate.getText().trim();
        String dept     = (String) sinipDeptBox.getSelectedItem();

        if (name.isEmpty())     { info("이름을 입력하세요."); return; }
        if (resident.isEmpty()) { info("주민번호를 입력하세요."); return; }
        if (educ.isEmpty())     { info("학력을 입력하세요."); return; }
        if (hireDate.isEmpty()) { info("입사일을 입력하세요."); return; }
        try { LocalDate.parse(hireDate); }
        catch (Exception ex) { error("입사일 형식이 올바르지 않습니다. (YYYY-MM-DD)"); return; }
        if ("경영관리".equals(dept) && sinipPermField.getText().trim().isEmpty()) {
            info("권한 단계를 입력하세요."); return;
        }

        // 경력 이력 검증 (선택사항 — 입력된 행만 검증)
        if (sinipCareerTable.isEditing()) sinipCareerTable.getCellEditor().stopCellEditing();
        List<String[]> careerRows = new ArrayList<>();
        List<Integer>  rowNums   = new ArrayList<>();
        for (int r = 0; r < sinipCareerModel.getRowCount(); r++) {
            String company = str(sinipCareerModel.getValueAt(r, 0));
            String start   = str(sinipCareerModel.getValueAt(r, 1));
            String end     = str(sinipCareerModel.getValueAt(r, 2));
            if (company.isEmpty() && start.isEmpty() && end.isEmpty()) continue;
            if (company.isEmpty()) { error((r + 1) + "행: 회사명을 입력하세요."); return; }
            if (start.isEmpty())   { error((r + 1) + "행: 입사일을 입력하세요."); return; }
            if (end.isEmpty())     { error((r + 1) + "행: 퇴사일을 입력하세요."); return; }
            try {
                LocalDate s    = LocalDate.parse(start);
                LocalDate e    = LocalDate.parse(end);
                LocalDate hire = LocalDate.parse(hireDate);
                if (e.isBefore(s))   { error((r + 1) + "행: 퇴사일이 입사일보다 이전입니다."); return; }
                if (e.isAfter(hire)) { error((r + 1) + "행: 경력 퇴사일이 현재 회사 입사일(" + hireDate + ")보다 이후입니다."); return; }
            } catch (Exception ex) { error((r + 1) + "행: 날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)"); return; }
            careerRows.add(new String[]{company, start, end});
            rowNums.add(r + 1);
        }
        for (int i = 0; i < careerRows.size(); i++) {
            LocalDate s1 = LocalDate.parse(careerRows.get(i)[1]);
            LocalDate e1 = LocalDate.parse(careerRows.get(i)[2]);
            for (int j = i + 1; j < careerRows.size(); j++) {
                LocalDate s2 = LocalDate.parse(careerRows.get(j)[1]);
                LocalDate e2 = LocalDate.parse(careerRows.get(j)[2]);
                if (s1.isBefore(e2) && s2.isBefore(e1)) {
                    error(rowNums.get(i) + "행과 " + rowNums.get(j) + "행의 경력 기간이 겹칩니다."); return;
                }
            }
        }

        long days = calcTotalSinipDays();
        if (days < 0) days = 0;
        Position pos = calcPosition(days);
        if (pos == null) { error("직급 계산 오류: 직급 데이터를 확인하세요."); return; }

        try {
            int id = dao.nextId();
            Employee emp = new Employee();
            emp.setId(id); emp.setPositionId(pos.getId()); emp.setEmployeeName(name);
            emp.setResidentNumber(resident); emp.setEducation(educ);
            emp.setDepartment(dept); emp.setPhoneNumber(phone);
            emp.setEmail(email); emp.setHireDate(hireDate);
            dao.insert(emp);
            if ("개발자".equals(dept)) {
                developerDAO.insert(new Developer(id, name, sinipTechField.getText().trim()));
            } else if ("경영관리".equals(dept)) {
                managementDAO.insert(new Management(id, name, sinipPermField.getText().trim()));
            }
            if (!careerRows.isEmpty()) {
                int careerId = careerDAO.nextId();
                for (String[] row : careerRows) {
                    careerDAO.insert(new Career(careerId++, id, name, row[0], row[1], row[2]));
                }
            }
            saved = true;
            dispose();
        } catch (Exception ex) { error("저장 오류: " + ex.getMessage()); }
    }

    private void saveGyeong() {
        String name     = gyeongName.getText().trim();
        String resident = gyeongResident.getText().trim();
        String educ     = gyeongEduc.getText().trim();
        String phone    = gyeongPhone.getText().trim();
        String email    = combineEmail(gyeongEmailLocal, gyeongEmailDomain);
        String hireDate = gyeongHireDate.getText().trim();

        if (name.isEmpty())     { info("이름을 입력하세요."); return; }
        if (resident.isEmpty()) { info("주민번호를 입력하세요."); return; }
        if (educ.isEmpty())     { info("학력을 입력하세요."); return; }
        if (hireDate.isEmpty()) { info("입사일을 입력하세요."); return; }
        try { LocalDate.parse(hireDate); }
        catch (Exception ex) { error("입사일 형식이 올바르지 않습니다. (YYYY-MM-DD)"); return; }

        if (careerTable.isEditing()) careerTable.getCellEditor().stopCellEditing();
        List<String[]>  rows    = new ArrayList<>();
        List<Integer>   rowNums = new ArrayList<>();
        for (int r = 0; r < careerModel.getRowCount(); r++) {
            String company = str(careerModel.getValueAt(r, 0));
            String start   = str(careerModel.getValueAt(r, 1));
            String end     = str(careerModel.getValueAt(r, 2));
            if (company.isEmpty() && start.isEmpty() && end.isEmpty()) continue;
            if (company.isEmpty()) { error((r + 1) + "행: 회사명을 입력하세요."); return; }
            if (start.isEmpty())   { error((r + 1) + "행: 입사일을 입력하세요."); return; }
            if (end.isEmpty())     { error((r + 1) + "행: 퇴사일을 입력하세요."); return; }
            try {
                LocalDate s    = LocalDate.parse(start);
                LocalDate e    = LocalDate.parse(end);
                LocalDate hire = LocalDate.parse(hireDate);
                if (e.isBefore(s))    { error((r + 1) + "행: 퇴사일이 입사일보다 이전입니다."); return; }
                if (e.isAfter(hire))  { error((r + 1) + "행: 경력 퇴사일이 현재 회사 입사일(" + hireDate + ")보다 이후입니다."); return; }
            } catch (Exception ex) { error((r + 1) + "행: 날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)"); return; }
            rows.add(new String[]{company, start, end});
            rowNums.add(r + 1);
        }
        if (rows.isEmpty()) { info("경력 이력을 최소 1건 입력하세요."); return; }
        for (int i = 0; i < rows.size(); i++) {
            LocalDate s1 = LocalDate.parse(rows.get(i)[1]);
            LocalDate e1 = LocalDate.parse(rows.get(i)[2]);
            for (int j = i + 1; j < rows.size(); j++) {
                LocalDate s2 = LocalDate.parse(rows.get(j)[1]);
                LocalDate e2 = LocalDate.parse(rows.get(j)[2]);
                if (s1.isBefore(e2) && s2.isBefore(e1)) {
                    error(rowNums.get(i) + "행과 " + rowNums.get(j) + "행의 경력 기간이 겹칩니다."); return;
                }
            }
        }

        Position pos = calcPosition(calcTotalGyeongDays());
        if (pos == null) { error("직급 계산 오류: 직급 데이터를 확인하세요."); return; }

        try {
            int empId = dao.nextId();
            Employee emp = new Employee();
            emp.setId(empId); emp.setPositionId(pos.getId()); emp.setEmployeeName(name);
            emp.setResidentNumber(resident); emp.setEducation(educ);
            emp.setDepartment("개발자"); emp.setPhoneNumber(phone);
            emp.setEmail(email); emp.setHireDate(hireDate);
            dao.insert(emp);
            developerDAO.insert(new Developer(empId, name, gyeongTechField.getText().trim()));

            int careerId = careerDAO.nextId();
            for (String[] row : rows) {
                careerDAO.insert(new Career(careerId++, empId, name, row[0], row[1], row[2]));
            }
            saved = true;
            dispose();
        } catch (Exception ex) { error("저장 오류: " + ex.getMessage()); }
    }

    // ════════════════════════  EDIT MODE  ════════════════════════

    private void buildEditPanel(Employee emp) {
        positions.forEach(positionBox::addItem);
        MaskingUtil.installResidentFilter(residentField);
        MaskingUtil.installPhoneFilter(phoneField);
        MaskingUtil.installDateFilter(hireDateField);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 24, 10, 24));
        GridBagConstraints lc = lc(), fc = fc();
        int r = 0;
        row(form, lc, fc, r++, "이름:",                 nameField);
        row(form, lc, fc, r++, "직급:",                 positionBox);
        row(form, lc, fc, r++, "부서:",                 deptBox);
        row(form, lc, fc, r++, "주민번호:",             residentField);
        row(form, lc, fc, r++, "학력:",                 educationField);
        row(form, lc, fc, r++, "전화번호:",             phoneField);
        row(form, lc, fc, r++, "이메일:",               emailPanel(emailLocalField, emailDomainField));
        row(form, lc, fc, r++, "입사일 (YYYY-MM-DD):", hireDateField);

        editExtraCards = new CardLayout();
        editExtraPanel = new JPanel(editExtraCards);
        editExtraPanel.add(new JPanel(), "none");
        editExtraPanel.add(extraRow("기술스택 (쉼표 구분):", editTechField), "dev");
        editExtraPanel.add(extraRow("권한 단계:",            editPermField), "mgmt");
        editExtraCards.show(editExtraPanel, "none");

        GridBagConstraints ec = new GridBagConstraints();
        ec.gridy = r; ec.fill = GridBagConstraints.HORIZONTAL;
        ec.weightx = 1.0; ec.gridwidth = GridBagConstraints.REMAINDER;
        form.add(editExtraPanel, ec);

        idField.setEditable(false);
        idField.setText(String.valueOf(emp.getId()));
        nameField.setText(emp.getEmployeeName());
        for (int i = 0; i < positionBox.getItemCount(); i++) {
            if (positionBox.getItemAt(i).getId() == emp.getPositionId()) {
                positionBox.setSelectedIndex(i); break;
            }
        }
        deptBox.setSelectedItem(emp.getDepartment());
        residentField.setText(emp.getResidentNumber());
        educationField.setText(emp.getEducation());
        phoneField.setText(emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "");
        String existingEmail = emp.getEmail() != null ? emp.getEmail() : "";
        int atIdx = existingEmail.indexOf('@');
        if (atIdx >= 0) {
            emailLocalField.setText(existingEmail.substring(0, atIdx));
            emailDomainField.setText(existingEmail.substring(atIdx + 1));
        } else {
            emailLocalField.setText(existingEmail);
            emailDomainField.setText("");
        }
        hireDateField.setText(emp.getHireDate() != null ? emp.getHireDate() : "");

        if ("개발자".equals(emp.getDepartment())) {
            try {
                Developer dev = developerDAO.getById(emp.getId());
                if (dev != null) editTechField.setText(dev.getTech() != null ? dev.getTech() : "");
            } catch (Exception ignored) {}
        } else if ("경영관리".equals(emp.getDepartment())) {
            try {
                Management mgmt = managementDAO.getById(emp.getId());
                if (mgmt != null) editPermField.setText(mgmt.getPermissionLevel() != null ? mgmt.getPermissionLevel() : "");
            } catch (Exception ignored) {}
        }

        updateEditExtra();
        deptBox.addActionListener(e -> updateEditExtra());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        saveBtn.addActionListener(e -> saveEdit());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(saveBtn);

        setLayout(new BorderLayout());
        add(form,     BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void updateEditExtra() {
        String dept = (String) deptBox.getSelectedItem();
        if ("개발자".equals(dept)) {
            editExtraCards.show(editExtraPanel, "dev");
            editPermField.setText("");
        } else if ("경영관리".equals(dept)) {
            editExtraCards.show(editExtraPanel, "mgmt");
            editTechField.setText("");
        } else {
            editExtraCards.show(editExtraPanel, "none");
            editTechField.setText(""); editPermField.setText("");
        }
    }

    private void saveEdit() {
        String name     = nameField.getText().trim();
        String resident = residentField.getText().trim();
        String educ     = educationField.getText().trim();
        String dept     = (String) deptBox.getSelectedItem();
        String phone    = phoneField.getText().trim();
        String email    = combineEmail(emailLocalField, emailDomainField);
        String hireDate = hireDateField.getText().trim();
        Position pos    = (Position) positionBox.getSelectedItem();

        if (name.isEmpty())     { info("이름을 입력하세요."); return; }
        if (resident.isEmpty()) { info("주민번호를 입력하세요."); return; }
        if (educ.isEmpty())     { info("학력을 입력하세요."); return; }
        if (hireDate.isEmpty()) { info("입사일을 입력하세요."); return; }
        if (pos == null)        { info("직급을 선택하세요."); return; }
        if ("경영관리".equals(dept) && editPermField.getText().trim().isEmpty()) {
            info("권한 단계를 입력하세요."); return;
        }

        try {
            int id = Integer.parseInt(idField.getText().trim());
            Employee emp = new Employee();
            emp.setId(id); emp.setPositionId(pos.getId()); emp.setEmployeeName(name);
            emp.setResidentNumber(resident); emp.setEducation(educ);
            emp.setDepartment(dept); emp.setPhoneNumber(phone);
            emp.setEmail(email); emp.setHireDate(hireDate);
            dao.update(emp);

            if ("개발자".equals(dept)) {
                String tech = editTechField.getText().trim();
                Developer existing = developerDAO.getById(id);
                if (existing == null) developerDAO.insert(new Developer(id, name, tech));
                else                  developerDAO.update(new Developer(id, name, tech));
            } else if ("경영관리".equals(dept)) {
                String perm = editPermField.getText().trim();
                Management existing = managementDAO.getById(id);
                if (existing == null) managementDAO.insert(new Management(id, name, perm));
                else                  managementDAO.update(new Management(id, name, perm));
            }
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            info("ID 값이 올바르지 않습니다.");
        } catch (Exception ex) {
            error("저장 오류: " + ex.getMessage());
        }
    }

    // ════════════════════════  CAREER VALIDATION  ════════════════════════

    /** 해당 행의 날짜 오류 메시지를 반환. 오류 없으면 null. */
    private String getCareerRowError(int row, String hireDate) {
        String s = str(careerModel.getValueAt(row, 1));
        String e = str(careerModel.getValueAt(row, 2));
        if (s.isEmpty() || e.isEmpty()) return null;
        try {
            LocalDate start = LocalDate.parse(s);
            LocalDate end   = LocalDate.parse(e);
            if (end.isBefore(start)) return "퇴사일이 입사일보다 이전입니다";
            if (!hireDate.isEmpty()) {
                try {
                    if (end.isAfter(LocalDate.parse(hireDate)))
                        return "경력 퇴사일이 현재 회사 입사일(" + hireDate + ")보다 이후입니다";
                } catch (Exception ignored) {}
            }
            for (int other = 0; other < careerModel.getRowCount(); other++) {
                if (other == row) continue;
                String os = str(careerModel.getValueAt(other, 1));
                String oe = str(careerModel.getValueAt(other, 2));
                if (os.isEmpty() || oe.isEmpty()) continue;
                try {
                    LocalDate os2 = LocalDate.parse(os);
                    LocalDate oe2 = LocalDate.parse(oe);
                    if (start.isBefore(oe2) && os2.isBefore(end))
                        return (other + 1) + "행과 기간이 겹칩니다";
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** 테이블 하단 에러 레이블 갱신 + 셀 배경 재렌더링 */
    private void refreshCareerValidation() {
        String hire  = gyeongHireDate.getText().trim();
        String first = null;
        for (int r = 0; r < careerModel.getRowCount(); r++) {
            String err = getCareerRowError(r, hire);
            if (err != null) { first = (r + 1) + "행: " + err; break; }
        }
        careerErrorLabel.setText(first != null ? "⚠ " + first : " ");
        careerTable.repaint();
    }

    // ════════════════════════  UTILS  ════════════════════════

    /** [local] @ [domain] 형태의 이메일 입력 패널 */
    private JPanel emailPanel(JTextField local, JTextField domain) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.45;
        p.add(local, c);
        GridBagConstraints at = new GridBagConstraints();
        at.insets = new Insets(0, 4, 0, 4);
        p.add(new JLabel("@"), at);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.weightx = 0.55;
        p.add(domain, c2);
        return p;
    }

    /** 이메일 두 칸 → "local@domain" 합성 (둘 다 비면 빈 문자열) */
    private String combineEmail(JTextField local, JTextField domain) {
        String l = local.getText().trim();
        String d = domain.getText().trim();
        return (l.isEmpty() && d.isEmpty()) ? "" : l + "@" + d;
    }

    private JPanel extraRow(String label, JTextField field) {
        JPanel row = new JPanel(new GridBagLayout());
        GridBagConstraints lc = lc(), fc = fc();
        lc.gridy = 0; fc.gridy = 0;
        lc.insets = new Insets(5, 0, 5, 8);
        row.add(new JLabel(label), lc);
        row.add(field, fc);
        return row;
    }

    private String str(Object o) { return o == null ? "" : o.toString().trim(); }
    public boolean isSaved() { return saved; }

    private void row(JPanel p, GridBagConstraints lc, GridBagConstraints fc,
                     int r, String label, JComponent field) {
        lc.gridy = r; fc.gridy = r;
        p.add(new JLabel(label), lc);
        p.add(field, fc);
    }

    private void info(String msg)  { JOptionPane.showMessageDialog(this, msg); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }

    private GridBagConstraints lc() {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(6, 5, 6, 10);
        return c;
    }

    private GridBagConstraints fc() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(6, 0, 6, 5);
        c.gridwidth = GridBagConstraints.REMAINDER;
        return c;
    }
}
