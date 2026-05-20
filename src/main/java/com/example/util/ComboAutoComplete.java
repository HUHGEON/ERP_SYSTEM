package com.example.util;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.*;
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
            @Override public void focusGained(FocusEvent e) { SwingUtilities.invokeLater(editor::selectAll); }
            @Override public void focusLost(FocusEvent e) { endIme(); }
        });

        Timer filterTimer = new Timer(200, ev -> {
            filtering[0] = true;
            SwingUtilities.invokeLater(() -> {
                String typed = editor.getText();
                String lower = typed.toLowerCase();
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
                if (!lower.isEmpty() && model.getSize() > 0) box.showPopup(); else box.hidePopup();
                filtering[0] = false;
            });
        });
        filterTimer.setRepeats(false);

        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) endIme();
            }
            @Override public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE ||
                    code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN ||
                    e.isControlDown() || e.isAltDown()) return;
                filterTimer.restart();
            }
        });

        box.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (filtering[0]) return;
                endIme();
                Object sel = box.getSelectedItem();
                model.removeAllElements();
                allItems.forEach(model::addElement);
                T found = null;
                if (sel != null) {
                    for (T item : allItems) { if (item.equals(sel)) { found = item; break; } }
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

    public static void endIme() {
        java.awt.Component focused = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused != null && focused.getInputContext() != null) focused.getInputContext().endComposition();
    }
}
