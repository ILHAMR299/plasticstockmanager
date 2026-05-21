package com.plasticstock.utils;

public class Validator {

    /** Cek apakah string kosong atau hanya spasi */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Cek apakah string merupakan angka bulat valid */
    public static boolean isValidInteger(String value) {
        if (isEmpty(value)) return false;
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Cek apakah string merupakan angka desimal valid */
    public static boolean isValidDouble(String value) {
        if (isEmpty(value)) return false;
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Cek panjang minimum */
    public static boolean hasMinLength(String value, int min) {
        return value != null && value.trim().length() >= min;
    }

    /** Cek format kode barang: huruf besar + angka, min 4 karakter */
    public static boolean isValidKodeBarang(String kode) {
        return kode != null && kode.matches("[A-Z0-9]{4,}");
    }
}
