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

package org.netbeans.core.windows;

import org.netbeans.junit.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import org.openide.windows.*;


/** 
 * 
 * @author Dafe Simonek
 */
public class Bug82319Test extends NbTestCase {

    public Bug82319Test (String name) {
        super (name);
    }

    protected boolean runInEQ () {
        return true;
    }
     
    public void test82319ActivatedNodesUpdate () throws Exception {
        Node node1 = new AbstractNode(Children.LEAF);
        Node node2 = new AbstractNode(Children.LEAF);
        
        Mode mode = WindowManagerImpl.getInstance().createMode("test82319Mode",
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        
        TopComponent tc1 = new TopComponent();
        tc1.setActivatedNodes(new Node[] { node1 });
        mode.dockInto(tc1);
        
        TopComponent tc2 = new TopComponent();
        tc2.setActivatedNodes(null);
        mode.dockInto(tc2);
        
        tc1.open();
        tc2.open();
        
        tc1.requestActive();
        
        System.out.println("Checking bugfix 82319...");
        Node[] actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node1);

        tc2.requestActive();
        
        // activated nodes should stay the same, tc2 doesn't have any
        actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node1);
        
        tc1.setActivatedNodes(new Node[] { node2 });
        
        System.out.println("Checking update of activated nodes...");
        // activated nodes should change, as still nodes should be grabbed from tc1 
        actNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        assertTrue("Expected 1 activated node, but got " + actNodes.length, actNodes.length == 1);
        assertSame("Wrong activated node", actNodes[0], node2);
    }
    
}
