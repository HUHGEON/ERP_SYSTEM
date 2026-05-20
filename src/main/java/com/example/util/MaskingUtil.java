package com.example.util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.YearMonth;

public class MaskingUtil {

    // 920105-5123456 → 920105-5******
    public static String maskResidentNumber(String residentNumber) {
        if (residentNumber == null) return null;
        if (!residentNumber.matches("\\d{6}-\\d{7}")) return residentNumber;
        return residentNumber.substring(0, 8) + "******";
    }

    /**
     * 전화번호 자동 포맷 필터를 설치한다.
     * - 숫자만 허용
     * - 02 시작: 02-XXXX-XXXX (최대 10자리)
     * - 기타:   XXX-XXXX-XXXX (최대 11자리)
     */
    public static void installPhoneFilter(JTextComponent field) {
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

                int dStart = fmtToDigit(current, fmtOffset);
                int dEnd   = fmtToDigit(current, fmtOffset + fmtLen);
                dStart = Math.min(dStart, raw.length());
                dEnd   = Math.min(dEnd,   raw.length());

                String newRaw = raw.substring(0, dStart) + insertDigits + raw.substring(dEnd);
                int maxDigits = newRaw.startsWith("02") ? 10 : 11;
                if (newRaw.length() > maxDigits) newRaw = newRaw.substring(0, maxDigits);

                String formatted = formatPhoneDigits(newRaw);
                fb.replace(0, current.length(), formatted, null);

                final int targetDigit = Math.min(dStart + insertDigits.length(), newRaw.length());
                final String fmt = formatted;
                final int caret = digitPosToCaret(fmt, targetDigit);
                SwingUtilities.invokeLater(() -> {
                    if (comp.isDisplayable()) comp.setCaretPosition(Math.min(caret, fmt.length()));
                });
            }

            private int fmtToDigit(String s, int pos) {
                pos = Math.min(pos, s.length());
                int count = 0;
                for (int i = 0; i < pos; i++) if (s.charAt(i) != '-') count++;
                return count;
            }

            private int digitPosToCaret(String formatted, int digitPos) {
                int count = 0;
                for (int i = 0; i < formatted.length(); i++) {
                    if (count >= digitPos) return i;
                    if (formatted.charAt(i) != '-') count++;
                }
                return formatted.length();
            }
        });
    }

    private static String formatPhoneDigits(String digits) {
        int len = digits.length();
        if (len == 0) return digits;
        if (digits.startsWith("02")) {
            if (len <= 2) return digits;
            if (len <= 5) return digits.substring(0,2) + "-" + digits.substring(2);
            if (len <= 9) return digits.substring(0,2) + "-" + digits.substring(2,5) + "-" + digits.substring(5);
            return digits.substring(0,2) + "-" + digits.substring(2,6) + "-" + digits.substring(6, Math.min(10,len));
        } else {
            if (len <= 3) return digits;
            if (len <= 6) return digits.substring(0,3) + "-" + digits.substring(3);
            if (len <= 10) return digits.substring(0,3) + "-" + digits.substring(3,6) + "-" + digits.substring(6);
            return digits.substring(0,3) + "-" + digits.substring(3,7) + "-" + digits.substring(7, Math.min(11,len));
        }
    }

    /**
     * 날짜 자동 포맷 필터를 설치한다.
     * - 숫자만 허용, 최대 8자리
     * - YYYY-MM-DD 형식 자동 삽입
     * - MM: 01~12, DD: 01~31 범위 초과 시 자동 차단
     */
    public static void installDateFilter(JTextComponent field) {
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

                int dStart = fmtToDigit(current, fmtOffset);
                int dEnd   = fmtToDigit(current, fmtOffset + fmtLen);
                dStart = Math.min(dStart, raw.length());
                dEnd   = Math.min(dEnd,   raw.length());

                String newRaw = raw.substring(0, dStart) + insertDigits + raw.substring(dEnd);
                if (newRaw.length() > 8) newRaw = newRaw.substring(0, 8);
                newRaw = clampDate(newRaw);

                String formatted = formatDate(newRaw);
                fb.replace(0, current.length(), formatted, null);

                final int targetDigit = Math.min(dStart + insertDigits.length(), newRaw.length());
                final String fmt = formatted;
                final int caret = digitPosToCaret(fmt, targetDigit);
                SwingUtilities.invokeLater(() -> {
                    if (comp.isDisplayable()) comp.setCaretPosition(Math.min(caret, fmt.length()));
                });
            }

            /** MM(01~12), DD(01~해당월 최대일) 범위 초과 시 해당 자리 이후를 잘라냄 */
            private String clampDate(String raw) {
                // Month first digit: 0 or 1 only
                if (raw.length() >= 5) {
                    char m1 = raw.charAt(4);
                    if (m1 != '0' && m1 != '1') return raw.substring(0, 4);
                }
                // Month second digit
                if (raw.length() >= 6) {
                    char m1 = raw.charAt(4), m2 = raw.charAt(5);
                    if ((m1 == '0' && m2 == '0') || (m1 == '1' && m2 > '2'))
                        return raw.substring(0, 5);
                }
                // Day first digit: d1 * 10 이 해당 월 최대일 초과 시 차단
                if (raw.length() >= 7) {
                    int max = maxDayOf(raw);
                    if ((raw.charAt(6) - '0') * 10 > max) return raw.substring(0, 6);
                }
                // Day second digit: 완성된 DD가 01..maxDay 범위 벗어나면 차단
                if (raw.length() >= 8) {
                    int max = maxDayOf(raw);
                    int day = (raw.charAt(6) - '0') * 10 + (raw.charAt(7) - '0');
                    if (day == 0 || day > max) return raw.substring(0, 7);
                }
                return raw;
            }

            /** 연도·월로 해당 월 최대 일수 반환 (윤년 자동 반영) */
            private int maxDayOf(String raw) {
                try {
                    int year  = Integer.parseInt(raw.substring(0, 4));
                    int month = Integer.parseInt(raw.substring(4, 6));
                    if (month < 1 || month > 12) return 31;
                    return YearMonth.of(year, month).lengthOfMonth();
                } catch (Exception e) {
                    return 31;
                }
            }

            private String formatDate(String digits) {
                int len = digits.length();
                if (len <= 4) return digits;
                if (len <= 6) return digits.substring(0, 4) + "-" + digits.substring(4);
                return digits.substring(0, 4) + "-" + digits.substring(4, 6) + "-" + digits.substring(6);
            }

            private int fmtToDigit(String s, int pos) {
                pos = Math.min(pos, s.length());
                int count = 0;
                for (int i = 0; i < pos; i++) if (s.charAt(i) != '-') count++;
                return count;
            }

            private int digitPosToCaret(String formatted, int digitPos) {
                int count = 0;
                for (int i = 0; i < formatted.length(); i++) {
                    if (count >= digitPos) return i;
                    if (formatted.charAt(i) != '-') count++;
                }
                return formatted.length();
            }
        });
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
