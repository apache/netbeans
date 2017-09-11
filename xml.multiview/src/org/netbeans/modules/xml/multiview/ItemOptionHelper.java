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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        buttons = (AbstractButton[]) Collections.list(group.getElements()).toArray(new AbstractButton[0]);
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
