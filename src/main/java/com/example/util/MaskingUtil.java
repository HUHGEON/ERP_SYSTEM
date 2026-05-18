package com.example.util;

public class MaskingUtil {

    // 920105-5123456 → 920105-5******
    public static String maskResidentNumber(String residentNumber) {
        if (residentNumber == null) return null;
        if (!residentNumber.matches("\\d{6}-\\d{7}")) return residentNumber;
        return residentNumber.substring(0, 8) + "******";
    }
}
