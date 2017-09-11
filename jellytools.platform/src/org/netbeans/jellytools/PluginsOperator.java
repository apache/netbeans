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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import javax.swing.JRadioButton;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Handle Plugins dialog which is opened from main menu "Tools|Plugins".
 * This dialog shows list of installed plugins and also list of available
 * plugins. Plugins can be installed, uninstalled, activated and deactivated.
 * <p>
 * Usage:<br>
 * <pre>
 *      pluginsOper = PluginsOperator.invoke();
 *      pluginsOper.install("My Plugin");
 *      pluginsOper.close();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class PluginsOperator extends NbDialogOperator {

    // "Plugins"
    private static final String TITLE = Bundle.getString(
            "org.netbeans.modules.autoupdate.ui.actions.Bundle",
            "PluginManager_Panel_Name");
    // "Tools"
    private static final String TOOLS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.core.ui.resources.Bundle", "Menu/Tools");
    // "Plugins"
    private static final String PLUGINS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.autoupdate.ui.actions.Bundle",
            "PluginManagerAction_Name");
    // "Installed"
    private static final String INSTALLED_LABEL = Bundle.getStringTrimmed(
            "org.netbeans.modules.autoupdate.ui.Bundle",
            "PluginManagerUI_UnitTab_Installed_Title");
    // "Available Plugins"
    private static final String AVAILABLE_PLUGINS_LABEL = Bundle.getStringTrimmed(
            "org.netbeans.modules.autoupdate.ui.Bundle",
            "PluginManagerUI_UnitTab_Available_Title");
    private JTabbedPaneOperator _tabbedPane;

    /** Waits for Plugins dialog and waits until initial refresh is finished. */
    public PluginsOperator() {
        super(TITLE);
        tabbedPane();
        waitTabEnabled(INSTALLED_LABEL);
        // this works around when Available Plugins tab is not enabled by default
        if (!tabbedPane().isEnabledAt(tabbedPane().waitPage(AVAILABLE_PLUGINS_LABEL))) {
            selectInstalled();
            reloadCatalog();
            selectAvailablePlugins();
        }
    }

    /** Invokes Plugins dialog from main menu "Tools|Plugins".
     * @return instance of PluginsOperator
     */
    public static PluginsOperator invoke() {
        new ActionNoBlock(TOOLS_ITEM + "|" + PLUGINS_ITEM, null).perform();
        // increase timeout to 120 seconds
        long oldTime = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
	JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 120000);
        try {
            return new PluginsOperator();
        } finally {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", oldTime);
        }
    }

    /** Wait for JTabbedPane.
     * @return JTabbedPaneOperator
     */
    public JTabbedPaneOperator tabbedPane() {
        if (_tabbedPane == null) {
            _tabbedPane = new JTabbedPaneOperator(this);
            _tabbedPane.getTimeouts().setTimeout("ComponentOperator.WaitComponentEnabledTimeout", 120000);
            _tabbedPane.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        }
        _tabbedPane.wtComponentEnabled();
        return _tabbedPane;
    }

    /** Tries to find JTable in selected tab.
     * @return JTableOperator instance
     */
    public JTableOperator table() {
        return new JTableOperator(this);
    }

    /** Switches to Updates tab and finds "Update" JButton.
     * @return JButtonOperator
     */
    public JButtonOperator btUpdate() {
        selectUpdates();
        return new JButtonOperator(this, Bundle.getStringTrimmed(
                "org.netbeans.modules.autoupdate.ui.Bundle",
                "UnitTab_bTabAction_Name_UPDATE"));
    }

    /** Tries to find "Install" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btInstall() {
        return new JButtonOperator(this, Bundle.getStringTrimmed(
                "org.netbeans.modules.autoupdate.ui.Bundle",
                "UnitTab_bTabAction_Name_AVAILABLE"));
    }

    /** Tries to find "Check for Updates" or "Check for Newest" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btReloadCatalog() {
        // do not cache it because it is on more tabs
        return new JButtonOperator(this, "Check for");
    }

    /** Tries to find "Add Plugins..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAddPlugins() {
        selectDownloaded();
        return new JButtonOperator(this,
                Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.Bundle",
                "UnitTab_bAddLocallyDownloads_Name"));
    }

    /** Switches to Installed tab and finds "Deactivate" JButton.
     * @return JButtonOperator
     */
    public JButtonOperator btDeactivate() {
        selectInstalled();
        return new JButtonOperator(this, Bundle.getStringTrimmed(
                "org.netbeans.modules.autoupdate.ui.Bundle",
                "UnitTab_DeactivateAction"));
    }

    /** Switches to Installed tab and finds "Uninstall" JButton.
     * @return JButtonOperator
     */
    public JButtonOperator btUninstall() {
        selectInstalled();
        return new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.Bundle",
                "UnitTab_bTabAction_Name_INSTALLED"));
    }

    /** Switches to Installed tab and finds "Show details" JCheckBox.
     * @return JButtonOperator
     */
    public JCheckBoxOperator cbShowDetails()
    {
         return new JCheckBoxOperator(selectInstalled(), Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.Bundle",
                "UnitTab.detailView.text"));
         
    }

    /** Finds search text field. */
    public JTextFieldOperator txtSearch() {
        return new JTextFieldOperator(this);
    }

    /** Waits for "NetBeans IDE Installer" dialog.
     * @return WizardOperator instance
     */
    public WizardOperator installer() {
        // "NetBeans IDE Installer"
        String installerTitle = Bundle.getString(
                "org.netbeans.modules.autoupdate.ui.wizards.Bundle",
                "UninstallUnitWizard_Title");
        return new WizardOperator(installerTitle);
    }

    /** Changes current selected tab to "Updates"
     * @return JTabbedPaneOperator instance
     */
    public JTabbedPaneOperator selectUpdates() {
        String updatesTitle = Bundle.getStringTrimmed(
                "org.netbeans.modules.autoupdate.ui.Bundle",
                "PluginManagerUI_UnitTab_Update_Title");
        return selectTab(updatesTitle);
    }

    /** Changes current selected tab to "Available Plugins"
     * @return JTabbedPaneOperator instance
     */
    public JTabbedPaneOperator selectAvailablePlugins() {
        return selectTab(AVAILABLE_PLUGINS_LABEL);
    }

    /** Changes current selected tab to "Downloaded"
     * @return JTabbedPaneOperator of parent tabbed pane
     */
    public JTabbedPaneOperator selectDownloaded() {
        String downloadedTitle = Bundle.getString(
                "org.netbeans.modules.autoupdate.ui.Bundle",
                "PluginManagerUI_UnitTab_Local_Title");
        return selectTab(downloadedTitle);
    }

    /** Changes current selected tab to "Installed"
     * @return JTabbedPaneOperator instance
     */
    public JTabbedPaneOperator selectInstalled() {
        return selectTab(INSTALLED_LABEL);
    }

    /** Changes current selected tab to "Settings"
     * @return JTabbedPaneOperator instance
     */
    public JTabbedPaneOperator selectSettings() {
        String settingsTitle = Bundle.getStringTrimmed(
                "org.netbeans.modules.autoupdate.ui.Bundle",
                "SettingsTab_displayName");
        return selectTab(settingsTitle);
    }

    /** Changes current selected tab to one specified in argument.
     * @return JTabbedPaneOperator instance
     */
    public JTabbedPaneOperator selectTab(String tabName) {
        waitTabEnabled(tabName);
        tabbedPane().selectPage(tabName);
        return tabbedPane();
    }

    /** Waits until specified tab is enabled.
     * @param tabName name of tab
     */
    public void waitTabEnabled(final String tabName) {
        final int installedIndex = tabbedPane().waitPage(tabName);
        tabbedPane().waitState(new ComponentChooser() {

            public boolean checkComponent(Component comp) {
                return tabbedPane().isEnabledAt(installedIndex);
            }

            public String getDescription() {
                return "page " + tabName + " enabled";// NOI18N
            }
        });
    }

    /** Switches to Updates tab and clicks "Update" button. */
    public void update() {
        btUpdate().pushNoBlock();
    }

    /** Clicks "Install" button. */
    public void install() {
        btInstall().pushNoBlock();
    }

    /** Switches to Installed tab and clicks "Uninstall" button. */
    public void uninstall() {
        btUninstall().pushNoBlock();
    }

    /** Switches to Installed tab and clicks "Deactivate" button. */
    public void deactivate() {
        btDeactivate().pushNoBlock();
    }

    /** Type given text into search text field and waits until row count of
     * table is changed.
     * @param text text to be searched for
     */
    public void search(String text) {
        final int rowCount = table().getRowCount();
        txtSearch().setText(text);
        table().waitState(new ComponentChooser() {

            public boolean checkComponent(Component comp) {
                return table().getRowCount() != rowCount;
            }

            public String getDescription() {
                return "table changed row count";// NOI18N
            }
        });
    }

    /** Clicks "Reload Catalog" button and wait until tabbed pane is again enabled. */
    public void reloadCatalog() {
        btReloadCatalog().push();
        tabbedPane();
    }

    /** Switches to Downloaded tab and clicks "Add Plugins" button. */
    public void addPlugins() {
        btAddPlugins().pushNoBlock();
    }

    /** Selects plugin in the table.
     * @param pluginName name of plugin to be selected
     */
    public void selectPlugin(String pluginName) {
        selectPlugins(new String[]{pluginName});
    }

    /** Selects plugins in the table.
     * @param pluginNames array of plugin names to be selected
     */
    public void selectPlugins(String[] pluginNames) {
        JTableOperator tableOper = table();
        for (int i = 0; i < pluginNames.length; i++) {
            String name = pluginNames[i];
            int row = tableOper.findCellRow(name, new DefaultStringComparator(true, true), 1, 0);
            if(row == -1) {
                throw new JemmyException("Plugin "+name+" not found.");
            }
            tableOper.selectCell(row, 1);
            tableOper.clickOnCell(row, 0);
        }
    }

    /** Adds plugin specified by its NBM path to the list at Downloaded tab. 
     * @param nbmPath absolute path to nbm file
     */
    public void addPlugin(String nbmPath) {
        addPlugins();
        JFileChooserOperator fileChooserOp = new JFileChooserOperator();
        fileChooserOp.setSelectedFile(new File(nbmPath));
        fileChooserOp.approve();
        tabbedPane();
    }

    /** Installs given plugin. It switches to Available Plugins tab, selects
     * desired plugin, click Install button and finish installation.
     * @param pluginName
     */
    public void install(String pluginName) {
        install(new String[]{pluginName});
    }

    /** Installs given plugins. It switches to Available Plugins tab, selects
     * desired plugins, click Install button and finish installation.
     * @param pluginNames array of plugin names to be installed
     */
    public void install(String[] pluginNames) {
        selectAvailablePlugins();
        selectPlugins(pluginNames);
        install();
        finishInstall();
    }

    /** Finish installation of selected plugins. Do the following:
     * <ul>
     *      <li>waits for "NetBeans IDE Installer" dialog</li>
     *      <li>in "NetBeans IDE Installer" dialog click Next</li>
     *      <li>click "I accept..." check box</li>
     *      <li>click Install button</li>
     *      <li>wait until message that plugins were successfully installed appears</li>
     *      <li>if restart is not needed wait until the module is turned on (message in main window status bar)</li>
     *      <li>click Finish button to dismiss the dialog</li>
     * </ul>
     */
    public void finishInstall() {
        WizardOperator installerOper = installer();
        installerOper.next();
        // "I accept the terms..."
        String acceptLabel = Bundle.getStringTrimmed(
                "org.netbeans.modules.autoupdate.ui.wizards.Bundle",
                "LicenseApprovalPanel.cbAccept.text");
        JCheckBoxOperator acceptCheckboxOper = new JCheckBoxOperator(installerOper, acceptLabel);
        if (!acceptCheckboxOper.isEnabled()) {
            // wait until licence is shown and dialog is re-created
            acceptCheckboxOper.waitComponentShowing(false);
            // find check box again
            acceptCheckboxOper = new JCheckBoxOperator(installerOper, acceptLabel);
        }
        acceptCheckboxOper.push();
        // "Install"
        String installInDialogLabel = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.wizards.Bundle", "InstallUnitWizardModel_Buttons_Install");
        new JButtonOperator(installerOper, installInDialogLabel).push();
        // "The NetBeans IDE Installer has successfully installed the following plugins:"
        String installedLabel = Bundle.getString("org.netbeans.modules.autoupdate.ui.wizards.Bundle", "InstallStep_InstallDone_Text");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 120000);
        new JTextAreaOperator(installerOper, installedLabel);
        // "Restart IDE Now"
        String restartNowLabel = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.wizards.Bundle", "InstallUnitWizardModel_Buttons_RestartNow");
        JRadioButton restartButton = JRadioButtonOperator.findJRadioButton((Container)installerOper.getSource(), restartNowLabel, true, true);
        if(restartButton == null) {
            // check Status line
            // "Turning on modules...done."
            String turningOnLabel = Bundle.getString("org.netbeans.core.startup.Bundle", "MSG_finish_enable_modules");
            // increase timeout to 120 seconds
            MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 120000);
            MainWindowOperator.getDefault().waitStatusText(turningOnLabel);
        }
        installerOper.finish();
    }
}
