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
 * This class represents an option containing a label and radio buttons
 * where the values are displayed.
 */

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;


class RadioButtonOptionUI extends OptionUI implements ItemListener {

    JRadioButton[]  buttons  ;                    // the radio buttons
    String[] buttonLabels ;                       // labels on the buttons
    String[] buttonDescrs ;                       // descriptions for the buttons
    String[] buttonValues;                        // values for each button
    ButtonGroup buttonGroup = new ButtonGroup();  // button group holding radiobuttons

    private TextFieldOptionUI subOptionUI;
 
    public static int RB_COLUMNS = 12;            // indicates the columns in the radio button label

    public RadioButtonOptionUI(Option inOption) {
	super(inOption);
	//createRadioButtons();
    }


    /**
     * Each given radio button is placed in a panel and the text for
     * the button is set the radio button is added to button group returns
     * the created panel
     */

    protected JPanel createButtonPanel(int index) {
	buttons[index] = new JRadioButton();
	if ((index == 0) && hasLabel) {
	    label.setLabelFor(buttons[index]);
	}
	
	JPanel buttonPanel = new JPanel(new BorderLayout());
	buttonPanel.add(buttons[index],BorderLayout.WEST);
	//if (buttonLabels[index].length() <= RB_COLUMNS) {
	buttons[index].setText(buttonLabels[index]);
	/*
	    Actions.setMenuText(buttons[index],
				buttonLabels[index], true); // NOI18N
	*/
	    
	    /* What is this???   I took it out and now "Dbx Window" is
	       no longer truncated (a few pixels of the last w)
	       so removing this seems like an improvement!
	}
	else   {     //if the label is too long
	    JLabel jl = new JLabel(buttonLabels[index]);
	    buttonPanel.add(jl,BorderLayout.CENTER);
	}
	    */
	    
	buttons[index].getAccessibleContext()
		.setAccessibleDescription(buttonDescrs[index]);

	buttonGroup.add(buttons[index]);
	updateUI();

	return buttonPanel;

    }

    //find the button index which is selected
    protected int getSelectionIndex() {
	for (int i = 0; i < buttons.length; i++) {
	    if (buttons[i].isSelected())
		return i;
	}
	return -1;         //should never reach here

    }

    // callback for radio buttons  which have a text area associated with them
    @Override
    public void itemStateChanged(ItemEvent ie) {
	String value = null;
	JRadioButton rb = (JRadioButton)ie.getItem();

	for (int i = 0; i < buttons.length; i++) {
	    if (buttons[i] == rb)
		value =  buttonValues[i];
	}
	Option subOption = optionType.getSubOption(value);
	if (subOption != null){
	    subOptionUI.setEnabled(rb.isSelected());
	}
    }

    /**
     * Bind this UI and it's subOptionUI to the associated Values in 'options'.
     */

    // override OptionUI
    @Override
    void bind(OptionSet options) {
	super.bind(options);
	if (subOptionUI != null)
	    subOptionUI.bind(options);
    }

    /**
     * This functions set the newly entered value for this option
     */

    // override OptionUI
    @Override
    public void applyChanges() {

	// We must apply the parent option first 
	// For example, run_io is an option with run_pty as a suboption.
	// -- see DbxDebugger.setOption for a reason. Essentially, if I set
	// the pty before I've set the run_io, I think that run_io=window
	// and coerce the pty to the pio window's pty value, where run_io
	// really was supposed to be set to pty but had not been applied yet!

	super.applyChanges();

	if (subOptionUI != null) {

	    // This is a weird test.
	    // getSubOption uses the current value of the "super" option
	    // to return the sub option or null. If it returns a non-null
	    // it means that the suboption is active (for example if the
	    // super option has value "custom").

	    Option VSubOption =
		optionType.getSubOption(getValueFromUI());
	    if (VSubOption != null) {
		// apply custom value
		subOptionUI.applyChanges();
	    } else {
		// apply default value
		subOptionUI.setValue(null);
		subOptionUI.applyChanges();
	    }
	}
    }


    /**
     * This function resets the newly entered value for this option
     * to the last known value of the option
     */

    @Override
    public void cancelChanges() {
	Option subOption;
	//check if this option has subOption at all
	if((subOption = optionType.getSubOption()) != null) {
	    //it has a sub option here
	    subOptionUI.cancelChanges();
	}

	super.cancelChanges();
    }

    /**
     * returns the value displayed on the UI
     */

    @Override
    protected String getValueFromUI() {
	return buttonValues[getSelectionIndex()];
    }


    /**
     * update UI with currValue
     */
    @Override
    protected void updateUI(){
	if (currValue == null)
	    return;
	//find the button having the currentValue
	for (int i = 0; i < buttons.length; i++) {
	    if (buttonValues[i].equals(currValue))
		buttons[i].setSelected(true);
	}
    }


    /**
     * Add this option into the given panel
     */

    @Override
    public void addTo(JPanel parent) {
	GridBagConstraints gridBagConstraints;

	if (hasLabel) {
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.gridwidth = 1;
	    gridBagConstraints.weightx = 0.5;
	    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	    gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
	    
	    label = createLabel();
	    parent.add(label, gridBagConstraints);

	}

	JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,2,2));

	if (optionType.verticalLayout()) {
	    buttonsPanel.setLayout(new GridLayout(0, 2));
	}

	buttonLabels = optionType.getValueLabels();
	buttonDescrs = optionType.getValueDescrs();
	buttonValues = optionType.getValues();
	buttons = new JRadioButton[buttonLabels.length];

	Option subOption = null;
	JPanel buttonPanel = null;
	JPanel jp = null;

	for (int i = 0; i < buttonLabels.length; i++) {
	    buttonPanel = createButtonPanel(i);
	    subOption = optionType.getSubOption(buttonValues[i]);
	    if (subOption != null) {
		// for options having a text area associated with them
		jp = new JPanel();
		jp.setLayout(new GridBagLayout());

		subOptionUI =
		    new TextFieldOptionUI(subOption, LABEL_COLUMNS_MIN);
		subOptionUI.addTo(jp, 0);
		if (!buttons[i].isSelected()) {
		    subOptionUI.setEnabled(false);
		}

		buttons[i].addItemListener(this);
	    }
	    buttonsPanel.add(buttonPanel);
	}

	// always add suboption to the last button
	// The 'buttonPanel' here is the last buttonPanel
	if (jp != null) {
	    buttonPanel.add(jp, BorderLayout.EAST);
	}


	gridBagConstraints = new GridBagConstraints();
	gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
	gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);

	parent.add(buttonsPanel, gridBagConstraints);
    }
}
