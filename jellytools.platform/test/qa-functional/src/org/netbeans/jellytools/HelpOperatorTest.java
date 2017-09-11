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

import java.awt.event.KeyEvent;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

/** Test of HelpOperator.
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class HelpOperatorTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testInvoke",
        "testContentsSelection",
        "testSearchFind",
        "testPreviousAndNext",
        "testPrint",
        "testPageSetup",
        "testClose",};

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public HelpOperatorTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(HelpOperatorTest.class, tests);
    }

    /** Print out test name. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        // find help window if not found before
        if (help == null && !getName().equals("testInvoke")) {
            help = new HelpOperator();
        }
    }

    /** method called after each testcase
     */
    @Override
    protected void tearDown() {
    }
    private static HelpOperator help;

    /** Test invoke  */
    public void testInvoke() {
        // push Escape key to close potentially open popup menu from previous execution
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        long oldTimeout = JemmyProperties.getCurrentTimeout("JMenuOperator.PushMenuTimeout");
        // increase time to open help window
        JemmyProperties.setCurrentTimeout("JMenuOperator.PushMenuTimeout", 60000);
        try {
            help = HelpOperator.invoke();
        } finally {
            // reset timeout
            JemmyProperties.setCurrentTimeout("JMenuOperator.PushMenuTimeout", oldTimeout);
        }
    }

    /** simple test case
     */
    public void testContentsSelection() {
        String text = help.getContentText();
        help.treeContents().selectRow(0);
        new EventTool().waitNoEvent(5000);
        assertTrue(!text.equals(help.getContentText()));
    }

    /** simple test case
     */
    public void testSearchFind() {
        help.searchFind("help");
        new EventTool().waitNoEvent(5000);
        String text = help.getContentText();
        help.searchFind("menu");
        new EventTool().waitNoEvent(5000);
        assertTrue(!text.equals(help.getContentText()));
    }

    /** simple test case
     */
    public void testPreviousAndNext() throws InterruptedException {
        final String text = help.getContentText();
        help.back();
        new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object oper) {
                return text.equals(help.getContentText()) ? null : Boolean.TRUE;
            }

            @Override
            public String getDescription() {
                return ("Text after back not equal to previous text"); // NOI18N
            }
        }).waitAction(null);
        help.next();
        new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object oper) {
                return text.equals(help.getContentText()) ? Boolean.TRUE : null;
            }

            @Override
            public String getDescription() {
                return ("Text after next equal to previous text"); // NOI18N
            }
        }).waitAction(null);
    }

    /** Test btPrint() method. */
    public void testPrint() {
        String tooltip = help.btPrint().getToolTipText();
        if (!tooltip.equals("Print") && !tooltip.equals("Tisk")) {
            fail("btPrint() returned wrong button: " + tooltip);
        }
    }

    /** Test btPageSetup() method. */
    public void testPageSetup() {
        String tooltip = help.btPageSetup().getToolTipText();
        if (!tooltip.equals("Page Setup") && !tooltip.startsWith("Nastaven")) { // "Nastavení stránky"
            fail("btPageSetup() returned wrong button: " + tooltip);
        }
    }

    /** Test close() method. */
    public void testClose() {
        help.close();
    }
}
