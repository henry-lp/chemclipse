/*******************************************************************************
 * Copyright (c) 2008, 2017 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.msd.peak.detector.supplier.amdis.core;

import java.io.File;
import java.util.Date;

import org.eclipse.core.runtime.NullProgressMonitor;

import junit.framework.TestCase;

import org.eclipse.chemclipse.msd.converter.chromatogram.ChromatogramConverterMSD;
import org.eclipse.chemclipse.msd.converter.processing.chromatogram.IChromatogramMSDImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.selection.ChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.chromatogram.msd.peak.detector.processing.IPeakDetectorMSDProcessingInfo;
import org.eclipse.chemclipse.chromatogram.msd.peak.detector.supplier.amdis.TestPathHelper;
import org.eclipse.chemclipse.chromatogram.msd.peak.detector.supplier.amdis.core.PeakDetector;
import org.eclipse.chemclipse.chromatogram.msd.peak.detector.supplier.amdis.settings.AmdisSettings;
import org.eclipse.chemclipse.chromatogram.msd.peak.detector.supplier.amdis.settings.IAmdisSettings;

/**
 * @author eselmeister
 */
public class AmdisPeakDetector_1 extends TestCase {

	private File file;
	private IChromatogramMSD chromatogram;
	private IChromatogramSelectionMSD chromatogramSelection;
	private IAmdisSettings peakDetectorSettings;
	private PeakDetector detector;

	@Override
	protected void setUp() throws Exception {

		super.setUp();
		file = new File(TestPathHelper.getAbsolutePath(TestPathHelper.TESTFILE_IMPORT_OP17760));
		IChromatogramMSDImportConverterProcessingInfo processingInfo = ChromatogramConverterMSD.convert(file, new NullProgressMonitor());
		chromatogram = processingInfo.getChromatogram();
		chromatogramSelection = new ChromatogramSelectionMSD(chromatogram);
		peakDetectorSettings = new AmdisSettings();
		detector = new PeakDetector();
	}

	@Override
	protected void tearDown() throws Exception {

		file = null;
		chromatogram = null;
		chromatogramSelection = null;
		peakDetectorSettings = null;
		detector = null;
		super.tearDown();
	}

	public void testIntegrate() {

		Date start = new Date();
		IPeakDetectorMSDProcessingInfo processingInfo = detector.detect(chromatogramSelection, peakDetectorSettings, new NullProgressMonitor());
		assertFalse(processingInfo.hasErrorMessages());
		Date stop = new Date();
		System.out.println("Zeit ms:" + (stop.getTime() - start.getTime()));
	}
}
