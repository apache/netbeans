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
package org.openide.loaders;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstanceNodeTest extends NbTestCase {
    Node node;

    public InstanceNodeTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        FileObject root = FileUtil.getConfigRoot ();
        DataObject dobj = InstanceDataObject.create (DataFolder.findFolder (root), null, A.class);
        node = dobj.getNodeDelegate ();
        assertTrue ("Is InstanceNode", node instanceof InstanceNode);
    }

    /**
     * Test of getDisplayName method, of class org.openide.loaders.InstanceNode.
     */
    public void testGetDisplayName () throws Exception {
        Node instance = node;
        
        String expResult = "Ahoj";
        // node's name is calculated later, let's wait
        SwingUtilities.invokeAndWait (new Runnable () {
            public void run () {
                
            }
            
        });
        String result = instance.getDisplayName();
        assertEquals(expResult, result);
    }
    
    public static class A extends CallableSystemAction {
        public void performAction () {
        }

        public String getName () {
            assertTrue ("Called from AWT", SwingUtilities.isEventDispatchThread ());
            return "Ahoj";
        }

        public HelpCtx getHelpCtx () {
            assertTrue ("Called from AWT", SwingUtilities.isEventDispatchThread ());
            return HelpCtx.DEFAULT_HELP;
        }
        
    }

}
