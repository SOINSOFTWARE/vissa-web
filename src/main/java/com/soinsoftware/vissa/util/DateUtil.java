package com.soinsoftware.vissa.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtil {

	protected static final Logger log = Logger.getLogger(DateUtil.class);

	/**
	 * Metodo para convertir una fecha en LocalDate a Date
	 * 
	 * @param localDate
	 * @return
	 */

	public static Date localDateToDate(LocalDate localDate) {

		Date date = null;
		try {
			date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		} catch (Exception e) {
			log.error("Error al convertir LocalDate " + localDate + " a Date");
		}
		return date;
	}

	/**
	 * Metodo para convertir una fecha en Date a LocalDate
	 * 
	 * @param localDate
	 * @return
	 */

	public static LocalDate dateToLocalDate(Date date) {
		LocalDate localDate = null;
		try {
			localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		} catch (Exception e) {
			log.error("Error al convertir Date " + date + " a LocalDate");
		}
		return localDate;
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
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			dateStr = sdf.format(date);
		} catch (Exception e) {
			log.error("Error al convertir Date " + date + " a String");
		}
		return dateStr;

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

}
