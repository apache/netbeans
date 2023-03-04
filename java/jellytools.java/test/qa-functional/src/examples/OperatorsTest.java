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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.BuildJavaProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

public class OperatorsTest extends JellyTestCase {

    /** Constructor required by JUnit */
    public OperatorsTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(OperatorsTest.class);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    public void testOperators() {
        ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode("SampleProject");
        new BuildJavaProjectAction().perform(projectNode);
        
        // toolbars, status text
        MainWindowOperator.getDefault().waitStatusText("SampleProject");
        
        SourcePackagesNode sourceNode = new SourcePackagesNode("SampleProject");
        new OpenAction().perform(new Node(sourceNode, "sample1|SampleClass1.java"));

        new FilesTabOperator();
        FavoritesOperator.invoke();
        RuntimeTabOperator.invoke().getRootNode();

        // editing, toolbar, annotations
        new EditorOperator("SampleClass1.java").insert("INSERTED");
        EditorOperator.closeDiscardAll();

        // popup, getText, waitText
        //new OutputTabOperator("SampleProject").waitText("compile");

        // pushMenuOnTab, close, cloneDocument
        new TopComponentOperator("Projects");

        new JavaNode(sourceNode, "sample1|SampleClass1.java").delete();
        new NbDialogOperator("Delet").close();
    }

    /** Wait for something using Jemmy. If time to wait expires and condition
     * is not true, it throws JemmyException.
     */
    public void testWaiting() throws Exception {
        final ProjectsTabOperator pto = new ProjectsTabOperator();
        new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                // it should be true in general
                return pto.isShowing() ? Boolean.TRUE : null;
            }

            @Override
            public String getDescription() {
                return ("Wait Projects tab is showing");
            }
        }).waitAction(null);
    }
}
