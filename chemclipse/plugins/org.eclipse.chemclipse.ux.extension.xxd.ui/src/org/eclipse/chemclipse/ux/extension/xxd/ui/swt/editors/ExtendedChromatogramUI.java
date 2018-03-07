/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.ux.extension.xxd.ui.swt.editors;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.chemclipse.chromatogram.csd.filter.core.chromatogram.ChromatogramFilterCSD;
import org.eclipse.chemclipse.chromatogram.csd.filter.core.chromatogram.IChromatogramFilterSupportCSD;
import org.eclipse.chemclipse.chromatogram.filter.core.chromatogram.ChromatogramFilter;
import org.eclipse.chemclipse.chromatogram.filter.core.chromatogram.IChromatogramFilterSupplier;
import org.eclipse.chemclipse.chromatogram.filter.core.chromatogram.IChromatogramFilterSupport;
import org.eclipse.chemclipse.chromatogram.filter.exceptions.NoChromatogramFilterSupplierAvailableException;
import org.eclipse.chemclipse.chromatogram.msd.filter.core.chromatogram.ChromatogramFilterMSD;
import org.eclipse.chemclipse.chromatogram.msd.filter.core.chromatogram.IChromatogramFilterSupportMSD;
import org.eclipse.chemclipse.csd.model.core.IChromatogramCSD;
import org.eclipse.chemclipse.csd.model.core.selection.ChromatogramSelectionCSD;
import org.eclipse.chemclipse.csd.model.core.selection.IChromatogramSelectionCSD;
import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.comparator.PeakRetentionTimeComparator;
import org.eclipse.chemclipse.model.core.AbstractChromatogram;
import org.eclipse.chemclipse.model.core.IChromatogram;
import org.eclipse.chemclipse.model.core.IPeak;
import org.eclipse.chemclipse.model.core.IScan;
import org.eclipse.chemclipse.model.exceptions.ChromatogramIsNullException;
import org.eclipse.chemclipse.model.selection.ChromatogramSelectionSupport;
import org.eclipse.chemclipse.model.selection.IChromatogramSelection;
import org.eclipse.chemclipse.model.selection.MoveDirection;
import org.eclipse.chemclipse.msd.model.core.IChromatogramMSD;
import org.eclipse.chemclipse.msd.model.core.selection.ChromatogramSelectionMSD;
import org.eclipse.chemclipse.msd.model.core.selection.IChromatogramSelectionMSD;
import org.eclipse.chemclipse.rcp.ui.icons.core.ApplicationImageFactory;
import org.eclipse.chemclipse.rcp.ui.icons.core.IApplicationImage;
import org.eclipse.chemclipse.support.comparator.SortOrder;
import org.eclipse.chemclipse.support.events.IChemClipseEvents;
import org.eclipse.chemclipse.support.text.ValueFormat;
import org.eclipse.chemclipse.support.ui.addons.ModelSupportAddon;
import org.eclipse.chemclipse.swt.ui.preferences.PreferencePageSWT;
import org.eclipse.chemclipse.swt.ui.preferences.PreferenceSupplier;
import org.eclipse.chemclipse.swt.ui.support.Colors;
import org.eclipse.chemclipse.ux.extension.ui.support.PartSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.Activator;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.charts.ChromatogramChart;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.charts.IdentificationLabelMarker;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.parts.EditorUpdateSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.support.ChromatogramChartSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.support.ChromatogramDataSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.support.PeakChartSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.support.ScanChartSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.internal.validation.RetentionTimeValidator;
import org.eclipse.chemclipse.ux.extension.xxd.ui.preferences.PreferenceConstants;
import org.eclipse.chemclipse.ux.extension.xxd.ui.preferences.PreferencePageChromatogram;
import org.eclipse.chemclipse.wsd.model.core.IChromatogramWSD;
import org.eclipse.chemclipse.wsd.model.core.selection.ChromatogramSelectionWSD;
import org.eclipse.chemclipse.wsd.model.core.selection.IChromatogramSelectionWSD;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.eavp.service.swtchart.axisconverter.MillisecondsToScanNumberConverter;
import org.eclipse.eavp.service.swtchart.core.BaseChart;
import org.eclipse.eavp.service.swtchart.core.IChartSettings;
import org.eclipse.eavp.service.swtchart.core.ICustomSelectionHandler;
import org.eclipse.eavp.service.swtchart.core.IExtendedChart;
import org.eclipse.eavp.service.swtchart.core.ISecondaryAxisSettings;
import org.eclipse.eavp.service.swtchart.core.RangeRestriction;
import org.eclipse.eavp.service.swtchart.core.ScrollableChart;
import org.eclipse.eavp.service.swtchart.core.SecondaryAxisSettings;
import org.eclipse.eavp.service.swtchart.events.AbstractHandledEventProcessor;
import org.eclipse.eavp.service.swtchart.events.IHandledEventProcessor;
import org.eclipse.eavp.service.swtchart.linecharts.ILineSeriesData;
import org.eclipse.eavp.service.swtchart.linecharts.ILineSeriesSettings;
import org.eclipse.eavp.service.swtchart.linecharts.LineChart;
import org.eclipse.eavp.service.swtchart.menu.AbstractChartMenuEntry;
import org.eclipse.eavp.service.swtchart.menu.IChartMenuEntry;
import org.eclipse.eavp.service.swtchart.menu.ResetChartHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.swtchart.IAxis.Position;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.IPlotArea;
import org.swtchart.LineStyle;
import org.swtchart.Range;

public class ExtendedChromatogramUI {

	private static final Logger logger = Logger.getLogger(ExtendedChromatogramUI.class);
	//
	private static final String LABEL_SCAN_NUMBER = "Scan Number";
	//
	private static final String TYPE_GENERIC = "TYPE_GENERIC";
	private static final String TYPE_MSD = "TYPE_MSD";
	private static final String TYPE_CSD = "TYPE_CSD";
	private static final String TYPE_WSD = "TYPE_WSD";
	//
	private static final String SERIES_ID_CHROMATOGRAM = "Chromatogram";
	private static final String SERIES_ID_BASELINE = "Baseline";
	private static final String SERIES_ID_PEAKS_NORMAL_ACTIVE = "Peak(s) [Active]";
	private static final String SERIES_ID_PEAKS_NORMAL_INACTIVE = "Peak(s) [Inactive]";
	private static final String SERIES_ID_PEAKS_ISTD_ACTIVE = "Peak(s) ISTD [Active]";
	private static final String SERIES_ID_PEAKS_ISTD_INACTIVE = "Peak(s) ISTD [Inactive]";
	private static final String SERIES_ID_SELECTED_PEAK_MARKER = "Selected Peak Marker";
	private static final String SERIES_ID_SELECTED_PEAK_SHAPE = "Selected Peak Shape";
	private static final String SERIES_ID_SELECTED_PEAK_BACKGROUND = "Selected Peak Background";
	private static final String SERIES_ID_SELECTED_SCAN = "Selected Scan";
	private static final String SERIES_ID_IDENTIFIED_SCANS = "Identified Scans";
	private static final String SERIES_ID_IDENTIFIED_SCAN_SELECTED = "Identified Scan Selected";
	//
	private static final int FIVE_MINUTES = (int)(AbstractChromatogram.MINUTE_CORRELATION_FACTOR * 5);
	private static final int THREE_MINUTES = (int)(AbstractChromatogram.MINUTE_CORRELATION_FACTOR * 3);
	//
	private Composite toolbarInfo;
	private Label labelChromatogramInfo;
	private Composite toolbarChromatograms;
	private Combo comboChromatograms;
	private Composite toolbarEdit;
	private ChromatogramChart chromatogramChart;
	private Combo comboTargetTransfer;
	//
	private IChromatogramSelection chromatogramSelection = null;
	private List<IChromatogramSelection> referenceChromatogramSelections = null; // Might be null ... no references.
	private List<IChromatogramSelection> editorChromatogramSelections = new ArrayList<IChromatogramSelection>(); // Is filled dynamically.
	//
	private List<IChartMenuEntry> chartMenuEntriesFilter;
	//
	private Map<String, IdentificationLabelMarker> peakLabelMarkerMap = new HashMap<String, IdentificationLabelMarker>();
	private Map<String, IdentificationLabelMarker> scanLabelMarkerMap = new HashMap<String, IdentificationLabelMarker>();
	//
	private PeakRetentionTimeComparator peakRetentionTimeComparator = new PeakRetentionTimeComparator(SortOrder.ASC);
	private EditorUpdateSupport editorUpdateSupport = new EditorUpdateSupport();
	private PeakChartSupport peakChartSupport = new PeakChartSupport();
	private ScanChartSupport scanChartSupport = new ScanChartSupport();
	private ChromatogramDataSupport chromatogramDataSupport = new ChromatogramDataSupport();
	private ChromatogramChartSupport chromatogramChartSupport = new ChromatogramChartSupport();
	//
	private boolean suspendUpdate = false;
	//
	private IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
	//
	private Display display = Display.getDefault();
	private Shell shell = display.getActiveShell();

	private class ChromatogramResetHandler extends ResetChartHandler {

		@Override
		public void execute(Shell shell, ScrollableChart scrollableChart) {

			super.execute(shell, scrollableChart);
			if(chromatogramSelection != null) {
				chromatogramSelection.reset(true);
			}
		}
	}

	private class ChromatogramSelectionHandler implements ICustomSelectionHandler {

		private BaseChart baseChart;

		public ChromatogramSelectionHandler(BaseChart baseChart) {
			this.baseChart = baseChart;
		}

		@Override
		public void handleUserSelection(Event event) {

			if(chromatogramSelection != null) {
				/*
				 * Get the range.
				 */
				Range rangeX = baseChart.getAxisSet().getXAxis(BaseChart.ID_PRIMARY_X_AXIS).getRange();
				Range rangeY = baseChart.getAxisSet().getYAxis(BaseChart.ID_PRIMARY_Y_AXIS).getRange();
				//
				int startRetentionTime = (int)rangeX.lower;
				int stopRetentionTime = (int)rangeX.upper;
				float startAbundance = (float)rangeY.lower;
				float stopAbundance = (float)rangeY.upper;
				//
				setChromatogramSelectionRange(startRetentionTime, stopRetentionTime, startAbundance, stopAbundance);
			}
		}
	}

	private class ScanSelectionHandler extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		@Override
		public int getEvent() {

			return BaseChart.EVENT_MOUSE_DOUBLE_CLICK;
		}

		@Override
		public int getButton() {

			return BaseChart.BUTTON_LEFT;
		}

		@Override
		public int getStateMask() {

			return SWT.NONE;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			if(chromatogramSelection != null) {
				IChromatogram chromatogram = chromatogramSelection.getChromatogram();
				int retentionTime = (int)baseChart.getSelectedPrimaryAxisValue(event.x, IExtendedChart.X_AXIS);
				int scanNumber = chromatogram.getScanNumber(retentionTime);
				IScan scan = chromatogram.getScan(scanNumber);
				if(scan != null) {
					chromatogramSelection.setSelectedScan(scan);
					display.asyncExec(new Runnable() {

						@Override
						public void run() {

							IEventBroker eventBroker = ModelSupportAddon.getEventBroker();
							eventBroker.send(IChemClipseEvents.TOPIC_SCAN_XXD_UPDATE_SELECTION, scan);
						}
					});
				}
			}
		}
	}

	private class PeakSelectionHandler extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		@Override
		public int getEvent() {

			return BaseChart.EVENT_MOUSE_DOUBLE_CLICK;
		}

		@Override
		public int getButton() {

			return BaseChart.BUTTON_LEFT;
		}

		@Override
		public int getStateMask() {

			return SWT.ALT;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			if(chromatogramSelection != null) {
				IChromatogram chromatogram = chromatogramSelection.getChromatogram();
				int retentionTime = (int)baseChart.getSelectedPrimaryAxisValue(event.x, IExtendedChart.X_AXIS);
				IPeak peak = null;
				if(chromatogram instanceof IChromatogramMSD) {
					IChromatogramMSD chromatogramMSD = (IChromatogramMSD)chromatogram;
					peak = chromatogramMSD.getPeak(retentionTime);
				} else if(chromatogram instanceof IChromatogramCSD) {
					IChromatogramCSD chromatogramCSD = (IChromatogramCSD)chromatogram;
					peak = chromatogramCSD.getPeak(retentionTime);
				} else if(chromatogram instanceof IChromatogramWSD) {
					// IChromatogramWSD chromatogramWSD = (IChromatogramWSD)chromatogram;
					// peak = chromatogramWSD.getPeak(retentionTime);
				}
				if(peak != null) {
					/*
					 * Fire an update.
					 */
					chromatogramSelection.setSelectedPeak(peak);
					boolean moveRetentionTimeOnPeakSelection = preferenceStore.getBoolean(PreferenceConstants.P_MOVE_RETENTION_TIME_ON_PEAK_SELECTION);
					if(moveRetentionTimeOnPeakSelection) {
						adjustChromatogramSelection(peak, chromatogramSelection);
					}
					//
					updateSelection();
					IEventBroker eventBroker = ModelSupportAddon.getEventBroker();
					eventBroker.send(IChemClipseEvents.TOPIC_PEAK_XXD_UPDATE_SELECTION, peak);
				}
			}
		}

		private void adjustChromatogramSelection(IPeak peak, IChromatogramSelection chromatogramSelection) {

			if(chromatogramSelection != null) {
				IChromatogram chromatogram = chromatogramSelection.getChromatogram();
				List<? extends IPeak> peaks = chromatogramDataSupport.getPeaks(chromatogram);
				List<? extends IPeak> peaksSelection = new ArrayList<>(chromatogramDataSupport.getPeaks(chromatogram, chromatogramSelection));
				Collections.sort(peaks, peakRetentionTimeComparator);
				Collections.sort(peaksSelection, peakRetentionTimeComparator);
				//
				if(peaks.get(0).equals(peak) || peaks.get(peaks.size() - 1).equals(peak)) {
					/*
					 * Don't move if it is the first or last peak of the chromatogram.
					 */
				} else {
					/*
					 * First peak of the selection: move left
					 * Last peak of the selection: move right
					 */
					if(peaksSelection.get(0).equals(peak)) {
						ChromatogramSelectionSupport.moveRetentionTimeWindow(chromatogramSelection, MoveDirection.LEFT, 5);
					} else if(peaksSelection.get(peaksSelection.size() - 1).equals(peak)) {
						ChromatogramSelectionSupport.moveRetentionTimeWindow(chromatogramSelection, MoveDirection.RIGHT, 5);
					}
				}
			}
		}
	}

	private class ScanSelectionArrowKeyHandler extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		private int keyCode;

		public ScanSelectionArrowKeyHandler(int keyCode) {
			this.keyCode = keyCode;
		}

		@Override
		public int getEvent() {

			return BaseChart.EVENT_KEY_UP;
		}

		@Override
		public int getButton() {

			return keyCode;
		}

		@Override
		public int getStateMask() {

			return SWT.CTRL;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			handleControlScanSelection(keyCode);
		}
	}

	private class ChromatogramMoveArrowKeyHandler extends AbstractHandledEventProcessor implements IHandledEventProcessor {

		private int keyCode;

		public ChromatogramMoveArrowKeyHandler(int keyCode) {
			this.keyCode = keyCode;
		}

		@Override
		public int getEvent() {

			return BaseChart.EVENT_KEY_UP;
		}

		@Override
		public int getButton() {

			return keyCode;
		}

		@Override
		public int getStateMask() {

			return SWT.NONE;
		}

		@Override
		public void handleEvent(BaseChart baseChart, Event event) {

			handleArrowMoveWindowSelection(keyCode);
		}
	}

	private class FilterMenuEntry extends AbstractChartMenuEntry implements IChartMenuEntry {

		private String name;
		private String filterId;
		private String type;
		private IChromatogramSelection chromatogramSelection;

		public FilterMenuEntry(String name, String filterId, String type, IChromatogramSelection chromatogramSelection) {
			this.name = name;
			this.filterId = filterId;
			this.type = type;
			this.chromatogramSelection = chromatogramSelection;
		}

		@Override
		public String getCategory() {

			return "Filter";
		}

		@Override
		public String getName() {

			return name;
		}

		@Override
		public void execute(Shell shell, ScrollableChart scrollableChart) {

			if(chromatogramSelection != null) {
				/*
				 * Create the runnable.
				 */
				IRunnableWithProgress runnable = new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

						switch(type) {
							case TYPE_GENERIC:
								ChromatogramFilter.applyFilter(chromatogramSelection, filterId, monitor);
								break;
							case TYPE_MSD:
								if(chromatogramSelection instanceof IChromatogramSelectionMSD) {
									IChromatogramSelectionMSD chromatogramSelectionMSD = (IChromatogramSelectionMSD)chromatogramSelection;
									ChromatogramFilterMSD.applyFilter(chromatogramSelectionMSD, filterId, monitor);
								}
								break;
							case TYPE_CSD:
								if(chromatogramSelection instanceof IChromatogramSelectionCSD) {
									IChromatogramSelectionCSD chromatogramSelectionCSD = (IChromatogramSelectionCSD)chromatogramSelection;
									ChromatogramFilterCSD.applyFilter(chromatogramSelectionCSD, filterId, monitor);
								}
								break;
							case TYPE_WSD:
								//
								break;
						}
					}
				};
				/*
				 * Excecute
				 */
				ProgressMonitorDialog monitor = new ProgressMonitorDialog(display.getActiveShell());
				try {
					monitor.run(true, true, runnable);
					updateChromatogram();
					updateSelection();
				} catch(InvocationTargetException e) {
					logger.warn(e);
				} catch(InterruptedException e) {
					logger.warn(e);
				}
			}
		}
	}

	@Inject
	public ExtendedChromatogramUI(Composite parent) {
		chartMenuEntriesFilter = new ArrayList<IChartMenuEntry>();
		initialize(parent);
	}

	public synchronized void updateChromatogramSelection(IChromatogramSelection chromatogramSelection) {

		this.chromatogramSelection = chromatogramSelection;
		if(chromatogramSelection != null) {
			/*
			 * Adjust
			 */
			clearPeakAndScanLabels();
			adjustMinuteScale();
			addChartMenuEntriesFilter();
			updateChromatogram();
			if(referenceChromatogramSelections == null) {
				updateChromatogramCombo();
			}
		} else {
			comboChromatograms.setItems(new String[0]);
			updateChromatogram();
		}
	}

	public void update() {

		if(!suspendUpdate) {
			updateChromatogram();
			adjustChromatogramSelectionRange();
		}
	}

	public void updateSelectedScan() {

		chromatogramChart.deleteSeries(SERIES_ID_SELECTED_SCAN);
		chromatogramChart.deleteSeries(SERIES_ID_IDENTIFIED_SCAN_SELECTED);
		//
		List<ILineSeriesData> lineSeriesDataList = new ArrayList<ILineSeriesData>();
		addSelectedScanData(lineSeriesDataList);
		addSelectedIdentifiedScanData(lineSeriesDataList);
		addLineSeriesData(lineSeriesDataList);
		adjustChromatogramSelectionRange();
	}

	public void updateSelectedPeak() {

		chromatogramChart.deleteSeries(SERIES_ID_SELECTED_PEAK_MARKER);
		chromatogramChart.deleteSeries(SERIES_ID_SELECTED_PEAK_SHAPE);
		chromatogramChart.deleteSeries(SERIES_ID_SELECTED_PEAK_BACKGROUND);
		//
		List<ILineSeriesData> lineSeriesDataList = new ArrayList<ILineSeriesData>();
		addSelectedPeakData(lineSeriesDataList);
		addLineSeriesData(lineSeriesDataList);
		adjustChromatogramSelectionRange();
	}

	public IChromatogramSelection getChromatogramSelection() {

		return chromatogramSelection;
	}

	public boolean isActiveChromatogramSelection(IChromatogramSelection chromatogramSelection) {

		if(this.chromatogramSelection == chromatogramSelection) {
			return true;
		}
		return false;
	}

	private void updateChromatogramTargetTransferSelections() {

		editorChromatogramSelections = editorUpdateSupport.getChromatogramSelections();
		updateChromatogramTargetTransferCombo();
	}

	private void clearPeakAndScanLabels() {

		peakLabelMarkerMap.clear();
		scanLabelMarkerMap.clear();
	}

	private void adjustMinuteScale() {

		int startRetentionTime = chromatogramSelection.getStartRetentionTime();
		int stopRetentionTime = chromatogramSelection.getStopRetentionTime();
		int deltaRetentionTime = stopRetentionTime - startRetentionTime + 1;
		IChartSettings chartSettings = chromatogramChart.getChartSettings();
		List<ISecondaryAxisSettings> axisSettings = chartSettings.getSecondaryAxisSettingsListX();
		for(ISecondaryAxisSettings axisSetting : axisSettings) {
			if(axisSetting.getTitle().equals("Minutes")) {
				if(deltaRetentionTime >= FIVE_MINUTES) {
					axisSetting.setDecimalFormat(new DecimalFormat(("0.00"), new DecimalFormatSymbols(Locale.ENGLISH)));
				} else if(deltaRetentionTime >= THREE_MINUTES) {
					axisSetting.setDecimalFormat(new DecimalFormat(("0.000"), new DecimalFormatSymbols(Locale.ENGLISH)));
				} else {
					axisSetting.setDecimalFormat(new DecimalFormat(("0.0000"), new DecimalFormatSymbols(Locale.ENGLISH)));
				}
			}
		}
		chromatogramChart.applySettings(chartSettings);
	}

	private void addChartMenuEntriesFilter() {

		IChartSettings chartSettings = chromatogramChart.getChartSettings();
		cleanChartMenuEntriesFilter(chartSettings);
		//
		if(chromatogramSelection != null) {
			/*
			 * Generic
			 */
			addChartMenuEntriesFilter(chartSettings, ChromatogramFilter.getChromatogramFilterSupport(), TYPE_GENERIC);
			/*
			 * Specific
			 */
			if(chromatogramSelection instanceof IChromatogramSelectionMSD) {
				addChartMenuEntriesFilterMSD(chartSettings, ChromatogramFilterMSD.getChromatogramFilterSupport(), TYPE_MSD);
			} else if(chromatogramSelection instanceof IChromatogramSelectionCSD) {
				addChartMenuEntriesFilterCSD(chartSettings, ChromatogramFilterCSD.getChromatogramFilterSupport(), TYPE_CSD);
			} else if(chromatogramSelection instanceof IChromatogramSelectionWSD) {
				//
			}
		}
		//
		chromatogramChart.applySettings(chartSettings);
	}

	private void addChartMenuEntriesFilter(IChartSettings chartSettings, IChromatogramFilterSupport chromatogramFilterSupport, String type) {

		try {
			for(String filterId : chromatogramFilterSupport.getAvailableFilterIds()) {
				IChromatogramFilterSupplier filter = chromatogramFilterSupport.getFilterSupplier(filterId);
				String name = filter.getFilterName();
				FilterMenuEntry filterMenuEntry = new FilterMenuEntry(name, filterId, type, chromatogramSelection);
				chartMenuEntriesFilter.add(filterMenuEntry);
				chartSettings.addMenuEntry(filterMenuEntry);
			}
		} catch(NoChromatogramFilterSupplierAvailableException e) {
			logger.warn(e);
		}
	}

	// TODO Refactor
	private void addChartMenuEntriesFilterMSD(IChartSettings chartSettings, IChromatogramFilterSupportMSD chromatogramFilterSupport, String type) {

		try {
			for(String filterId : chromatogramFilterSupport.getAvailableFilterIds()) {
				IChromatogramFilterSupplier filter = chromatogramFilterSupport.getFilterSupplier(filterId);
				String name = filter.getFilterName();
				FilterMenuEntry filterMenuEntry = new FilterMenuEntry(name, filterId, type, chromatogramSelection);
				chartMenuEntriesFilter.add(filterMenuEntry);
				chartSettings.addMenuEntry(filterMenuEntry);
			}
		} catch(NoChromatogramFilterSupplierAvailableException e) {
			logger.warn(e);
		}
	}

	// TODO Refactor
	private void addChartMenuEntriesFilterCSD(IChartSettings chartSettings, IChromatogramFilterSupportCSD chromatogramFilterSupport, String type) {

		try {
			for(String filterId : chromatogramFilterSupport.getAvailableFilterIds()) {
				IChromatogramFilterSupplier filter = chromatogramFilterSupport.getFilterSupplier(filterId);
				String name = filter.getFilterName();
				FilterMenuEntry filterMenuEntry = new FilterMenuEntry(name, filterId, type, chromatogramSelection);
				chartMenuEntriesFilter.add(filterMenuEntry);
				chartSettings.addMenuEntry(filterMenuEntry);
			}
		} catch(NoChromatogramFilterSupplierAvailableException e) {
			logger.warn(e);
		}
	}

	private void cleanChartMenuEntriesFilter(IChartSettings chartSettings) {

		for(IChartMenuEntry chartMenuEntry : chartMenuEntriesFilter) {
			chartSettings.removeMenuEntry(chartMenuEntry);
		}
		chartMenuEntriesFilter.clear();
	}

	private void updateChromatogram() {

		updateChromatogramTargetTransferSelections();
		//
		updateLabel();
		deleteScanNumberSecondaryAxisX();
		chromatogramChart.deleteSeries();
		//
		if(chromatogramSelection != null) {
			addjustChromatogramChart();
			addChromatogramSeriesData();
			addScanNumberSecondaryAxisX();
		}
	}

	private void addjustChromatogramChart() {

		IChartSettings chartSettings = chromatogramChart.getChartSettings();
		RangeRestriction rangeRestriction = chartSettings.getRangeRestriction();
		rangeRestriction.setForceZeroMinY(false);
		//
		if(chromatogramSelection instanceof IChromatogramSelectionMSD) {
			rangeRestriction.setZeroY(true);
		} else if(chromatogramSelection instanceof IChromatogramSelectionCSD) {
			rangeRestriction.setZeroY(false);
		} else if(chromatogramSelection instanceof IChromatogramSelectionWSD) {
			rangeRestriction.setZeroY(false);
		}
		chromatogramChart.applySettings(chartSettings);
	}

	private void addChromatogramSeriesData() {

		List<ILineSeriesData> lineSeriesDataList = new ArrayList<ILineSeriesData>();
		//
		addChromatogramData(lineSeriesDataList);
		addPeakData(lineSeriesDataList);
		addIdentifiedScansData(lineSeriesDataList);
		addSelectedPeakData(lineSeriesDataList);
		addSelectedScanData(lineSeriesDataList);
		addSelectedIdentifiedScanData(lineSeriesDataList);
		addBaselineData(lineSeriesDataList);
		addLineSeriesData(lineSeriesDataList);
	}

	private void addChromatogramData(List<ILineSeriesData> lineSeriesDataList) {

		Color color = Colors.getColor(preferenceStore.getString(PreferenceConstants.P_COLOR_CHROMATOGRAM));
		boolean enableChromatogramArea = preferenceStore.getBoolean(PreferenceConstants.P_ENABLE_CHROMATOGRAM_AREA);
		//
		IChromatogram chromatogram = chromatogramSelection.getChromatogram();
		ILineSeriesData lineSeriesData = chromatogramChartSupport.getLineSeriesDataChromatogram(chromatogram, SERIES_ID_CHROMATOGRAM, color);
		lineSeriesData.getLineSeriesSettings().setEnableArea(enableChromatogramArea);
		lineSeriesDataList.add(lineSeriesData);
	}

	private void addPeakData(List<ILineSeriesData> lineSeriesDataList) {

		if(chromatogramSelection != null) {
			IChromatogram chromatogram = chromatogramSelection.getChromatogram();
			int symbolSize = preferenceStore.getInt(PreferenceConstants.P_CHROMATOGRAM_PEAK_LABEL_SYMBOL_SIZE);
			//
			List<? extends IPeak> peaks = chromatogramDataSupport.getPeaks(chromatogram);
			List<IPeak> peaksActiveNormal = new ArrayList<IPeak>();
			List<IPeak> peaksInactiveNormal = new ArrayList<IPeak>();
			List<IPeak> peaksActiveISTD = new ArrayList<IPeak>();
			List<IPeak> peaksInactiveISTD = new ArrayList<IPeak>();
			//
			for(IPeak peak : peaks) {
				if(peak.getInternalStandards().size() > 0) {
					if(peak.isActiveForAnalysis()) {
						peaksActiveISTD.add(peak);
					} else {
						peaksInactiveISTD.add(peak);
					}
				} else {
					if(peak.isActiveForAnalysis()) {
						peaksActiveNormal.add(peak);
					} else {
						peaksInactiveNormal.add(peak);
					}
				}
			}
			//
			addPeaks(lineSeriesDataList, peaksActiveNormal, PlotSymbolType.INVERTED_TRIANGLE, symbolSize, Colors.DARK_GRAY, SERIES_ID_PEAKS_NORMAL_ACTIVE);
			addPeaks(lineSeriesDataList, peaksInactiveNormal, PlotSymbolType.INVERTED_TRIANGLE, symbolSize, Colors.GRAY, SERIES_ID_PEAKS_NORMAL_INACTIVE);
			addPeaks(lineSeriesDataList, peaksActiveISTD, PlotSymbolType.DIAMOND, symbolSize, Colors.RED, SERIES_ID_PEAKS_ISTD_ACTIVE);
			addPeaks(lineSeriesDataList, peaksInactiveISTD, PlotSymbolType.DIAMOND, symbolSize, Colors.GRAY, SERIES_ID_PEAKS_ISTD_INACTIVE);
		}
	}

	private void addPeaks(List<ILineSeriesData> lineSeriesDataList, List<IPeak> peaks, PlotSymbolType plotSymbolType, int symbolSize, Color symbolColor, String seriesId) {

		if(peaks.size() > 0) {
			boolean showChromatogramPeakLabels = preferenceStore.getBoolean(PreferenceConstants.P_SHOW_CHROMATOGRAM_PEAK_LABELS);
			//
			Collections.sort(peaks, peakRetentionTimeComparator);
			ILineSeriesData lineSeriesData = peakChartSupport.getPeaks(peaks, true, false, symbolColor, seriesId);
			ILineSeriesSettings lineSeriesSettings = lineSeriesData.getLineSeriesSettings();
			lineSeriesSettings.setEnableArea(false);
			lineSeriesSettings.setLineStyle(LineStyle.NONE);
			lineSeriesSettings.setSymbolType(plotSymbolType);
			lineSeriesSettings.setSymbolSize(symbolSize);
			lineSeriesSettings.setSymbolColor(symbolColor);
			lineSeriesDataList.add(lineSeriesData);
			//
			IPlotArea plotArea = (IPlotArea)chromatogramChart.getBaseChart().getPlotArea();
			IdentificationLabelMarker peakLabelMarker = peakLabelMarkerMap.get(seriesId);
			/*
			 * Remove the label marker.
			 */
			if(peakLabelMarker != null) {
				plotArea.removeCustomPaintListener(peakLabelMarker);
			}
			/*
			 * Add the labels.
			 */
			if(showChromatogramPeakLabels) {
				int indexSeries = lineSeriesDataList.size() - 1;
				peakLabelMarker = new IdentificationLabelMarker(chromatogramChart.getBaseChart(), indexSeries, peaks, null);
				plotArea.addCustomPaintListener(peakLabelMarker);
				peakLabelMarkerMap.put(seriesId, peakLabelMarker);
			}
		}
	}

	private void addIdentifiedScansData(List<ILineSeriesData> lineSeriesDataList) {

		if(chromatogramSelection != null) {
			String seriesId = SERIES_ID_IDENTIFIED_SCANS;
			List<IScan> scans = chromatogramDataSupport.getIdentifiedScans(chromatogramSelection.getChromatogram());
			int symbolSize = preferenceStore.getInt(PreferenceConstants.P_CHROMATOGRAM_SCAN_LABEL_SYMBOL_SIZE);
			addIdentifiedScansData(lineSeriesDataList, scans, PlotSymbolType.CIRCLE, symbolSize, Colors.DARK_GRAY, seriesId);
			//
			IPlotArea plotArea = (IPlotArea)chromatogramChart.getBaseChart().getPlotArea();
			IdentificationLabelMarker scanLabelMarker = scanLabelMarkerMap.get(seriesId);
			/*
			 * Remove the label marker.
			 */
			if(scanLabelMarker != null) {
				plotArea.removeCustomPaintListener(scanLabelMarker);
			}
			/*
			 * Add the labels.
			 */
			boolean showChromatogramScanLabels = preferenceStore.getBoolean(PreferenceConstants.P_SHOW_CHROMATOGRAM_SCAN_LABELS);
			if(showChromatogramScanLabels) {
				int indexSeries = lineSeriesDataList.size() - 1;
				scanLabelMarker = new IdentificationLabelMarker(chromatogramChart.getBaseChart(), indexSeries, null, scans);
				plotArea.addCustomPaintListener(scanLabelMarker);
				scanLabelMarkerMap.put(seriesId, scanLabelMarker);
			}
		}
	}

	private void addIdentifiedScansData(List<ILineSeriesData> lineSeriesDataList, List<IScan> scans, PlotSymbolType plotSymbolType, int symbolSize, Color symbolColor, String seriesId) {

		if(scans.size() > 0) {
			ILineSeriesData lineSeriesData = scanChartSupport.getLineSeriesDataPoint(scans, false, seriesId);
			ILineSeriesSettings lineSeriesSettings = lineSeriesData.getLineSeriesSettings();
			lineSeriesSettings.setLineStyle(LineStyle.NONE);
			lineSeriesSettings.setSymbolType(plotSymbolType);
			lineSeriesSettings.setSymbolSize(symbolSize);
			lineSeriesSettings.setSymbolColor(symbolColor);
			lineSeriesDataList.add(lineSeriesData);
		}
	}

	private void addSelectedIdentifiedScanData(List<ILineSeriesData> lineSeriesDataList) {

		if(chromatogramSelection != null) {
			IScan scan = chromatogramSelection.getSelectedIdentifiedScan();
			if(scan != null) {
				String seriesId = SERIES_ID_IDENTIFIED_SCAN_SELECTED;
				Color color = Colors.getColor(preferenceStore.getString(PreferenceConstants.P_COLOR_CHROMATOGRAM_SELECTED_SCAN_IDENTIFIED));
				int symbolSize = preferenceStore.getInt(PreferenceConstants.P_CHROMATOGRAM_SCAN_LABEL_SYMBOL_SIZE);
				List<IScan> scans = new ArrayList<>();
				scans.add(scan);
				addIdentifiedScansData(lineSeriesDataList, scans, PlotSymbolType.CIRCLE, symbolSize, color, seriesId);
			}
		}
	}

	private void addSelectedPeakData(List<ILineSeriesData> lineSeriesDataList) {

		IPeak peak = chromatogramSelection.getSelectedPeak();
		if(peak != null) {
			/*
			 * Settings
			 */
			boolean mirrored = false;
			ILineSeriesData lineSeriesData;
			Color colorPeak = Colors.getColor(preferenceStore.getString(PreferenceConstants.P_COLOR_CHROMATOGRAM_SELECTED_PEAK));
			/*
			 * Peak Marker
			 */
			int symbolSize = preferenceStore.getInt(PreferenceConstants.P_CHROMATOGRAM_PEAK_LABEL_SYMBOL_SIZE);
			List<IPeak> peaks = new ArrayList<>();
			peaks.add(peak);
			addPeaks(lineSeriesDataList, peaks, PlotSymbolType.INVERTED_TRIANGLE, symbolSize, colorPeak, SERIES_ID_SELECTED_PEAK_MARKER);
			/*
			 * Peak
			 */
			int markerSize = preferenceStore.getInt(PreferenceConstants.P_CHROMATOGRAM_SELECTED_PEAK_MARKER_SIZE);
			PlotSymbolType symbolType = PlotSymbolType.valueOf(preferenceStore.getString(PreferenceConstants.P_CHROMATOGRAM_SELECTED_PEAK_MARKER_TYPE));
			lineSeriesData = peakChartSupport.getPeak(peak, true, mirrored, colorPeak, SERIES_ID_SELECTED_PEAK_SHAPE);
			ILineSeriesSettings lineSeriesSettings = lineSeriesData.getLineSeriesSettings();
			lineSeriesSettings.setSymbolType(symbolType);
			lineSeriesSettings.setSymbolColor(colorPeak);
			lineSeriesSettings.setSymbolSize(markerSize);
			lineSeriesDataList.add(lineSeriesData);
			/*
			 * Background
			 */
			Color colorBackground = Colors.getColor(preferenceStore.getString(PreferenceConstants.P_COLOR_PEAK_BACKGROUND));
			lineSeriesData = peakChartSupport.getPeakBackground(peak, mirrored, colorBackground, SERIES_ID_SELECTED_PEAK_BACKGROUND);
			lineSeriesDataList.add(lineSeriesData);
		}
	}

	private void addSelectedScanData(List<ILineSeriesData> lineSeriesDataList) {

		IScan scan = chromatogramSelection.getSelectedScan();
		if(scan != null) {
			Color color = Colors.getColor(preferenceStore.getString(PreferenceConstants.P_COLOR_CHROMATOGRAM_SELECTED_SCAN));
			int markerSize = preferenceStore.getInt(PreferenceConstants.P_CHROMATOGRAM_SELECTED_SCAN_MARKER_SIZE);
			PlotSymbolType symbolType = PlotSymbolType.valueOf(preferenceStore.getString(PreferenceConstants.P_CHROMATOGRAM_SELECTED_SCAN_MARKER_TYPE));
			ILineSeriesData lineSeriesData = scanChartSupport.getLineSeriesDataPoint(scan, false, SERIES_ID_SELECTED_SCAN);
			ILineSeriesSettings lineSeriesSettings = lineSeriesData.getLineSeriesSettings();
			lineSeriesSettings.setLineStyle(LineStyle.NONE);
			lineSeriesSettings.setSymbolType(symbolType);
			lineSeriesSettings.setSymbolSize(markerSize);
			lineSeriesSettings.setSymbolColor(color);
			lineSeriesDataList.add(lineSeriesData);
		}
	}

	private void addBaselineData(List<ILineSeriesData> lineSeriesDataList) {

		boolean showChromatogramBaseline = preferenceStore.getBoolean(PreferenceConstants.P_SHOW_CHROMATOGRAM_BASELINE);
		//
		if(chromatogramSelection != null && showChromatogramBaseline) {
			Color color = Colors.getColor(preferenceStore.getString(PreferenceConstants.P_COLOR_CHROMATOGRAM_BASELINE));
			boolean enableBaselineArea = preferenceStore.getBoolean(PreferenceConstants.P_ENABLE_BASELINE_AREA);
			IChromatogram chromatogram = chromatogramSelection.getChromatogram();
			ILineSeriesData lineSeriesData = chromatogramChartSupport.getLineSeriesDataBaseline(chromatogram, SERIES_ID_BASELINE, color);
			lineSeriesData.getLineSeriesSettings().setEnableArea(enableBaselineArea);
			lineSeriesDataList.add(lineSeriesData);
		}
	}

	private void addLineSeriesData(List<ILineSeriesData> lineSeriesDataList) {

		/*
		 * Define the compression level.
		 */
		int compressionToLength;
		String compressionType = preferenceStore.getString(PreferenceConstants.P_CHROMATOGRAM_CHART_COMPRESSION_TYPE);
		switch(compressionType) {
			case LineChart.COMPRESSION_EXTREME:
				compressionToLength = LineChart.EXTREME_COMPRESSION;
				break;
			case LineChart.COMPRESSION_HIGH:
				compressionToLength = LineChart.HIGH_COMPRESSION;
				break;
			case LineChart.COMPRESSION_MEDIUM:
				compressionToLength = LineChart.MEDIUM_COMPRESSION;
				break;
			case LineChart.COMPRESSION_LOW:
				compressionToLength = LineChart.LOW_COMPRESSION;
				break;
			default:
				compressionToLength = LineChart.NO_COMPRESSION;
				break;
		}
		chromatogramChart.addSeriesData(lineSeriesDataList, compressionToLength);
	}

	private void initialize(Composite parent) {

		parent.setLayout(new GridLayout(1, true));
		//
		createToolbarMain(parent);
		toolbarInfo = createToolbarInfo(parent);
		toolbarChromatograms = createToolbarChromatograms(parent);
		toolbarEdit = createToolbarEdit(parent);
		createChromatogramChart(parent);
		//
		PartSupport.setCompositeVisibility(toolbarInfo, false);
		PartSupport.setCompositeVisibility(toolbarChromatograms, false);
		PartSupport.setCompositeVisibility(toolbarEdit, false);
	}

	private void createToolbarMain(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = SWT.END;
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(8, false));
		//
		createButtonToggleToolbarInfo(composite);
		createButtonToggleToolbarChromatograms(composite);
		createButtonToggleToolbarEdit(composite);
		createToggleChartSeriesLegendButton(composite);
		createToggleLegendMarkerButton(composite);
		createToggleRangeSelectorButton(composite);
		createResetButton(composite);
		createSettingsButton(composite);
	}

	private Composite createToolbarInfo(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(1, false));
		//
		labelChromatogramInfo = new Label(composite, SWT.NONE);
		labelChromatogramInfo.setText("");
		labelChromatogramInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		//
		return composite;
	}

	private Composite createToolbarChromatograms(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(3, false));
		//
		createButtonSelectPreviousChromatogram(composite);
		createComboChromatograms(composite);
		createButtonSelectNextChromatogram(composite);
		//
		return composite;
	}

	private Composite createToolbarEdit(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(10, false));
		//
		createComboTargetTransfer(composite);
		createTextTargetDelta(composite);
		createCheckBoxTargets(composite);
		createButtonTransferTargets(composite);
		createVerticalSeparator(composite);
		createButtonShrinkChromatograms(composite);
		createButtonAlignChromatograms(composite);
		createButtonStretchChromatograms(composite);
		createVerticalSeparator(composite);
		createButtonSetRanges(composite);
		//
		return composite;
	}

	private void createComboTargetTransfer(Composite parent) {

		comboTargetTransfer = new Combo(parent, SWT.READ_ONLY);
		comboTargetTransfer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboTargetTransfer.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});
	}

	private void createTextTargetDelta(Composite parent) {

		Text text = new Text(parent, SWT.BORDER);
		text.setText("0.45");
		text.setToolTipText("Delta retention time in minutes.");
		GridData gridData = new GridData();
		gridData.widthHint = 100;
		text.setLayoutData(gridData);
		//
		RetentionTimeValidator retentionTimeValidator = new RetentionTimeValidator();
		ControlDecoration controlDecoration = new ControlDecoration(text, SWT.LEFT | SWT.TOP);
		text.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {

				validate(retentionTimeValidator, controlDecoration, text);
			}
		});
	}

	private boolean validate(IValidator validator, ControlDecoration controlDecoration, Text text) {

		IStatus status = validator.validate(text.getText());
		if(status.isOK()) {
			controlDecoration.hide();
			return true;
		} else {
			controlDecoration.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());
			controlDecoration.showHoverText(status.getMessage());
			controlDecoration.show();
			return false;
		}
	}

	private void createCheckBoxTargets(Composite parent) {

		Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText("Best Target");
		checkbox.setSelection(true);
		checkbox.setToolTipText("Transfer only the best matching target.");
	}

	private void createButtonTransferTargets(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText("Transfer the targets from this chromatogram to the selected chromatogram.");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EXECUTE, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});
	}

	private void createButtonShrinkChromatograms(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText("Shrink the chromatograms");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_SHRINK_CHROMATOGRAMS, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});
	}

	private void createButtonAlignChromatograms(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText("Align the chromatograms");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ALIGN_CHROMATOGRAMS, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});
	}

	private void createButtonStretchChromatograms(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText("Stretch the chromatograms");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_STRETCH_CHROMATOGRAMS, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});
	}

	private void createButtonSetRanges(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText("Set the time range for all editors.");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EXECUTE_ADD, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				setRanges();
				MessageDialog.openInformation(shell, "Range Selection", "The selected editor range has been set successfully to all opened chromatograms.");
			}
		});
	}

	private void setRanges() {

		int startRetentionTime = chromatogramSelection.getStartRetentionTime();
		int stopRetentionTime = chromatogramSelection.getStopRetentionTime();
		float startAbundance = chromatogramSelection.getStartAbundance();
		float stopAbundance = chromatogramSelection.getStopAbundance();
		boolean setChromatogramIntensityRange = preferenceStore.getBoolean(PreferenceConstants.P_SET_CHROMATOGRAM_INTENSITY_RANGE);
		/*
		 * Editor
		 */
		for(IChromatogramSelection selection : editorChromatogramSelections) {
			if(selection != chromatogramSelection) {
				/*
				 * Don't fire an update. The next time the selection is on focus,
				 * the correct range will be loaded.
				 * selection.fireUpdateChange(true);
				 */
				selection.setStartRetentionTime(startRetentionTime);
				selection.setStopRetentionTime(stopRetentionTime);
				if(setChromatogramIntensityRange) {
					selection.setStartAbundance(startAbundance);
					selection.setStopAbundance(stopAbundance);
				}
			}
		}
	}

	private void createVerticalSeparator(Composite parent) {

		Label label = new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gridData = new GridData();
		gridData.heightHint = 35;
		label.setLayoutData(gridData);
	}

	private void createButtonSelectPreviousChromatogram(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText("Select previous chromatogram.");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ARROW_BACKWARD, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				int index = comboChromatograms.getSelectionIndex();
				index--;
				index = (index < 0) ? 0 : index;
				selectChromatogram(index);
			}
		});
	}

	private void createComboChromatograms(Composite parent) {

		comboChromatograms = new Combo(parent, SWT.READ_ONLY);
		comboChromatograms.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboChromatograms.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if(referenceChromatogramSelections != null) {
					int index = comboChromatograms.getSelectionIndex();
					selectChromatogram(index);
				}
			}
		});
	}

	private void createButtonSelectNextChromatogram(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setText("");
		button.setToolTipText("Select next chromatogram.");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_ARROW_FORWARD, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				int index = comboChromatograms.getSelectionIndex();
				index++;
				index = (index >= comboChromatograms.getItemCount()) ? comboChromatograms.getItemCount() - 1 : index;
				selectChromatogram(index);
			}
		});
	}

	private void selectChromatogram(int index) {

		comboChromatograms.select(index);
		IChromatogramSelection chromatogramSelection = referenceChromatogramSelections.get(index);
		if(chromatogramSelection != null) {
			updateChromatogramSelection(chromatogramSelection);
		}
	}

	private void updateChromatogramCombo() {

		List<String> references = new ArrayList<String>();
		/*
		 * Get the original and the referenced data.
		 */
		if(chromatogramSelection != null) {
			/*
			 * Initialize
			 */
			referenceChromatogramSelections = new ArrayList<IChromatogramSelection>();
			/*
			 * Original Data
			 */
			referenceChromatogramSelections.add(chromatogramSelection);
			references.add("Original Data");
			/*
			 * References
			 */
			List<IChromatogram> referencedChromatograms = chromatogramSelection.getChromatogram().getReferencedChromatograms();
			int i = 1;
			for(IChromatogram referencedChromatogram : referencedChromatograms) {
				IChromatogramSelection referencedChromatogramSelection = null;
				try {
					if(referencedChromatogram instanceof IChromatogramMSD) {
						referencedChromatogramSelection = new ChromatogramSelectionMSD(referencedChromatogram);
					} else if(referencedChromatogram instanceof IChromatogramCSD) {
						referencedChromatogramSelection = new ChromatogramSelectionCSD(referencedChromatogram);
					} else if(referencedChromatogram instanceof IChromatogramWSD) {
						referencedChromatogramSelection = new ChromatogramSelectionWSD(referencedChromatogram);
					}
				} catch(ChromatogramIsNullException e) {
					logger.warn(e);
				}
				//
				referenceChromatogramSelections.add(referencedChromatogramSelection);
				references.add("Chromatogram Reference #" + i++);
			}
		}
		/*
		 * Set the items.
		 */
		comboChromatograms.setItems(references.toArray(new String[references.size()]));
		if(references.size() > 0) {
			comboChromatograms.select(0);
		}
	}

	private void updateChromatogramTargetTransferCombo() {

		List<String> references = new ArrayList<String>();
		int index = 1;
		for(IChromatogramSelection chromatogramSelection : editorChromatogramSelections) {
			references.add(chromatogramSelection.getChromatogram().getName() + " [Tab#: " + index++ + "]");
		}
		/*
		 * Set the items.
		 */
		comboTargetTransfer.setItems(references.toArray(new String[references.size()]));
		if(references.size() > 0) {
			comboTargetTransfer.select(0);
		}
	}

	private void createChromatogramChart(Composite parent) {

		chromatogramChart = new ChromatogramChart(parent, SWT.BORDER);
		chromatogramChart.setLayoutData(new GridData(GridData.FILL_BOTH));
		/*
		 * Custom Selection Handler
		 */
		BaseChart baseChart = chromatogramChart.getBaseChart();
		baseChart.addCustomRangeSelectionHandler(new ChromatogramSelectionHandler(baseChart));
		/*
		 * Chart Settings
		 */
		IChartSettings chartSettings = chromatogramChart.getChartSettings();
		chartSettings.setCreateMenu(true);
		chartSettings.setEnableRangeSelector(true);
		chartSettings.setRangeSelectorDefaultAxisX(1); // Minutes
		chartSettings.setRangeSelectorDefaultAxisY(1); // Relative Abundance
		chartSettings.setShowRangeSelectorInitially(false);
		IChartMenuEntry chartMenuEntry = chartSettings.getChartMenuEntry(ResetChartHandler.NAME);
		chartSettings.removeMenuEntry(chartMenuEntry);
		chartSettings.addMenuEntry(new ChromatogramResetHandler());
		chartSettings.addHandledEventProcessor(new ScanSelectionHandler());
		chartSettings.addHandledEventProcessor(new PeakSelectionHandler());
		chartSettings.addHandledEventProcessor(new ScanSelectionArrowKeyHandler(SWT.ARROW_LEFT));
		chartSettings.addHandledEventProcessor(new ScanSelectionArrowKeyHandler(SWT.ARROW_RIGHT));
		chartSettings.addHandledEventProcessor(new ChromatogramMoveArrowKeyHandler(SWT.ARROW_LEFT));
		chartSettings.addHandledEventProcessor(new ChromatogramMoveArrowKeyHandler(SWT.ARROW_RIGHT));
		chartSettings.addHandledEventProcessor(new ChromatogramMoveArrowKeyHandler(SWT.ARROW_UP));
		chartSettings.addHandledEventProcessor(new ChromatogramMoveArrowKeyHandler(SWT.ARROW_DOWN));
		//
		chromatogramChart.applySettings(chartSettings);
	}

	private Button createButtonToggleToolbarInfo(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Toggle info toolbar.");
		button.setText("");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_INFO, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				boolean visible = PartSupport.toggleCompositeVisibility(toolbarInfo);
				if(visible) {
					button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_INFO, IApplicationImage.SIZE_16x16));
				} else {
					button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_INFO, IApplicationImage.SIZE_16x16));
				}
			}
		});
		//
		return button;
	}

	private Button createButtonToggleToolbarChromatograms(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Toggle chromatograms toolbar.");
		button.setText("");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CHROMATOGRAM, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				boolean visible = PartSupport.toggleCompositeVisibility(toolbarChromatograms);
				if(visible) {
					button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CHROMATOGRAM, IApplicationImage.SIZE_16x16));
				} else {
					button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CHROMATOGRAM, IApplicationImage.SIZE_16x16));
				}
			}
		});
		//
		return button;
	}

	private Button createButtonToggleToolbarEdit(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Toggle edit toolbar.");
		button.setText("");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EDIT, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				boolean visible = PartSupport.toggleCompositeVisibility(toolbarEdit);
				if(visible) {
					button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EDIT, IApplicationImage.SIZE_16x16));
				} else {
					button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_EDIT, IApplicationImage.SIZE_16x16));
				}
			}
		});
		//
		return button;
	}

	private void createToggleChartSeriesLegendButton(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Toggle the chart series legend.");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_TAG, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				chromatogramChart.toggleSeriesLegendVisibility();
			}
		});
	}

	private void createToggleLegendMarkerButton(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Toggle the chart legend marker.");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CHART_LEGEND_MARKER, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				chromatogramChart.togglePositionLegendVisibility();
				chromatogramChart.redraw();
			}
		});
	}

	private void createToggleRangeSelectorButton(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Toggle the chart range selector.");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CHART_RANGE_SELECTOR, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				chromatogramChart.toggleRangeSelectorVisibility();
			}
		});
	}

	private void createResetButton(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Reset the chromatogram");
		button.setText("");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_RESET, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				reset();
			}
		});
	}

	private void createSettingsButton(Composite parent) {

		Button button = new Button(parent, SWT.PUSH);
		button.setToolTipText("Open the Settings");
		button.setText("");
		button.setImage(ApplicationImageFactory.getInstance().getImage(IApplicationImage.IMAGE_CONFIGURE, IApplicationImage.SIZE_16x16));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				IPreferencePage preferencePageChromatogram = new PreferencePageChromatogram();
				preferencePageChromatogram.setTitle("Chromatogram Settings ");
				IPreferencePage preferencePageSWT = new PreferencePageSWT();
				preferencePageSWT.setTitle("Settings (SWT)");
				//
				PreferenceManager preferenceManager = new PreferenceManager();
				preferenceManager.addToRoot(new PreferenceNode("1", preferencePageChromatogram));
				preferenceManager.addToRoot(new PreferenceNode("2", preferencePageSWT));
				//
				PreferenceDialog preferenceDialog = new PreferenceDialog(shell, preferenceManager);
				preferenceDialog.create();
				preferenceDialog.setMessage("Settings");
				if(preferenceDialog.open() == PreferenceDialog.OK) {
					try {
						applySettings();
					} catch(Exception e1) {
						MessageDialog.openError(shell, "Settings", "Something has gone wrong to apply the settings.");
					}
				}
			}
		});
	}

	private void applySettings() {

		updateChromatogram();
	}

	private void reset() {

		updateChromatogram();
	}

	private void updateLabel() {

		if(chromatogramSelection != null) {
			labelChromatogramInfo.setText(chromatogramDataSupport.getChromatogramLabelExtended(chromatogramSelection.getChromatogram()));
		} else {
			labelChromatogramInfo.setText("");
		}
	}

	private void deleteScanNumberSecondaryAxisX() {

		IChartSettings chartSettings = chromatogramChart.getChartSettings();
		List<ISecondaryAxisSettings> secondaryAxisSettings = chartSettings.getSecondaryAxisSettingsListX();
		//
		ISecondaryAxisSettings secondaryAxisScanNumber = null;
		exitloop:
		for(ISecondaryAxisSettings secondaryAxis : secondaryAxisSettings) {
			if(secondaryAxis.getLabel().equals(LABEL_SCAN_NUMBER)) {
				secondaryAxisScanNumber = secondaryAxis;
				break exitloop;
			}
		}
		//
		if(secondaryAxisScanNumber != null) {
			secondaryAxisSettings.remove(secondaryAxisScanNumber);
		}
		//
		chromatogramChart.applySettings(chartSettings);
	}

	private void addScanNumberSecondaryAxisX() {

		boolean showChromatogramScanAxis = preferenceStore.getBoolean(PreferenceConstants.P_SHOW_CHROMATOGRAM_SCAN_AXIS);
		//
		try {
			deleteScanNumberSecondaryAxisX();
			IChartSettings chartSettings = chromatogramChart.getChartSettings();
			if(chromatogramSelection != null && showChromatogramScanAxis) {
				//
				IChromatogram chromatogram = chromatogramSelection.getChromatogram();
				int scanDelay = chromatogram.getScanDelay();
				int scanInterval = chromatogram.getScanInterval();
				//
				ISecondaryAxisSettings secondaryAxisSettings = new SecondaryAxisSettings(LABEL_SCAN_NUMBER, new MillisecondsToScanNumberConverter(scanDelay, scanInterval));
				secondaryAxisSettings.setPosition(Position.Secondary);
				secondaryAxisSettings.setDecimalFormat(ValueFormat.getDecimalFormatEnglish("0"));
				secondaryAxisSettings.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				secondaryAxisSettings.setExtraSpaceTitle(0);
				chartSettings.getSecondaryAxisSettingsListX().add(secondaryAxisSettings);
			}
			//
			chromatogramChart.applySettings(chartSettings);
			chromatogramChart.adjustRange(true);
			chromatogramChart.redraw();
		} catch(Exception e) {
			logger.warn(e);
		}
	}

	private void setChromatogramSelectionRange(int startRetentionTime, int stopRetentionTime, float startAbundance, float stopAbundance) {

		chromatogramSelection.setRanges(startRetentionTime, stopRetentionTime, startAbundance, stopAbundance, false);
		suspendUpdate = true;
		chromatogramSelection.update(true);
		suspendUpdate = false;
		adjustChromatogramSelectionRange();
	}

	private void adjustChromatogramSelectionRange() {

		if(chromatogramSelection != null) {
			chromatogramChart.setRange(IExtendedChart.X_AXIS, chromatogramSelection.getStartRetentionTime(), chromatogramSelection.getStopRetentionTime());
			chromatogramChart.setRange(IExtendedChart.Y_AXIS, chromatogramSelection.getStartAbundance(), chromatogramSelection.getStopAbundance());
		}
	}

	private void handleControlScanSelection(int keyCode) {

		if(chromatogramSelection != null) {
			/*
			 * Select the next or previous scan.
			 */
			int scanNumber = chromatogramSelection.getSelectedScan().getScanNumber();
			if(keyCode == SWT.ARROW_RIGHT) {
				scanNumber++;
			} else {
				scanNumber--;
			}
			/*
			 * Set and fire an update.
			 */
			IScan selectedScan = chromatogramSelection.getChromatogram().getScan(scanNumber);
			//
			IEventBroker eventBroker = ModelSupportAddon.getEventBroker();
			eventBroker.send(IChemClipseEvents.TOPIC_SCAN_XXD_UPDATE_SELECTION, selectedScan);
			//
			if(selectedScan != null) {
				/*
				 * The selection should slide with the selected scans.
				 */
				int scanRetentionTime = selectedScan.getRetentionTime();
				int startRetentionTime = chromatogramSelection.getStartRetentionTime();
				int stopRetentionTime = chromatogramSelection.getStopRetentionTime();
				/*
				 * Left or right slide on demand.
				 */
				if(scanRetentionTime <= startRetentionTime) {
					ChromatogramSelectionSupport.moveRetentionTimeWindow(chromatogramSelection, MoveDirection.LEFT, 5);
				} else if(scanRetentionTime >= stopRetentionTime) {
					ChromatogramSelectionSupport.moveRetentionTimeWindow(chromatogramSelection, MoveDirection.RIGHT, 5);
				}
				//
				chromatogramSelection.setSelectedScan(selectedScan, false);
				updateSelection();
			}
		}
	}

	private void handleArrowMoveWindowSelection(int keyCode) {

		if(chromatogramSelection != null) {
			if(keyCode == SWT.ARROW_RIGHT || keyCode == SWT.ARROW_LEFT) {
				/*
				 * Left, Right
				 * (Retention Time)
				 */
				boolean useAlternateWindowMoveDirection = preferenceStore.getBoolean(PreferenceConstants.P_ALTERNATE_WINDOW_MOVE_DIRECTION);
				//
				if(keyCode == SWT.ARROW_RIGHT) {
					MoveDirection moveDirection = (useAlternateWindowMoveDirection) ? MoveDirection.LEFT : MoveDirection.RIGHT;
					ChromatogramSelectionSupport.moveRetentionTimeWindow(chromatogramSelection, moveDirection, 20);
				} else {
					MoveDirection moveDirection = (useAlternateWindowMoveDirection) ? MoveDirection.RIGHT : MoveDirection.LEFT;
					ChromatogramSelectionSupport.moveRetentionTimeWindow(chromatogramSelection, moveDirection, 20);
				}
				updateSelection();
				//
			} else if(keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_DOWN) {
				/*
				 * Up, Down
				 * (Abundance)
				 * Doesn't work if auto adjust signals is enabled.
				 */
				float stopAbundance = chromatogramSelection.getStopAbundance();
				float newStopAbundance;
				if(PreferenceSupplier.useAlternateWindowMoveDirection()) {
					newStopAbundance = (keyCode == SWT.ARROW_UP) ? stopAbundance - stopAbundance / 20.0f : stopAbundance + stopAbundance / 20.0f;
				} else {
					newStopAbundance = (keyCode == SWT.ARROW_UP) ? stopAbundance + stopAbundance / 20.0f : stopAbundance - stopAbundance / 20.0f;
				}
				//
				int startRetentionTime = chromatogramSelection.getStartRetentionTime();
				int stopRetentionTime = chromatogramSelection.getStopRetentionTime();
				float startAbundance = chromatogramSelection.getStartAbundance();
				setChromatogramSelectionRange(startRetentionTime, stopRetentionTime, startAbundance, newStopAbundance);
				updateSelection();
			}
		}
	}

	private void updateSelection() {

		if(chromatogramSelection != null) {
			chromatogramSelection.update(true);
			adjustChromatogramSelectionRange();
		}
	}
}
