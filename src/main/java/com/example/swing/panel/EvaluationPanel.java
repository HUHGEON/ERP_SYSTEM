package com.example.swing.panel;

import com.example.dao.EvaluationDAO;
import com.example.dao.EvaluationItemDAO;
import com.example.model.Evaluation;
import com.example.model.EvaluationItem;
import com.example.swing.dialog.EvaluationDialog;
import com.example.swing.dialog.EvaluationItemDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EvaluationPanel extends JPanel {

    private static final String[] CATEGORIES = {"", "업무 수행", "커뮤니케이션"};
    private static final String[] EVAL_COLS  = {"ID", "직원명", "프로젝트명", "역할", "평가유형"};
    private static final String[] ITEM_COLS  = {"항목ID", "평점", "평가내용"};

    private final EvaluationDAO     evalDAO = new EvaluationDAO();
    private final EvaluationItemDAO itemDAO = new EvaluationItemDAO();

    private final DefaultTableModel evalModel = new DefaultTableModel(EVAL_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel itemModel = new DefaultTableModel(ITEM_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable evalTable = new JTable(evalModel);
    private final JTable itemTable = new JTable(itemModel);

    private final JTextField employeeField = new JTextField(10);
    private final JTextField projectField  = new JTextField(10);
    private final JComboBox<String> categoryBox = new JComboBox<>(CATEGORIES);
    private final JLabel itemLabel = new JLabel("평가 항목 — 평가를 선택하세요");

    private List<Evaluation>     evalList;
    private List<EvaluationItem> itemList;
    private int selectedEvalId = -1;

    public EvaluationPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── 상단: 평가 목록 ──
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("직원명:"));
        searchPanel.add(employeeField);
        searchPanel.add(new JLabel("프로젝트:"));
        searchPanel.add(projectField);
        searchPanel.add(new JLabel("평가유형:"));
        searchPanel.add(categoryBox);
        JButton searchBtn = new JButton("검색");
        JButton resetBtn  = new JButton("초기화");
        searchPanel.add(searchBtn);
        searchPanel.add(resetBtn);

        evalTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        evalTable.getTableHeader().setReorderingAllowed(false);
        evalTable.setRowHeight(24);

        JPanel evalBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addEvalBtn    = new JButton("추가");
        JButton editEvalBtn   = new JButton("수정");
        JButton deleteEvalBtn = new JButton("삭제");
        evalBtnPanel.add(addEvalBtn);
        evalBtnPanel.add(editEvalBtn);
        evalBtnPanel.add(deleteEvalBtn);

        JPanel topPanel = new JPanel(new BorderLayout(3, 3));
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(new JScrollPane(evalTable), BorderLayout.CENTER);
        topPanel.add(evalBtnPanel, BorderLayout.SOUTH);

        // ── 하단: 평가 항목 ──
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.getTableHeader().setReorderingAllowed(false);
        itemTable.setRowHeight(24);

        JPanel itemBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addItemBtn    = new JButton("항목 추가");
        JButton editItemBtn   = new JButton("항목 수정");
        JButton deleteItemBtn = new JButton("항목 삭제");
        itemBtnPanel.add(addItemBtn);
        itemBtnPanel.add(editItemBtn);
        itemBtnPanel.add(deleteItemBtn);

        itemLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 0));
        itemLabel.setFont(itemLabel.getFont().deriveFont(Font.BOLD));

        JPanel bottomPanel = new JPanel(new BorderLayout(3, 3));
        bottomPanel.add(itemLabel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        bottomPanel.add(itemBtnPanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        split.setResizeWeight(0.55);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        // ── 이벤트 ──
        searchBtn.addActionListener(e -> loadEvaluations());
        resetBtn.addActionListener(e -> {
            employeeField.setText(""); projectField.setText(""); categoryBox.setSelectedIndex(0);
            loadEvaluations();
        });

        addEvalBtn.addActionListener(e -> openEvalDialog(null));
        editEvalBtn.addActionListener(e -> {
            int row = evalTable.getSelectedRow();
            if (row < 0) { info("수정할 평가를 선택하세요."); return; }
            openEvalDialog(evalList.get(row));
        });
        deleteEvalBtn.addActionListener(e -> deleteEval());

        evalTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = evalTable.getSelectedRow();
                if (row >= 0) {
                    Evaluation ev = evalList.get(row);
                    selectedEvalId = ev.getId();
                    String label = ev.getEmployeeName() != null
                        ? ev.getEmployeeName() + " / " + ev.getProjectName()
                        : "평가 #" + ev.getId();
                    itemLabel.setText("평가 항목 — " + label);
                    loadItems();
                }
            }
        });

        addItemBtn.addActionListener(e -> {
            if (selectedEvalId < 0) { info("평가를 먼저 선택하세요."); return; }
            openItemDialog(null);
        });
        editItemBtn.addActionListener(e -> {
            int row = itemTable.getSelectedRow();
            if (row < 0) { info("수정할 항목을 선택하세요."); return; }
            openItemDialog(itemList.get(row));
        });
        deleteItemBtn.addActionListener(e -> deleteItem());

        loadEvaluations();
    }

    private void loadEvaluations() {
        try {
            evalList = evalDAO.searchWithDetails(
                employeeField.getText().trim(),
                projectField.getText().trim(),
                (String) categoryBox.getSelectedItem()
            );
            evalModel.setRowCount(0);
            for (Evaluation ev : evalList) {
                evalModel.addRow(new Object[]{
                    ev.getId(),
                    ev.getEmployeeName() != null ? ev.getEmployeeName() : "-",
                    ev.getProjectName()  != null ? ev.getProjectName()  : "-",
                    ev.getProjectRole()  != null ? ev.getProjectRole()  : "-",
                    ev.getParticipationCategory()
                });
            }
            selectedEvalId = -1;
            itemModel.setRowCount(0);
            itemLabel.setText("평가 항목 — 평가를 선택하세요");
        } catch (Exception ex) { error("데이터 로드 오류: " + ex.getMessage()); }
    }

    private void loadItems() {
        if (selectedEvalId < 0) return;
        try {
            itemList = itemDAO.searchByEvaluationId(selectedEvalId);
            itemModel.setRowCount(0);
            for (EvaluationItem item : itemList)
                itemModel.addRow(new Object[]{item.getId(), item.getRate(), item.getContent()});
        } catch (Exception ex) { error("평가 항목 로드 오류: " + ex.getMessage()); }
    }

    private void openEvalDialog(Evaluation ev) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EvaluationDialog dialog = new EvaluationDialog(frame, ev, evalDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadEvaluations();
    }

    private void openItemDialog(EvaluationItem item) {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EvaluationItemDialog dialog = new EvaluationItemDialog(frame, item, itemDAO, selectedEvalId);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadItems();
    }

    private void deleteEval() {
        int row = evalTable.getSelectedRow();
        if (row < 0) { info("삭제할 평가를 선택하세요."); return; }
        Evaluation ev = evalList.get(row);
        if (JOptionPane.showConfirmDialog(this, "해당 평가를 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { evalDAO.delete(ev.getId()); loadEvaluations(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    private void deleteItem() {
        int row = itemTable.getSelectedRow();
        if (row < 0) { info("삭제할 항목을 선택하세요."); return; }
        EvaluationItem item = itemList.get(row);
        if (JOptionPane.showConfirmDialog(this, "해당 평가 항목을 삭제하시겠습니까?",
                "삭제 확인", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { itemDAO.delete(item.getId()); loadItems(); }
            catch (Exception ex) { error("삭제 오류: " + ex.getMessage()); }
        }
    }

    private void info(String msg) { JOptionPane.showMessageDialog(this, msg); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "오류", JOptionPane.ERROR_MESSAGE); }
}
