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
 * CookieDisplay.java
 *
 *
 * Created: Wed Jan 31 18:04:22 2001
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import java.util.ResourceBundle;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRelation;
import javax.swing.JLabel;

import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;

// PENDING: can be more helpful with what the cookie data means. Like
// I had the expires at the end of this session before, that was kind
// of useful. Could also show the actual date that the cookie
// expires. 

public class CookieDisplay extends DataDisplay {
    
    private final static boolean debug = false;
        
    public CookieDisplay() {
	super();
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	if(debug) System.out.println("in CookieDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;
	 
	CookiesData cd = md.getCookiesData();
	CookieIn[] in = cd.getCookieIn();
	CookieOut[] out = cd.getCookieOut();

	int gridy = -1;
	String headerIn;
	JLabel incomingLabel;
	if(in == null || in.length == 0) {
	    headerIn = NbBundle.getBundle(CookieDisplay.class).getString("MON_No_incoming");
	    incomingLabel = createDataLabel(headerIn);

	} else {
	    headerIn = NbBundle.getBundle(CookieDisplay.class).getString("MON_Incoming_cookie");
	    incomingLabel = createHeaderLabel(headerIn, NbBundle.getBundle(CookieDisplay.class).getString("ACS_MON_Incoming_cookieA11yDesc"), null);
	}

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, incomingLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.HORIZONTAL,
			    labelInsets,
			    0, 0);

	AccessibleContext aCtx;
	AccessibleRelation aRel; 
	DisplayTable dt; 

	if(in != null && in.length > 0) {
 
	    for(int i=0; i<in.length; ++i) {
		String[] data = {
		    in[i].getAttributeValue("name"), //NOI18N
		    in[i].getAttributeValue("value") //NOI18N
		};

		String[] categoriesIn = { 
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Name"),
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Value"),
		};

		dt = new DisplayTable(categoriesIn, data);
		aCtx = dt.getAccessibleContext(); 
                aCtx.setAccessibleName
		    (NbBundle.getMessage(CookieDisplay.class, 
					 "ACS_MON_Incoming_cookieTableA11yName"));//NOI18N
                aCtx.setAccessibleDescription
		    (NbBundle.getMessage(CookieDisplay.class, 
					 "ACS_MON_Incoming_cookieTableA11yDesc"));//NOI18N
                dt.setToolTipText
		    (NbBundle.getMessage(CookieDisplay.class,
					 "ACS_MON_Incoming_cookieTableA11yDesc")); //NOI18N
		aRel = new AccessibleRelation(AccessibleRelation.LABELED_BY, 
					      incomingLabel); 
		aCtx.getAccessibleRelationSet().add(aRel); 
		addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);
	    }
	}

	String headerOut;
	JLabel outgoingLabel;
	if(out == null || out.length == 0) {
	    headerOut = NbBundle.getBundle(CookieDisplay.class).getString("MON_No_outgoing");
	    outgoingLabel = createDataLabel(headerOut);
	} else {
	    headerOut = NbBundle.getBundle(CookieDisplay.class).getString("MON_Outgoing_cookie");
	    outgoingLabel = createHeaderLabel(headerOut, NbBundle.getBundle(CookieDisplay.class).getString("ACS_MON_Outgoing_cookieA11yDesc"), null);
	}
	addGridBagComponent(this, outgoingLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	if(out != null && out.length > 0) {

	    for(int i=0; i<out.length; ++i) {
		String cookieMaxAge =
		    out[i].getAttributeValue("maxAge"); //NOI18N
		if(cookieMaxAge.equals("-1")) //NOI18N
		    cookieMaxAge = NbBundle.getBundle(CookieDisplay.class).getString("MON_this_session");
		
		String[] data = {
		    out[i].getAttributeValue("name"),    //NOI18N
		    out[i].getAttributeValue("value"),   //NOI18N
		    out[i].getAttributeValue("domain"),  //NOI18N
		    out[i].getAttributeValue("path"),    //NOI18N
		    cookieMaxAge,
		    out[i].getAttributeValue("version"), //NOI18N
		    out[i].getAttributeValue("secure"),  //NOI18N
		    out[i].getAttributeValue("comment")  //NOI18N
		};

		String[] categoriesOut = { 
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Name"),
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Value"),
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Domain"),
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Path"),
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Max_age"),
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Version"),
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Secure"),
		    NbBundle.getBundle(CookieDisplay.class).getString("MON_Comment"),
		};

		dt = new DisplayTable(categoriesOut, data);
		aCtx = dt.getAccessibleContext(); 
                aCtx.setAccessibleName
		    (NbBundle.getMessage(CookieDisplay.class, 
					 "ACS_MON_Outgoing_cookieTableA11yName"));//NOI18N
                aCtx.setAccessibleDescription
		    (NbBundle.getMessage(CookieDisplay.class, 
					 "ACS_MON_Outgoing_cookieTableA11yDesc"));//NOI18N
                dt.setToolTipText
		    (NbBundle.getMessage(CookieDisplay.class,
					 "ACS_MON_Outgoing_cookieTableA11yDesc")); //NOI18N
		aRel = new AccessibleRelation(AccessibleRelation.LABELED_BY, 
					      outgoingLabel); 
		aCtx.getAccessibleRelationSet().add(aRel); 
		addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);
	    }
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
} // CookieDisplay
