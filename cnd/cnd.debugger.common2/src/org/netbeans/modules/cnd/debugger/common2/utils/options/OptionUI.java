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

/**
 * This class is the base class for each different GUI representing an option.
 */

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.awt.*;
import javax.swing.*;

public abstract class OptionUI {
    protected Option optionType = null;

    protected OptionValue value = null;

    protected JLabel label = null;		// contains the label string
    protected boolean hasLabel = true;

    // max columns in the label
    protected static final int LABEL_COLUMNS_MAX = 25;

    // min columns in the label used for subOptions
    protected static final int LABEL_COLUMNS_MIN = 10;

    String currValue = null;		// the current value for the option

    
    OptionUI(Option inOption) {
	this(inOption,true, LABEL_COLUMNS_MAX);
    }

    OptionUI(Option inOption, boolean hasLabel) {
	this(inOption, hasLabel, LABEL_COLUMNS_MAX);
    }

    OptionUI(Option inOption, int labelColumns) {
	this(inOption,true, labelColumns);
    }

    OptionUI(Option inOption, boolean hasLabel, int labelColumns) {
	optionType = inOption;
	if (hasLabel)
	    hasLabel = inOption.overrideHasLabel();
	this.hasLabel = hasLabel;
    }

    /**
     * Bind this UI to the associated Value in 'options'.
     */

    void bind(OptionSet options) {
	value = options.byType(optionType);
	if (value != null) {
	    currValue = value.get();
	    updateUI();
	}
	/* LATER 
	else {
	    setEnabled(false);
	}
	*/
    }

    /** creates the label widget and sets the properties
	wrapping the words are enabled
	this function wrapps the words at the character number
	which is specified by param labelColumns
	returns the created widget
	LABEL_COLUMNS_MIN for labelColumns indicates
	that this is a lbel for subOption
    */
    public JLabel createLabel() {
	JLabel l = new JLabel();
        // OLD IpeUtils.setLabelText(l, optionType.getLabel(), true); // NOI18N
	l.setText(optionType.getLabel());
	l.setDisplayedMnemonic(optionType.getMnemonic());

	if (optionType.getLabelTip() != null) {
	    l.setToolTipText(optionType.getLabelTip());
	}
	return l;
    }

    public Option getOption() {
	return optionType;
    }

    // returns the value for this option
    public String getValue() {
	return currValue;
    }

    /**
     * returns the value displayed on the UI
     */
    abstract protected String getValueFromUI();

    /**
     * update UI with currValue
     */
    abstract protected void updateUI();

    /**
     * Add this option into the given panel
     */
    public abstract void addTo(JPanel parent);



    /** This functions resets the newly entered value for this option
	to the last known value of the option
    */
    public void cancelChanges() {
	String prevValue = currValue;
	currValue = getValueFromUI();

	if (currValue.equals(prevValue)) //no change happened
	    return;

	setValue(prevValue);
    }


    /**
     * Transfer the values stored in the UI to the corresponding OptionValue
     * and possibly forward to the engine.
     */

    public void applyChanges() {
	String prevValue = currValue;
	currValue = getValueFromUI();

	if (currValue.equals(prevValue)) //no change happened
	    return;

	if (value == null) {
	    System.out.println("OptionUI: No value bound for " + optionType.getName()); // NOI18N
	} else {
	    value.set(currValue);
	}
    }

    /**
     * set the value to the new value
     * update UI accordingly
     */

    protected void setValue(String newValue){
	currValue =  newValue;
	updateUI();
    }

    /**
     * creates and returns a subpanel
     * with the specified bordertype and layout
     * this subpanel holds the given options
     */

     public static OptionUI createSubPanel(OptionUI[] optionUIS,
				    CatalogDynamic catalog,
				    String titleResource ) {

	// OLD String title = Catalog.get("SPT_" + titleResource); // NOI18N
	String title = catalog.get("SPT_" + titleResource); // NOI18N
	return new SubcategoryOptionUI(optionUIS, title);
    }

    public static void fillPanel(JPanel panel, OptionUI[] optionPanels) {
	panel.setLayout(new GridBagLayout());
	// ?? panel.setBorder(BorderFactory.createEtchedBorder());

	for (int i = 0; i < optionPanels.length; i++ ) {
	    optionPanels[i].addTo(panel);
	}

	// Add dummy
	JPanel yAbsorber = new JPanel();
	// Make the dialog a bit taller
	yAbsorber.setPreferredSize(new Dimension(100,100));
	
	GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
	gridBagConstraints.weighty = 1.0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        panel.add(yAbsorber, gridBagConstraints);
    }

}
