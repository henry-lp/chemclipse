/*******************************************************************************
 * Copyright (c) 2016, 2018 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.msd.converter.supplier.csv.core;

import java.io.File;

import org.eclipse.chemclipse.msd.converter.database.AbstractDatabaseImportConverter;
import org.eclipse.chemclipse.msd.converter.processing.database.DatabaseImportConverterProcessingInfo;
import org.eclipse.chemclipse.msd.converter.processing.database.IDatabaseImportConverterProcessingInfo;
import org.eclipse.core.runtime.IProgressMonitor;

public class MassSpectrumIdentificationImportConverter extends AbstractDatabaseImportConverter {

	@Override
	public IDatabaseImportConverterProcessingInfo convert(File file, IProgressMonitor monitor) {

		IDatabaseImportConverterProcessingInfo processingInfo = new DatabaseImportConverterProcessingInfo();
		processingInfo.addErrorMessage("CSV Mass Spectrum List Import", "Mass spectrum import through CSV list files isn't implemented yet.");
		return processingInfo;
	}
}
