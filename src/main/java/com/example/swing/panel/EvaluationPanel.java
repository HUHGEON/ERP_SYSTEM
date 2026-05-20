package com.example.swing.panel;

import com.example.dao.EvaluationItemDAO;
import com.example.dao.ProjectDAO;
import com.example.dao.DatabaseConnection;
import com.example.dao.ProjectParticipationDAO;
import com.example.model.EvaluationItem;
import com.example.model.EvaluatorSummary;
import com.example.model.Project;
import com.example.model.ProjectParticipation;
import com.example.swing.dialog.EvaluationAddDialog;
import com.example.util.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.List;

public class EvaluationPanel extends JPanel {

    private static final String[] PROJECT_COLS = {"프로젝트명", "고객사", "상태"};
    private static final String[] PARTNER_COLS = {"동료명", "평균 평점", "평가 건수"};

    private static final Color CARD_BG      = Color.WHITE;
    private static final Color CARD_BORDER  = new Color(222, 226, 230);
    private static final Color STAR_COLOR   = new Color(255, 180, 0);
    private static final Color AVG_BG       = new Color(248, 249, 250);
    private static final Color PM_NAME_COLOR = new Color(66, 133, 244);
    private static final Color DELETE_COLOR  = new Color(200, 50, 50);

    private final boolean isAdmin = UserSession.getInstance().isAdmin();
    private final int myId = UserSession.getInstance().getEmployeeId();

    private final ProjectDAO              projectDAO = new ProjectDAO();
    private final EvaluationItemDAO       itemDAO    = new EvaluationItemDAO();
    private final ProjectParticipationDAO ppDAO      = new ProjectParticipationDAO();

    private final DefaultTableModel projectModel = noEditModel(PROJECT_COLS);
    private final JTable projectTable = new JTable(projectModel);
    private List<Project> projectList = new ArrayList<>();

    private int currentProjectId = -1;
    private int currentCustomerId = -1;
    /** 비관리자 전용: 현재 선택된 프로젝트에서 본인의 참여 레코드 */
    private ProjectParticipation myParticipation = null;

    // ── 고객 평가 탭 ──
    private final JLabel  customerAvgLabel  = avgLabel("프로젝트를 선택하세요");
    private final JPanel  customerCardPanel = cardContainer();

    // ── PM 평가 탭 ──
    private final JLabel  pmAvgLabel  = avgLabel("프로젝트를 선택하세요");
    private final JPanel  pmCardPanel = cardContainer();

    // ── 동료 평가 탭 ──
    private final DefaultTableModel partnerListModel = noEditModel(PARTNER_COLS);
    private final JTable partnerListTable = new JTable(partnerListModel);
    private final JLabel partnerCardLabel = avgLabel("동료를 선택하세요");
    private final JPanel partnerCardPanel = cardContainer();
    private List<EvaluatorSummary> partnerSummaryList = new ArrayList<>();

    // ── 탭 패널 참조 (추가 버튼 위치용) ──
    private JTabbedPane tabs;

    public EvaluationPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        configTable(projectTable);
        configTable(partnerListTable);

        // 프로젝트 목록
        JLabel title = new JLabel("프로젝트 목록");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setBorder(new EmptyBorder(0, 2, 4, 0));
        JPanel projectPanel = new JPanel(new BorderLayout(0, 4));
        projectPanel.add(title, BorderLayout.NORTH);
        projectPanel.add(new JScrollPane(projectTable), BorderLayout.CENTER);

        // 평가 탭 (고객 평가는 모두 열람 가능, 추가는 관리자만)
        tabs = new JTabbedPane();
        tabs.addTab("고객 평가", buildSimpleTab(customerAvgLabel, customerCardPanel,
                EvaluationAddDialog.Type.CUSTOMER));
        tabs.addTab("PM 평가",   buildSimpleTab(pmAvgLabel, pmCardPanel,
                EvaluationAddDialog.Type.PM));
        tabs.addTab("동료 평가", buildPartnerTab());

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, projectPanel, tabs);
        mainSplit.setResizeWeight(0.28);
        mainSplit.setDividerSize(7);
        add(mainSplit, BorderLayout.CENTER);

        // 프로젝트 선택 이벤트
        projectTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && projectTable.getSelectedRow() >= 0) {
                Project p = projectList.get(projectTable.getSelectedRow());
                currentProjectId  = p.getId();
                currentCustomerId = p.getCustomerId();
                loadMyParticipation();
                onProjectSelected(p);
            }
        });

        // 동료 선택 이벤트
        partnerListTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && partnerListTable.getSelectedRow() >= 0) {
                EvaluatorSummary s = partnerSummaryList.get(partnerListTable.getSelectedRow());
                loadPartnerCards(s);
            }
        });

        loadProjects();
    }

    // ── 탭 빌더 ──

    private JPanel buildSimpleTab(JLabel avgLbl, JPanel cardPanel, EvaluationAddDialog.Type type) {
        avgLbl.setBorder(new EmptyBorder(10, 14, 10, 14));

        // 평균 바 + 추가 버튼
        JPanel avgBar = new JPanel(new BorderLayout());
        avgBar.setBackground(AVG_BG);
        avgBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        avgBar.add(avgLbl, BorderLayout.WEST);

        // 추가 버튼 (고객 평가는 관리자만, PM/동료는 모두)
        boolean showAdd = (type == EvaluationAddDialog.Type.CUSTOMER) ? isAdmin : true;
        if (showAdd) {
            JButton addBtn = addButton();
            addBtn.addActionListener(e -> openAddDialog(type));
            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            btnWrap.setOpaque(false);
            btnWrap.add(addBtn);
            avgBar.add(btnWrap, BorderLayout.EAST);
        }

        JScrollPane scroll = new JScrollPane(cardPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel tab = new JPanel(new BorderLayout());
        tab.add(avgBar, BorderLayout.NORTH);
        tab.add(scroll, BorderLayout.CENTER);
        return tab;
    }

    private JPanel buildPartnerTab() {
        // 좌: 동료 목록
        partnerListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel listPanel = new JPanel(new BorderLayout(0, 4));
        JLabel lbl = new JLabel("동료 목록");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        lbl.setBorder(new EmptyBorder(6, 4, 4, 0));
        listPanel.add(lbl, BorderLayout.NORTH);
        listPanel.add(new JScrollPane(partnerListTable), BorderLayout.CENTER);

        // 우: 카드 섹션 (추가 버튼 포함)
        partnerCardLabel.setBorder(new EmptyBorder(10, 14, 10, 14));
        JPanel avgBar = new JPanel(new BorderLayout());
        avgBar.setBackground(AVG_BG);
        avgBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER));
        avgBar.add(partnerCardLabel, BorderLayout.WEST);

        JButton addBtn = addButton();
        addBtn.addActionListener(e -> openAddDialog(EvaluationAddDialog.Type.PARTNER));
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnWrap.setOpaque(false);
        btnWrap.add(addBtn);
        avgBar.add(btnWrap, BorderLayout.EAST);

        JScrollPane cardScroll = new JScrollPane(partnerCardPanel);
        cardScroll.setBorder(null);
        cardScroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel cardSection = new JPanel(new BorderLayout());
        cardSection.add(avgBar, BorderLayout.NORTH);
        cardSection.add(cardScroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, cardSection);
        split.setResizeWeight(0.35);
        split.setDividerSize(6);

        JPanel tab = new JPanel(new BorderLayout());
        tab.add(split, BorderLayout.CENTER);
        return tab;
    }

    // ── 추가 버튼 ──

    private JButton addButton() {
        JButton btn = new JButton("+ 평가 추가");
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
        btn.setForeground(new Color(33, 100, 200));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void openAddDialog(EvaluationAddDialog.Type type) {
        if (currentProjectId < 0) {
            JOptionPane.showMessageDialog(this, "먼저 프로젝트를 선택하세요.");
            return;
        }
        if (!isAdmin && myParticipation == null) {
            JOptionPane.showMessageDialog(this, "해당 프로젝트에 참여 기록이 없습니다.");
            return;
        }

        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        EvaluationAddDialog dialog = new EvaluationAddDialog(
                parent, type, currentProjectId, currentCustomerId, isAdmin, myParticipation);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            Project p = projectList.get(projectTable.getSelectedRow());
            onProjectSelected(p);
        }
    }

    // ── 데이터 로드 ──

    private void loadProjects() {
        try {
            projectList = isAdmin
                ? projectDAO.search("", "완료")
                : projectDAO.getCompletedByEmployeeId(myId);
            projectModel.setRowCount(0);
            for (Project p : projectList)
                projectModel.addRow(new Object[]{p.getProjectName(), p.getCustomerName(), p.getStatus()});
        } catch (Exception ex) { error("프로젝트 로드 오류: " + ex.getMessage()); }
    }

    private void loadMyParticipation() {
        if (isAdmin) { myParticipation = null; return; }
        try {
            myParticipation = ppDAO.getByProjectAndEmployee(currentProjectId, myId);
        } catch (Exception ex) { myParticipation = null; }
    }

    private void onProjectSelected(Project p) {
        try {
            List<EvaluationItem> items = itemDAO.getByProjectCustomer(p.getId());
            populateSimpleTab(items, customerAvgLabel, customerCardPanel, null);
        } catch (Exception ex) { error("고객평가 로드 오류: " + ex.getMessage()); }

        try {
            List<EvaluationItem> items = itemDAO.getByProjectPmWithName(p.getId());
            populateSimpleTab(items, pmAvgLabel, pmCardPanel, true);
        } catch (Exception ex) { error("PM평가 로드 오류: " + ex.getMessage()); }

        try {
            partnerSummaryList = itemDAO.getPartnerSummaryByProject(p.getId());
            partnerListModel.setRowCount(0);
            for (EvaluatorSummary s : partnerSummaryList)
                partnerListModel.addRow(new Object[]{
                    s.getName(), formatRate(s.getAvgRate()), s.getCount() + "건"
                });
            partnerCardPanel.removeAll();
            partnerCardLabel.setText("동료를 선택하세요");
            partnerCardPanel.revalidate();
            partnerCardPanel.repaint();
        } catch (Exception ex) { error("동료평가 로드 오류: " + ex.getMessage()); }
    }

    private void populateSimpleTab(List<EvaluationItem> items, JLabel avgLbl,
                                   JPanel cardPanel, Boolean showName) {
        cardPanel.removeAll();
        if (items.isEmpty()) {
            avgLbl.setText("평가 데이터 없음");
            avgLbl.setForeground(Color.GRAY);
            addEmptyNote(cardPanel);
        } else {
            double avg = items.stream().mapToDouble(EvaluationItem::getRate).average().orElse(0);
            avgLbl.setText(String.format("평균 평점  %s  %.1f / 5.0  (%d건)",
                    toStars(avg), avg, items.size()));
            avgLbl.setForeground(new Color(33, 37, 41));
            cardPanel.add(Box.createVerticalStrut(8));
            for (EvaluationItem item : items) {
                boolean canDel = canDelete(item);
                cardPanel.add(createReviewCard(item, Boolean.TRUE.equals(showName) ? item.getEvaluatorName() : null,
                        canDel, () -> deleteEvaluation(item.getEvaluationId(), cardPanel, avgLbl, items, showName)));
            }
            cardPanel.add(Box.createVerticalGlue());
        }
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void loadPartnerCards(EvaluatorSummary summary) {
        try {
            List<EvaluationItem> items = itemDAO.getByProjectAndPartner(currentProjectId, summary.getEvaluatorId());
            partnerCardPanel.removeAll();

            if (items.isEmpty()) {
                partnerCardLabel.setText(summary.getName() + " — 평가 항목 없음");
                partnerCardLabel.setForeground(Color.GRAY);
                addEmptyNote(partnerCardPanel);
            } else {
                double avg = items.stream().mapToDouble(EvaluationItem::getRate).average().orElse(0);
                partnerCardLabel.setText(String.format("%s  |  평균 %s  %.1f / 5.0  (%d건)",
                        summary.getName(), toStars(avg), avg, items.size()));
                partnerCardLabel.setForeground(new Color(33, 37, 41));

                LinkedHashMap<String, List<EvaluationItem>> grouped = new LinkedHashMap<>();
                for (EvaluationItem item : items) {
                    String cat = item.getCategory() != null ? item.getCategory() : "기타";
                    grouped.computeIfAbsent(cat, k -> new ArrayList<>()).add(item);
                }

                partnerCardPanel.add(Box.createVerticalStrut(8));
                for (Map.Entry<String, List<EvaluationItem>> entry : grouped.entrySet())
                    partnerCardPanel.add(createToggleSection(entry.getKey(), entry.getValue(), summary));
                partnerCardPanel.add(Box.createVerticalGlue());
            }

            partnerCardPanel.revalidate();
            partnerCardPanel.repaint();
        } catch (Exception ex) { error("동료 평가 로드 오류: " + ex.getMessage()); }
    }

    // ── 카드 UI ──

    private JPanel createToggleSection(String category, List<EvaluationItem> items, EvaluatorSummary summary) {
        double avg = items.stream().mapToDouble(EvaluationItem::getRate).average().orElse(0);
        String sectionSummary = String.format("  %s  |  평균 %s  %.1f점  (%d건)",
                category, toStars(avg), avg, items.size());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setVisible(false);
        for (EvaluationItem item : items) {
            boolean canDel = canDelete(item);
            contentPanel.add(createReviewCard(item, null, canDel,
                    () -> deleteEvaluation(item.getEvaluationId(), null, null, null, null)));
        }

        JButton toggleBtn = new JButton("▶" + sectionSummary);
        toggleBtn.setHorizontalAlignment(SwingConstants.LEFT);
        toggleBtn.setBackground(new Color(225, 232, 245));
        toggleBtn.setForeground(new Color(33, 50, 80));
        toggleBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        toggleBtn.setOpaque(true);
        toggleBtn.setBorderPainted(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        toggleBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggleBtn.setBorder(new EmptyBorder(0, 14, 0, 14));

        toggleBtn.addActionListener(e -> {
            boolean nowVisible = !contentPanel.isVisible();
            contentPanel.setVisible(nowVisible);
            toggleBtn.setText((nowVisible ? "▼" : "▶") + sectionSummary);
            partnerCardPanel.revalidate();
            partnerCardPanel.repaint();
        });

        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(new EmptyBorder(0, 0, 6, 0));
        section.add(toggleBtn);
        section.add(contentPanel);
        return section;
    }

    private JPanel createReviewCard(EvaluationItem item, String nameTag,
                                    boolean showDelete, Runnable onDelete) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(0, 10, 6, 10),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER),
                new EmptyBorder(8, 14, 8, 14)
            )
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHeader.setOpaque(false);

        JLabel rateLabel = new JLabel(String.format("%s  %.1f점", toStars(item.getRate()), item.getRate()));
        rateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        rateLabel.setForeground(STAR_COLOR);
        leftHeader.add(rateLabel);

        if (nameTag != null && !nameTag.isBlank()) {
            JLabel nameLabel = new JLabel("[ PM: " + nameTag + " ]");
            nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            nameLabel.setForeground(PM_NAME_COLOR);
            leftHeader.add(nameLabel);
        }
        headerRow.add(leftHeader, BorderLayout.WEST);

        if (showDelete) {
            JButton delBtn = new JButton("✕");
            delBtn.setFont(new Font("SansSerif", Font.PLAIN, 10));
            delBtn.setForeground(DELETE_COLOR);
            delBtn.setBorderPainted(false);
            delBtn.setContentAreaFilled(false);
            delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            delBtn.setMargin(new Insets(0, 4, 0, 4));
            delBtn.setToolTipText("이 평가 삭제");
            delBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "이 평가를 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION && onDelete != null) onDelete.run();
            });
            headerRow.add(delBtn, BorderLayout.EAST);
        }

        JTextArea contentArea = new JTextArea(item.getContent() != null ? item.getContent() : "");
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setOpaque(false);
        contentArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        contentArea.setForeground(new Color(52, 58, 64));
        contentArea.setBorder(null);

        card.add(headerRow, BorderLayout.NORTH);
        card.add(contentArea, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        return card;
    }

    private void addEmptyNote(JPanel panel) {
        JLabel lbl = new JLabel("등록된 평가가 없습니다.", SwingConstants.CENTER);
        lbl.setForeground(Color.GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(30));
        panel.add(lbl);
    }

    // ── 삭제 ──

    private boolean canDelete(EvaluationItem item) {
        if (isAdmin) return true;
        if (myParticipation == null) return false;
        return item.getParticipationId() == myParticipation.getId();
    }

    private void deleteEvaluation(int evaluationId, JPanel cardPanel, JLabel avgLbl,
                                  List<EvaluationItem> items, Boolean showName) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM evaluation WHERE id=?")) {
            ps.setInt(1, evaluationId);
            ps.executeUpdate();
        } catch (Exception ex) {
            error("삭제 오류: " + ex.getMessage());
            return;
        }
        // 현재 프로젝트 뷰 갱신
        if (projectTable.getSelectedRow() >= 0) {
            Project p = projectList.get(projectTable.getSelectedRow());
            onProjectSelected(p);
        }
    }

    // ── 유틸 ──

    private String toStars(double rate) {
        int filled = (int) Math.round(rate);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < filled ? "★" : "☆");
        return sb.toString();
    }

    private String formatRate(double rate) {
        return rate == 0 ? "-" : String.format("%.1f", rate);
    }

    private static JPanel cardContainer() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(241, 243, 245));
        return p;
    }

    private static JLabel avgLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        return l;
    }

    private static DefaultTableModel noEditModel(String[] cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private static void configTable(JTable t) {
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.setRowHeight(24);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE);
    }
}
