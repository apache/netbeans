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
package org.netbeans.jellytools.actions;

import java.awt.Toolkit;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

/** Test org.netbeans.jellytools.actions.CutAction.
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class CutActionTest extends JellyTestCase {

    public static final String[] tests = new String[]{"testPerformPopup", "testPerformMenu", "testPerformAPI", "testPerformShortcut"};

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CutActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(CutActionTest.class, tests);
    }
    private Object clipboard1;
    private static Node node;

    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");  // NOI18N
        openDataProjects("SampleProject");
        clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (node == null) {
            node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java");
        }
    }

    @Override
    public void tearDown() throws Exception {
        Waiter waiter = new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                Object clipboard2 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                return clipboard1 != clipboard2 ? Boolean.TRUE : null;
            }

            @Override
            public String getDescription() {
                return ("Wait clipboard contains data");
            }
        });
        waiter.waitAction(null);
    }

    /** Test performPopup */
    public void testPerformPopup() {
        new CutAction().performPopup(node);
    }

    /** Test performMenu.  */
    public void testPerformMenu() {
        new CutAction().performMenu(node);
    }

    /** Test performAPI. */
    public void testPerformAPI() {
        new CutAction().performAPI(node);
    }

    /** Test performShortcut. */
    public void testPerformShortcut() {
        new CutAction().performShortcut(node);
    }
}
