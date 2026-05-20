package com.example.swing.panel;

import com.example.dao.CustomerEvaluationDAO;
import com.example.dao.EvaluationItemDAO;
import com.example.model.CustomerEvaluation;
import com.example.model.EvaluationItem;
import com.example.swing.dialog.CustomerEvaluationDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerEvaluationPanel extends JPanel {

    private static final String[] COLUMNS      = {"발주처명", "평가유형"};
    private static final String[] ITEM_COLS    = {"평점", "평가내용"};

    private final CustomerEvaluationDAO dao     = new CustomerEvaluationDAO();
    private final EvaluationItemDAO     itemDAO = new EvaluationItemDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel itemModel = new DefaultTableModel(ITEM_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable table     = new JTable(tableModel);
    private final JTable itemTable = new JTable(itemModel);
    private final JLabel itemLabel = new JLabel("평가 항목 — 목록에서 항목을 선택하세요");
    private final JTextField nameField = new JTextField(15);

    private List<CustomerEvaluation> currentList;

    public CustomerEvaluationPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── 검색 ──
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("발주처명:")); searchPanel.add(nameField);
        JButton searchBtn = new JButton("검색"); JButton resetBtn = new JButton("초기화");
        searchPanel.add(searchBtn); searchPanel.add(resetBtn);

        // ── 상단: 목록 ──
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);  // 발주처명
        table.getColumnModel().getColumn(1).setPreferredWidth(80);   // 평가유형

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("추가"); JButton editBtn = new JButton("수정"); JButton deleteBtn = new JButton("삭제");
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn);

        JPanel topPanel = new JPanel(new BorderLayout(3, 3));
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        // ── 하단: 평가 항목 (평점 + 내용) ──
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.getTableHeader().setReorderingAllowed(false);
        itemTable.setRowHeight(24);
        itemTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // 평점
        itemTable.getColumnModel().getColumn(1).setPreferredWidth(250);  // 평가내용

        itemLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 0));
        itemLabel.setFont(itemLabel.getFont().deriveFont(Font.BOLD));

        JPanel bottomPanel = new JPanel(new BorderLayout(3, 3));
        bottomPanel.add(itemLabel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        split.setResizeWeight(0.55);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        // ── 이벤트 ──
        searchBtn.addActionListener(e -> loadData());
        resetBtn.addActionListener(e -> { nameField.setText(""); loadData(); });
        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "수정할 항목을 선택하세요."); return; }
            openDialog(currentList.get(table.convertRowIndexToModel(row)));
        });
        deleteBtn.addActionListener(e -> deleteSelected());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) loadItems(currentList.get(table.convertRowIndexToModel(row)));
            }
        });

        loadData();
    }

    private void loadData() {
        try {
            currentList = dao.search(nameField.getText().trim());
            tableModel.setRowCount(0);
            for (CustomerEvaluation ce : currentList)
                tableModel.addRow(new Object[]{ce.getCustomerName(), ce.getParticipationCategory()});
            itemModel.setRowCount(0);
            itemLabel.setText("평가 항목 — 목록에서 항목을 선택하세요");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "오류: " + ex.getMessage(), "DB 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadItems(CustomerEvaluation ce) {
        try {
            List<EvaluationItem> items = itemDAO.searchByEvaluationId(ce.getId());
            itemModel.setRowCount(0);
            for (EvaluationItem item : items)
                itemModel.addRow(new Object[]{item.getRate(), item.getContent()});
            itemLabel.setText("평가 항목 — " + ce.getCustomerName() + " / " + ce.getParticipationCategory());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "항목 로드 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDialog(CustomerEvaluation ce) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        CustomerEvaluationDialog dialog = new CustomerEvaluationDialog(frame, ce, dao);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "삭제할 항목을 선택하세요."); return; }
        CustomerEvaluation ce = currentList.get(table.convertRowIndexToModel(row));
        if (JOptionPane.showConfirmDialog(this, "해당 고객 평가를 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { dao.delete(ce.getId()); loadData(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "삭제 오류: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE); }
        }
    }
}
