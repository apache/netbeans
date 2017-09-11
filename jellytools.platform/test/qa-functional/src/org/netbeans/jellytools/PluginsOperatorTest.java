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
