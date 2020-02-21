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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
