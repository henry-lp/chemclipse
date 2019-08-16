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
package org.eclipse.chemclipse.msd.identifier.supplier.nist.internal.results;

import java.io.File;

import org.eclipse.chemclipse.msd.identifier.supplier.nist.TestPathHelper;
import org.eclipse.chemclipse.msd.identifier.supplier.nist.internal.results.ICompound;
import org.eclipse.chemclipse.msd.identifier.supplier.nist.internal.results.ICompounds;
import org.eclipse.chemclipse.msd.identifier.supplier.nist.internal.results.IHit;
import org.eclipse.chemclipse.msd.identifier.supplier.nist.internal.results.NistResultFileParser;

import junit.framework.TestCase;

public class NistResultFileParser_5_Test extends TestCase {

	private NistResultFileParser nistResultFileParser;
	private File results;
	private ICompounds compounds;
	private ICompound compound;
	private IHit hit;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		nistResultFileParser = new NistResultFileParser();
		results = new File(TestPathHelper.getAbsolutePath(TestPathHelper.TESTFILE_NIST_SRCRESLT_3));
		compounds = nistResultFileParser.getCompounds(results);
		compound = compounds.getCompound(2);
		hit = compound.getHit(2);
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

	public void testGetIdentifier_1() {

		assertEquals("PEAK-V-NAME", compound.getIdentfier());
	}

	public void testGetCompoundInLibraryFactor_1() {

		assertEquals("-830", compound.getCompoundInLibraryFactor());
	}

	public void testGetName_1() {

		assertEquals("Cyclohexanol", hit.getName());
	}

	public void testGetFormula_1() {

		assertEquals("C6H12O", hit.getFormula());
	}

	public void testGetMF_1() {

		assertEquals(57.4f, hit.getMF());
	}

	public void testGetRMF_1() {

		assertEquals(65.3f, hit.getRMF());
	}

	public void testGetProb_1() {

		assertEquals(9.71f, hit.getProb());
	}

	public void testGetCAS_1() {

		assertEquals("108-93-0", hit.getCAS());
	}

	public void testGetMw_1() {

		assertEquals(100, hit.getMw());
	}

	public void testGetLib_1() {

		assertEquals("mainlib", hit.getLib());
	}

	public void testGetId_1() {

		assertEquals(1222, hit.getId());
	}
}