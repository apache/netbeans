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
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.PropertySheetOperator;

public class ActionsTest extends JellyTestCase {
    
    /** Constructor required by JUnit */
    public ActionsTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. */
    public static Test suite() {
        return createModuleTest(ActionsTest.class);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    public void testActions() {
        // call main menu item "Windows|Properties"
        Action action = new Action("Window|IDE Tools|Properties", null);
        action.perform();
        new PropertySheetOperator().close();
        
        // call main menu item "Help|About" with 'no block' action because a modal
        // dialog is expected to appear
        new ActionNoBlock("Help|About", null).perform();
        new NbDialogOperator("About").close();      
        
        // use specialized action
        CopyAction copyAction = new CopyAction();
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1");
        copyAction.perform(node);
        
        new OpenAction().perform(new Node(node, "SampleClass1.java"));
        EditorOperator eo = new EditorOperator("SampleClass1.java");
        eo.select(1);
        // perform action on an operator
        copyAction.perform(eo);
        
        // perform action in different modes on a node
        copyAction.performMenu(node);
        copyAction.performShortcut(node);
        copyAction.performAPI(node);
        copyAction.performPopup(node);
        
        System.out.println("\nFinished.");
    }
}
