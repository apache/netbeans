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
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JDialogOperator;

/** Test org.netbeans.jellytools.actions.RenameAction
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class RenameActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public RenameActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(RenameActionTest.class);
    }

    @Override
    protected void setUp() throws IOException {
        openDataProjects("SampleProject");
    }
    private static final String RENAME_TITLE = Bundle.getString("org.openide.actions.Bundle", "CTL_RenameTitle");

    /** Test performPopup */
    public void testPerformPopup() {
        Node node = new Node(new FilesTabOperator().getProjectNode("SampleProject"), "build.xml"); // NOI18N
        new RenameAction().performPopup(node);
        new JDialogOperator(RENAME_TITLE).requestClose();
    }

    /** Test performAPI */
    public void testPerformAPI() {
        Node node = new Node(new FilesTabOperator().getProjectNode("SampleProject"), "src|sample1|SampleClass1.java"); // NOI18N
        new RenameAction().performAPI(node);
        new JDialogOperator(RENAME_TITLE).requestClose();
    }
}
