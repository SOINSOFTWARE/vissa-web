package com.soinsoftware.vissa.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtil {

	protected static final Logger log = Logger.getLogger(DateUtil.class);

	/**
	 * Metodo para convertir una fecha en LocalDateTime a Date
	 * 
	 * @param localDate
	 * @return
	 */

	public static Date localDateTimeToDate(LocalDateTime ldt) {

		Date parseDate = null;

		try {
			Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			SimpleDateFormat parseador = new SimpleDateFormat(Commons.FORMAT_DATE);
			parseDate = parseador.parse(dateToString(date));
		} catch (Exception e) {
			log.error("Error al convertir LocalDateTime " + ldt + " a Date");
		}
		return parseDate;
	}

	/**
	 * Metodo para convertir una fecha en Date a LocalDateTime
	 * 
	 * @param localDate
	 * @return
	 */

	public static LocalDateTime dateToLocalDateTime(Date date) {

		LocalDateTime ldt = null;
		try {
			ldt = date.toInstant()
				      .atZone(ZoneId.systemDefault())
				      .toLocalDateTime();
		} catch (Exception e) {
			log.error("Error al convertir Date " + date + " a LocalDateTime");
		}
		return ldt;
	}

	/**
	 * Metodo para convertir una fecha Date a String
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date) {
		String dateStr = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(Commons.FORMAT_DATE);
			dateStr = sdf.format(date);
		} catch (Exception e) {
			log.error("Error al convertir Date " + date + " a String");
		}
		return dateStr;

	}

	/**
	 * Metodo para convertir un Strig a Date
	 * 
	 * @param date
	 * @return
	 */
	public static Date stringToDate(String dateStr) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(Commons.FORMAT_DATE);
			date = sdf.parse(dateStr);
		} catch (Exception e) {
			log.error("Error al convertir String " + dateStr + " a Date");
		}
		return date;

	}

	/**
	 * Sumar dias a una fehca
	 * 
	 * @param date
	 * @param dias
	 * @return
	 */
	public static Date addDaysToDate(Date date, int days) {
		if (days == 0)
			return date;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, days);
		return calendar.getTime();
	}

	
	/**
	 * Retorna la fecha inicial para reportes, hasta las 00:00:00
	 * @return
	 */
	public static LocalDateTime getDefaultIniDate() {
		return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);		
	}
	
	/**
	 * Retorna la fecha fin para reportes, hasta las 23:59:59
	 * @return
	 */
	public static LocalDateTime getDefaultEndDate() {
		return LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);		
	}
}
