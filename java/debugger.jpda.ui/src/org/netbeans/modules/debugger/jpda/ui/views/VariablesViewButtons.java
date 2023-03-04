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

package org.netbeans.modules.debugger.jpda.ui.views;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


public class VariablesViewButtons {

    public static final String PREFERENCES_NAME = "variables_view"; // NOI18N
    public static final String SHOW_VALUE_PROPERTY_EDITORS = "show_value_property_editors"; // NOI18N
    private static final String SHOW_FORMATTERS_PROP_NAME = "org.netbeans.modules.debugger.jpda.ui.options.SHOW_FORMATTERS"; // NOI18N
    private static final String OPTIONS_JAVA_DEBUGGER_ID = "org-netbeans-modules-debugger-jpda-ui-options-JavaDebuggerOptionsPanelController"; // NOI18N

    private static JToggleButton showValuePropertyEditorsToggle = null;

    public static JButton createOpenOptionsButton() {
        JButton button = createButton(
                "org/netbeans/modules/debugger/jpda/resources/formatters_options_16.png",
                NbBundle.getMessage (VariablesViewButtons.class, "Hint_Open_Formatters")
            );
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.setProperty(SHOW_FORMATTERS_PROP_NAME, "true"); // NOI18N
		OptionsDisplayer.getDefault().open(JavaOptions.JAVA + "/" + OPTIONS_JAVA_DEBUGGER_ID); // NOI18N
            }
        });
        return button;
    }

    public static synchronized JToggleButton createShowValuePropertyEditorsButton() {
        if (showValuePropertyEditorsToggle != null) {
            return showValuePropertyEditorsToggle;
        }
        showValuePropertyEditorsToggle = createToggleButton(
                SHOW_VALUE_PROPERTY_EDITORS,
                "org/netbeans/modules/debugger/jpda/resources/show_property_editors_16.png",
                NbBundle.getMessage (VariablesViewButtons.class, "Hint_Show_Value_Property_Editors")
            );
        showValuePropertyEditorsToggle.addActionListener(new ShowValuePropertyEditorsActionListener(showValuePropertyEditorsToggle));
        return showValuePropertyEditorsToggle;
    }

    private static JButton createButton (String iconPath, String tooltip) {
        Icon icon = ImageUtilities.loadImageIcon(iconPath, false);
        final JButton button = new JButton(icon);
        // ensure small size, just for the icon
        Dimension size = new Dimension(icon.getIconWidth() + 8, icon.getIconHeight() + 8);
        button.setPreferredSize(size);
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setBorder(new EmptyBorder(button.getBorder().getBorderInsets(button)));
        button.setToolTipText(tooltip);
        button.setFocusable(false);
        return button;
    }

    private static JToggleButton createToggleButton (final String id, String iconPath, String tooltip) {
        Icon icon = ImageUtilities.loadImageIcon(iconPath, false);
        boolean isSelected = isButtonSelected(id);
        final JToggleButton toggleButton = new JToggleButton(icon, isSelected);
        // ensure small size, just for the icon
        Dimension size = new Dimension(icon.getIconWidth() + 8, icon.getIconHeight() + 8);
        toggleButton.setPreferredSize(size);
        toggleButton.setMargin(new Insets(1, 1, 1, 1));
        if (!"Aqua".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            // We do not want an ugly border with the exception of Mac, where it paints the toggle state!
            toggleButton.setBorder(new EmptyBorder(toggleButton.getBorder().getBorderInsets(toggleButton)));
        }
        toggleButton.setToolTipText(tooltip);
        toggleButton.setFocusable(false);
        return toggleButton;
    }

    public static boolean isShowValuePropertyEditors() {
        return isButtonSelected(SHOW_VALUE_PROPERTY_EDITORS);
    }

    private static boolean isButtonSelected(String name) {
        Preferences preferences = NbPreferences.forModule(VariablesViewButtons.class).node(PREFERENCES_NAME);
        return preferences.getBoolean(name, getDefaultSelected(name));
    }

    private static void setButtonSelected(String name, boolean selected) {
        Preferences preferences = NbPreferences.forModule(VariablesViewButtons.class).node(PREFERENCES_NAME);
        preferences.putBoolean(name, selected);
    }
    
    private static boolean getDefaultSelected(String name) {
        if (SHOW_VALUE_PROPERTY_EDITORS.equals(name)) {
            return true;
        }
        return false;
    }

    // **************************************************************************

    private static class ShowValuePropertyEditorsActionListener implements ActionListener {

        private JToggleButton button;

        ShowValuePropertyEditorsActionListener(JToggleButton toggleButton) {
            this.button = toggleButton;
        }

        public void actionPerformed(ActionEvent e) {
            boolean isSelected = button.isSelected();
            setButtonSelected(SHOW_VALUE_PROPERTY_EDITORS, isSelected);
        }

    }

}
