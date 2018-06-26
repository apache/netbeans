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
 * ContextDisplay.java
 *
 *
 * Created: Wed Jan 16 14:44:11 PST 2002
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.table.*;     // widgets
import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.*;


public class ContextDisplay extends DataDisplay {

    private final static boolean debug = false;

    DisplayTable contextTable;

    public ContextDisplay() {
	super();
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	//boolean iplanet = false;
	boolean servlet = true;
	boolean failed = false; 
	
	if(debug) System.out.println("in ContextDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;
	
	ContextData cd = md.getContextData();
	if(cd != null) displayContextData(cd);
	else {
	    ServletData sd = md.getServletData();
	    displayServletData(sd);
	    
	}
    }
    
    private void displayContextData(ContextData cd) 
    {
	if(debug) System.out.println(cd.dumpBeanNode());

	String[] props = { 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Context_name"),
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Absolute_path"),
	};	
    
	contextTable = new DisplayTable(props);

	int gridy = -1;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	// context data 
	addGridBagComponent(this, 
			    createHeaderLabel
			    (NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_context"), NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextA11yDesc"), contextTable), 
			    0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	
	contextTable.setValueAt(cd.getAttributeValue("contextName"), 0, 1);  //NOI18N
	contextTable.setValueAt(cd.getAttributeValue("absPath"), 1, 1);  //NOI18N
        contextTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextTableA11yName"));
        contextTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextTableA11yDesc"));
	addGridBagComponent(this, contextTable, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);
	
	
	Param[] ctxtparam = null;
	try {
	    ctxtparam = cd.getContextAttributes().getParam();
	}
	catch(Exception ex) {
	}

	if(ctxtparam != null && ctxtparam.length > 0) {

	    DisplayTable dt = new DisplayTable(ctxtparam);
	    addGridBagComponent(this,
				createHeaderLabel
				(NbBundle.getBundle(ClientDisplay.class).getString("MON_Context_att"), 
				 NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Context_att_A11yDesc"),
				 dt),
				0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    dt.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Context_att_TableA11yName"));
	    dt.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Context_att_TableA11yDesc"));
	    addGridBagComponent(this, dt, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);	
	}

	// Init parameters
	Param[] param = cd.getParam();
	if(param == null || param.length == 0) 
	    addGridBagComponent(this, 
				createDataLabel
				(NbBundle.getBundle(ClientDisplay.class).getString("MON_No_init")),
				0, 
				++gridy, 
				fullGridWidth, 
				1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	else {

	    DisplayTable paramTable = new DisplayTable(param); 
	    paramTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Init_parametersTableA11yName"));
	    paramTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Init_parametersTableA11yDesc"));
	    addGridBagComponent(this, 
				createHeaderLabel
				(NbBundle.getBundle(ClientDisplay.class).getString("MON_Init_parameters"),
				 NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Init_parametersA11yDesc"),
				 paramTable),
				0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    addGridBagComponent(this, paramTable, 0, ++gridy,
				fullGridWidth, 1, tableWeightX,
				tableWeightY,  
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
    }

    // This method is for backwards compatibility only, it will
    // display context data recorded with FFJ 3.0
    private void displayServletData(ServletData sd) 
    {
	if(debug) System.out.println(sd.dumpBeanNode());

	String[] props = { 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Context_name"),
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Absolute_path"),
	};	

	contextTable = new DisplayTable(props);
	
	int gridy = -1;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	// context data 
	addGridBagComponent(this, 
			    createHeaderLabel
			    (NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_context"), NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextA11yDesc"), contextTable), 
			    0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	
	contextTable.setValueAt(sd.getAttributeValue("contextName"), 0, 1);  //NOI18N
	contextTable.setValueAt(sd.getAttributeValue("absPath"), 1, 1);  //NOI18N
        contextTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextTableA11yName"));
        contextTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextTableA11yDesc"));
	addGridBagComponent(this, contextTable, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);
	
	addGridBagComponent(this, createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    zeroInsets,
			    0, 0);
    }
} // ContextDisplay
