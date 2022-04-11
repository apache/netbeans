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
import javax.swing.border.*;

class SubcategoryOptionUI extends OptionUI {

    OptionUI[] optionUIs;
    String title;
    
    public SubcategoryOptionUI(OptionUI[] optionUIs, String title) {
	super(null, false);
	this.optionUIs = optionUIs;
	this.title = title;
    }

    /**returns the value displayed on the UI
     */
    @Override
    protected String getValueFromUI() {
	return null;
    }

    /** update UI with currValue
     */
    @Override
    protected void updateUI(){
    }


    /**
     * Bind this UI to the associated Value in 'options'.
     */

    // override OptionUI
    @Override
    void bind(OptionSet options) {
	for (int uix = 0; uix < optionUIs.length; uix++) {
	    OptionUI ui = optionUIs[uix];
	    ui.bind(options);
	}
    }

    // override OptionUI
    @Override
    public void applyChanges() {
	for (int uix = 0; uix < optionUIs.length; uix++) {
	    OptionUI ui = optionUIs[uix];
	    ui.applyChanges();
	}
    }

    /** Add this option into the given panel */
    @Override
    public void addTo(JPanel parent) {

	JPanel panel = new JPanel();
	panel.setLayout(new GridBagLayout());
	panel.setBorder(new TitledBorder(title));
	panel.getAccessibleContext()
		.setAccessibleDescription(title);

	for(int i =0; i< optionUIs.length; i++ ) {
	    OptionUI o = optionUIs[i];
	    o.addTo(panel);
	}

	GridBagConstraints gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
	gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);

	parent.add(panel, gridBagConstraints);
    }
}
