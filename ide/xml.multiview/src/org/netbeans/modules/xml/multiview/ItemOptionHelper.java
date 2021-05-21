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

package org.netbeans.modules.xml.multiview;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

/**
 * The class simplifies use of an option button group to show/set value of an item
 *
 * @author pfiala
 */
public abstract class ItemOptionHelper implements ActionListener, Refreshable {

    private final AbstractButton[] buttons;
    private final AbstractButton unmatchedOption;
    private XmlMultiViewDataSynchronizer synchronizer;

    /**
     * Constructor initializes object by button group which will be handled
     *
     * @param synchronizer
     * @param group handled ButtonGroup.
     *              If the group contains at least one button that has empty text value
     *              (see {@link #getOptionText(javax.swing.AbstractButton)}, the last one of such buttons
     *              is used as "unmatched option". The "unmatched option" is selected,
     */
    public ItemOptionHelper(XmlMultiViewDataSynchronizer synchronizer, ButtonGroup group) {
        
        this.synchronizer = synchronizer;
        buttons = Collections.list(group.getElements()).toArray(new AbstractButton[0]);
        AbstractButton unmatchedOpt = null;
        for (int i = 0; i < buttons.length; i++) {
            final AbstractButton button = buttons[i];
            button.addActionListener(this);
            if (getOptionText(button) == null) {
                unmatchedOpt = button;
            }
        }
        this.unmatchedOption = unmatchedOpt;
        setOption(getItemValue());
    }

    /**
     * Invoked when an action occurs on an option button.
     */
    public final void actionPerformed(ActionEvent e) {
        String option = getOption();
        if (!option.equals(getItemValue())) {
            setItemValue(option);
            synchronizer.requestUpdateData();
        }
    }

    /**
     * Selects option matched the item value.
     * If no option matches the value the unmatchedOption option is selected,
     * if the "unmatchedOption" uption exists.
     * See {@link #ItemOptionHelper(XmlMultiViewDataSynchronizer, ButtonGroup)}
     *
     * @param itemValue value of item to be selected in button group
     */
    public void setOption(String itemValue) {
        AbstractButton matchingButton = getMatchingButton(itemValue);
        if (matchingButton != null && !matchingButton.isSelected()) {
            matchingButton.setSelected(true);
        }
        return;
    }

    private AbstractButton getMatchingButton(String itemValue) {
        AbstractButton matchingButton = null;
        for (int i = 0; i < buttons.length; i++) {
            final AbstractButton button = buttons[i];
            if (getOptionText(button).equals(itemValue)) {
                matchingButton = button;
                break;
            }
        }
        if (matchingButton == null && unmatchedOption != null) {
            matchingButton = unmatchedOption;
        }
        return matchingButton;
    }

    private String getOptionText(AbstractButton button) {
        String fixedValue = (String)button.getClientProperty(PROPERTY_FIXED_VALUE);
        if (fixedValue!=null) return fixedValue;
        else return button.getText();
    }

    /**
     * Retrieves the text value represented by the selected option.
     *
     * @return client property:prop_fixed_value of the button representing the selected option. 
     * If the client property is null, a text property of the button is used.
     */
    public String getOption() {
        for (int i = 0; i < buttons.length; i++) {
            AbstractButton button = buttons[i];
            if (button.isSelected()) {
                return getOptionText(button);
            }
        }
        return null;
    }

    /**
     * Called by the helper in order to retrieve the value of the item.
     *
     * @return value of the handled item.
     */
    public abstract String getItemValue();

    /**
     * Called by the helper in order to set the value of the item
     *
     * @param value new value of the hanlded item
     */
    public abstract void setItemValue(String value);

    public void refresh() {
        setOption(getItemValue());
    }
}
