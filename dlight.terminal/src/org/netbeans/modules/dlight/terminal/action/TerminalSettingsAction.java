/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.terminal.action;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(id = "TerminalSettingsAction", category = "Window")
@ActionRegistration(iconInMenu = true, displayName = "#TerminalOptionsShortDescr", iconBase = TerminalSettingsAction.SETTINGS_ICON)
@ActionReference(path = TerminalAction.TERMINAL_ACTIONS_PATH, name = "org-netbeans-modules-dlight-terminal-action-TerminalSettingsAction", position = 300)
public class TerminalSettingsAction extends AbstractAction implements Presenter.Toolbar {
    public static final String SETTINGS_ICON = "org/netbeans/modules/dlight/terminal/action/terminal_options.png"; //NOI18N

    public TerminalSettingsAction() {
        putValue(Action.NAME, "TerminalSettingsAction"); //NOI18N
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(TerminalSettingsAction.class, "TerminalOptionsShortDescr")); //NOI18N
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(SETTINGS_ICON, false));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OptionsDisplayer.getDefault().open("Advanced/TermAdvancedOption"); //NOI18N
    }

    @Override
    public Component getToolbarPresenter() {
        JButton component = createButton(SETTINGS_ICON, NbBundle.getMessage(TerminalSettingsAction.class, "TerminalOptionsShortDescr")); //NOI18N

        component.addActionListener(this);

        return component;
    }

    private static JButton createButton(String iconPath, String tooltip) {
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

}
