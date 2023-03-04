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

package org.openide.actions;

import org.netbeans.junit.*;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import java.util.Arrays;
import org.openide.windows.TopComponent;

/** Test behavior of MoveUpAction (also MoveDownAction and ReorderAction).
 * @author Jesse Glick
 */
public class MoveUpActionTest extends NbTestCase {

    static {
        // Get Lookup right to begin with.
        ActionsInfraHid.class.getName();
    }
    
    public MoveUpActionTest(String name) {
        super(name);
    }
    
    private Node n, n1, n2, n3;
    
    protected @Override void setUp() throws Exception {
        n1 = new AbstractNode(Children.LEAF);
        n1.setName("n1");
        n2 = new AbstractNode(Children.LEAF);
        n2.setName("n2");
        n3 = new AbstractNode(Children.LEAF);
        n3.setName("n3");
        final Index.ArrayChildren c = new Index.ArrayChildren() {
            {
                add(new Node[] {n1, n2, n3});
            }
            public @Override void reorder() {
                reorder(new int[] {1, 2, 0});
            }
        };
        n = new AbstractNode(c) {
            {
                getCookieSet().add(c);
            }
        };
        n.setName("n");
    }
    
    /**
     * in order to run in awt event queue
     * fix for #39789
     */
    protected @Override boolean runInEQ()
    {
        return true;
    }
    
    public void testBasicUsage() throws Exception {
        SystemAction mua = SystemAction.get(MoveUpAction.class);
        SystemAction mda = SystemAction.get(MoveDownAction.class);
        SystemAction roa = SystemAction.get(ReorderAction.class);
        ActionsInfraHid.WaitPCL l = null;
        TopComponent tc = new TopComponent();
        tc.requestActive();
        try {
            assertNull(tc.getActivatedNodes());
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            l = new ActionsInfraHid.WaitPCL(SystemAction.PROP_ENABLED);
            mua.addPropertyChangeListener(l);
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertTrue(roa.isEnabled());
            assertEquals(Arrays.asList(new Node[] {n1, n2, n3}), Arrays.asList(n.getChildren().getNodes()));
            roa.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n3, n1, n2}), Arrays.asList(n.getChildren().getNodes()));
            assertTrue(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n1, n2});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n1});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue("MoveUp is enabled on a node in the middle of its parents", mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            mua.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n1, n3, n2}), Arrays.asList(n.getChildren().getNodes()));
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue("MoveUp is turned off after a node is moved to the very top", !mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n2});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n3});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            mda.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n1, n2, n3}), Arrays.asList(n.getChildren().getNodes()));
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
        } finally {
            if (l != null) {
                mua.removePropertyChangeListener(l);
                mda.removePropertyChangeListener(l);
                roa.removePropertyChangeListener(l);
            }
            tc.setActivatedNodes(new Node[0]);
            tc.setActivatedNodes(null);
        }
    }
    
}
