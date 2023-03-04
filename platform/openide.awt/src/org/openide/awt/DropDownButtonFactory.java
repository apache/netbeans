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

package org.openide.awt;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import org.openide.awt.IconWithArrow.ArrowIcon;

/**
 * Factory creating buttons with a small arrow icon that shows a popup menu when clicked.
 * The default button behavior hasn't changed except that the button doesn't
 * display any text, just the icon.
 *
 * @author S. Aubrecht
 * @since 6.11
 */
public final class DropDownButtonFactory {

    /**
     * Use this property name to assign or remove popup menu to/from buttons created by this factory,
     * e.g. <code>dropDownButton.putClientProperty( PROP_DROP_DOWN_MENU, new JPopupMenu() )</code>
     * The property value must be <code>JPopupMenu</code>, removing this property removes the arrow from the button.
     */
    public static final String PROP_DROP_DOWN_MENU = "dropDownMenu";

    /** Creates a new instance of DropDownButtonFactory */
    private DropDownButtonFactory() {
    }

    /**
     * Creates JButton with a small arrow that shows the provided popup menu when clicked.
     *
     * @param icon The default icon, cannot be null
     * @param dropDownMenu Popup menu to display when the arrow is clicked. If this parameter is null
     * then the button doesn't show any arrow and behaves like a regular JButton. It is possible to add
     * the popup menu later using PROP_DROP_DOWN_MENU client property.
     * @return A button that is capable of displaying an 'arrow' in its icon to open a popup menu.
     */
    public static JButton createDropDownButton( Icon icon, JPopupMenu dropDownMenu ) {
        return new DropDownButton( icon, dropDownMenu );
    }

    /**
     * Creates JToggleButton with a small arrow that shows the provided popup menu when clicked.
     *
     * @param icon The default icon, cannot be null
     * @param dropDownMenu Popup menu to display when the arrow is clicked. If this parameter is null
     * then the button doesn't show any arrow and behaves like a regular JToggleButton. It is possible to add
     * the popup menu later using PROP_DROP_DOWN_MENU client property.
     * @return A toggle-button that is capable of displaying an 'arrow' in its icon to open a popup menu.
     */
    public static JToggleButton createDropDownToggleButton( Icon icon, JPopupMenu dropDownMenu ) {
        return new DropDownToggleButton( icon, dropDownMenu );
    }

    /**
     * Get a dropdown button icon that is identical to those used in the dropdown buttons returned
     * by other methods in this class. The returned icon scales properly on HiDPI screens.
     *
     * @since 7.74
     * @param disabled whether to get a disabled version of the icon or not
     * @return a dropdown arrow icon
     */
    public static Icon getArrowIcon(boolean disabled) {
        return disabled ? ArrowIcon.INSTANCE_DEFAULT : ArrowIcon.INSTANCE_DISABLED;
    }
}
