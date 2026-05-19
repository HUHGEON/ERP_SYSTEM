package com.example.swing.panel;

import com.example.dao.StudyActivityHistoryDAO;
import com.example.dao.StudyDAO;
import com.example.dao.StudyParticipationDAO;
import com.example.model.Study;
import com.example.model.StudyActivityHistory;
import com.example.model.StudyParticipation;
import com.example.swing.dialog.StudyActivityHistoryDialog;
import com.example.swing.dialog.StudyDialog;
import com.example.swing.dialog.StudyParticipationDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudyPanel extends JPanel {

    private static final String[] STUDY_COLS  = {"스터디명", "카테고리"};
    private static final String[] MEMBER_COLS = {"참여 직원"};

    private static final Color CARD_BG      = new Color(252, 252, 253);
    private static final Color CARD_BORDER  = new Color(218, 220, 226);
    private static final Color NAVY         = new Color(25,  50, 120);
    private static final Color DATE_FG      = new Color(35,  60, 130);
    private static final Color CONTENT_FG   = new Color(40,  45,  65);
    private static final Color SECTION_BG   = new Color(244, 245, 248);

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int myId = UserSession.getInstance().getEmployeeId();

    private final StudyDAO                 studyDAO  = new StudyDAO();
    private final StudyParticipationDAO    spDAO     = new StudyParticipationDAO();
    private final StudyActivityHistoryDAO  histDAO   = new StudyActivityHistoryDAO();

    private final DefaultTableModel studyModel  = new DefaultTableModel(STUDY_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel memberModel = new DefaultTableModel(MEMBER_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable studyTable  = new JTable(studyModel);
    private final JTable memberTable = new JTable(memberModel);

    private final JTextField nameField     = new JTextField(15);
    private final JTextField categoryField = new JTextField(10);

    // 활동 카드 영역
    private final JPanel  activityCardPanel = new JPanel();
    private final JLabel  activityTitleLbl  = new JLabel("활동 기록");

    private List<Study>              studyList;
    private List<StudyParticipation> memberList;
    private List<StudyActivityHistory> activityList;
    private int selectedStudyId = -1;

    // 하단 버튼 (권한 제어)
    private final JButton addMemberBtn    = new JButton("직원 추가");
    private final JButton deleteMemberBtn = new JButton("직원 제거");
    private final JButton addActivityBtn  = new JButton("+ 활동 추가");

    public StudyPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── 상단: 검색 + 스터디 목록 ─────────────────────────────
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("스터디명:"));
        searchPanel.add(nameField);
        searchPanel.add(new JLabel("카테고리:"));
        searchPanel.add(categoryField);
        JButton searchBtn = new JButton("검색");
        JButton resetBtn  = new JButton("초기화");
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studyTable.getTableHeader().setReorderingAllowed(false);
        studyTable.setRowHeight(24);

        JPanel studyBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addStudyBtn    = new JButton("추가");
        JButton editStudyBtn   = new JButton("수정");
        JButton deleteStudyBtn = new JButton("삭제");
        studyBtnPanel.add(addStudyBtn);
        studyBtnPanel.add(editStudyBtn);
        studyBtnPanel.add(deleteStudyBtn);

        JPanel topPanel = new JPanel(new BorderLayout(3, 3));
        topPanel.add(searchPanel,            BorderLayout.NORTH);
        topPanel.add(new JScrollPane(studyTable), BorderLayout.CENTER);
        topPanel.add(studyBtnPanel,          BorderLayout.SOUTH);

        // ── 하단 왼쪽: 참여 직원 ─────────────────────────────────
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

        // ── 하단 오른쪽: 활동 기록 카드 ─────────────────────────
        activityCardPanel.setLayout(new BoxLayout(activityCardPanel, BoxLayout.Y_AXIS));
        activityCardPanel.setBackground(Color.WHITE);

        JScrollPane activityScroll = new JScrollPane(activityCardPanel);
        activityScroll.getVerticalScrollBar().setUnitIncrement(12);
        activityScroll.setBorder(null);

        JPanel activityHeader = new JPanel(new BorderLayout());
        activityHeader.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
        activityTitleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        activityHeader.add(activityTitleLbl, BorderLayout.WEST);
        activityHeader.add(addActivityBtn,   BorderLayout.EAST);

        activityHeader.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && activityHeader.isShowing()) {
                javax.swing.table.JTableHeader th = memberTable.getTableHeader();
                Component rendered = th.getDefaultRenderer()
                    .getTableCellRendererComponent(memberTable, "참여 직원", false, false, -1, 0);
                activityHeader.setBackground(rendered.getBackground());
                activityTitleLbl.setForeground(rendered.getForeground());
                activityTitleLbl.setFont(rendered.getFont());
                int h = th.getPreferredSize().height;
                activityHeader.setPreferredSize(new Dimension(activityHeader.getWidth(), h));
                activityHeader.setMinimumSize(new Dimension(0, h));
                activityHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
                activityHeader.revalidate();
                activityHeader.repaint();
            }
        });

        JPanel activityPanel = new JPanel(new BorderLayout(3, 3));
        activityPanel.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
        activityPanel.add(activityHeader, BorderLayout.NORTH);
        activityPanel.add(activityScroll, BorderLayout.CENTER);

        // ── 하단 좌/우 분할 ──────────────────────────────────────
        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, memberPanel, activityPanel);
        bottomSplit.setResizeWeight(0.35);
        bottomSplit.setDividerSize(5);

        // ── 전체 상/하 분할 ──────────────────────────────────────
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomSplit);
        mainSplit.setResizeWeight(0.45);
        mainSplit.setDividerSize(6);
        add(mainSplit, BorderLayout.CENTER);

        // ── 권한 제어 ─────────────────────────────────────────────
        if (!isAdmin) {
            searchPanel.setVisible(false);
            addStudyBtn.setVisible(false);
            editStudyBtn.setVisible(false);
            deleteStudyBtn.setVisible(false);
            deleteMemberBtn.setVisible(false);
        }

        // ── 이벤트 ───────────────────────────────────────────────
        searchBtn.addActionListener(e -> loadStudies());
        nameField.addActionListener(e -> loadStudies());
        categoryField.addActionListener(e -> loadStudies());
        resetBtn.addActionListener(e -> { nameField.setText(""); categoryField.setText(""); loadStudies(); });

        addStudyBtn.addActionListener(e -> openStudyDialog(null));
        editStudyBtn.addActionListener(e -> {
            int row = studyTable.getSelectedRow();
            if (row < 0) { info("수정할 스터디를 선택하세요."); return; }
            openStudyDialog(studyList.get(row));
        });
        deleteStudyBtn.addActionListener(e -> deleteStudy());

        studyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = studyTable.getSelectedRow();
                if (row >= 0) {
                    Study s = studyList.get(row);
                    selectedStudyId = s.getId();
                    activityTitleLbl.setText("활동 기록 — " + s.getStudyName());
                    loadMembers();
                    loadActivityCards();
                }
            }
        });

        addMemberBtn.addActionListener(e -> {
            if (selectedStudyId < 0) { info("스터디를 먼저 선택하세요."); return; }
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            StudyParticipationDialog dialog = new StudyParticipationDialog(frame, null, spDAO);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadMembers();
        });

        deleteMemberBtn.addActionListener(e -> deleteMember());

        addActivityBtn.addActionListener(e -> {
            if (selectedStudyId < 0) { info("스터디를 먼저 선택하세요."); return; }
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            StudyActivityHistory template = new StudyActivityHistory(0, selectedStudyId, "", "", "");
            StudyActivityHistoryDialog dialog = new StudyActivityHistoryDialog(frame, template, histDAO);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadActivityCards();
        });

        loadStudies();
    }

    // ── 데이터 로드 ───────────────────────────────────────────────
    private void loadStudies() {
        try {
            studyList = isAdmin
                ? studyDAO.search(nameField.getText().trim(), categoryField.getText().trim())
                : studyDAO.getByEmployeeId(myId);
            studyModel.setRowCount(0);
            for (Study s : studyList)
                studyModel.addRow(new Object[]{s.getStudyName(), s.getCategory()});
            selectedStudyId = -1;
            memberModel.setRowCount(0);
            activityCardPanel.removeAll();
            activityCardPanel.revalidate();
            activityCardPanel.repaint();
            activityTitleLbl.setText("활동 기록");
        } catch (Exception ex) { error("데이터 로드 오류: " + ex.getMessage()); }
    }

    private void loadMembers() {
        if (selectedStudyId < 0) return;
        try {
            memberList = spDAO.searchByStudyId(selectedStudyId);
            memberModel.setRowCount(0);
            for (StudyParticipation sp : memberList)
                memberModel.addRow(new Object[]{sp.getEmployeeName()});
        } catch (Exception ex) { error("참여 직원 로드 오류: " + ex.getMessage()); }
    }

    private void loadActivityCards() {
        if (selectedStudyId < 0) return;
        activityCardPanel.removeAll();
        try {
            activityList = histDAO.getByStudyId(selectedStudyId);
            if (activityList.isEmpty()) {
                JLabel empty = new JLabel("등록된 활동 기록이 없습니다.", SwingConstants.CENTER);
                empty.setForeground(new Color(160, 170, 190));
                empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                activityCardPanel.add(Box.createVerticalStrut(20));
                activityCardPanel.add(empty);
            } else {
                activityCardPanel.add(Box.createVerticalStrut(6));
                for (StudyActivityHistory h : activityList) {
                    activityCardPanel.add(buildActivityCard(h));
                    activityCardPanel.add(Box.createVerticalStrut(5));
                }
            }
        } catch (Exception ex) { error("활동 기록 로드 오류: " + ex.getMessage()); }
        activityCardPanel.revalidate();
        activityCardPanel.repaint();
    }

    private JPanel buildActivityCard(StudyActivityHistory h) {
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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel dateLbl = new JLabel(h.getActivityDate() != null ? h.getActivityDate() : "");
        dateLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        dateLbl.setForeground(DATE_FG);

        String escaped = (h.getContent() == null ? "" : h.getContent())
            .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\n", "<br>");
        JLabel contentLbl = new JLabel(
            "<html><div style='font-size:11px;color:#28304d;font-family:SansSerif;'>" + escaped + "</div></html>");
        contentLbl.setVerticalAlignment(SwingConstants.TOP);

        card.add(dateLbl,    BorderLayout.NORTH);
        card.add(contentLbl, BorderLayout.CENTER);

        return card;
    }

    // ── 다이얼로그 ────────────────────────────────────────────────
    private void openStudyDialog(Study s) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        StudyDialog dialog = new StudyDialog(frame, s, studyDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadStudies();
    }

    private void deleteStudy() {
        int row = studyTable.getSelectedRow();
        if (row < 0) { info("삭제할 스터디를 선택하세요."); return; }
        Study s = studyList.get(row);
        if (JOptionPane.showConfirmDialog(this, "'" + s.getStudyName() + "' 스터디를 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { studyDAO.delete(s.getId()); loadStudies(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    private void deleteMember() {
        int row = memberTable.getSelectedRow();
        if (row < 0) { info("제거할 직원을 선택하세요."); return; }
        StudyParticipation sp = memberList.get(row);
        if (JOptionPane.showConfirmDialog(this, "'" + sp.getEmployeeName() + "' 직원을 스터디에서 제거하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { spDAO.delete(sp.getId()); loadMembers(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    /** 외부 호출: 스터디명으로 필터 후 해당 행 선택. */
    public void filterByStudyName(String studyName, int studyId) {
        nameField.setText(studyName);
        categoryField.setText("");
        loadStudies();
        selectRow(studyId);
    }

    /** 외부 호출: 스터디 id로 행 선택. 필터/권한으로 안 보이면 필터 초기화 후 재시도. */
    public void selectStudyById(int studyId) {
        if (!selectRow(studyId)) {
            if (isAdmin) {
                nameField.setText("");
                categoryField.setText("");
            }
            loadStudies();
            selectRow(studyId);
        }
    }

    private boolean selectRow(int studyId) {
        if (studyList == null) return false;
        for (int i = 0; i < studyList.size(); i++) {
            if (studyList.get(i).getId() == studyId) {
                studyTable.setRowSelectionInterval(i, i);
                studyTable.scrollRectToVisible(studyTable.getCellRect(i, 0, true));
                return true;
            }
        }
        return false;
    }

    private void info(String msg)  { JOptionPane.showMessageDialog(this, msg); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }
}
