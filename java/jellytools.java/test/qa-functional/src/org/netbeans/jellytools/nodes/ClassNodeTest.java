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
package org.netbeans.jellytools.nodes;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.CompileJavaAction;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jellytools.testutils.NodeUtils;

/**
 * Test of org.netbeans.jellytools.nodes.ClassNode
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class ClassNodeTest extends JellyTestCase {

    public static String[] tests = new String[]{
        "testVerifyPopup", "testProperties"
    };

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public ClassNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(ClassNodeTest.class, tests);
    }
    /**
     * ClassNode instance used in all test cases.
     */
    protected static ClassNode classNode = null;

    /** Finds data node before each test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        // find class node
        if (classNode == null) { // NOI18N
            Node sampleClass1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
            MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
            statusTextTracer.start();
            new CompileJavaAction().perform(sampleClass1Node);
            // wait status text "Building SampleProject (compile-single)"
            statusTextTracer.waitText("compile-single", true); // NOI18N
            // wait status text "Finished building SampleProject (compile-single).
            statusTextTracer.waitText("compile-single", true); // NOI18N
            statusTextTracer.stop();
            // create exactly (full match) and case sensitively comparing comparator to distinguish build and build.xml node
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            Node filesProjectNode = new FilesTabOperator().getProjectNode("SampleProject");
            filesProjectNode.setComparator(comparator);
            classNode = new ClassNode(filesProjectNode, "build|classes|sample1|SampleClass1.class"); // NOI18N
        }
    }

    /** Test verifyPopup */
    public void testVerifyPopup() {
        classNode.verifyPopup();
    }

    /** Test properties */
    public void testProperties() {
        classNode.properties();
        NodeUtils.closeProperties("SampleClass1.class");
    }
}
