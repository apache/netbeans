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

/** Test org.netbeans.jellytools.actions.DeleteAction
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class DeleteActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DeleteActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(DeleteActionTest.class);
    }
    private static Node node;

    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");  // NOI18N
        openDataProjects("SampleProject");
        if (node == null) {
            node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        }
    }

    @Override
    public void tearDown() {
        // "Delete"
        String deleteTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_SafeDel_Delete"); // NOI18N
        new NbDialogOperator(deleteTitle).close();
        // On some linux it may happen autorepeat is activated and it 
        // opens dialog multiple times. So, we need to close all modal dialogs.
        // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
        closeAllModal();
    }

    /** Test performPopup */
    public void testPerformPopup() {
        new DeleteAction().performPopup(node);
    }

    /** Test performMenu */
    public void testPerformMenu() {
        new DeleteAction().performMenu(node);
    }

    /** Test performAPI.  */
    public void testPerformAPI() {
        new DeleteAction().performAPI(node);
    }

    /** Test performShortcut. */
    public void testPerformShortcut() {
        new DeleteAction().performShortcut(node);
    }
}
