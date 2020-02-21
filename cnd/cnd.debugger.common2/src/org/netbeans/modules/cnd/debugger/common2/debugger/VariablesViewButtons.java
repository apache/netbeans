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
