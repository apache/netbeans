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
 * RequestDisplay.java
 *
 *
 * Created: Wed Jan 31 18:04:22 2001
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.table.*;     // widgets
import javax.swing.JLabel;
import javax.swing.JTextArea;
import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.ResourceBundle;
import java.awt.Component;

// Can this go into displayTable?
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class RequestDisplay extends DataDisplay {
    
    private final static boolean debug = false;
    
    private DisplayTable dt = null; 
    DisplayTable paramTable = null;
        
    public RequestDisplay() {

	super();
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	if(debug) System.out.println("in RequestDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;
	
	String[] requestCategories = { 
	    NbBundle.getBundle(RequestDisplay.class).getString("MON_Request_URI"),
	    NbBundle.getBundle(RequestDisplay.class).getString("MON_Method"),
	    NbBundle.getBundle(RequestDisplay.class).getString("MON_Querystring"),
	    NbBundle.getBundle(RequestDisplay.class).getString("MON_Protocol"),
	    NbBundle.getBundle(RequestDisplay.class).getString("MON_Remote_Address"),
	    NbBundle.getBundle(RequestDisplay.class).getString("MON_Scheme"),
	    NbBundle.getBundle(RequestDisplay.class).getString("MON_Status"),
	};

	RequestData rd = md.getRequestData();
	dt = new DisplayTable(requestCategories);
	dt.setValueAt(rd.getAttributeValue("uri"), 0,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("method"),1,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("queryString"), 2,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("protocol"), 3,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("ipaddress"), 4,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("scheme"), 5,1); //NOI18N
	dt.setValueAt(rd.getAttributeValue("status"), 6,1); //NOI18N
        dt.getAccessibleContext().setAccessibleName(NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_RequestTable_19A11yName")); 
        dt.setToolTipText(NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_RequestTable_19A11yDesc"));

	int gridy = -1;
	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

        JLabel requestHeaderLabel = createHeaderLabel(NbBundle.getBundle(RequestDisplay.class).getString("MON_Request_19"), NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_Request_19A11yDesc"), dt);
	addGridBagComponent(this, requestHeaderLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);


	String msg;
	
	// add the parameters

	Param[] params2 = rd.getParam();
	String msg2 = ""; //NOI18N
	Component queryDataLabel = null;
	boolean bad = false;
	
	if(params2 == null || params2.length == 0) {
	    if("POST".equals(rd.getAttributeValue("method"))) { //NOI18N

		String type = rd.getAttributeValue("urlencoded"); //NOI18N
		
		if(type != null) {

		    if (type.equals("false")) { //NOI18N
			msg2 = NbBundle.getBundle(RequestDisplay.class).getString("MON_Unparameterized");
		    }
		    else if (type.equals("bad")) { //NOI18N
			msg2 = NbBundle.getBundle(RequestDisplay.class).getString("MON_Warning_param"); 
			queryDataLabel =
			    createHeaderLabel(msg2); 
			bad = true;
		    }
		    else msg2 = NbBundle.getBundle(RequestDisplay.class).getString("MON_No_posted_data");
		}
		else msg2 = NbBundle.getBundle(RequestDisplay.class).getString("MON_No_posted_data");
	    } else {
		msg2 = NbBundle.getBundle(RequestDisplay.class).getString("MON_No_querystring");
	    }
	    if(queryDataLabel == null) 
		queryDataLabel = createDataLabel(msg2);
	    
	} else {
	    msg2 = NbBundle.getBundle(RequestDisplay.class).getString("MON_Parameters");
	    paramTable = new DisplayTable(params2, true);
	    paramTable.addTableModelListener(new TableModelListener() {
		public void tableChanged(TableModelEvent evt) {
		    paintTable();
		}});
	    

            paramTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_ParametersTableA11yName"));
            paramTable.setToolTipText(NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_ParametersTableA11yDesc"));
	    queryDataLabel = createSortButtonLabel(msg2, paramTable, NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_ParametersA11yDesc"));
	}
	
	addGridBagComponent(this, queryDataLabel, 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	if (params2 != null && params2.length > 0) {
	    addGridBagComponent(this, paramTable, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);
	}
	else if(bad) {
	    JTextArea ta = new JTextArea(NbBundle.getBundle(RequestDisplay.class).getString("MON_Unparameterized_bad"));
	    ta.setEditable(false);
	    ta.setLineWrap(true);
	    ta.setBackground(this.getBackground());
	    addGridBagComponent(this, ta, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);
			
	}
	
	this.add(createRigidArea()); 

	Param[] param = null;
	try {
	    param = rd.getRequestAttributesIn().getParam();
	}
	catch(Exception ex) {
	}

	if(param != null && param.length > 0) {

	    dt = new DisplayTable(param);
	    JLabel requestAttrBeforeLabel =
		createHeaderLabel(NbBundle.getBundle(RequestDisplay.class).getString("MON_Request_att_before"), NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_Request_att_beforeA11yDesc"), dt); 
	    addGridBagComponent(this, requestAttrBeforeLabel, 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    dt.getAccessibleContext().setAccessibleName(NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_Request_att_beforeTableA11yName"));
	    dt.setToolTipText(NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_Request_att_beforeTableA11yDesc"));
	    addGridBagComponent(this, dt, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);	
	    this.add(createRigidArea()); 
	}
    
	param = null;
	try {
	    param = rd.getRequestAttributesOut().getParam();
	}
	catch(Exception ex) {
	}

	if(param != null && param.length > 0) {

	    dt = new DisplayTable(param);
	    JLabel requestAttrAfterLabel =
		createHeaderLabel(NbBundle.getBundle(RequestDisplay.class).getString("MON_Request_att_after"),NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_Request_att_afterA11yDesc"),dt); 
	    addGridBagComponent(this, requestAttrAfterLabel, 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    dt.getAccessibleContext().setAccessibleName(NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_Request_att_afterTableA11yName"));
	    dt.setToolTipText(NbBundle.getBundle(RequestDisplay.class).getString("ACS_MON_Request_att_afterTableA11yDesc"));
	    addGridBagComponent(this, dt, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);	
	}
    
	addGridBagComponent(this, createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    zeroInsets,
			    0, 0);



	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }

    void paintTable() 
    {
	paramTable.repaint();
    }
    

} // RequestDisplay
