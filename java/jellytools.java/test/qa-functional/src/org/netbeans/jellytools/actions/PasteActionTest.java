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

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/** Test org.netbeans.jellytools.actions.PasteAction
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class PasteActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public PasteActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(PasteActionTest.class);
    }
    // "Confirm Object Deletion"
    private static final String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
    private static Node sample1Node;
    private static final String SAMPLE_FILE = "properties.properties";  //NOI18N
    private static final String PASTED_FILE = "properties_1.properties";  //NOI18N

    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");  // NOI18N
        openDataProjects("SampleProject");
        if (sample1Node == null) {
            sample1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        }
        new CopyAction().perform(new Node(sample1Node, SAMPLE_FILE));
    }

    @Override
    public void tearDown() {
        Node pastedNode = new Node(sample1Node, PASTED_FILE);
        new DeleteAction().perform(pastedNode);
        new NbDialogOperator(confirmTitle).yes();
        pastedNode.waitNotPresent();
    }

    /** Test performPopup  */
    public void testPerformPopup() {
        new PasteAction().performPopup(sample1Node);
    }

    /** Test performMenu  */
    public void testPerformMenu() {
        new PasteAction().performMenu(sample1Node);
    }

    /** Test performAPI */
    public void testPerformAPI() {
        new PasteAction().performAPI(sample1Node);
    }

    /** Test performShortcut */
    public void testPerformShortcut() {
        new PasteAction().performShortcut(sample1Node);
    }
}
