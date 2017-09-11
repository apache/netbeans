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
package javahelp.gui;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.actions.HelpAction;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;

//import org.netbeans.jellytools.Bundle;
//import org.netbeans.jellytools.JellyTestCase;
//import org.netbeans.jellytools.HelpOperator;
//import org.netbeans.jellytools.MainWindowOperator;
//import org.netbeans.jellytools.NbDialogOperator;
//import org.netbeans.jellytools.OptionsOperator;
//import org.netbeans.jellytools.actions.HelpAction;
//
//import org.netbeans.jemmy.operators.JButtonOperator;
//import org.netbeans.jemmy.operators.JMenuBarOperator;
//import org.netbeans.jemmy.operators.JTreeOperator;
//import org.netbeans.junit.NbModuleSuite;
//import junit.framework.Test;

/**
 * JellyTestCase test case with implemented Java Help Test support stuff
 *
 * @author  mmirilovic@netbeans.org
 */
public class JavaHelpDialogTest extends JellyTestCase {

    private HelpOperator helpWindow;

    /** Creates a new instance of JavaHelpDialogTest */
    public JavaHelpDialogTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(JavaHelpDialogTest.class)
                .addTest("testHelpF1")
                .addTest("testHelpFromMenu")
                .addTest("testHelpByButtonNonModal")
                .addTest("testHelpByButtonModal")
                .addTest("testContextualSearch")
                .addTest("testHelpByButtonNestedModal")
                .gui(true)
                .enableModules(".*")
                .clusters(".*") );
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
        closeAllModal();

        if (helpWindow != null && helpWindow.isVisible()) {
            helpWindow.close();
        }

        helpWindow = null;
    }

    public void testHelpF1() {
        MainWindowOperator.getDefault().pressKey(java.awt.event.KeyEvent.VK_F1);
        new org.netbeans.jemmy.EventTool().waitNoEvent(7000);
        helpWindow = new HelpOperator();
    }

    public void testHelpFromMenu() {
        new HelpAction().performMenu();
        helpWindow = new HelpOperator();
    }

    public void testHelpCoreFromMenu() {
        String helpMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Help"); // Help
        String helpSetsMenu = Bundle.getStringTrimmed("org.netbeans.modules.javahelp.resources.Bundle", "Menu/Help/HelpShortcuts");  // Help Sets
        String coreIDEHelpMenu = Bundle.getString("org.netbeans.modules.usersguide.Bundle", "Actions/Help/org-netbeans-modules-usersguide-mainpage.xml"); // Core IDE Help

        MainWindowOperator.getDefault().menuBar().pushMenu(helpMenu + "|" + helpSetsMenu + "|" + coreIDEHelpMenu, "|");
        helpWindow = new HelpOperator();
    }

    public void testHelpByButtonNonModal() {
        OptionsOperator.invoke();
        OptionsOperator options = new OptionsOperator();
        options.help();
        helpWindow = new HelpOperator();
        options.close();
    }

    public void testHelpByButtonModal() {
        String toolsMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Tools"); // Tools
        
        String javaPlatformMenu = Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle", "CTL_PlatformManager"); // Java Platforms

        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(toolsMenu + "|" + javaPlatformMenu, "|");
        new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.api.java.platform.Bundle", "TXT_PlatformsManager")).help();    // Java Platform Manager
        helpWindow = new HelpOperator();
    }

    public void testHelpByButtonNestedModal() {
        String toolsMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Tools"); // Tools
        String javaPlatformMenu = Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle", "CTL_PlatformManager"); // Java Platforms

        new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar()).pushMenuNoBlock(toolsMenu + "|" + javaPlatformMenu, "|");
        //new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.core.ui.Bundle", "CTL_SetupWizardTitle")).help();    // Setup Wizard
        NbDialogOperator javaPlatformManager = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.api.java.platform.Bundle", "TXT_PlatformsManager"));// Java Platform Manager

        new JButtonOperator(javaPlatformManager, Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle", "CTL_AddPlatform")).pushNoBlock(); // Add Platform...
        NbDialogOperator addJavaPlatform = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.java.platform.ui.Bundle", "CTL_AddPlatformTitle"));// Add Java Platform
        addJavaPlatform.help();
        helpWindow = new HelpOperator();

        // close
        addJavaPlatform.cancel();
        javaPlatformManager.closeByButton();
    }

    public void testContextualSearch() {
        new HelpAction().perform();
        helpWindow = new HelpOperator();
        helpWindow.selectPageSearch();
        helpWindow.searchFind("compile");

        try {
            Thread.sleep(5000);
        } catch (Exception exc) {
            exc.printStackTrace(getLog());
        }


        JTreeOperator tree = helpWindow.treeSearch();
        log("Selection path=" + tree.getSelectionPath());
        log("Selection count=" + tree.getSelectionCount());

        if (tree.getSelectionCount() < 1) {
            fail("None founded text in the help, it isn't obvious");
        }
    }

    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
//    public static void main(String[] args) {
//        junit.textui.TestRunner.run(suite());
//    }
}
