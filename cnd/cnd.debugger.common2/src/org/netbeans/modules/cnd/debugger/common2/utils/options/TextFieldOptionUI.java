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
 * This class represents an option containing a label and a textField
 * where the value is displayed
 */
 
package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.awt.*;
import javax.swing.*;

class TextFieldOptionUI extends OptionUI {

    JTextField  textField  = null;     // the textField holding the value

    public static int VALUE_COLUMNS = 15;   // columns in the value text field

    public TextFieldOptionUI(Option inOption) {
	this(inOption, LABEL_COLUMNS_MAX);
    }

    public TextFieldOptionUI(Option inOption, int labelColumns) {
	super(inOption, labelColumns);
    }

    /*enables or disables the widgets fepending on the
     * value of the flag
     * mainly used for subOptions
     */
    public void setEnabled(boolean flag) {
	if (label != null) {
	    label.setEnabled(flag);
	}
	if (textField != null) {
	    textField.setEnabled(flag);
	}
    }

    /**returns the value displayed on the UI
     */
    @Override
    protected String getValueFromUI() {
	return textField.getText();
    }


    /* update UI with currValue
     */
    @Override
    protected void updateUI(){
	textField.setText(currValue);
    }

    @Override
    public void addTo(JPanel parent) {
	addTo(parent, 5);
    }

    /** Add this option into the given panel */
    public void addTo(JPanel parent, int verticalGap) {
	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	if (hasLabel) {
	    gridBagConstraints.gridwidth = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 12, verticalGap, 12);
	    label = createLabel();
	    parent.add(label, gridBagConstraints);

	    gridBagConstraints = new GridBagConstraints();
	}

	gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
	gridBagConstraints.insets = new java.awt.Insets(0, 12, verticalGap, 12);

	textField = new JTextField(currValue, VALUE_COLUMNS);
	textField.getAccessibleContext()
		.setAccessibleDescription(optionType.getOptionDescription());

	JPanel tfPanel = new JPanel();
	tfPanel.setLayout(new BorderLayout());
	tfPanel.add(textField, BorderLayout.WEST);

	parent.add(tfPanel, gridBagConstraints);

	if (hasLabel) {
	    label.setLabelFor(textField);
	}
    }
}
