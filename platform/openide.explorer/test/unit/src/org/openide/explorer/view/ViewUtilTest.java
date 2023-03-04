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
package org.openide.explorer.view;

import java.awt.EventQueue;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

public class ViewUtilTest extends NbTestCase {
    
    public ViewUtilTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected int timeOut() {
        return 20000;
    }
    
    public void testRenameNormalNode() {
        Node n = new AbstractNode(Children.LEAF) {
            @Override
            public void setName(String s) {
                assertTrue("In AWT", EventQueue.isDispatchThread());
                super.setName(s);
            }
        };
        
        n.setName("newName");
        assertEquals("newName", n.getName());
    }

    public void testRenameForSlowNode() {
        Node n = new AbstractNode(Children.LEAF) {
            boolean renamed;
            
            @Override
            public synchronized void setName(String s) {
                renamed = true;
                notifyAll();
                assertFalse("Not in AWT", EventQueue.isDispatchThread());
                super.setName(s);
            }

            @Override
            public synchronized String toString() {
                while (!renamed) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return super.getName();
            }
        };

        n.setValue("slowRename", true);
        ViewUtil.nodeRename(n, "newName");
        assertEquals("newName", n.toString());
    }
}
