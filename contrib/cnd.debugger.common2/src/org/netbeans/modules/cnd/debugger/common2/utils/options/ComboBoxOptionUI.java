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
 * This class represents an option containing a label and a combo
 * Box where the alternative values are displayed.
 */

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.awt.*;
import javax.swing.*;

class ComboBoxOptionUI extends OptionUI {

    JComboBox comboBox = new JComboBox(); // holds the alternative values

    public ComboBoxOptionUI(Option inOption) {
	super(inOption);
    }

    /** creates the combo box GUI in this option
     */
    protected JPanel createComboBox() {
	JPanel comboBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	String[] labels = optionType.getValueLabels();
	String[] values = optionType.getValues();
	for (int i = 0; i < labels.length; i++) {
	    comboBox.addItem(labels[i]);

	    //if (currValue.equals(values[i]))
	    //  comboBox.setSelectedItem(labels[i]); 
	}
	comboBox.getAccessibleContext()
		.setAccessibleDescription(optionType.getOptionDescription());
	updateUI();
	comboBoxPanel.add(comboBox);
	//mainPanel.add(comboBoxPanel, BorderLayout.CENTER);
	return comboBoxPanel;
    }
    
    /**returns the value displayed on the UI
     */
    @Override
    protected String getValueFromUI() {
	return optionType.getValue((String) comboBox.getSelectedItem());
    }


    /** update UI with currValue
     */
    @Override
    protected void updateUI(){
	if (currValue == null)
	    return;
	comboBox.setSelectedItem(optionType.getValueLabel(currValue));
    }

    /** Add this option into the given panel */
    @Override
    public void addTo(JPanel parent) {
	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	if (hasLabel) {
	    gridBagConstraints.gridwidth = 1;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);

	    label = createLabel();
	    parent.add(label, gridBagConstraints);

	    gridBagConstraints = new GridBagConstraints();
	}

	gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);

	JPanel cp = createComboBox();
	parent.add(cp, gridBagConstraints);

	if (hasLabel) {
	    label.setLabelFor(comboBox);
	}
    }
}
