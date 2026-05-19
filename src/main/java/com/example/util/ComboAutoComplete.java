package com.example.util;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ComboAutoComplete {

    public static <T> void apply(JComboBox<T> box) {
        box.setEditable(true);

        List<T> allItems = new ArrayList<>();
        for (int i = 0; i < box.getItemCount(); i++) allItems.add(box.getItemAt(i));

        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        allItems.forEach(model::addElement);
        box.setModel(model);

        JTextField editor = (JTextField) box.getEditor().getEditorComponent();
        boolean[] filtering = {false};

        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(editor::selectAll);
            }

            @Override
            public void focusLost(FocusEvent e) {
                endIme();
            }
        });

        // 200ms 타이머: 한국어 IME 조합(ㅎ→허→허건)이 끝난 뒤 필터 실행
        Timer filterTimer = new Timer(200, ev -> {
            filtering[0] = true;
            SwingUtilities.invokeLater(() -> {
                String typed = editor.getText();
                String lower  = typed.toLowerCase();

                model.removeAllElements();
                if (lower.isEmpty()) {
                    allItems.forEach(model::addElement);
                } else {
                    for (T item : allItems) {
                        if (item.toString().toLowerCase().contains(lower)) model.addElement(item);
                    }
                }
                editor.setText(typed);
                editor.setCaretPosition(typed.length());

                if (!lower.isEmpty() && model.getSize() > 0) box.showPopup();
                else box.hidePopup();

                filtering[0] = false;
            });
        });
        filterTimer.setRepeats(false);

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Tab 키로 포커스 이동 직전에 IME 조합 확정 — focusLost보다 먼저 실행됨
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    endIme();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE ||
                    code == KeyEvent.VK_UP   || code == KeyEvent.VK_DOWN   ||
                    e.isControlDown() || e.isAltDown()) return;
                filterTimer.restart();
            }
        });

        box.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (filtering[0]) return;

                // 팝업이 닫히는 시점에 IME 조합 확정 — 이후 다른 필드로 이동해도 누출 없음
                endIme();

                Object sel = box.getSelectedItem();
                model.removeAllElements();
                allItems.forEach(model::addElement);

                T found = null;
                if (sel != null) {
                    for (T item : allItems) {
                        if (item.equals(sel)) { found = item; break; }
                    }
                }
                if (found != null) box.setSelectedItem(found);

                final String display = found != null ? found.toString() : "";
                SwingUtilities.invokeLater(() -> {
                    editor.setText(display);
                    editor.setCaretPosition(display.length());
                });
            }
        });
    }

    private static void endIme() {
        // 현재 포커스를 가진 컴포넌트가 아닌 에디터 기준이 아니라,
        // 시스템 공유 InputContext에서 조합 중인 문자를 여기서 확정시킴
        java.awt.Component focused =
            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused != null && focused.getInputContext() != null) {
            focused.getInputContext().endComposition();
        }
    }
}
