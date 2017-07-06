/*******************************************************************************
 * Copyright (c) 2017 Jan Holy.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Jan Holy - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.ui.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.core.PcaUtils;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.IPcaResults;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.model.ISample;
import org.eclipse.chemclipse.chromatogram.xxd.process.supplier.pca.ui.editors.PcaEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class SamplesSelectionTree {

	private PcaEditor pcaEditor;
	private List<ISample> samples = new ArrayList<>();
	private Tree tree;

	public SamplesSelectionTree(PcaEditor pcaEditor, Composite parent) {
		this.pcaEditor = pcaEditor;
		this.tree = new Tree(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
		update();
	}

	private void setSampleTreeItem(ISample sample, TreeItem item) {

		item.setChecked(sample.isSelected());
		item.setText(sample.getName());
		item.setData(sample);
	}

	public void update() {

		/*
		 * clear all
		 */
		tree.clearAll(true);
		samples.clear();
		/*
		 * insert and sort samples
		 */
		IPcaResults results = pcaEditor.getPcaResults();
		if(results != null) {
			samples.addAll(results.getSampleList());
			PcaUtils.sortSampleListByName(samples);
			PcaUtils.sortSampleListByGroup(samples);
		}
		TreeItem groupTreeItem;
		String groupName;
		boolean isSelectSample = false;
		TreeItem treeItem;
		Iterator<ISample> it = samples.iterator();
		if(it.hasNext()) {
			/*
			 * set first branch, which contains group name
			 */
			ISample sample = it.next();
			groupTreeItem = new TreeItem(tree, SWT.None);
			groupName = sample.getGroupName();
			treeItem = new TreeItem(groupTreeItem, SWT.None);
			setSampleTreeItem(sample, treeItem);
			isSelectSample = isSelectSample || sample.isSelected();
			while(it.hasNext()) {
				sample = it.next();
				if(ObjectUtils.compare(sample.getGroupName(), groupName) == 0) {
					treeItem = new TreeItem(groupTreeItem, SWT.None);
					setSampleTreeItem(sample, treeItem);
					isSelectSample = isSelectSample || sample.isSelected();
				} else {
					groupTreeItem.setChecked(isSelectSample);
					/*
					 * set branch, which contains group name
					 */
					groupTreeItem = new TreeItem(tree, SWT.None);
					isSelectSample = false;
					if(groupName != null) {
						groupTreeItem.setText(groupName);
					} else {
						groupTreeItem.setText("----");
					}
					groupName = sample.getGroupName();
					treeItem = new TreeItem(groupTreeItem, SWT.None);
					setSampleTreeItem(sample, treeItem);
					isSelectSample = isSelectSample || sample.isSelected();
				}
			}
			groupTreeItem.setChecked(isSelectSample);
		}
		/*
		 * add check listener
		 */
		tree.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				if(event.detail != SWT.CHECK) {
					return;
				}
				TreeItem item = (TreeItem)event.item;
				boolean isChecked = item.getChecked();
				/*
				 * set selected item
				 */
				ISample sample = (ISample)item.getData();
				if(sample != null) {
					sample.setSelected(isChecked);
				}
				/*
				 * set children are checked according to selected item
				 */
				TreeItem[] children = item.getItems();
				for(TreeItem treeItem : children) {
					treeItem.setChecked(isChecked);
					sample = (ISample)treeItem.getData();
					if(sample != null) {
						sample.setSelected(isChecked);
					}
				}
				/*
				 * set parent if any child of parent is set checked, parent is checked also.
				 */
				TreeItem parentItem = item.getParentItem();
				if(parentItem != null) {
					TreeItem[] sibs = parentItem.getItems();
					boolean checkParentItem = false;
					for(TreeItem sib : sibs) {
						sample = (ISample)sib.getData();
						if(sample != null) {
							checkParentItem = checkParentItem || sample.isSelected();
						}
						parentItem.setChecked(checkParentItem);
					}
				}
				/*
				 * update editors
				 */
				pcaEditor.updateSelection();
			}
		});
	}
}
