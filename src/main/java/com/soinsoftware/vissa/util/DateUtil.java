package com.soinsoftware.vissa.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
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
			if (ldt != null) {
				Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
				SimpleDateFormat parseador = new SimpleDateFormat(Commons.FORMAT_DATE_TIME);
				parseDate = parseador.parse(dateToString(date));
			}
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
			if (date != null) {
				ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			}
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
			if (date != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(Commons.FORMAT_DATE_TIME);
				dateStr = sdf.format(date);
			}
		} catch (Exception e) {
			log.error("Error al convertir Date " + date + " a String");
		}
		return dateStr;

	}

	/**
	 * Metodo para convertir una fecha LocalDateTime a String
	 * 
	 * @param date
	 * @return
	 */
	public static String localDateTimeToString(LocalDateTime date) {
		String dateStr = null;
		try {
			if (date != null) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Commons.FORMAT_DATE_TIME);
				dateStr = date.format(formatter);
			}
		} catch (Exception e) {
			log.error("Error al convertir Date " + date + " a String");
		}
		return dateStr;

	}

	/**
	 * Metodo para convertir una fecha Date a String indicando el formato
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		String dateStr = null;
		try {
			if (date != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				dateStr = sdf.format(date);
			}
		} catch (Exception e) {
			log.error("Error al convertir Date " + date + " a String en formati: " + format);
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
			if (dateStr != null) {
				SimpleDateFormat sdf = new SimpleDateFormat(Commons.FORMAT_DATE_TIME);
				date = sdf.parse(dateStr);
			}
		} catch (Exception e) {
			log.error("Error al convertir String " + dateStr + " a Date");
		}
		return date;

	}

	/**
	 * Metodo para convertir un Strig a LocalDateTime
	 * 
	 * @param date
	 * @return
	 */
	public static LocalDateTime stringToLocalDateTime(String dateStr) {
		LocalDateTime date = null;
		try {
			if (dateStr != null) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Commons.FORMAT_DATE_TIME);
				date = LocalDateTime.parse(dateStr, formatter);
			}
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
		if (date != null) {
			if (days == 0) {
				return date;
			}
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_YEAR, days);
			return calendar.getTime();
		}
		return null;
	}

	/**
	 * Retorna la fecha inicial para reportes, hasta las 00:00:00
	 * 
	 * @return
	 */
	public static LocalDateTime getDefaultIniDate() {
		return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
	}

	/**
	 * Retorna la fecha fin DateTime para reportes, hasta las 23:59:59
	 * 
	 * @return
	 */
	public static LocalDateTime getDefaultEndDateTime() {
		return LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
	}

	/**
	 * Retorna la fecha fin LocalDate para reportes, hasta las 23:59:59
	 * 
	 * @return
	 */
	public static LocalDate getDefaultEndLocalDate() {
		return LocalDate.now();
	}

	/**
	 * Retorna la fecha fin Date para reportes, hasta las 23:59:59
	 * 
	 * @return
	 */
	public static Date getDefaultEndDate() {
		return localDateTimeToDate(getDefaultEndDateTime());
	}

	/**
	 * Retorna la fecha inicial DateTime para reportes, hasta las 00:00:00
	 * 
	 * @return
	 */
	public static LocalDateTime getDefaultIniMonthDateTime() {
		return LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0);
	}

	/**
	 * Retorna la fecha Date inicial para reportes
	 * 
	 * @return
	 */
	public static LocalDate getDefaultIniMonthDate() {
		return LocalDate.now().minusMonths(1);
	}

	/**
	 * Metodo para convertir una fecha en LocalDate a Date
	 * 
	 * @param localDate
	 * @return
	 */

	public static Date localDateToDate(LocalDate ld) {

		Date parseDate = null;

		try {
			if (ld != null) {
				Date date = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
				SimpleDateFormat parseador = new SimpleDateFormat(Commons.FORMAT_DATE);
				parseDate = parseador.parse(dateToString(date));
			}
		} catch (Exception e) {
			log.error("Error al convertir LocalDateTime " + ld + " a Date");
		}
		return parseDate;
	}

	/**
	 * Metodo para convertir una fecha en Date a LocalDate
	 * 
	 * @param localDate
	 * @return
	 */

	public static LocalDate dateToLocalDate(Date date) {

		LocalDate ld = null;
		try {
			if (date != null) {
				ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
		} catch (Exception e) {
			log.error("Error al convertir Date " + date + " a LocalDate");
		}
		return ld;
	}

	public static Date iniDate(Date date) {
		Date returnDate = DateUtils.truncate(date, Calendar.DATE);
		return returnDate;
	}

	public static Date endDate(Date date) {
		Date returnDate = DateUtils.addHours(date, 23);
		returnDate = DateUtils.addMinutes(returnDate, 59);
		returnDate = DateUtils.addSeconds(returnDate, 59);
		return returnDate;
	}
}
