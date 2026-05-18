package com.example.util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class MaskingUtil {

    // 920105-5123456 → 920105-5******
    public static String maskResidentNumber(String residentNumber) {
        if (residentNumber == null) return null;
        if (!residentNumber.matches("\\d{6}-\\d{7}")) return residentNumber;
        return residentNumber.substring(0, 8) + "******";
    }

    /**
     * 주민번호 자동 포맷 필터를 설치한다.
     * - 숫자만 입력 허용
     * - 6자리 입력 후 자동으로 '-' 삽입
     * - 최대 13자리(하이픈 포함 14자)
     * JTextField, JPasswordField 모두 사용 가능.
     */
    public static void installResidentFilter(JTextComponent field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                    throws BadLocationException {
                if (text == null) return;
                applyChange(fb, field, offset, 0, text.replaceAll("[^0-9]", ""));
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr)
                    throws BadLocationException {
                String digits = text != null ? text.replaceAll("[^0-9]", "") : "";
                applyChange(fb, field, offset, length, digits);
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length)
                    throws BadLocationException {
                applyChange(fb, field, offset, length, "");
            }

            private void applyChange(FilterBypass fb, JTextComponent comp,
                                     int fmtOffset, int fmtLen, String insertDigits)
                    throws BadLocationException {
                String current = fb.getDocument().getText(0, fb.getDocument().getLength());
                String raw = current.replace("-", "");

                // 포맷된 위치 → 숫자 위치
                int dStart = fmtToDigit(current, fmtOffset);
                int dEnd   = fmtToDigit(current, fmtOffset + fmtLen);
                dStart = Math.min(dStart, raw.length());
                dEnd   = Math.min(dEnd,   raw.length());

                String newRaw = raw.substring(0, dStart) + insertDigits + raw.substring(dEnd);
                if (newRaw.length() > 13) newRaw = newRaw.substring(0, 13);

                String formatted = newRaw.length() > 6
                        ? newRaw.substring(0, 6) + "-" + newRaw.substring(6)
                        : newRaw;

                fb.replace(0, current.length(), formatted, null);

                // 커서 위치 복원
                int newDigit = Math.min(dStart + insertDigits.length(), newRaw.length());
                int newCaret = newDigit > 6 ? newDigit + 1 : newDigit;
                final int caretPos = Math.min(newCaret, formatted.length());
                SwingUtilities.invokeLater(() -> {
                    if (comp.isDisplayable()) comp.setCaretPosition(caretPos);
                });
            }

            /** 포맷 문자열에서 pos 이전의 숫자 개수(하이픈 제외) */
            private int fmtToDigit(String formatted, int pos) {
                pos = Math.min(pos, formatted.length());
                int count = 0;
                for (int i = 0; i < pos; i++) {
                    if (formatted.charAt(i) != '-') count++;
                }
                return count;
            }
        });
    }
}
