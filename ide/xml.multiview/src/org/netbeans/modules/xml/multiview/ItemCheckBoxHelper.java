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

package org.netbeans.modules.xml.multiview;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The class simplifies use of a combo box to show/set value of an item
 *
 * @author pfiala
 */
public abstract class ItemCheckBoxHelper implements ActionListener, Refreshable {
    private JCheckBox checkBox;
    private XmlMultiViewDataSynchronizer synchronizer;

    /**
     * Constructor initializes object by combo box and data object which will be handled
     *
     * @param synchronizer
     * @param checkBox   handled JComboBox.
     */
    public ItemCheckBoxHelper(XmlMultiViewDataSynchronizer synchronizer, JCheckBox checkBox) {
        this.synchronizer = synchronizer;
        this.checkBox = checkBox;
        checkBox.addActionListener(this);
        setValue(getItemValue());
    }

    /**
     * Invoked when an action occurs on a combo box.
     */
    public final void actionPerformed(ActionEvent e) {
        final boolean value = getValue();
        if (value != getItemValue()) {
            setItemValue(value);
            synchronizer.requestUpdateData();
        }
    }

    /**
     * Selects the item value in check box.
     *
     * @param itemValue value of item to be selected in check box
     */
    public void setValue(boolean itemValue) {
        checkBox.setSelected(itemValue);
    }

    /**
     * Check box getter
     *
     * @return handled check box
     */
    public JCheckBox getCheckBox() {
        return checkBox;
    }

    /**
     * Retrieves the text value selected in the check box.
     *
     * @return selected item of the check box
     */
    public boolean getValue() {
        return checkBox.isSelected();
    }

    /**
     * Called by the helper in order to retrieve the value of the item.
     *
     * @return value of the handled item.
     */
    public abstract boolean getItemValue();

    /**
     * Called by the helper in order to set the value of the item
     *
     * @param value new value of the hanlded item
     */
    public abstract void setItemValue(boolean value);

    public void refresh() {
        setValue(getItemValue());
    }
}
