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
package org.eclipse.chemclipse.msd.converter.chromatogram;

import java.io.File;
import java.util.Map;

import org.eclipse.chemclipse.chromatogram.xxd.calculator.io.AMDISConverter;
import org.eclipse.chemclipse.chromatogram.xxd.calculator.io.MassLibConverter;
import org.eclipse.chemclipse.converter.chromatogram.AbstractChromatogramConverter;
import org.eclipse.chemclipse.converter.chromatogram.IChromatogramConverter;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.columns.ISeparationColumnIndices;
import org.eclipse.chemclipse.model.core.IScan;
import org.eclipse.chemclipse.model.identifier.ComparisonResult;
import org.eclipse.chemclipse.model.identifier.IComparisonResult;
import org.eclipse.chemclipse.model.identifier.IIdentificationTarget;
import org.eclipse.chemclipse.model.identifier.ILibraryInformation;
import org.eclipse.chemclipse.model.identifier.LibraryInformation;
import org.eclipse.chemclipse.model.implementation.IdentificationTarget;
import org.eclipse.chemclipse.model.support.LibraryInformationSupport;
import org.eclipse.chemclipse.msd.converter.preferences.PreferenceSupplier;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.IScanMSD;
import org.eclipse.chemclipse.processing.core.IProcessingInfo;
import org.eclipse.chemclipse.processing.core.exceptions.TypeCastException;
import org.eclipse.core.runtime.IProgressMonitor;

public final class ChromatogramConverterMSD extends AbstractChromatogramConverter<IChromatogramMSD> implements IChromatogramConverter<IChromatogramMSD> {

	private static final Logger logger = Logger.getLogger(ChromatogramConverterMSD.class);
	private static IChromatogramConverter<IChromatogramMSD> instance = null;

	public ChromatogramConverterMSD() {
		super("org.eclipse.chemclipse.msd.converter.chromatogramSupplier", IChromatogramMSD.class);
	}

	public static IChromatogramConverter<IChromatogramMSD> getInstance() {

		if(instance == null) {
			instance = new ChromatogramConverterMSD();
		}
		//
		return instance;
	}

	@Override
	public void postProcessChromatogram(IProcessingInfo processingInfo, IProgressMonitor monitor) {

		/*
		 * TODO: Create an extension point for the post processing supplier!
		 */
		if(processingInfo != null && processingInfo.getProcessingResult() instanceof IChromatogramMSD) {
			IChromatogramMSD chromatogramMSD = processingInfo.getProcessingResult(IChromatogramMSD.class);
			File file = chromatogramMSD.getFile();
			File directory = getDirectory(file);
			if(directory != null && directory.exists()) {
				parseCalibrationMassLib(chromatogramMSD, directory);
				parseTargetMassLib(chromatogramMSD, directory);
				parseCalibrationAMDIS(chromatogramMSD, directory);
			}
		}
	}

	private File getDirectory(File file) {

		File directory = null;
		if(file != null) {
			if(file.isFile()) {
				directory = file.getParentFile();
			} else {
				directory = file;
			}
		}
		//
		return directory;
	}

	private File getFile(File directory, String fileName, String fileExtension) {

		if(directory.exists()) {
			for(File file : directory.listFiles()) {
				if(file.isFile()) {
					String name = file.getName().toLowerCase();
					if(name.endsWith(fileExtension.toLowerCase()) && name.startsWith(fileName.toLowerCase())) {
						return file;
					}
				}
			}
		}
		//
		return null;
	}

	private void parseCalibrationMassLib(IChromatogramMSD chromatogramMSD, File directory) {

		if(PreferenceSupplier.isParseRetentionIndexDataMassLib()) {
			String fileName = PreferenceSupplier.isUseChromatogramNameMassLib() ? chromatogramMSD.getName() : PreferenceSupplier.getDefaultNameMassLib();
			File file = getFile(directory, fileName, ".inf");
			//
			if(file != null) {
				MassLibConverter massLibConverter = new MassLibConverter();
				try {
					IProcessingInfo processingInfo = massLibConverter.parseRetentionIndices(file);
					ISeparationColumnIndices separationColumnIndices = processingInfo.getProcessingResult(ISeparationColumnIndices.class);
					chromatogramMSD.getSeparationColumnIndices().putAll(separationColumnIndices);
				} catch(TypeCastException e) {
					logger.warn(e);
				}
			}
		}
	}

	private void parseTargetMassLib(IChromatogramMSD chromatogramMSD, File directory) {

		if(PreferenceSupplier.isParseTargetDataMassLib()) {
			String fileName = PreferenceSupplier.isUseChromatogramNameMassLib() ? chromatogramMSD.getName() : PreferenceSupplier.getDefaultNameMassLib();
			File file = getFile(directory, fileName, ".inf");
			//
			if(file != null) {
				MassLibConverter massLibConverter = new MassLibConverter();
				LibraryInformationSupport libraryInformationSupport = new LibraryInformationSupport();
				String referenceIdentifierMarker = PreferenceSupplier.getReferenceIdentifierMarker();
				String referenceIdentifierPrefix = PreferenceSupplier.getReferenceIdentifierPrefix();
				//
				try {
					IProcessingInfo processingInfo = massLibConverter.parseTargets(file);
					@SuppressWarnings("unchecked")
					Map<Integer, String> targets = (Map<Integer, String>)processingInfo.getProcessingResult(Map.class);
					for(Map.Entry<Integer, String> target : targets.entrySet()) {
						IScan scan = chromatogramMSD.getScan(target.getKey());
						if(scan != null && scan instanceof IScanMSD) {
							IScanMSD scanMSD = (IScanMSD)scan;
							ILibraryInformation libraryInformation = new LibraryInformation();
							libraryInformationSupport.extractNameAndReferenceIdentifier(target.getValue(), libraryInformation, referenceIdentifierMarker, referenceIdentifierPrefix);
							IComparisonResult comparisonResult = ComparisonResult.createBestMatchComparisonResult();
							IIdentificationTarget scanTargetMSD = new IdentificationTarget(libraryInformation, comparisonResult);
							scanMSD.getTargets().add(scanTargetMSD);
						}
					}
				} catch(TypeCastException e) {
					logger.warn(e);
				}
			}
		}
	}

	private void parseCalibrationAMDIS(IChromatogramMSD chromatogramMSD, File directory) {

		if(PreferenceSupplier.isParseRetentionIndexDataAMDIS()) {
			String fileName = PreferenceSupplier.isUseChromatogramNameAMDIS() ? chromatogramMSD.getName() : PreferenceSupplier.getDefaultNameAMDIS();
			File file = getFile(directory, fileName, ".cal");
			//
			if(file != null) {
				AMDISConverter amdisConverter = new AMDISConverter();
				try {
					IProcessingInfo processingInfo = amdisConverter.parseRetentionIndices(file);
					ISeparationColumnIndices separationColumnIndices = processingInfo.getProcessingResult(ISeparationColumnIndices.class);
					chromatogramMSD.getSeparationColumnIndices().putAll(separationColumnIndices);
				} catch(TypeCastException e) {
					logger.warn(e);
				}
			}
		}
	}
}
