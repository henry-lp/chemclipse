/*******************************************************************************
 * Copyright (c) 2017 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ui.service.swt.charts;

public class PrimaryAxisSettings extends AbstractAxisSettings implements IPrimaryAxisSettings {

	private boolean enableLogScale;
	private boolean enableCategory;
	private String[] categorySeries;

	public PrimaryAxisSettings(String title) {
		super(title);
		enableLogScale = false;
		enableCategory = false;
		categorySeries = new String[]{};
	}

	@Override
	public boolean isEnableLogScale() {

		return enableLogScale;
	}

	@Override
	public void setEnableLogScale(boolean enableLogScale) {

		this.enableLogScale = enableLogScale;
	}

	@Override
	public boolean isEnableCategory() {

		return enableCategory;
	}

	@Override
	public void setEnableCategory(boolean enableCategory) {

		this.enableCategory = enableCategory;
	}

	@Override
	public String[] getCategorySeries() {

		return categorySeries;
	}

	@Override
	public void setCategorySeries(String[] categorySeries) {

		this.categorySeries = categorySeries;
	}
}
