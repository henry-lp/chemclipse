/*******************************************************************************
 * Copyright (c) 2008, 2018 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.msd.peak.detector.supplier.amdis.settings;

import junit.framework.TestCase;

public class AmdisPeakDetectorSettings_1_Test extends TestCase {

	private PeakDetectorSettings settings;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		settings = new PeakDetectorSettings();
	}

	@Override
	protected void tearDown() throws Exception {

		settings = null;
		super.tearDown();
	}

	public void test_1() {

		assertNotNull(settings.getOnsiteSettings());
	}
}
