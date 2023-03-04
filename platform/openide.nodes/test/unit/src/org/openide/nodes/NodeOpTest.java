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

package org.openide.nodes;

import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;

/** Checking some of the behaviour of Node.
 * @author Jaroslav Tulach
 */
public class NodeOpTest extends NbTestCase {

    public NodeOpTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NodeOpTest.class));
    }


    private static final class A extends AbstractAction {
        public void actionPerformed(ActionEvent ev) {
        }
    }
    
    public void testFindActions () throws Exception {
        class N extends AbstractNode {
            private Action[] arr;
            
            N (Action[] arr) {
                super (org.openide.nodes.Children.LEAF);
                this.arr = arr;
            }
            
            public Action[] getActions (boolean f) {
                return arr;
            }
        }
        
        Action[] arr = { new A(), new A(), new A(), new A() };
        
        assertArray (
            "Finding actions for one node is simple",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr) })
        );

        assertArray (
            "Finding actions for two nodes with same actions",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr), new N (arr) })
        );
            
        assertArray (
            "Finding actions for three nodes with same actions",
            arr, 
            NodeOp.findActions(new Node[] { new N (arr), new N (arr), new N (arr) })
        );
          
            
        assertArray (
            "Otherwise only common actions are taken",
            new Action[] { arr[3] }, 
            NodeOp.findActions(new Node[] { new N (arr), new N (new Action[] { arr[3], null }) })
        );
            
    }
    
    /**
     * Test that it is OK to return a different list each time.
     * There was a bug in NodeOp preventing this.
     */
    public void testFindActions2() throws Exception {
        class N extends AbstractNode {
            private Action a1 = new A();
            private Action a3 = new A();
            N() {
                super (org.openide.nodes.Children.LEAF);
            }
            public Action[] getActions(boolean f) {
                return new Action[] {
                    a1,
                    new A(),
                    a3,
                };
            }
        }
        Action[] actions = NodeOp.findActions(new Node[] {new N()});
        assertEquals("NodeOp.findActions does not gratuitously remove nonconstant actions", 3, actions.length);
    }
    
    private static void assertArray (String msg, Object[] a1, Object[] a2) {
        assertEquals(msg, Arrays.asList(a1), Arrays.asList(a2));
    }
}
