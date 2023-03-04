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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/**
 * Test of org.netbeans.jellytools.actions.SaveAction.
 *
 * @author Jiri Skrivanek
 */
public class SaveActionTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testPerformMenu", "testPerformAPI", "testPerformShortcut"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public SaveActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(SaveActionTest.class, tests);
    }
    protected static EditorOperator eo;

    /** Open a source in editor and modify something. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new OpenAction().perform(node);
        eo = new EditorOperator("SampleClass1.java");   // NOI18N
        eo.setCaretPosition(0);
        eo.insert(" ");
        eo.setCaretPosition(0);
        eo.delete(1);
    }

    /** Clean up after each test case. */
    @Override
    protected void tearDown() {
        eo.closeDiscard();
    }

    /** Test of performMenu method. */
    public void testPerformMenu() {
        new SaveAction().performMenu();
        eo.waitModified(false);
    }

    /** Test of performAPI method. */
    public void testPerformAPI() {
        new SaveAction().performAPI();
        eo.waitModified(false);
    }

    /** Test of performShortcut method. */
    public void testPerformShortcut() {
        new SaveAction().performShortcut();
        eo.waitModified(false);
    }
}
