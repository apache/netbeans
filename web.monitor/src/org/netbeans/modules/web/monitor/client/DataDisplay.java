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
 * DataDisplay.java
 *
 *
 * Created: Wed Jan 16 14:53:40 2002
 *
 * @author Ana von Klopp
 * @version
 */
package org.netbeans.modules.web.monitor.client;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.Box;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import org.netbeans.modules.web.monitor.data.DataRecord;
import org.netbeans.modules.web.monitor.data.Param; 
import org.openide.awt.Mnemonics;

abstract public class DataDisplay extends JPanel {
    
    //
    // Common Insets
    // Insets(top, left, bottom, right)
    final static Insets zeroInsets =       new Insets( 0,  0,  0,  0);
    final static Insets tableInsets =      new Insets( 0, 18, 12, 12);
    final static Insets labelInsets =      new Insets( 0,  6,  0,  0);
    final static Insets buttonInsets =     new Insets( 6,  0,  5,  6);
    final static Insets sortButtonInsets = new Insets( 0, 12,  0,  0);
    final static Insets indentInsets =     new Insets( 0, 18,  0,  0);
    final static Insets topSpacerInsets =  new Insets(12,  0,  0,  0);

    final static int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;
    final static double tableWeightX = 1.0;
    final static double tableWeightY = 0;

    public DataDisplay() {
	super();
	setLayout(new GridBagLayout());
    }
    
    //abstract public void setData(DataRecord md);
    
    void addGridBagComponent(Container parent,
			     Component comp,
			     int gridx, int gridy,
			     int gridwidth, int gridheight,
			     double weightx, double weighty,
			     int anchor, int fill,
			     Insets insets,
			     int ipadx, int ipady) {
	GridBagConstraints cons = new GridBagConstraints();
	cons.gridx = gridx;
	cons.gridy = gridy;
	cons.gridwidth = gridwidth;
	cons.gridheight = gridheight;
	cons.weightx = weightx;
	cons.weighty = weighty;
	cons.anchor = anchor;
	cons.fill = fill;
	cons.insets = insets;
	cons.ipadx = ipadx;
	cons.ipady = ipady;
	parent.add(comp,cons);
    } 


    /**
     * create a toggle-able button that changes the sort-order of a
     * DisplayTable. Showing different buttons (up & down arrow)
     * depending on the state. 
     */
    static JButton createSortButton(DisplayTable dt) {
	SortButton b = new SortButton(dt); 
	return(JButton)b;
    } 

    static Component createTopSpacer() {
	return Box.createVerticalStrut(1);
    }

    static Component createRigidArea() {
	return Box.createRigidArea(new Dimension(0,5));
    }

    static Component createGlue() {
	return Box.createGlue();
    }


    //
    // Routines for creating widgets in centralzied styles.
    //
    /**
     * create a header label that uses bold.
     */


    static JLabel createHeaderLabel(String label) {
        return createHeaderLabel(label, null, null);
    }


    static JLabel createHeaderLabel(String label, String ad, Component comp) {
	JLabel jl = new JLabel();
        Mnemonics.setLocalizedText(jl, label);
	Font labelFont = jl.getFont();
	Font boldFont = labelFont.deriveFont(Font.BOLD);
	jl.setFont(boldFont);
        if (ad != null)
            jl.getAccessibleContext().setAccessibleDescription(ad);
        if (comp != null)
            jl.setLabelFor(comp);
	return jl;
    }

    static JLabel createDataLabel(String label) {
	JLabel jl = new JLabel(label);
	return jl;
    }

    
    static Component createSortButtonLabel(String label, final DisplayTable dt, String ad) {
	JPanel panel = new JPanel();
	panel.add(createHeaderLabel(label, ad, dt));
	panel.add(createSortButton(dt));
	return panel;
    }

    void log(String s) { 
	System.out.println("DataDisplay::" + s); // NOI18N
    }


    Param findParam(Param [] myParams, String name, String value) {

	for (int i=0; i < myParams.length; i++) {
	
	    Param param = myParams[i];
	    if (name.equals(param.getName()) &&
		value.equals(param.getValue()) ) {
		return param;
	    }
	}
	return null;
    }

} // DataDisplay
