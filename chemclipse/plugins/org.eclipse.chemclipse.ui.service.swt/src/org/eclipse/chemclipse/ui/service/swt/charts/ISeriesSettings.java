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

public interface ISeriesSettings {

	String getDescription();

	void setDescription(String description);

	boolean isVisible();

	void setVisible(boolean visible);

	boolean isVisibleInLegend();

	void setVisibleInLegend(boolean visibleInLegend);
}
