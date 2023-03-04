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

import junit.framework.Test;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

/**
 * Test of NavigatorOperator.
 * @author Jindrich Sedek
 */
public class NavigatorOperatorTest extends JellyTestCase {

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NavigatorOperatorTest(String testName) {
        super(testName);
    }

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(NavigatorOperatorTest.class, "testOperator");
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        Node sourcePackagesNode = new Node(new ProjectsTabOperator().getProjectRootNode("SampleProject"), "Source Packages");
        Node lrNode = new Node(sourcePackagesNode, "sample1|SampleClass1.java");
        new OpenAction().perform(lrNode);
    }

    /** Invokes and verifies the dialog. */
    public void testOperator() {
        new EditorOperator("SampleClass1.java").setVisible(true);
        // test INVOKE
        NavigatorOperator operator = NavigatorOperator.invokeNavigator();
        assertNotNull(operator);
        // test CONSTRUCTOR
        NavigatorOperator operator2 = new NavigatorOperator();
        assertNotNull(operator2);
        assertNotNull("TREE", operator.getTree());
    }
}
