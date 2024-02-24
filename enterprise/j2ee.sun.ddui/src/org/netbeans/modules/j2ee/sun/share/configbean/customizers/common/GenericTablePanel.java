/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * GenericTablePanel.java
 *
 * Created on October 7, 2003, 4:09 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;

/**
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */

public class GenericTablePanel extends BeanTablePanel {

	// Resource bundle to retrieve string from and the base resource name to use.
	private ResourceBundle resourceBundle;
	private String resourceBase;
	
	// Title of popup dialog used for entering table rows.
	private String dialogTitle;
	
	// Heading that goes above the table.
	private String panelHeading;
	
	// JLabel containing the heading
	private JLabel jLblTableHeading;
	
	// 'entryList' is a hack to get the field list passed to via the GenericTableDialog
	// to the GenericTableDialogPanel object.  getInputDialog(...) is called from
	// the base class constructor and needs access to the entry list.  Correct solution
	// would involve redesigning the base class but that would have a ripple effect
	// across all derivations.
	//
	private List entryList;
	
	// This field holds the class of the object used for the entry panel in the
	// input dialog to allow for easier customization.  (Some tables require
	// sophisticated layout and/or input controls.)
	private Class entryPanelClass;
	
	// This field holds extra data to be passed to the popup dialog.  Currently,
	// it's main use is for the dynamic property popup and it holds the list of
	// properties along with their value definitions but it can be used for 
	// anything the entry dialog needs.
	private Object extraData;
	
	// This field holds the help id for the popup entry panel used to enter data
	// into this table.
	private String entryPanelHelpId;
	
	/** Creates a new instance of GenericTablePanel -- accessibility enabled 
	 *  Resources required to match the base resource tag:
	 *		TITLE_##
	 *		HEADING_##
	 *		ACSN_TABLE_##
	 *		ACSD_TABLE_##
	 *		ACSN_POPUP_##
	 *		ACSD_POPUP_##
	 *
	 * @param model the table model for this table.
	 * @param bundle the resource bundle to retrieve the resources from.
	 * @param resourceBase the base resource tag (combined via described rules).
	 * @param helpId the helpId for the popup dialog
	 */
	public GenericTablePanel(GenericTableModel model, ResourceBundle bundle, String resourceBase, String helpId) {
		this(model, bundle, resourceBase, GenericTableDialogPanel.class, helpId);
	}
	
	/** Creates a new instance of GenericTablePanel */
	public GenericTablePanel(GenericTableModel model, ResourceBundle bundle, String resourceBase,
        Class entryPanelClass, String helpId) {
        this(model, bundle, resourceBase, entryPanelClass, helpId, null);
	}
	
	/** Creates a new instance of GenericTablePanel */
	public GenericTablePanel(GenericTableModel model, ResourceBundle bundle, String resourceBase,
        Class entryPanelClass, String helpId, Object extraData) {
		super(model);
		
		this.resourceBundle = bundle;
		this.resourceBase = resourceBase;
		this.dialogTitle = bundle.getString("TITLE_" + resourceBase);		// NOI18N
		this.panelHeading = bundle.getString("HEADING_" + resourceBase);	// NOI18N
		this.entryPanelClass = entryPanelClass;
		this.extraData = extraData;
		this.entryPanelHelpId = helpId;
		 
		table.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_TABLE_" + resourceBase));	// NOI18N
		table.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TABLE_" + resourceBase));	// NOI18N
		getAccessibleContext().setAccessibleName(bundle.getString("ACSN_TABLE_" + resourceBase));	// NOI18N
		getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TABLE_" + resourceBase));	// NOI18N
		
		initUserComponents();
	}
	
	protected void initUserComponents() {
		jLblTableHeading = new JLabel(panelHeading);
		jLblTableHeading.setLabelFor(table);
		add(jLblTableHeading, java.awt.BorderLayout.NORTH);
	}
	
	public void setHeadingMnemonic(char mnemonic) {
		jLblTableHeading.setDisplayedMnemonic(mnemonic);
	}
	
	public void setModel(CommonDDBean parent, ASDDVersion asVersion) {
		getTableModel().setData(parent, asVersion);
		
/* !PW tinkering with table colunn widths... hasn't really worked well though.
		JTableHeader header = table.getTableHeader();
		TableColumnModel columnModel = header.getColumnModel();
		
		int width = columnModel.getTotalColumnWidth();
		int [] columnWidths = new int [entryList.size()];
		columnWidths[0] = columnWidths[1] = width/5;
		columnWidths[2] = width - (columnWidths[0] + columnWidths[1]);
		
		for(int i = 0; i < entryList.size() && i < columnModel.getColumnCount(); i++) {
			TableColumn column = columnModel.getColumn(i);
			column.setPreferredWidth(columnWidths[i]);
		}
 */
	}
	
	public void setModel(List rows, ASDDVersion asVersion) {
		getTableModel().setData(rows, asVersion);
	}
    
	public void setModel(CommonDDBean [] rows, ASDDVersion asVersion) {
		getTableModel().setData(rows, asVersion);
	}
    
	
	public void setModelBaseBean(CommonDDBean parent, ASDDVersion asVersion) {
		getTableModel().setDataBaseBean(parent, asVersion);
	}
	
	public BeanInputDialog getInputDialog(Object[] values) {
		// Called during EDIT operation
		entryList = getTableModel().getPropertyDefinitions();
		return new GenericTableDialog(this, dialogTitle, values);
	}

	public BeanInputDialog getInputDialog() {
		// Called during ADD operation
		entryList = getTableModel().getPropertyDefinitions();
		return new GenericTableDialog(this, dialogTitle);
	}

	private GenericTableModel getTableModel() {
		return (GenericTableModel) model;
	}
	
	private String getHelpId() {
		return entryPanelHelpId;
	}
	
	/** GenericTableDialog nested class.  This class definition cannot be static
	 *  or an outer classes, as it relies on a bound inner->outer class reference
	 *  to access the entryList and entryPanelClass objects in the outer class.
	 *  In particular, it accesses these fields during execution of the base
	 *  class constructor, so they are not even viable constructor parameters.
	 */
	public class GenericTableDialog extends BeanInputDialog {
		private GenericTablePanel parentPanel;
		private GenericTableDialogPanelAccessor entryPanel;

		public GenericTableDialog(GenericTablePanel parent, String title){
			super(parent, title, true);
			parentPanel = parent;
			initAccessibility();
		}

		public GenericTableDialog(GenericTablePanel parent, String title, Object[] values) {
			super(parent, title, true, values);
			parentPanel = parent;
			initAccessibility();
		}
		
		protected void initAccessibility() {
			getAccessibleContext().setAccessibleName(
				resourceBundle.getString("ACSN_POPUP_" + resourceBase));	// NOI18N
			getAccessibleContext().setAccessibleDescription(
				resourceBundle.getString("ACSD_POPUP_" + resourceBase));	// NOI18N
		}

		protected JPanel getDialogPanel(Object[] values) {
			// Called during EDIT operation
			//   Create panel
			//   Initialize all the components in the panel
			//   Provide handlers for all the components
			entryPanel = internalGetDialogPanel();
			entryPanel.setValues(values);
			return (JPanel) entryPanel;
		}

		protected JPanel getDialogPanel() {
			// Called during ADD operation
			//   Create panel
			//   Initialize all the components in the panel
			//   Provide handlers for all the components
			entryPanel = internalGetDialogPanel();
			entryPanel.setValues(null);
			return (JPanel) entryPanel;
		}
		
		private GenericTableDialogPanelAccessor internalGetDialogPanel() {
			GenericTableDialogPanelAccessor subPanel = null;
			
			try {
				subPanel = (GenericTableDialogPanelAccessor) entryPanelClass.getDeclaredConstructor().newInstance();
				subPanel.init(getTableModel().getAppServerVersion(), 
                        GenericTablePanel.this.getWidth()*3/4, entryList, extraData);
				
				((JPanel) subPanel).getAccessibleContext().setAccessibleName(
					resourceBundle.getString("ACSN_POPUP_" + resourceBase));	// NOI18N
				((JPanel) subPanel).getAccessibleContext().setAccessibleDescription(
					resourceBundle.getString("ACSD_POPUP_" + resourceBase));	// NOI18N
			} catch(ReflectiveOperationException ex) {
				// !PW Should never happen, but it's fatal for field editing if 
				// it does so what should exception should we throw?
			}
			
			return subPanel;
		}

		protected Object[] getValues() {
			return entryPanel.getValues();
		}

		protected Collection getErrors() {
			return entryPanel.getErrors(validationSupport);
		}
		
		protected String getHelpId() {
			return GenericTablePanel.this.getHelpId();
		}
	}
}
