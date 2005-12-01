/*******************************************************************************
 * Copyright (c) 2001, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jens Lukowski/Innoopract - initial renaming/restructuring
 *     
 *******************************************************************************/
package org.eclipse.wst.sse.ui.views.properties;

/**
 * Declares that this IPropertySource might support outright removal of a
 * property
 * 
 * @plannedfor 1.0
 */
public interface IPropertySourceExtension {

	/**
	 * @param name
	 *            the name of the property
	 * @return whether the property matching this ID can be removed
	 */
	boolean isPropertyRemovable(Object name);

	/**
	 * Removes the property with the given displayed name. If no such property
	 * exists, nothing is done.
	 * 
	 * @param name
	 *            the displayed name of the property
	 */
	void removeProperty(Object name);
}
