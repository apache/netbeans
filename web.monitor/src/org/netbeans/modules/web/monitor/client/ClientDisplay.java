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
 * ClientDisplay.java
 *
 *
 * Created: Wed Jan 31 18:04:22 2001
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.table.*;     // widgets
import org.netbeans.modules.web.monitor.data.*;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.util.NbBundle;
import java.util.*;

public class ClientDisplay extends DataDisplay {

    private final static boolean debug = false;
    private DisplayTable clientTable = null;
    private DisplayTable engineTable = null;

    private String[] categories = { 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Protocol"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Remote_Address"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Software"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Locale"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Encodings"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Fileformats"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Charsets")
	};

    private static final String[] props = { 
	NbBundle.getBundle(ClientDisplay.class).getString("MON_Java_version"),
	NbBundle.getBundle(ClientDisplay.class).getString("MON_Platform"),
	NbBundle.getBundle(ClientDisplay.class).getString("MON_Server_name"),
	NbBundle.getBundle(ClientDisplay.class).getString("MON_Server_port"),

    };

    public ClientDisplay() {
	super();
    }

    private void createPanelWidgets() {

	int gridy = -1;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	clientTable = new DisplayTable(categories);
        clientTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_ClientTable_3A11yName"));
        clientTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_ClientTable_3A11yDesc"));
	addGridBagComponent(this, 
			    createHeaderLabel
			    (NbBundle.getBundle(ClientDisplay.class).getString("MON_Client_3"), 
			     NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Client_3A11yDesc"), 
			     clientTable),
                            0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	addGridBagComponent(this, clientTable, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	engineTable = new DisplayTable(props);
        engineTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_engineTableA11yName"));
        engineTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_engineTableA11yDesc"));
	addGridBagComponent(this, 
			    createHeaderLabel
			    (NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_engine"), 
			     NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_engineA11yDesc"),
			     engineTable),
                            0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);


	addGridBagComponent(this, engineTable, 0, ++gridy,
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

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	if(debug) System.out.println("in ClientDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;

	createPanelWidgets();

	ClientData cd = md.getClientData();
	clientTable.setValueAt(cd.getAttributeValue("protocol"), 0,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("remoteAddress"),1,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("software"), 2,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("locale"), 3,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("encodingsAccepted"), 4,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("formatsAccepted"), 5,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("charsetsAccepted"), 6,1); // NOI18N

	EngineData ed = md.getEngineData();
	if(ed != null) {
	    engineTable.setValueAt(ed.getAttributeValue("jre"), 0, 1); //NOI18N
	    engineTable.setValueAt(ed.getAttributeValue("platform"), 1, 1); //NOI18N
	    engineTable.setValueAt(ed.getAttributeValue("serverName"), 2, 1); //NOI18N
	    engineTable.setValueAt(ed.getAttributeValue("serverPort"), 3, 1); //NOI18N
	}
	// This is only for backwards compatibility with data
	// collected under FFJ 3.0
	else {
	    ServletData sd = md.getServletData();
	    engineTable.setValueAt(sd.getAttributeValue("jre"), 0, 1); //NOI18N
	    engineTable.setValueAt(sd.getAttributeValue("platform"), 1, 1); //NOI18N
	    engineTable.setValueAt(sd.getAttributeValue("serverName"), 2, 1); //NOI18N
	    engineTable.setValueAt(sd.getAttributeValue("serverPort"), 3, 1); //NOI18N
	}
    }

} // ClientDisplay
