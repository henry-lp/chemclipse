/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.ui.preferences;

import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.ui.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("PCA Preferences");
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected void createFieldEditors() {

		addField(new ComboFieldEditor(PreferenceConstants.P_ALGORITHM_TYPE, "Algorithm type:", PreferenceConstants.ALGORITHM_TYPES, getFieldEditorParent()));
		addIntegerEditor(PreferenceConstants.P_NUMBER_OF_COMPONENTS, "Number of Components", PreferenceConstants.MIN_NUMBER_OF_COMPONENTS, PreferenceConstants.MAX_NUMBER_OF_COMPONENTS);
		addField(new BooleanFieldEditor(PreferenceConstants.P_AUTO_REEVALUATE, "Reevaluete after change", getFieldEditorParent()));
	}

	private void addIntegerEditor(String name, String labelText, int min, int max) {

		IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor(name, labelText, getFieldEditorParent());
		integerFieldEditor.setValidRange(min, max);
		addField(integerFieldEditor);
	}
}