package com.soinsoftware.vissa.model;

import java.io.IOException;

import com.soinsoftware.vissa.bll.PersonBll;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;

public class PurchaseBean {

	private final PersonBll personBll;

	public PurchaseBean() throws IOException {
		personBll = PersonBll.getInstance();
	}

	/**
	 * Metodo encargado de listar personas
	 */

	public ConfigurableFilterDataProvider<Person, Void, SerializablePredicate<Person>> listPersons() {
		ListDataProvider<Person> dataProvider = new ListDataProvider<>(personBll.selectAll());
		ConfigurableFilterDataProvider<Person, Void, SerializablePredicate<Person>> filterPersonDataProv = dataProvider
				.withConfigurableFilter();
		return filterPersonDataProv;
	}

}
