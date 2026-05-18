package com.example.swing.panel;

import com.example.dao.StudyDAO;
import com.example.dao.StudyParticipationDAO;
import com.example.model.Study;
import com.example.model.StudyParticipation;
import com.example.swing.dialog.StudyDialog;
import com.example.swing.dialog.StudyParticipationDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudyPanel extends JPanel {

    private static final String[] STUDY_COLS   = {"ID", "스터디명", "카테고리"};
    private static final String[] MEMBER_COLS  = {"참여ID", "직원ID", "직원명"};

    private final StudyDAO              studyDAO  = new StudyDAO();
    private final StudyParticipationDAO spDAO     = new StudyParticipationDAO();

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
    private final JLabel memberLabel = new JLabel("참여 직원 — 스터디를 선택하세요");

    private List<Study>             studyList;
    private List<StudyParticipation> memberList;
    private int selectedStudyId = -1;

    public StudyPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── 상단: 스터디 목록 ──
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
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(new JScrollPane(studyTable), BorderLayout.CENTER);
        topPanel.add(studyBtnPanel, BorderLayout.SOUTH);

        // ── 하단: 참여 직원 ──
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getTableHeader().setReorderingAllowed(false);
        memberTable.setRowHeight(24);

        JPanel memberBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addMemberBtn    = new JButton("직원 추가");
        JButton deleteMemberBtn = new JButton("직원 제거");
        memberBtnPanel.add(addMemberBtn);
        memberBtnPanel.add(deleteMemberBtn);

        memberLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 0));
        memberLabel.setFont(memberLabel.getFont().deriveFont(Font.BOLD));

        JPanel bottomPanel = new JPanel(new BorderLayout(3, 3));
        bottomPanel.add(memberLabel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(memberTable), BorderLayout.CENTER);
        bottomPanel.add(memberBtnPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        split.setResizeWeight(0.55);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        // ── 이벤트 ──
        searchBtn.addActionListener(e -> loadStudies());
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
                    memberLabel.setText("참여 직원 — " + s.getStudyName());
                    loadMembers();
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

        loadStudies();
    }

    private void loadStudies() {
        try {
            studyList = studyDAO.search(nameField.getText().trim(), categoryField.getText().trim());
            studyModel.setRowCount(0);
            for (Study s : studyList)
                studyModel.addRow(new Object[]{s.getId(), s.getStudyName(), s.getCategory()});
            selectedStudyId = -1;
            memberModel.setRowCount(0);
            memberLabel.setText("참여 직원 — 스터디를 선택하세요");
        } catch (Exception ex) { error("데이터 로드 오류: " + ex.getMessage()); }
    }

    private void loadMembers() {
        if (selectedStudyId < 0) return;
        try {
            memberList = spDAO.searchByStudyId(selectedStudyId);
            memberModel.setRowCount(0);
            for (StudyParticipation sp : memberList)
                memberModel.addRow(new Object[]{sp.getId(), sp.getEmployeeId(), sp.getEmployeeName()});
        } catch (Exception ex) { error("참여 직원 로드 오류: " + ex.getMessage()); }
    }

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

    private void info(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }
}
