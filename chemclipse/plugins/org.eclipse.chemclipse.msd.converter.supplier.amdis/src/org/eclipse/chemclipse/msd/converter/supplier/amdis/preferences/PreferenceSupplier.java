/*******************************************************************************
 * Copyright (c) 2014, 2016 Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.amdis.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.chemclipse.msd.converter.supplier.amdis.Activator;
import org.eclipse.chemclipse.support.preferences.IPreferenceSupplier;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class PreferenceSupplier implements IPreferenceSupplier {

	public static final String P_SPLIT_LIBRARY = "splitLibrary";
	public static final boolean DEF_SPLIT_LIBRARY = false;
	public static final String P_EXCLUDE_UNCERTAIN_IONS = "excludeUncertainIons";
	public static final boolean DEF_EXCLUDE_UNCERTAIN_IONS = false;
	public static final String P_REFERENCE_IDENTIFIER_MARKER = "referenceIdentifierMarker";
	public static final String DEF_REFERENCE_IDENTIFIER_MARKER = "";
	public static final String P_REFERENCE_IDENTIFIER_PREFIX = "referenceIdentifierPrefix";
	public static final String DEF_REFERENCE_IDENTIFIER_PREFIX = "";
	//
	private static IPreferenceSupplier preferenceSupplier;

	public static IPreferenceSupplier INSTANCE() {

		if(preferenceSupplier == null) {
			preferenceSupplier = new PreferenceSupplier();
		}
		return preferenceSupplier;
	}

	@Override
	public IScopeContext getScopeContext() {

		return InstanceScope.INSTANCE;
	}

	@Override
	public String getPreferenceNode() {

		return Activator.getContext().getBundle().getSymbolicName();
	}

	@Override
	public Map<String, String> getDefaultValues() {

		Map<String, String> defaultValues = new HashMap<String, String>();
		defaultValues.put(P_SPLIT_LIBRARY, Boolean.toString(DEF_SPLIT_LIBRARY));
		defaultValues.put(P_EXCLUDE_UNCERTAIN_IONS, Boolean.toString(DEF_EXCLUDE_UNCERTAIN_IONS));
		defaultValues.put(P_REFERENCE_IDENTIFIER_MARKER, DEF_REFERENCE_IDENTIFIER_MARKER);
		defaultValues.put(P_REFERENCE_IDENTIFIER_PREFIX, DEF_REFERENCE_IDENTIFIER_PREFIX);
		return defaultValues;
	}

	@Override
	public IEclipsePreferences getPreferences() {

		return getScopeContext().getNode(getPreferenceNode());
	}

	public static boolean isSplitLibrary() {

		IEclipsePreferences preferences = INSTANCE().getPreferences();
		return preferences.getBoolean(P_SPLIT_LIBRARY, DEF_SPLIT_LIBRARY);
	}

	public static boolean isExcludeUncertainIons() {

		IEclipsePreferences preferences = INSTANCE().getPreferences();
		return preferences.getBoolean(P_EXCLUDE_UNCERTAIN_IONS, DEF_EXCLUDE_UNCERTAIN_IONS);
	}

	public static String getReferenceIdentifierMarker() {

		IEclipsePreferences preferences = INSTANCE().getPreferences();
		return preferences.get(P_REFERENCE_IDENTIFIER_MARKER, DEF_REFERENCE_IDENTIFIER_MARKER);
	}

	public static String getReferenceIdentifierPrefix() {

		IEclipsePreferences preferences = INSTANCE().getPreferences();
		return preferences.get(P_REFERENCE_IDENTIFIER_PREFIX, DEF_REFERENCE_IDENTIFIER_PREFIX);
	}
}
