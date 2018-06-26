/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/**
 * EditPanelCookies.java
 *
 *
 * Created: Fri Feb 9 2001
 *
 * @author Ana von Klopp
 * @version
 */

/**
 * Contains the Cookie sub-panel for the EditPanel
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.web.monitor.data.*;
import org.openide.awt.Mnemonics;

class EditPanelCookies extends DataDisplay {

    private final static boolean debug = false;
    
    private DisplayTable cookieTable = null;    
    private MonitorData monitorData = null;
    private boolean setCookies = false;

    //
    // Widgets
    //
    JButton newCookieB;
    JButton editCookieB;
    JButton deleteCookieB;
    
    EditPanelCookies() {
	super();
    }

    //
    // Redesign this, inefficient. 
    //
    void redisplayData() {
	setData(monitorData);
	this.revalidate(); 
	this.repaint(); 
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    void setData(MonitorData md) {

	this.monitorData = md;
	
	setCookieTable();
	 
	this.removeAll();
	
	// Cookies
	String msg = NbBundle.getBundle(EditPanelCookies.class).getString("MON_Cookies_4"); 
	 
	int gridy = -1;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, createSortButtonLabel(msg, cookieTable, NbBundle.getBundle(EditPanelCookies.class).getString("ACS_MON_CookiesA11yDesc")), 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	JScrollPane scrollpane = new JScrollPane(cookieTable);
	addGridBagComponent(this, scrollpane, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    //java.awt.GridBagConstraints.HORIZONTAL, 
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	newCookieB = new JButton();
        Mnemonics.setLocalizedText(newCookieB, NbBundle.getBundle(EditPanel.class).getString("MON_New_cookie"));
        newCookieB.setToolTipText(NbBundle.getBundle(EditPanelCookies.class).getString("ACS_MON_New_cookieA11yDesc"));
	newCookieB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ParamEditor pe = new ParamEditor("", "", //NOI18N
						     ParamEditor.Editable.BOTH,
						     ParamEditor.Condition.COOKIE, 
						     newCookieB.getText());

		    if(debug) log(" Now showing dialog");// NOI18N
		    
		    pe.showDialog();

		    if(debug) log(" Dialog closed"); // NOI18N

		    if (pe.getDialogOK()) {

			if(debug) log(" Dialog returned OK"); // NOI18N
			String name = pe.getName();
			String value = pe.getValue();
			if(debug) log(name + " " + value); //NOI18N
			monitorData.getRequestData().addCookie(name,value);
			redisplayData();
		    }
		}});

	deleteCookieB = new JButton();
        Mnemonics.setLocalizedText(deleteCookieB, NbBundle.getBundle(EditPanel.class).getString("MON_Delete_cookie"));
        deleteCookieB.setToolTipText(NbBundle.getBundle(EditPanelCookies.class).getString("ACS_MON_Delete_cookieA11yDesc"));

	deleteCookieB.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

		    int numRows = cookieTable.getRowCount();
		    StringBuffer buf = new StringBuffer
			(NbBundle.getBundle(EditPanelCookies.class).getString("MON_Confirm_Delete_Cookies")); 
		    buf.append("\n"); // NOI18N

		    for(int i=0; i<numRows; ++i) {

			if(cookieTable.isRowSelected(i)) {
			    buf.append(cookieTable.getValueAt(i, 0));
			    buf.append(" ");  // NOI18N
			    buf.append(cookieTable.getValueAt(i, 1));
			    buf.append("\n"); // NOI18N
			}
		    }

		    showConfirmDialog(buf.toString()); 
		    if(setCookies) {
			
			for(int i=0; i<numRows; ++i) {
			    if(cookieTable.isRowSelected(i)) {

				if(debug) log(" deleting cookie " + //NOI18N
					      String.valueOf(i));
		
				String name =
				    (String)cookieTable.getValueAt(i, 0); 
				String value =
				    (String)cookieTable.getValueAt(i,
								   1);

				if(debug) log(name + ":" + value); //NOI18N
				monitorData.getRequestData().deleteCookie(name, value);
			    }
			}
			redisplayData();
		    }
		}});
	
	int gridx = -1;
	addGridBagComponent(this, createGlue(), ++gridx, ++gridy,
			    1, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	addGridBagComponent(this, newCookieB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	addGridBagComponent(this, deleteCookieB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	setEnablings();
	
	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }
    
    void showConfirmDialog(String msg) {

	Object[] options = { NotifyDescriptor.OK_OPTION, 
			   NotifyDescriptor.CANCEL_OPTION 
	};
	
	NotifyDescriptor confirmDialog = 
	    new NotifyDescriptor((Object)msg, 
				 NbBundle.getBundle(EditPanelCookies.class).getString("MON_Confirmation_Required"),
				 NotifyDescriptor.OK_CANCEL_OPTION,
				 NotifyDescriptor.QUESTION_MESSAGE, 
				 options,
				 NotifyDescriptor.CANCEL_OPTION);

	DialogDisplayer.getDefault().notify(confirmDialog);
	if(confirmDialog.getValue().equals(NotifyDescriptor.OK_OPTION)) 
	    setCookies = true;
	else 
	    setCookies = false;
    }


    void showErrorDialog() {

	Object[] options = { NotifyDescriptor.OK_OPTION };
	
	NotifyDescriptor errorDialog = 
	    new NotifyDescriptor((Object)NbBundle.getBundle(EditPanelCookies.class).getString("MON_Bad_cookie"),
				 NbBundle.getBundle(EditPanelCookies.class).getString("MON_Invalid_input"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE, 
				 options,
				 NotifyDescriptor.OK_OPTION);

	DialogDisplayer.getDefault().notify(errorDialog);
    }

     
    void setEnablings() {
	// Always enable the Add button.
	newCookieB.setEnabled(true);

	// The delete row button is enabled if any rows are selected.
	int selectedRows[] = cookieTable.getSelectedRows();
	deleteCookieB.setEnabled(selectedRows.length > 0);
    }

    void setCookieTable() {

	Param[] params = monitorData.getRequestData().getCookiesAsParams(); 
	cookieTable = new DisplayTable(params, DisplayTable.COOKIES, true);

        cookieTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(EditPanelCookies.class).getString("ACS_MON_CookiesTableA11yName"));
        cookieTable.setToolTipText(NbBundle.getBundle(EditPanelCookies.class).getString("ACS_MON_CookiesTableA11yDesc"));

	ListSelectionModel selma = cookieTable.getSelectionModel();
	selma.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent evt) {
		if(debug) log(" list selection event"); // NOI18N
		setEnablings();
	    }
	});

	cookieTable.addTableModelListener(new TableModelListener() {
	    public void tableChanged(TableModelEvent evt) {
		if(debug) log(" table model changed"); //NOI18N
		updateCookieHeader();
	    }
	});
    }


    void updateCookieHeader() { 

	if(debug) log("updateCookieHeader()"); //NOI18N
	int numRows = cookieTable.getRowCount(); 
	if(debug) log("Number of rows is: " + // NOI18N
		      String.valueOf(numRows));
	if(numRows == 0) { 
	    monitorData.getRequestData().setCookieHeader(""); //NOI18N
	    return; 
	}
	StringBuffer buf = new StringBuffer(); 
	for(int i=0; i<numRows; ++i) { 
	    if(i>0) buf.append(";"); //NOI18N
	    buf.append(cookieTable.getValueAt(i,0));
	    buf.append("="); //NOI18N
	    buf.append(cookieTable.getValueAt(i,1));
	}
	monitorData.getRequestData().setCookieHeader(buf.toString());
	if(debug) log(" new cookie string is " + buf.toString()); //NOI18N
    }


    public void repaint() {
	super.repaint();
	//if (editPanel != null) 
	//editPanel.repaint();
    }

    void log(String s) {
	System.out.println("EditPanelCookies::" + s);//NOI18N
    }

} // EditPanelCookies


