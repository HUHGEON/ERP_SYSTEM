package com.example.swing.dialog;

import com.example.dao.DatabaseConnection;
import com.example.dao.ProjectParticipationDAO;
import com.example.model.ProjectParticipation;
import com.example.util.ComboAutoComplete;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/** JComboBox<ProjectParticipation> 에서 직원 이름만 표시하는 렌더러 */
class NameOnlyRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof ProjectParticipation pp) value = pp.getDeveloperName();
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}

/**
 * 평가 추가 다이얼로그 – EvaluationPanel 전용.
 *
 * TYPE별 동작:
 *  CUSTOMER  (관리자 전용): participation_id = 선택 참여자, customer_id = 프로젝트 고객
 *  PM        (모든 사용자): participation_id = 평가자 참여(직원 본인 or 관리자 선택), pm_id = 선택 PM developer
 *  PARTNER   (모든 사용자): participation_id = 평가자 참여, partner_id = 선택 동료 developer, 본인 제외
 */
public class EvaluationAddDialog extends JDialog {

    public enum Type { CUSTOMER, PM, PARTNER }

    private final Type type;
    private final int projectId;
    private final int projectCustomerId;
    private final boolean isAdmin;
    /** 비관리자 본인의 프로젝트 참여 레코드 (관리자는 null) */
    private final ProjectParticipation myParticipation;

    private final ProjectParticipationDAO ppDAO = new ProjectParticipationDAO();
    private List<ProjectParticipation> members;

    // ── 위젯 ──
    /** 관리자용: 평가자 참여 선택 */
    private JComboBox<ProjectParticipation> evaluatorBox;
    /** PM/파트너 평가 대상 선택 */
    private JComboBox<ProjectParticipation> targetBox;
    private JComboBox<String> categoryBox;
    private JTextField rateField;
    private JTextArea contentArea;

    private boolean saved = false;

    public EvaluationAddDialog(JFrame parent, Type type, int projectId, int projectCustomerId,
                               boolean isAdmin, ProjectParticipation myParticipation) {
        super(parent, dialogTitle(type), true);
        this.type = type;
        this.projectId = projectId;
        this.projectCustomerId = projectCustomerId;
        this.isAdmin = isAdmin;
        this.myParticipation = myParticipation;

        try {
            members = ppDAO.getByProjectId(projectId);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "참여자 로드 실패: " + ex.getMessage());
            members = List.of();
        }

        buildUI();
        pack();
        setMinimumSize(new Dimension(460, 0));
        setLocationRelativeTo(parent);
    }

    private static String dialogTitle(Type t) {
        return switch (t) {
            case CUSTOMER -> "고객 평가 추가";
            case PM       -> "PM 평가 추가";
            case PARTNER  -> "동료 평가 추가";
        };
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(15, 20, 10, 20));
        int row = 0;

        // ── 프로젝트명 (모든 타입 공통) ──
        JLabel projectLabel = new JLabel(fetchProjectName());
        projectLabel.setForeground(new Color(33, 100, 200));
        addRow(form, row++, "프로젝트:", projectLabel);

        // ── 고객 평가: 고객 이름 표시 ──
        if (type == Type.CUSTOMER) {
            JLabel custLabel = new JLabel(fetchCustomerName());
            custLabel.setForeground(new Color(66, 133, 244));
            addRow(form, row++, "고객:", custLabel);
        }

        // ── 관리자만: 평가자(참여자) 선택 (PM·PARTNER) ──
        if (isAdmin && type != Type.CUSTOMER) {
            evaluatorBox = new JComboBox<>();
            evaluatorBox.setRenderer(new NameOnlyRenderer());
            for (ProjectParticipation pp : members) evaluatorBox.addItem(pp);
            ComboAutoComplete.apply(evaluatorBox);
            addRow(form, row++, "평가자 (참여자):", evaluatorBox);
        }

        // ── PM 평가: PM 역할 참여자 콤보, 첫 PM 기본 선택 ──
        if (type == Type.PM) {
            targetBox = new JComboBox<>();
            targetBox.setRenderer(new NameOnlyRenderer());
            for (ProjectParticipation pp : members) {
                if ("PM".equalsIgnoreCase(pp.getProjectRole())) targetBox.addItem(pp);
            }
            if (targetBox.getItemCount() == 0) {
                for (ProjectParticipation pp : members) targetBox.addItem(pp);
            }
            ComboAutoComplete.apply(targetBox);
            addRow(form, row++, "평가할 PM:", targetBox);
        }

        // ── 동료 평가: 동료(본인 제외) 콤보 ──
        if (type == Type.PARTNER) {
            targetBox = new JComboBox<>();
            targetBox.setRenderer(new NameOnlyRenderer());
            for (ProjectParticipation pp : members) {
                if (myParticipation != null && pp.getDeveloperId() == myParticipation.getDeveloperId()) continue;
                targetBox.addItem(pp);
            }
            if (targetBox.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "평가할 동료가 없습니다.");
            }
            ComboAutoComplete.apply(targetBox);
            addRow(form, row++, "평가할 동료:", targetBox);
        }

        // ── 카테고리 (동료 평가만) ──
        if (type == Type.PARTNER) {
            categoryBox = new JComboBox<>(new String[]{"업무 수행", "커뮤니케이션"});
            addRow(form, row++, "카테고리:", categoryBox);
        }

        // ── 평점 (1.0~5.0) ── 소수점 1자리까지만 입력, 저장 시 범위 검증
        rateField = new JTextField(8);
        rateField.setHorizontalAlignment(JTextField.LEFT);
        ((javax.swing.text.AbstractDocument) rateField.getDocument()).setDocumentFilter(new javax.swing.text.DocumentFilter() {
            @Override
            public void insertString(javax.swing.text.DocumentFilter.FilterBypass fb, int off, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                replace(fb, off, 0, str, a);
            }
            @Override
            public void replace(javax.swing.text.DocumentFilter.FilterBypass fb, int off, int len, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                if (str == null) return;
                String cur = fb.getDocument().getText(0, fb.getDocument().getLength());
                String next = cur.substring(0, off) + str + cur.substring(off + len);
                if (next.isEmpty() || next.matches("\\d{0,1}(\\.\\d?)?")) {
                    fb.replace(off, len, str, a);
                }
            }
        });
        addRow(form, row++, "평점 (1.0~5.0):", rateField);

        // ── 내용 ──
        contentArea = new JTextArea(4, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        GridBagConstraints lc = lc(row), fc = fc(row);
        fc.weighty = 1.0;
        fc.fill = GridBagConstraints.BOTH;
        form.add(new JLabel("내용:"), lc);
        form.add(contentScroll, fc);

        // ── 버튼 ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void save() {
        String category = (type == Type.PARTNER) ? (String) categoryBox.getSelectedItem() : "업무 수행";
        String rateText = rateField.getText().trim();
        double raw;
        try {
            raw = Double.parseDouble(rateText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "평점은 1.0~5.0 사이 숫자여야 합니다."); return;
        }
        if (raw < 1.0 || raw > 5.0) {
            JOptionPane.showMessageDialog(this, "평점은 1.0~5.0 사이여야 합니다."); return;
        }
        double rate = Math.floor(raw * 10) / 10.0;
        String content = contentArea.getText().trim();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "내용을 입력하세요.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int evalId   = nextId(conn, "evaluation");
                int itemId   = nextId(conn, "evaluation_item");

                int participationId;
                if (type == Type.CUSTOMER) {
                    // 고객 평가는 프로젝트 단위 — 첫 번째 참여자를 participation_id로 사용
                    if (members.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "프로젝트 참여자가 없어 저장할 수 없습니다."); return;
                    }
                    participationId = members.get(0).getId();
                } else if (isAdmin) {
                    ProjectParticipation evaluator = (ProjectParticipation) evaluatorBox.getSelectedItem();
                    if (evaluator == null) { JOptionPane.showMessageDialog(this, "평가자를 선택하세요."); return; }
                    participationId = evaluator.getId();
                } else {
                    participationId = myParticipation.getId();
                }

                // 중복 평가 차단
                if (type == Type.CUSTOMER) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT COUNT(*) FROM customer_evaluation ce JOIN evaluation e ON ce.id = e.id " +
                            "JOIN project_participation pp ON e.participation_id = pp.id " +
                            "WHERE pp.project_id = ? AND ce.customer_id = ?")) {
                        ps.setInt(1, projectId); ps.setInt(2, projectCustomerId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                JOptionPane.showMessageDialog(this, "이 프로젝트에 대한 고객 평가가 이미 존재합니다.", "중복 평가", JOptionPane.WARNING_MESSAGE);
                                conn.rollback(); return;
                            }
                        }
                    }
                } else if (type == Type.PM) {
                    ProjectParticipation pmTarget = (ProjectParticipation) targetBox.getSelectedItem();
                    if (pmTarget != null) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "SELECT COUNT(*) FROM pm_evaluation pe JOIN evaluation e ON pe.id = e.id " +
                                "WHERE e.participation_id = ? AND pe.pm_id = ?")) {
                            ps.setInt(1, participationId); ps.setInt(2, pmTarget.getDeveloperId());
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next() && rs.getInt(1) > 0) {
                                    JOptionPane.showMessageDialog(this, "해당 PM에 대한 평가가 이미 존재합니다.", "중복 평가", JOptionPane.WARNING_MESSAGE);
                                    conn.rollback(); return;
                                }
                            }
                        }
                    }
                } else if (type == Type.PARTNER) {
                    ProjectParticipation partnerTarget = (ProjectParticipation) targetBox.getSelectedItem();
                    if (partnerTarget != null) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "SELECT COUNT(*) FROM partner_evaluation pe JOIN evaluation e ON pe.id = e.id " +
                                "WHERE e.participation_id = ? AND pe.partner_id = ? AND e.participation_category = ?")) {
                            ps.setInt(1, participationId); ps.setInt(2, partnerTarget.getDeveloperId()); ps.setString(3, category);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next() && rs.getInt(1) > 0) {
                                    JOptionPane.showMessageDialog(this, "해당 동료에 대한 [" + category + "] 평가가 이미 존재합니다.", "중복 평가", JOptionPane.WARNING_MESSAGE);
                                    conn.rollback(); return;
                                }
                            }
                        }
                    }
                }

                // evaluation 삽입
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO evaluation(id,participation_id,participation_category) VALUES(?,?,?)")) {
                    ps.setInt(1, evalId);
                    ps.setInt(2, participationId);
                    ps.setString(3, category);
                    ps.executeUpdate();
                }

                // PM·PARTNER: 평가자와 대상이 동일인인지 검사
                if (type == Type.PM || type == Type.PARTNER) {
                    ProjectParticipation target = (ProjectParticipation) targetBox.getSelectedItem();
                    int evaluatorDevId = isAdmin
                        ? ((ProjectParticipation) evaluatorBox.getSelectedItem()).getDeveloperId()
                        : myParticipation.getDeveloperId();
                    if (target != null && evaluatorDevId == target.getDeveloperId()) {
                        JOptionPane.showMessageDialog(this, "자기 자신에게는 평가를 남길 수 없습니다.");
                        conn.rollback();
                        return;
                    }
                }

                // 서브테이블 삽입
                if (type == Type.CUSTOMER) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO customer_evaluation(id,customer_id) VALUES(?,?)")) {
                        ps.setInt(1, evalId);
                        ps.setInt(2, projectCustomerId);
                        ps.executeUpdate();
                    }
                } else if (type == Type.PM) {
                    ProjectParticipation pmTarget = (ProjectParticipation) targetBox.getSelectedItem();
                    if (pmTarget == null) { JOptionPane.showMessageDialog(this, "PM을 선택하세요."); return; }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO pm_evaluation(id,pm_id) VALUES(?,?)")) {
                        ps.setInt(1, evalId);
                        ps.setInt(2, pmTarget.getDeveloperId());
                        ps.executeUpdate();
                    }
                } else {
                    ProjectParticipation partner = (ProjectParticipation) targetBox.getSelectedItem();
                    if (partner == null) { JOptionPane.showMessageDialog(this, "동료를 선택하세요."); return; }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO partner_evaluation(id,partner_id) VALUES(?,?)")) {
                        ps.setInt(1, evalId);
                        ps.setInt(2, partner.getDeveloperId());
                        ps.executeUpdate();
                    }
                }

                // evaluation_item 삽입
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO evaluation_item(id,evaluation_id,rate,content) VALUES(?,?,?,?)")) {
                    ps.setInt(1, itemId);
                    ps.setInt(2, evalId);
                    ps.setDouble(3, rate);
                    ps.setString(4, content);
                    ps.executeUpdate();
                }

                conn.commit();
                saved = true;
                dispose();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fetchProjectName() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT project_name FROM project WHERE id=?")) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignored) {}
        return "(프로젝트 정보 없음)";
    }

    private String fetchCustomerName() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT customer_name FROM customer WHERE id=?")) {
            ps.setInt(1, projectCustomerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        } catch (Exception ignored) {}
        return "(고객 정보 없음)";
    }

    private int nextId(Connection c, String table) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(id),0)+1 FROM " + table);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 1;
        }
    }

    private void addRow(JPanel panel, int row, String label, Component comp) {
        panel.add(new JLabel(label), lc(row));
        panel.add(comp, fc(row));
    }

    private GridBagConstraints lc(int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row; c.anchor = GridBagConstraints.NORTHEAST;
        c.insets = new Insets(5, 5, 5, 8);
        return c;
    }

    private GridBagConstraints fc(int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row; c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5, 0, 5, 5);
        return c;
    }

    public boolean isSaved() { return saved; }
}
