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
package org.eclipse.chemclipse.msd.converter.supplier.mzxml.converter;

import java.io.File;

import org.eclipse.chemclipse.converter.chromatogram.IChromatogramImportConverter;
import org.eclipse.chemclipse.converter.processing.chromatogram.ChromatogramOverviewImportConverterProcessingInfo;
import org.eclipse.chemclipse.converter.processing.chromatogram.IChromatogramOverviewImportConverterProcessingInfo;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.core.IChromatogramOverview;
import org.eclipse.chemclipse.msd.converter.chromatogram.AbstractChromatogramMSDImportConverter;
import org.eclipse.chemclipse.msd.converter.io.IChromatogramMSDReader;
import org.eclipse.chemclipse.msd.converter.supplier.mzxml.internal.io.IConstants;
import org.eclipse.chemclipse.msd.converter.supplier.mzxml.internal.io.SpecificationValidator;
import org.eclipse.chemclipse.msd.converter.supplier.mzxml.io.ChromatogramReader;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.core.runtime.IProgressMonitor;

public class ChromatogramImportConverter extends AbstractChromatogramMSDImportConverter implements IChromatogramImportConverter {

	private static final Logger logger = Logger.getLogger(ChromatogramImportConverter.class);
	private static final String DESCRIPTION = "mzXML Import Converter";

	@Override
	public IProcessingInfo convert(File file, IProgressMonitor monitor) {

		IProcessingInfo processingInfo = super.validate(file);
		if(!processingInfo.hasErrorMessages()) {
			/*
			 * Read the chromatogram.
			 */
			file = SpecificationValidator.validateSpecification(file);
			IChromatogramMSDReader reader = new ChromatogramReader();
			monitor.subTask(IConstants.IMPORT_CHROMATOGRAM);
			try {
				IChromatogramMSD chromatogram = reader.read(file, monitor);
				processingInfo.setProcessingResult(chromatogram);
			} catch(Exception e) {
				logger.warn(e);
				processingInfo.addErrorMessage(DESCRIPTION, "Something has definitely gone wrong with the file: " + file.getAbsolutePath());
			}
		}
		return processingInfo;
	}

	@Override
	public IChromatogramOverviewImportConverterProcessingInfo convertOverview(File file, IProgressMonitor monitor) {

		IChromatogramOverviewImportConverterProcessingInfo processingInfo = new ChromatogramOverviewImportConverterProcessingInfo();
		/*
		 * Validate the file.
		 */
		IProcessingInfo processingInfoValidate = super.validate(file);
		if(processingInfoValidate.hasErrorMessages()) {
			processingInfo.addMessages(processingInfoValidate);
		} else {
			/*
			 * Read the chromatogram overview.
			 */
			file = SpecificationValidator.validateSpecification(file);
			IChromatogramMSDReader reader = new ChromatogramReader();
			monitor.subTask(IConstants.IMPORT_CHROMATOGRAM_OVERVIEW);
			try {
				IChromatogramOverview chromatogramOverview = reader.readOverview(file, monitor);
				processingInfo.setChromatogramOverview(chromatogramOverview);
			} catch(Exception e) {
				logger.warn(e);
				processingInfo.addErrorMessage(DESCRIPTION, "Something has definitely gone wrong with the file: " + file.getAbsolutePath());
			}
		}
		return processingInfo;
	}
}
