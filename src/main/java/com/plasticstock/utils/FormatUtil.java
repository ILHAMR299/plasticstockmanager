package com.plasticstock.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatUtil {
    private static final DecimalFormat RUPIAH_FORMAT;

    static{
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        RUPIAH_FORMAT = new DecimalFormat("'Rp' #,##0", symbols);
        RUPIAH_FORMAT.setMaximumFractionDigits(0);
        RUPIAH_FORMAT.setMinimumFractionDigits(0);
    }

    public static String rupiah(double value){
        return RUPIAH_FORMAT.format(value);
    }

}
