// Soin Software 2019
package com.soinsoftware.vissa.util;

import java.util.NoSuchElementException;
import java.util.Set;

import com.soinsoftware.vissa.model.Permission;

/**
 * @author Carlos Rodriguez
 */
public class PermissionUtil {

	private final Set<Permission> permissions;

	public PermissionUtil(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public boolean canView(String menuName) {
		try {
			return getPermission(menuName).canView();
		} catch (NoSuchElementException ex) {
			return false;
		}
	}
	
	public boolean canEdit(String menuName) {
		try {
			return getPermission(menuName).canEdit();
		} catch (NoSuchElementException ex) {
			return false;
		}
	}
	
	public boolean canDelete(String menuName) {
		try {
			return getPermission(menuName).canDelete();
		} catch (NoSuchElementException ex) {
			return false;
		}
	}

	private Permission getPermission(String menuName) {
		return permissions.stream().filter(permission -> permission.getMenu().getName().equals(menuName)).findFirst()
				.get();
	}
}