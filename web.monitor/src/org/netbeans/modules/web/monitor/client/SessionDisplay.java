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
 * SessionDisplay.java
 *
 *
 * Created: Wed Jan 31 18:04:22 2001
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.JLabel;
import javax.swing.table.*;     // widgets
import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.*;


public class SessionDisplay extends DataDisplay {

    private final static boolean debug = false;

    public SessionDisplay() {
	super();
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	if(debug) System.out.println("in SessionDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;
	 
	SessionData sd = md.getSessionData();
	int gridy=-1;	

	if(sd == null ||
	   ("false".equals(sd.getAttributeValue("before")) &&  //NOI18N
	    "false".equals(sd.getAttributeValue("after")))) {  //NOI18N
	    


	    addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				topSpacerInsets,
				0, 0);

	    addGridBagComponent(this, 
				createDataLabel
				(NbBundle.getBundle(SessionDisplay.class).getString("MON_No_session")),
				0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);

	    addGridBagComponent(this, createGlue(), 0, ++gridy,
				1, 1, 1.0, 1.0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				zeroInsets,
				0, 0);
	    return;
	}

	String headerIn;
	String lastAccessed = null;
	String maxInactiveBefore = null;
	String maxInactiveAfter = null;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	// We need to refer to this label below, don't use the other
	// createHeader method.
        JLabel sessionHeaderLabel =
	    createHeaderLabel(NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_24"), NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_24A11yDesc"),null);
	
	addGridBagComponent(this, 
			    sessionHeaderLabel, 
			    0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);
	
	String msg;
	if("true".equals(sd.getAttributeValue("before"))) {  //NOI18N
	    msg = NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_existed");
	    lastAccessed = 
		sd.getSessionIn().getAttributeValue("lastAccessed");  //NOI18N
	    maxInactiveBefore =
		sd.getSessionIn().getAttributeValue("inactiveInterval");  //NOI18N
	}
	else {
	    msg = NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_created");
	    lastAccessed = 
		sd.getSessionOut().getAttributeValue("lastAccessed"); //NOI18N
	}
	
        JLabel sessionDataLabel = createHeaderLabel(msg, msg, null);
        sessionHeaderLabel.setLabelFor(sessionDataLabel);
	addGridBagComponent(this, sessionDataLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    indentInsets,
			    0, 0);


	if("false".equals(sd.getAttributeValue("after"))) {  //NOI18N
	    msg = NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_invalidated");
	    addGridBagComponent(this, createDataLabel(msg), 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				indentInsets,
				0, 0);
	}
	else {
	    maxInactiveAfter =
		sd.getSessionOut().getAttributeValue("inactiveInterval");  //NOI18N
	}
	

	boolean inactiveChanged = false;
	
	if(maxInactiveBefore == null || maxInactiveBefore.equals("")) { //NOI18N
	    if(maxInactiveAfter != null) 
		maxInactiveBefore = maxInactiveAfter;
	    // Should not happen
	    else maxInactiveBefore = ""; //NOI18N
	}
	else if(maxInactiveAfter != null && 
		!maxInactiveBefore.equals(maxInactiveAfter)) 
	    inactiveChanged = true;
    
	// Add session properties header
	DisplayTable dt = null;
       
	if(!inactiveChanged) {
	    String data[] = {
		sd.getAttributeValue("id"),  //NOI18N
		sd.getAttributeValue("created"), //NOI18N
		lastAccessed,
		maxInactiveBefore,
	    };

	    String[] props = { 
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_ID"),
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Created"),
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Last_accessed"),
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Max_inactive"),
	    };

	    dt = new DisplayTable(props, data);
	}
	else {
	    String data[] = {
		sd.getAttributeValue("id"),  //NOI18N
		sd.getAttributeValue("created"), //NOI18N
		lastAccessed,
		maxInactiveBefore,
		maxInactiveAfter,
	    };

	    String[] props2 = { 
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_ID"),
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Created"),
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Last_accessed"),
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Max_inactive_before"),
		NbBundle.getBundle(SessionDisplay.class).getString("MON_Max_inactive_after"),
	    };
	    dt = new DisplayTable(props2, data);
	}


        JLabel sessionPropertiesLabel = createHeaderLabel(NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_properties"), NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_propertiesA11yDesc"), dt);
	addGridBagComponent(this, sessionPropertiesLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

        dt.getAccessibleContext().setAccessibleName(NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_propertiesTableA11yName"));
        dt.setToolTipText(NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_propertiesTableA11yDesc"));
 	addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	// Attributes before and after 
	if("true".equals(sd.getAttributeValue("before"))) { //NOI18N
	    Param[] param = null;
	    try {
		param = sd.getSessionIn().getParam();
	    }
	    catch(Exception ex) {
	    }
	    
	    if(param == null || param.length == 0) {
		addGridBagComponent(this, createDataLabel(NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_no_att_before")), 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    tableInsets,
				    0, 0);
	    } else {
		dt = new DisplayTable(param);
                JLabel sessionBeforeLabel = createHeaderLabel(NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_att_before"), NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_att_beforeA11yDesc"),dt);
		addGridBagComponent(this, sessionBeforeLabel, 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    labelInsets,
				    0, 0);
                dt.getAccessibleContext().setAccessibleName(NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_att_beforeTableA11yName"));
                dt.setToolTipText(NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_att_beforeTableA11yDesc"));
		addGridBagComponent(this, dt, 0, ++gridy,
				    fullGridWidth, 1, tableWeightX, tableWeightY, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.BOTH,
				    tableInsets,
				    0, 0);
	    }
	}
	

	if("true".equals(sd.getAttributeValue("after"))) { //NOI18N
	    Param[] param = null;
	    try {
		param = sd.getSessionOut().getParam();
	    }
	    catch(Exception ex) {
	    }

	    if(param == null || param.length == 0) {
		addGridBagComponent(this, createDataLabel(NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_no_att_after")), 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    tableInsets,
				    0, 0);
	    } else {
		dt = new DisplayTable(param);
                JLabel sessionAfterLabel = createHeaderLabel(NbBundle.getBundle(SessionDisplay.class).getString("MON_Session_att_after"), NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_att_afterA11yDesc"), dt);
		addGridBagComponent(this, sessionAfterLabel, 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    labelInsets,
				    0, 0);
                dt.getAccessibleContext().setAccessibleName(NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_att_afterTableA11yName"));
                dt.setToolTipText(NbBundle.getBundle(SessionDisplay.class).getString("ACS_MON_Session_att_afterTableA11yDesc"));
		addGridBagComponent(this, dt, 0, ++gridy,
				    fullGridWidth, 1, tableWeightX, tableWeightY, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.BOTH,
				    tableInsets,
				    0, 0);
	    }
	    this.add(createRigidArea());
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
} // SessionDisplay
