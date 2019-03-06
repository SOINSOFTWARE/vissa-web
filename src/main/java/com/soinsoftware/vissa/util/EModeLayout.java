package com.soinsoftware.vissa.util;

public enum EModeLayout {

	LIST("LIST"), NEW("NEW"), ALL("ALL");
	private String name;

	private EModeLayout(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
