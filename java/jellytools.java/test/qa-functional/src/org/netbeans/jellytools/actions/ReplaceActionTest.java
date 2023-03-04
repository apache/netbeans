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
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Test org.netbeans.jellytools.actions.ReplaceAction
 *
 * @author Jiri Skrivanek
 */
public class ReplaceActionTest extends JellyTestCase {

    private static final String SAMPLE_CLASS_1 = "SampleClass1";
    private static EditorOperator eo;
    public static final String[] tests = {
        "testPerformMenu",
        "testPerformAPI",
        "testPerformShortcut"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ReplaceActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition */
    public static Test suite() {
        return createModuleTest(ReplaceActionTest.class, tests);
    }

    /** Opens sample class and finds EditorOperator instance */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (eo == null) {
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            JavaNode sampleClass1 = new JavaNode(sample1, SAMPLE_CLASS_1);
            sampleClass1.open();
            eo = new EditorOperator(SAMPLE_CLASS_1);
            eo.requestFocus();
        }
    }

    /** Close open Replace dialog. */
    @Override
    public void tearDown() {
        // waits for close button (it is second button with given tooltip ebcause first one for search row is hidden)
        JButtonOperator closeButton = new JButtonOperator(eo, new JButtonOperator.JComponentByTipFinder("Close Incremental Search Sidebar"));
        closeButton.push();
        // close editor after last test case
        if (getName().equals("testPerformShortcut")) {
            eo.close();
        }
    }

    /** Test performMenu */
    public void testPerformMenu() {
        new ReplaceAction().performMenu(eo);
    }

    /** Test performAPI */
    public void testPerformAPI() {
        new ReplaceAction().performAPI(eo);
    }

    /** Test performShortcut */
    public void testPerformShortcut() {
        new ReplaceAction().performShortcut(eo);
    }
}
