package com.example.swing.dialog;

import com.example.dao.DatabaseConnection;
import com.example.dao.DeveloperDAO;
import com.example.dao.PartnerEvaluationDAO;
import com.example.dao.ProjectParticipationDAO;
import com.example.model.Developer;
import com.example.model.PartnerEvaluation;
import com.example.model.ProjectParticipation;
import com.example.util.ComboAutoComplete;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.Predicate;

public class PartnerEvaluationDialog extends JDialog {

    private static final String[] CATEGORIES = {"업무 수행", "커뮤니케이션"};
    private static final String[] ITEM_COLS  = {"평점", "내용"};

    private final JComboBox<ProjectParticipation> participationBox = new JComboBox<>();
    private final JComboBox<String>    categoryBox = new JComboBox<>(CATEGORIES);
    private final JComboBox<Developer> partnerBox  = new JComboBox<>();

    private final DefaultTableModel itemModel = new DefaultTableModel(ITEM_COLS, 0);
    private final JTable itemTable = new JTable(itemModel);

    private final PartnerEvaluationDAO dao;
    private final PartnerEvaluation editing;
    private boolean saved = false;

    public PartnerEvaluationDialog(JFrame parent, PartnerEvaluation pe, PartnerEvaluationDAO dao) {
        super(parent, pe == null ? "동료 평가 추가" : "동료 평가 수정", true);
        this.dao = dao;
        this.editing = pe;

        loadCombos();
        buildUI();
        if (editing != null) loadExisting();

        pack();
        setLocationRelativeTo(parent);
    }

    private void loadCombos() {
        try {
            for (ProjectParticipation pp : new ProjectParticipationDAO().getAllParticipations())
                participationBox.addItem(pp);
            for (Developer d : new DeveloperDAO().getAllDevelopers())
                partnerBox.addItem(d);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "콤보 로드 실패: " + ex.getMessage());
        }
        ComboAutoComplete.apply(participationBox);
        ComboAutoComplete.apply(partnerBox);
    }

    private void loadExisting() {
        String sql = "SELECT participation_id, participation_category FROM evaluation WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, editing.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ppId = rs.getInt("participation_id");
                    String cat = rs.getString("participation_category");
                    selectInCombo(participationBox, pp -> pp.getId() == ppId);
                    categoryBox.setSelectedItem(cat);
                }
            }
            selectInCombo(partnerBox, d -> d.getId() == editing.getPartnerId());

            try (PreparedStatement ps2 = conn.prepareStatement(
                "SELECT rate, content FROM evaluation_item WHERE evaluation_id=? ORDER BY id")) {
                ps2.setInt(1, editing.getId());
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while (rs2.next())
                        itemModel.addRow(new Object[]{rs2.getDouble("rate"), rs2.getString("content")});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "기존 데이터 로드 실패: " + ex.getMessage());
        }
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        GridBagConstraints lc = lc(), fc = fc();
        lc.gridy = 0; fc.gridy = 0; form.add(new JLabel("프로젝트 참여:"), lc); form.add(participationBox, fc);
        lc.gridy = 1; fc.gridy = 1; form.add(new JLabel("카테고리:"),      lc); form.add(categoryBox,      fc);
        lc.gridy = 2; fc.gridy = 2; form.add(new JLabel("동료:"),          lc); form.add(partnerBox,       fc);

        itemTable.setRowHeight(24);
        JPanel itemPanel = new JPanel(new BorderLayout(3, 3));
        itemPanel.setBorder(BorderFactory.createTitledBorder("평가 항목 (평점 0.00 ~ 5.00)"));
        itemPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        JPanel itemBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRow = new JButton("+ 행 추가");
        JButton delRow = new JButton("- 행 삭제");
        addRow.addActionListener(e -> itemModel.addRow(new Object[]{0.0, ""}));
        delRow.addActionListener(e -> {
            int r = itemTable.getSelectedRow();
            if (r >= 0) itemModel.removeRow(r);
        });
        itemBtns.add(addRow); itemBtns.add(delRow);
        itemPanel.add(itemBtns, BorderLayout.SOUTH);

        JPanel dialogBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton("저장");
        JButton cancelBtn = new JButton("취소");
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        dialogBtns.add(saveBtn); dialogBtns.add(cancelBtn);

        setLayout(new BorderLayout());
        add(form,       BorderLayout.NORTH);
        add(itemPanel,  BorderLayout.CENTER);
        add(dialogBtns, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(520, 480));
    }

    private void save() {
        ProjectParticipation pp = (ProjectParticipation) participationBox.getSelectedItem();
        String category = (String) categoryBox.getSelectedItem();
        Developer partner = (Developer) partnerBox.getSelectedItem();
        if (pp == null || partner == null || category == null) {
            JOptionPane.showMessageDialog(this, "프로젝트 참여, 카테고리, 동료를 모두 선택하세요.");
            return;
        }
        if (itemTable.isEditing()) itemTable.getCellEditor().stopCellEditing();

        for (int r = 0; r < itemModel.getRowCount(); r++) {
            try {
                double rate = Double.parseDouble(String.valueOf(itemModel.getValueAt(r, 0)));
                if (rate < 0 || rate > 5) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, (r + 1) + "행 평점은 0~5 사이 숫자여야 합니다.");
                return;
            }
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int evalId;
                if (editing == null) {
                    evalId = nextId(conn, "evaluation");
                    insertEvaluation(conn, evalId, pp.getId(), category);
                    insertChild(conn, evalId, partner.getId());
                } else {
                    evalId = editing.getId();
                    updateEvaluation(conn, evalId, pp.getId(), category);
                    updateChild(conn, evalId, partner.getId());
                    deleteItems(conn, evalId);
                }
                int nextItemId = nextId(conn, "evaluation_item");
                for (int r = 0; r < itemModel.getRowCount(); r++) {
                    double rate = Double.parseDouble(String.valueOf(itemModel.getValueAt(r, 0)));
                    String content = String.valueOf(itemModel.getValueAt(r, 1));
                    insertItem(conn, nextItemId++, evalId, rate, content);
                }
                conn.commit();
                saved = true;
                dispose();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "저장 오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int nextId(Connection c, String table) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(id),0)+1 FROM " + table);
             ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 1; }
    }
    private void insertEvaluation(Connection c, int id, int ppId, String cat) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
            "INSERT INTO evaluation(id,participation_id,participation_category) VALUES(?,?,?)")) {
            ps.setInt(1,id); ps.setInt(2,ppId); ps.setString(3,cat); ps.executeUpdate();
        }
    }
    private void updateEvaluation(Connection c, int id, int ppId, String cat) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
            "UPDATE evaluation SET participation_id=?, participation_category=? WHERE id=?")) {
            ps.setInt(1,ppId); ps.setString(2,cat); ps.setInt(3,id); ps.executeUpdate();
        }
    }
    private void insertChild(Connection c, int id, int partnerId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
            "INSERT INTO partner_evaluation(id,partner_id) VALUES(?,?)")) {
            ps.setInt(1,id); ps.setInt(2,partnerId); ps.executeUpdate();
        }
    }
    private void updateChild(Connection c, int id, int partnerId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
            "UPDATE partner_evaluation SET partner_id=? WHERE id=?")) {
            ps.setInt(1,partnerId); ps.setInt(2,id); ps.executeUpdate();
        }
    }
    private void deleteItems(Connection c, int evalId) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
            "DELETE FROM evaluation_item WHERE evaluation_id=?")) {
            ps.setInt(1,evalId); ps.executeUpdate();
        }
    }
    private void insertItem(Connection c, int id, int evalId, double rate, String content) throws Exception {
        try (PreparedStatement ps = c.prepareStatement(
            "INSERT INTO evaluation_item(id,evaluation_id,rate,content) VALUES(?,?,?,?)")) {
            ps.setInt(1,id); ps.setInt(2,evalId); ps.setDouble(3,rate); ps.setString(4,content); ps.executeUpdate();
        }
    }

    private <T> void selectInCombo(JComboBox<T> box, Predicate<T> pred) {
        for (int i = 0; i < box.getItemCount(); i++)
            if (pred.test(box.getItemAt(i))) { box.setSelectedIndex(i); return; }
    }

    public boolean isSaved() { return saved; }
    private GridBagConstraints lc(){ GridBagConstraints c=new GridBagConstraints();
        c.anchor=GridBagConstraints.EAST; c.insets=new Insets(5,5,5,8); return c; }
    private GridBagConstraints fc(){ GridBagConstraints c=new GridBagConstraints();
        c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1.0; c.insets=new Insets(5,0,5,5);
        c.gridwidth=GridBagConstraints.REMAINDER; return c; }
}
