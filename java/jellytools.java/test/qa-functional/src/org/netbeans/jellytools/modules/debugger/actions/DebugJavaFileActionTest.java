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
package org.netbeans.jellytools.modules.debugger.actions;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/**
 * Test DebugJavaFileAction.
 *
 * @author Martin Schovanek
 */
public class DebugJavaFileActionTest extends JellyTestCase {

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public DebugJavaFileActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(DebugJavaFileActionTest.class);
    }
    private static final String SAMPLE_CLASS_1 = "SampleClass1";
    private static JavaNode sampleClass1 = null;
    private static final String outputTitle = "SampleProject (debug-single)";

    @Override
    protected void setUp() throws IOException {
        openDataProjects("SampleProject");
        System.out.println("### " + getName() + " ###");
        if (sampleClass1 == null) {
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            sampleClass1 = new JavaNode(sample1, SAMPLE_CLASS_1);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        new OutputTabOperator(outputTitle).waitText("BUILD SUCCESSFUL");
        super.tearDown();
    }

    /** Test performMenu() method. */
    public void testPerformMenu() {
        new DebugJavaFileAction().performMenu(sampleClass1);
    }

    /** Test performMenu() method. */
    public void testPerformPopup() {
        new DebugJavaFileAction().performPopup(sampleClass1);
    }

    /** Test performMenu() method. */
    public void testPerformShortcut() {
        new DebugJavaFileAction().performShortcut(sampleClass1);
    }
}
