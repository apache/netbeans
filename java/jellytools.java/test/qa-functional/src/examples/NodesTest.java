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
package examples;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;

public class NodesTest extends JellyTestCase {

    /** Constructor required by JUnit */
    public NodesTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(NodesTest.class);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    public void testNodes() {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        // find node in given tree
        Node node = new Node(pto.tree(), "SampleProject|Source Packages|sample1");
        // find node under given parent node
        Node node1 = new Node(node, "SampleClass1.java");
        // select node
        node1.select();
        // create instance of specialized JavaNode
        JavaNode javaNode = new JavaNode(node, "SampleClass1.java");
        // call predefined action
        javaNode.copy();

        System.out.println("\nFinished.");
    }
}
