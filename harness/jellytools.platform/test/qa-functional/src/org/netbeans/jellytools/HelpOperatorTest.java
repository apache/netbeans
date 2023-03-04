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
