package com.soinsoftware.vissa.util;

public class StringUtility {

	/**
	 * Metodo para concatenar nombre y apellido
	 * 
	 * @param name
	 * @param lastName
	 * @return
	 */
	public static String concatName(String name, String lastName) {
		return name.concat(" ").concat(lastName);
	}

	/**
	 * Metodo para validar si una cadena de texto es nula o vacía
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNull(String str) {
		return (str == null || str.isEmpty()) ? false : true;
	}

}
