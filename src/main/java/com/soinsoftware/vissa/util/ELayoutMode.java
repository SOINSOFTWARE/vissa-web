package com.soinsoftware.vissa.util;

public enum ELayoutMode {

	LIST("LIST"), NEW("NEW"), REPORT("REPORT"), ALL("ALL");
	private String name;

	private ELayoutMode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
