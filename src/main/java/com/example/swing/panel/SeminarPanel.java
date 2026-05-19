package com.example.swing.panel;

import com.example.dao.SeminarDAO;
import com.example.dao.SeminarEvaluationDAO;
import com.example.dao.SeminarParticipationDAO;
import com.example.model.Seminar;
import com.example.model.SeminarEvaluation;
import com.example.model.SeminarParticipation;
import com.example.swing.dialog.SeminarDialog;
import com.example.swing.dialog.SeminarEvaluationDialog;
import com.example.swing.dialog.SeminarParticipationDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SeminarPanel extends JPanel {

    private static final String[] SEMINAR_COLS = {"세미나명", "주제", "일시"};
    private static final String[] MEMBER_COLS  = {"참여 직원"};

    private static final Color CARD_BG     = new Color(252, 252, 253);
    private static final Color CARD_BORDER = new Color(218, 220, 226);
    private static final Color NAVY        = new Color(25,  50, 120);
    private static final Color DATE_FG     = new Color(35,  60, 130);
    private static final Color RATING_FG   = new Color(200, 140, 0);
    private static final Color CONTENT_FG  = new Color(40,  45,  65);

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int myId = UserSession.getInstance().getEmployeeId();

    private final SeminarDAO             seminarDAO = new SeminarDAO();
    private final SeminarParticipationDAO spDAO     = new SeminarParticipationDAO();
    private final SeminarEvaluationDAO   evalDAO    = new SeminarEvaluationDAO();

    private final DefaultTableModel seminarModel = new DefaultTableModel(SEMINAR_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel memberModel = new DefaultTableModel(MEMBER_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable seminarTable = new JTable(seminarModel);
    private final JTable memberTable  = new JTable(memberModel);

    private final JTextField nameField  = new JTextField(15);
    private final JTextField topicField = new JTextField(10);

    private final JPanel evalCardPanel  = new JPanel();
    private final JLabel evalTitleLbl   = new JLabel("평가");

    private List<Seminar>              seminarList;
    private List<SeminarParticipation> memberList;
    private List<SeminarEvaluation>    evalList;
    private int selectedSeminarId = -1;

    private final JButton addMemberBtn    = new JButton("직원 추가");
    private final JButton deleteMemberBtn = new JButton("직원 제거");
    private final JButton addEvalBtn      = new JButton("+ 평가 추가");

    public SeminarPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 상단: 검색 + 세미나 목록
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("세미나명:"));
        searchPanel.add(nameField);
        searchPanel.add(new JLabel("주제:"));
        searchPanel.add(topicField);
        JButton searchBtn = new JButton("검색");
        JButton resetBtn  = new JButton("초기화");
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        seminarTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        seminarTable.getTableHeader().setReorderingAllowed(false);
        seminarTable.setRowHeight(24);

        JPanel seminarBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addSeminarBtn    = new JButton("추가");
        JButton editSeminarBtn   = new JButton("수정");
        JButton deleteSeminarBtn = new JButton("삭제");
        seminarBtnPanel.add(addSeminarBtn);
        seminarBtnPanel.add(editSeminarBtn);
        seminarBtnPanel.add(deleteSeminarBtn);

        JPanel topPanel = new JPanel(new BorderLayout(3, 3));
        topPanel.add(searchPanel,               BorderLayout.NORTH);
        topPanel.add(new JScrollPane(seminarTable), BorderLayout.CENTER);
        topPanel.add(seminarBtnPanel,           BorderLayout.SOUTH);

        // 하단 왼쪽: 참여 직원
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getTableHeader().setReorderingAllowed(false);
        memberTable.setRowHeight(24);

        JPanel memberBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        memberBtnPanel.add(addMemberBtn);
        memberBtnPanel.add(deleteMemberBtn);

        JPanel memberPanel = new JPanel(new BorderLayout(3, 3));
        memberPanel.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        memberPanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);
        memberPanel.add(memberBtnPanel,               BorderLayout.SOUTH);

        // 하단 오른쪽: 평가 카드
        evalCardPanel.setLayout(new BoxLayout(evalCardPanel, BoxLayout.Y_AXIS));
        evalCardPanel.setBackground(Color.WHITE);

        JScrollPane evalScroll = new JScrollPane(evalCardPanel);
        evalScroll.getVerticalScrollBar().setUnitIncrement(12);
        evalScroll.setBorder(null);

        JPanel evalHeader = new JPanel(new BorderLayout());
        evalHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
        evalTitleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        evalHeader.add(evalTitleLbl, BorderLayout.WEST);
        evalHeader.add(addEvalBtn,   BorderLayout.EAST);

        evalHeader.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && evalHeader.isShowing()) {
                javax.swing.table.JTableHeader th = memberTable.getTableHeader();
                Component rendered = th.getDefaultRenderer()
                    .getTableCellRendererComponent(memberTable, "참여 직원", false, false, -1, 0);
                evalHeader.setBackground(rendered.getBackground());
                evalTitleLbl.setForeground(rendered.getForeground());
                evalTitleLbl.setFont(rendered.getFont());
                int h = th.getPreferredSize().height;
                evalHeader.setPreferredSize(new Dimension(evalHeader.getWidth(), h));
                evalHeader.setMinimumSize(new Dimension(0, h));
                evalHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
                evalHeader.revalidate();
                evalHeader.repaint();
            }
        });

        JPanel evalPanel = new JPanel(new BorderLayout(3, 3));
        evalPanel.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        evalPanel.add(evalHeader, BorderLayout.NORTH);
        evalPanel.add(evalScroll, BorderLayout.CENTER);

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, memberPanel, evalPanel);
        bottomSplit.setResizeWeight(0.35);
        bottomSplit.setDividerSize(5);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomSplit);
        mainSplit.setResizeWeight(0.45);
        mainSplit.setDividerSize(6);
        add(mainSplit, BorderLayout.CENTER);

        // 권한 제어
        if (!isAdmin) {
            searchPanel.setVisible(false);
            addSeminarBtn.setVisible(false);
            editSeminarBtn.setVisible(false);
            deleteSeminarBtn.setVisible(false);
            deleteMemberBtn.setVisible(false);
        }

        // 이벤트
        searchBtn.addActionListener(e -> loadSeminars());
        nameField.addActionListener(e -> loadSeminars());
        topicField.addActionListener(e -> loadSeminars());
        resetBtn.addActionListener(e -> { nameField.setText(""); topicField.setText(""); loadSeminars(); });

        addSeminarBtn.addActionListener(e -> openSeminarDialog(null));
        editSeminarBtn.addActionListener(e -> {
            int row = seminarTable.getSelectedRow();
            if (row < 0) { info("수정할 세미나를 선택하세요."); return; }
            openSeminarDialog(seminarList.get(row));
        });
        deleteSeminarBtn.addActionListener(e -> deleteSeminar());

        seminarTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = seminarTable.getSelectedRow();
                if (row >= 0) {
                    Seminar s = seminarList.get(row);
                    selectedSeminarId = s.getId();
                    evalTitleLbl.setText("평가 — " + s.getSeminarName());
                    loadMembers();
                    loadEvalCards();
                }
            }
        });

        addMemberBtn.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            SeminarParticipationDialog dialog = new SeminarParticipationDialog(frame, null, spDAO, selectedSeminarId);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadMembers();
        });

        deleteMemberBtn.addActionListener(e -> deleteMember());

        addEvalBtn.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            SeminarEvaluationDialog dialog = new SeminarEvaluationDialog(frame, null, selectedSeminarId, evalDAO);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadEvalCards();
        });

        loadSeminars();
    }

    // ── 데이터 로드 ───────────────────────────────────────────────
    private void loadSeminars() {
        try {
            seminarList = isAdmin
                ? seminarDAO.search(nameField.getText().trim(), topicField.getText().trim())
                : seminarDAO.getByEmployeeId(myId);
            seminarModel.setRowCount(0);
            for (Seminar s : seminarList)
                seminarModel.addRow(new Object[]{s.getSeminarName(), s.getTopic(), s.getDateTime()});
            selectedSeminarId = -1;
            memberModel.setRowCount(0);
            evalCardPanel.removeAll();
            evalCardPanel.revalidate();
            evalCardPanel.repaint();
            evalTitleLbl.setText("평가");
        } catch (Exception ex) { error("데이터 로드 오류: " + ex.getMessage()); }
    }

    private void loadMembers() {
        if (selectedSeminarId < 0) return;
        try {
            memberList = spDAO.searchBySeminarId(selectedSeminarId);
            memberModel.setRowCount(0);
            for (SeminarParticipation sp : memberList)
                memberModel.addRow(new Object[]{sp.getEmployeeName()});
        } catch (Exception ex) { error("참여 직원 로드 오류: " + ex.getMessage()); }
    }

    private void loadEvalCards() {
        if (selectedSeminarId < 0) return;
        evalCardPanel.removeAll();
        try {
            evalList = evalDAO.getBySeminarId(selectedSeminarId);
            if (evalList.isEmpty()) {
                JLabel empty = new JLabel("등록된 평가가 없습니다.", SwingConstants.CENTER);
                empty.setForeground(new Color(160, 170, 190));
                empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                evalCardPanel.add(Box.createVerticalStrut(20));
                evalCardPanel.add(empty);
            } else {
                evalCardPanel.add(Box.createVerticalStrut(6));
                for (SeminarEvaluation ev : evalList) {
                    evalCardPanel.add(buildEvalCard(ev));
                    evalCardPanel.add(Box.createVerticalStrut(5));
                }
            }
        } catch (Exception ex) { error("평가 로드 오류: " + ex.getMessage()); }
        evalCardPanel.revalidate();
        evalCardPanel.repaint();
    }

    private JPanel buildEvalCard(SeminarEvaluation ev) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, DATE_FG),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            )
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(CARD_BG);

        JLabel nameLbl = new JLabel(ev.getEmployeeName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        nameLbl.setForeground(DATE_FG);

        JLabel ratingLbl = new JLabel(String.format("★ %.2f", ev.getRating()));
        ratingLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        ratingLbl.setForeground(RATING_FG);

        topRow.add(nameLbl,  BorderLayout.WEST);
        topRow.add(ratingLbl, BorderLayout.EAST);

        String escaped = (ev.getComment() == null ? "" : ev.getComment())
            .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\n", "<br>");
        JLabel commentLbl = new JLabel(
            "<html><div style='font-size:11px;color:#28304d;font-family:SansSerif;'>" + escaped + "</div></html>");
        commentLbl.setVerticalAlignment(SwingConstants.TOP);

        card.add(topRow,    BorderLayout.NORTH);
        card.add(commentLbl, BorderLayout.CENTER);
        return card;
    }

    // ── 다이얼로그/삭제 ───────────────────────────────────────────
    private void openSeminarDialog(Seminar s) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        SeminarDialog dialog = new SeminarDialog(frame, s, seminarDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadSeminars();
    }

    private void deleteSeminar() {
        int row = seminarTable.getSelectedRow();
        if (row < 0) { info("삭제할 세미나를 선택하세요."); return; }
        Seminar s = seminarList.get(row);
        if (JOptionPane.showConfirmDialog(this, "'" + s.getSeminarName() + "' 세미나를 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { seminarDAO.delete(s.getId()); loadSeminars(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    private void deleteMember() {
        int row = memberTable.getSelectedRow();
        if (row < 0) { info("제거할 직원을 선택하세요."); return; }
        SeminarParticipation sp = memberList.get(row);
        if (JOptionPane.showConfirmDialog(this, "'" + sp.getEmployeeName() + "' 직원을 세미나에서 제거하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { spDAO.delete(sp.getId()); loadMembers(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    private void info(String msg)  { JOptionPane.showMessageDialog(this, msg); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }
}
