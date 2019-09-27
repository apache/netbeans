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
package org.mycompany.wizard.panels;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.swing.NbiCheckBox;
import org.netbeans.installer.wizard.components.panels.DestinationPanel;
import org.netbeans.installer.wizard.containers.SwingContainer;
import org.netbeans.installer.wizard.ui.SwingUi;
import org.netbeans.installer.wizard.ui.WizardUi;

public class HelloWorldPanel extends DestinationPanel {

    public HelloWorldPanel() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);

        setProperty(DESTINATION_LABEL_TEXT_PROPERTY,
                DEFAULT_DESTINATION_LABEL_TEXT);
        setProperty(DESTINATION_BUTTON_TEXT_PROPERTY,
                DEFAULT_DESTINATION_BUTTON_TEXT);
        
        setProperty(ERROR_CONTAINS_NON_ASCII_CHARS,
                DEFAULT_ERROR_CONTAINS_NON_ASCII_CHARS);
    }

    @Override
    public WizardUi getWizardUi() {
        if (wizardUi == null) {
            wizardUi = new HelloWorldPanelUi(this);
        }

        return wizardUi;
    }

    @Override
    public void initialize() {
        super.initialize();
        if(getWizard().getProperty(CREATE_DESKTOP_SHORTCUT_PROPERTY) == null) {
            getWizard().setProperty(CREATE_DESKTOP_SHORTCUT_PROPERTY, "" + true);
        }
        if(getWizard().getProperty(CREATE_START_MENU_SHORTCUT_PROPERTY) == null) {
            getWizard().setProperty(CREATE_START_MENU_SHORTCUT_PROPERTY, "" + true);
        }
    }


    public static class HelloWorldPanelUi extends DestinationPanelUi {

        protected HelloWorldPanel panel;

        public HelloWorldPanelUi(HelloWorldPanel panel) {
            super(panel);


            this.panel = panel;
        }

        @Override
        public SwingUi getSwingUi(SwingContainer container) {
            if (swingUi == null) {
                swingUi = new HelloWorldPanelSwingUi(panel, container);
            }

            return super.getSwingUi(container);
        }
    }

    public static class HelloWorldPanelSwingUi extends DestinationPanelSwingUi {

        protected HelloWorldPanel panel;
        private NbiCheckBox desktopShortcutComboBox;
        private NbiCheckBox startMenuShortcutComboBox;

        public HelloWorldPanelSwingUi(
                final HelloWorldPanel panel,
                final SwingContainer container) {
            super(panel, container);

            this.panel = panel;

            initComponents();
        }

        // protected ////////////////////////////////////////////////////////////////
        @Override
        protected void initialize() {
            desktopShortcutComboBox.setText(CREATE_DESKTOP_SHORTCUT_NAME);            
            desktopShortcutComboBox.setSelected(false);
            if(Boolean.parseBoolean(panel.getWizard().getProperty(CREATE_DESKTOP_SHORTCUT_PROPERTY))) {
                desktopShortcutComboBox.doClick();
            }

            startMenuShortcutComboBox.setText(
                    SystemUtils.isWindows() ? CREATE_START_MENU_SHORTCUT_NAME_WINDOWS :
                        (SystemUtils.isMacOS() ? CREATE_START_MENU_SHORTCUT_NAME_MAC :
                            CREATE_START_MENU_SHORTCUT_NAME_UNIX));
            startMenuShortcutComboBox.setSelected(false);
            if(Boolean.parseBoolean(panel.getWizard().getProperty(CREATE_START_MENU_SHORTCUT_PROPERTY))) {
                startMenuShortcutComboBox.doClick();
            }

            super.initialize();
        }

        @Override
        protected void saveInput() {
            super.saveInput();
            panel.getWizard().setProperty(
                    CREATE_DESKTOP_SHORTCUT_PROPERTY,
                    StringUtils.EMPTY_STRING + desktopShortcutComboBox.isSelected());
            
            panel.getWizard().setProperty(
                    CREATE_START_MENU_SHORTCUT_PROPERTY,
                    StringUtils.EMPTY_STRING + startMenuShortcutComboBox.isSelected());
        }

        @Override
        protected String validateInput() {
            String errorMessage = super.validateInput();
            
            if (errorMessage == null) {
                // #222846 - non-ascii characters in installation path
                File installationFolder = new File(getDestinationPath());
                CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
                if (!encoder.canEncode(installationFolder.getAbsolutePath())) {
                    return StringUtils.format(panel.getProperty(ERROR_CONTAINS_NON_ASCII_CHARS));
                }
            }
            
            return errorMessage;
        }

        // private //////////////////////////////////////////////////////////////////
        private void initComponents() {
            // selectedLocationField ////////////////////////////////////////////////
            desktopShortcutComboBox = new NbiCheckBox();
            startMenuShortcutComboBox = new NbiCheckBox();

            // this /////////////////////////////////////////////////////////////////
            add(desktopShortcutComboBox, new GridBagConstraints(
                    0, 2, // x, y
                    2, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(15, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???
            add(startMenuShortcutComboBox, new GridBagConstraints(
                    0, 3, // x, y
                    2, 1, // width, height
                    1.0, 0.0, // weight-x, weight-y
                    GridBagConstraints.LINE_START, // anchor
                    GridBagConstraints.HORIZONTAL, // fill
                    new Insets(7, 11, 0, 11), // padding
                    0, 0));                           // padx, pady - ???

        }
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String CREATE_DESKTOP_SHORTCUT_PROPERTY =
            "create.desktop.shortcut";
    public static final String CREATE_START_MENU_SHORTCUT_PROPERTY =
            "create.start.menu.shortcut";    
    public static final String ERROR_CONTAINS_NON_ASCII_CHARS =
            "error.contains.non.ascii.chars"; // NOI18N
    
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.description"); // NOI18N
    public static final String DEFAULT_DESTINATION_LABEL_TEXT =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.destination.label.text"); // NOI18N
    public static final String DEFAULT_DESTINATION_BUTTON_TEXT =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.destination.button.text"); // NOI18N
    public static final String CREATE_DESKTOP_SHORTCUT_NAME =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.create.desktop.shortcut"); // NOI18N
    public static final String CREATE_START_MENU_SHORTCUT_NAME_WINDOWS =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.create.start.menu.shortcut.windows"); // NOI18N
    public static final String CREATE_START_MENU_SHORTCUT_NAME_UNIX =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.create.start.menu.shortcut.unix"); // NOI18N
    public static final String CREATE_START_MENU_SHORTCUT_NAME_MAC =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.create.start.menu.shortcut.macosx"); // NOI18N
    public static final String DEFAULT_ERROR_CONTAINS_NON_ASCII_CHARS =
            ResourceUtils.getString(HelloWorldPanel.class,
            "P.error.contains.non.ascii.chars"); // NOI18N   
}
