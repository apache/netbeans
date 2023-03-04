/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

public abstract class DataDisplay extends JPanel {
    
    //
    // Common Insets
    // Insets(top, left, bottom, right)
    static final Insets zeroInsets =       new Insets( 0,  0,  0,  0);
    static final Insets tableInsets =      new Insets( 0, 18, 12, 12);
    static final Insets labelInsets =      new Insets( 0,  6,  0,  0);
    static final Insets buttonInsets =     new Insets( 6,  0,  5,  6);
    static final Insets sortButtonInsets = new Insets( 0, 12,  0,  0);
    static final Insets indentInsets =     new Insets( 0, 18,  0,  0);
    static final Insets topSpacerInsets =  new Insets(12,  0,  0,  0);

    static final int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;
    static final double tableWeightX = 1.0;
    static final double tableWeightY = 0;

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
