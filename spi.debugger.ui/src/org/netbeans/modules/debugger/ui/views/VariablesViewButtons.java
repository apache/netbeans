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

package org.netbeans.modules.debugger.ui.views;

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
import org.netbeans.modules.debugger.ui.actions.AddWatchAction;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


public class VariablesViewButtons {

    public static final String SHOW_WATCHES = "show_watches"; // NOI18N
    public static final String SHOW_EVALUTOR_RESULT = "show_evaluator_result"; // NOI18N

    public static final String PREFERENCES_NAME = "variables_view"; // NOI18N

    public static JToggleButton createShowWatchesButton() {
        JToggleButton button = createToggleButton(
                SHOW_WATCHES,
                "org/netbeans/modules/debugger/resources/localsView/show_watches_16.png",
                NbBundle.getMessage (VariablesViewButtons.class, "Hint_Show_Watches")
            );
        button.addActionListener(new ShowWatchesActionListener(button));
        return button;
    }

    public static JToggleButton createShowResultButton() {
        JToggleButton button = createToggleButton(
                SHOW_EVALUTOR_RESULT,
                "org/netbeans/modules/debugger/resources/localsView/show_evaluator_result_16.png",
                NbBundle.getMessage (VariablesViewButtons.class, "Hint_Show_Result")
            );
        button.addActionListener(new ShowResultActionListener(button));
        return button;
    }

    public static JButton createNewWatchButton() {
        JButton button = createButton(
                "org/netbeans/modules/debugger/resources/watchesView/create_new_watch_16.png",
                NbBundle.getMessage (VariablesViewButtons.class, "Hint_Create_New_Watch")
            );
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((AddWatchAction) AddWatchAction.findObject(AddWatchAction.class, true)).actionPerformed(null);
            }
        });
        return button;
    }

    public static boolean isWatchesViewNested() {
        Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(PREFERENCES_NAME); // NOI18N
        return preferences.getBoolean(SHOW_WATCHES, true);
    }

    public static boolean isResultsViewNested() {
        Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(PREFERENCES_NAME); // NOI18N
        return preferences.getBoolean(SHOW_EVALUTOR_RESULT, true);
    }

    private static JToggleButton createToggleButton (final String id, String iconName, String tooltip) {
        Icon icon = loadIcon(iconName);
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

    public static JButton createButton (String iconName, String tooltip) {
        Icon icon = loadIcon(iconName);
        return createButton(icon, tooltip);
    }

    public static JButton createButton (Icon icon, String tooltip) {
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

    private static Icon loadIcon(String iconPath) {
        return ImageUtilities.loadImageIcon(iconPath, false);
    }

    private static boolean isButtonSelected(String name) {
        if (name.equals(SHOW_WATCHES)) {
            return isWatchesViewNested();
        } else if (name.equals(SHOW_EVALUTOR_RESULT)) {
            return isResultsViewNested();
        }
        return false;
    }

    private static void setButtonSelected(String name, boolean selected) {
        Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(PREFERENCES_NAME); // NOI18N [TODO]
        preferences.putBoolean(name, selected);
    }

    private static void openView (String viewName, boolean activate) {
        TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
        view.open();
        if (activate) {
            view.requestActive();
        }
    }

    private static void closeView (String viewName) {
        TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
        view.close();
    }

    // **************************************************************************

    private static class ShowWatchesActionListener implements ActionListener {

        private JToggleButton button;

        ShowWatchesActionListener(JToggleButton toggleButton) {
            this.button = toggleButton;
        }

        public void actionPerformed(ActionEvent e) {
            boolean isSelected = button.isSelected();
            setButtonSelected(SHOW_WATCHES, isSelected);
            if (isSelected) {
                // close watches view
                //closeView("watchesView"); Do not close the view, leave it up to the user choice. // NOI18N
            } else {
                // open watches view
                openView("watchesView", false); // NOI18N
            }
        }

    }

    private static class ShowResultActionListener implements ActionListener {

        private JToggleButton button;

        ShowResultActionListener(JToggleButton toggleButton) {
            this.button = toggleButton;
        }

        public void actionPerformed(ActionEvent e) {
            boolean isSelected = button.isSelected();
            setButtonSelected(SHOW_EVALUTOR_RESULT, isSelected);
            if (isSelected) {
                // close watches view
                //closeView("resultsView"); Do not close the view, leave it up to the user choice. // NOI18N
            } else {
                // open watches view
                openView("resultsView", false); // NOI18N
            }
        }

    }

}
