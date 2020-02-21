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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


public class VariablesViewButtons {

    public static final String PREFERENCES_NAME = "variables_view"; // NOI18N
    public static final String SHOW_AUTOS = "show_autos"; // NOI18N

    private static JToggleButton showAutosToggle = null;

    public static synchronized JToggleButton createShowAutosButton() {
        if (showAutosToggle != null) {
            return showAutosToggle;
        }
        showAutosToggle = createToggleButton(
                SHOW_AUTOS,
                "org/netbeans/modules/cnd/debugger/common2/icons/autos.png", // NOI18N
                NbBundle.getMessage (VariablesViewButtons.class, "Hint_Show_Autos")
            );
        showAutosToggle.addActionListener(new ShowAutosActionListener(showAutosToggle));
        return showAutosToggle;
    }

    private static JToggleButton createToggleButton (final String id, String iconPath, String tooltip) {
        Icon icon = ImageUtilities.loadImageIcon(iconPath, false);
        boolean isSelected = isButtonSelected(id);
        final JToggleButton toggleButton = new JToggleButton(icon, isSelected);
        // ensure small size, just for the icon
        Dimension size = new Dimension(icon.getIconWidth() + 8, icon.getIconHeight() + 8);
        toggleButton.setPreferredSize(size);
        toggleButton.setMargin(new Insets(1, 1, 1, 1));
        toggleButton.setToolTipText(tooltip);
        toggleButton.setFocusable(false);
        toggleButton.setEnabled(!NativeDebuggerManager.isStandalone()); //Disable for tool
        return toggleButton;
    }

    public static boolean isShowAutos() {
        if (NativeDebuggerManager.isStandalone()) {
            return false; // always false in tool
        }
        Preferences preferences = NbPreferences.forModule(VariablesViewButtons.class).node(PREFERENCES_NAME);
        return preferences.getBoolean(SHOW_AUTOS, false); //disabled by default
    }

    private static boolean isButtonSelected(String name) {
        if (name.equals(SHOW_AUTOS)) {
            return isShowAutos();
        }
        return false;
    }

    private static void setButtonSelected(String name, boolean selected) {
        Preferences preferences = NbPreferences.forModule(VariablesViewButtons.class).node(PREFERENCES_NAME);
        preferences.putBoolean(name, selected);
    }

    // **************************************************************************

    private static class ShowAutosActionListener implements ActionListener {

        private JToggleButton button;

        ShowAutosActionListener(JToggleButton toggleButton) {
            this.button = toggleButton;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean isSelected = button.isSelected();
            setButtonSelected(SHOW_AUTOS, isSelected);
        }
    }
}
