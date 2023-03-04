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
package org.netbeans.jellytools;

import javax.swing.JTextField;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTest;

/** Test PluginsOperator.
 *
 * @author Jiri Skrivanek
 */
public class PluginsOperatorTest extends JellyTestCase {

    private static PluginsOperator pluginsOper;
    private static final String TEST_PLUGIN = "Archiver"; //NOI18N
    public static final String[] tests = new String[]{
        "testSetProxy",
        "testInvoke",
        "testInstall",
        "testUninstall",
        "testDeactivate",
        "testSettings",
        "testDowloaded",
        "testClose"};

    /** Creates test case with given name.
     * @param testName name of test case
     */
    public PluginsOperatorTest(String testName) {
        super(testName);
    }

    /** Define test suite.
     * @return suite.
     */
    public static NbTest suite() {
        return (NbTest) createModuleTest(PluginsOperatorTest.class, tests);
    }

    /** Print out test name. */
    @Override
    public void setUp() throws Exception {
        System.out.println("### " + getClass().getSimpleName() + "." + getName() + " ###");
    }

    /** Sets proxy for network connection. */
    public void testSetProxy() {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectGeneral();
        // "Manual Proxy Setting"
        String hTTPProxyLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Use_HTTP_Proxy");
        new JRadioButtonOperator(optionsOper, hTTPProxyLabel).push();
        // "HTTP Proxy:"
        String proxyHostLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Proxy_Host");
        JLabelOperator jloHost = new JLabelOperator(optionsOper, proxyHostLabel);
        new JTextFieldOperator((JTextField) jloHost.getLabelFor()).typeText("emea-proxy.uk.oracle.com"); // NOI18N
        // "Port:"
        String proxyPortLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Proxy_Port");
        JLabelOperator jloPort = new JLabelOperator(optionsOper, proxyPortLabel);
        new JTextFieldOperator((JTextField) jloPort.getLabelFor()).setText("80"); // NOI18N
        optionsOper.ok();
    }

    /** Test of invoke method. */
    public void testInvoke() throws InterruptedException {

        //Make sure the menu has time to load
        new Action(Bundle.getStringTrimmed(
                "org.netbeans.core.ui.resources.Bundle", "Menu/Tools"), null).performMenu();

        Thread.sleep(1000);


        new Action(Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle",
                "Menu/BuildProject"), null).performMenu();

        Thread.sleep(1000);

        pluginsOper = PluginsOperator.invoke();
    }

    /** Test install() method. 
     * - select Available Plugins tab
     * - type "netbeans.org Source Browser" into Search text field
     * - finish installation
     */
    public void testInstall() {
        pluginsOper.selectAvailablePlugins();
        pluginsOper.reloadCatalog();
        pluginsOper.search(TEST_PLUGIN);
        pluginsOper.install(TEST_PLUGIN);
    }

    /** Test uninstallation
     * - select Installed tab
     * - select Java and "netbeans.org Source Browser" plugins
     * - click Uninstall button
     * - wait for "NetBeans IDE Installer" dialog
     * - click Cancel
     */
    public void testUninstall() throws Exception {
        try {
            pluginsOper.cbShowDetails().setSelected(true);
        } catch (JemmyException e) {
            // check box not available
        }
        pluginsOper.selectPlugins(new String[]{
                    "Java",
                    TEST_PLUGIN
                });
        pluginsOper.uninstall();
        pluginsOper.installer().cancel();
    }

    /** Test deactivation
     * - select Installed tab
     * - select "netbeans.org Source Browser" plugin
     * - click Deactivate button
     * - wait for "NetBeans IDE Installer" dialog
     * - click Cancel
     */
    public void testDeactivate() {
        try {
            pluginsOper.cbShowDetails().setSelected(true);
        } catch (JemmyException e) {
            // check box not available
        }
        pluginsOper.selectPlugin(TEST_PLUGIN);
        pluginsOper.deactivate();
        pluginsOper.installer().cancel();
    }

    /** Test settings
     * - select Settings tab
     */
    public void testSettings() {
        pluginsOper.selectSettings();
    }

    /** Test Downloaded tab
     * - select Downloaded tab
     * - wait for file chooser
     * - close file chooser
     */
    public void testDowloaded() {
        pluginsOper.addPlugins();
        new JFileChooserOperator().cancel();
    }

    /** Close Plugins dialog. */
    public void testClose() {
        pluginsOper.close();
    }
}
