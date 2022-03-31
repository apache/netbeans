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
 * This class represents an option containing a check box.
 */

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.awt.*;
import javax.swing.*;


class CheckBoxOptionUI extends OptionUI {

    JCheckBox   checkBox    = null;     // the cheak box holding the values

    public CheckBoxOptionUI(Option inOption) {
	super(inOption, false);
    }

 
    /**returns the value displayed on the UI
     */
    @Override
    protected String getValueFromUI() {
	return (checkBox.isSelected()) ? "on" : "off";    // NOI18N
    }


    /** update UI with currValue
     */
    @Override
    protected void updateUI(){
	if (currValue == null)
	    return;
	checkBox.setSelected(currValue.equals("on"));    // NOI18N
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
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
	gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);

	checkBox = new JCheckBox();
	checkBox.setText(optionType.getLabel()); // NOI18N
	checkBox.getAccessibleContext()
		.setAccessibleDescription(optionType.getOptionDescription());
	checkBox.setMnemonic(optionType.getMnemonic());
	
	updateUI();
	parent.add(checkBox, gridBagConstraints);

	if (hasLabel) {
	    label.setLabelFor(checkBox);
	}
    }
}
