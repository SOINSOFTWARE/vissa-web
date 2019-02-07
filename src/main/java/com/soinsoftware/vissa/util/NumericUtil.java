package com.soinsoftware.vissa.util;

import java.math.BigDecimal;

import org.jfree.util.Log;

public class NumericUtil {

	public static BigDecimal doubleToBigDecimal(Double doubleVlr) {
		String strLog = "[doubleToBigDecimal]";
		BigDecimal bigDecVlr = new BigDecimal("0");
		try {
			bigDecVlr = BigDecimal.valueOf(doubleVlr);

		} catch (Exception e) {
			Log.error(strLog + "[Exception]" + e.getMessage());
		}
		return bigDecVlr;
	}

	public static Double bigDecimalToDouble(BigDecimal bigDecimalVlr) {
		String strLog = "[bigDecimalToDouble]";
		Double doubleVlr = new Double("0");
		try {
			if (bigDecimalVlr != null) {
				doubleVlr = Double.valueOf(String.valueOf(bigDecimalVlr));
			}

		} catch (Exception e) {
			Log.error(strLog + "[Exception]" + e.getMessage());
		}
		return doubleVlr;
	}

	public static BigDecimal stringToBigDecimal(String strVlr) {
		String strLog = "[stringToBigDecimal]";
		BigDecimal bigDecVlr = new BigDecimal("0");
		try {

			bigDecVlr = doubleToBigDecimal(Double.parseDouble(strVlr));

		} catch (Exception e) {
			Log.error(strLog + "[Exception]" + e.getMessage());
		}
		return bigDecVlr;
	}

}
