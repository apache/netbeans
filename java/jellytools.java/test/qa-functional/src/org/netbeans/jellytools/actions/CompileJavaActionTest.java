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
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/**
 * Test org.netbeans.jellytools.actions.CleanProjectAction
 *
 * @author Jiri Skrivanek
 */
public class CompileJavaActionTest extends JellyTestCase {

    public static String[] tests = new String[]{
        "testPerformPopup", "testPerformMenu", "testPerformShortcut"
    };

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public CompileJavaActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(CompileJavaActionTest.class, tests);
    }
    private static Node node;
    private static MainWindowOperator.StatusTextTracer statusTextTracer;

    @Override
    public void setUp() throws IOException {
        openDataProjects("SampleProject");
        if (node == null) {
            node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java");
        }
        if (statusTextTracer == null) {
            statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        }
        statusTextTracer.start();
    }

    @Override
    public void tearDown() {
        // wait status text "Building SampleProject (compile-single)"
        statusTextTracer.waitText("compile-single", true); // NOI18N
        // wait status text "Finished building SampleProject (compile-single).
        statusTextTracer.waitText("compile-single", true); // NOI18N
        statusTextTracer.stop();
    }

    /** Test performPopup method. */
    public void testPerformPopup() {
        new CompileJavaAction().performPopup(node);
    }

    /** Test performMenu method. */
    public void testPerformMenu() {
        new CompileJavaAction().performMenu(node);
    }

    /** Test performShortcut method. */
    public void testPerformShortcut() {
        new CompileJavaAction().performShortcut(node);
    }
}
