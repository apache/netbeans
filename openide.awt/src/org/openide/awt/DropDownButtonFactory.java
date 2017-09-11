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

package org.openide.awt;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

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
}
