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
