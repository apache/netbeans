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

package org.openide.explorer.propertysheet;

import java.awt.GraphicsEnvironment;
import javax.swing.JWindow;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
public class MorePropertySheetTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MorePropertySheetTest.class);
    }

    public MorePropertySheetTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
    }
    
    public void testSetNodesSurvivesMultipleAdd_RemoveNotifyCalls() throws Exception {
        final PropertySheet ps = new PropertySheet();
        Node n = new AbstractNode( Children.LEAF );
        JWindow window = new JWindow();
        ps.setNodes( new Node[] {n} );
        window.add( ps );
        window.remove( ps );
        window.add( ps );
        window.remove( ps );
        window.add( ps );
        window.remove( ps );
        window.setVisible(true);
        assertNotNull(ps.helperNodes);
        assertEquals("Helper nodes are still available even after several addNotify()/removeNotify() calls",
                ps.helperNodes[0], n);
    }

    @RandomlyFails
    public void testSheetCleared_126818 () throws Exception {
        final PropertySheet ps = new PropertySheet();
        Node n = new AbstractNode( Children.LEAF );
        ps.setNodes( new Node[] {n} );
        Thread.sleep(70);
        ps.setNodes(null);
        
        for (int i = 0; i < 10; i++) {
            Node[] curNodes = ps.getCurrentNodes();
            assertTrue("Cur nodes should be empty", 
                    curNodes == null || curNodes.length == 0);
            Thread.sleep(50);
        }
        
    }
}
