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

package org.netbeans.modules.viewmodel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.SwingUtilities;

import org.netbeans.junit.NbTestCase;

import org.netbeans.spi.viewmodel.*;

import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;



/**
 *
 */
public class ModelEventTest  extends NbTestCase implements NodeListener {

    BasicTest.CompoundModel cm;
    Node n;
    volatile Object event;
    Vector propEvents = new Vector();

    public ModelEventTest (String s) {
        super (s);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ArrayList l = new ArrayList ();
        cm = new CompoundModel1 ();
        l.add (cm);
        OutlineTable tt = BasicTest.createView(Models.createCompoundModel (l));
        BasicTest.waitFinished (tt.currentTreeModelRoot.getRootNode().getRequestProcessor());
        n = tt.getExplorerManager ().getRootContext ();
        n.addNodeListener(this);
    }

    public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
        assertNull("Already fired", event);
        event = ev;
    }

    public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
        assertNull("Already fired", event);
        event = ev;
    }

    public void childrenReordered(org.openide.nodes.NodeReorderEvent ev) {
        assertNull("Already fired", event);
        event = ev;
    }

    public void nodeDestroyed(org.openide.nodes.NodeEvent ev) {
        assertNull("Already fired", event);
        event = ev;
    }

    public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
        propEvents.add(propertyChangeEvent.getPropertyName());
        /*
        System.out.println("propertyChangeEvent = "+propertyChangeEvent);
        assertNull("Already fired", event);
        event = propertyChangeEvent;
         */
    }
    
    public void testDisplayName() {
        ModelEvent e = new ModelEvent.NodeChanged(this, "Root", ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
        cm.fire(e);
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {}
            });
        } catch (InterruptedException iex) {
            fail(iex.toString());
        } catch (InvocationTargetException itex) {
            fail(itex.toString());
        }
        assertTrue("Display Name was not fired", propEvents.contains(Node.PROP_DISPLAY_NAME));
        //assertNotNull("Was not fired", this.event);
    }

    public void testIcon() {
        ModelEvent e = new ModelEvent.NodeChanged(this, "Root", ModelEvent.NodeChanged.ICON_MASK);
        cm.fire(e);
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {}
            });
        } catch (InterruptedException iex) {
            fail(iex.toString());
        } catch (InvocationTargetException itex) {
            fail(itex.toString());
        }
        assertTrue("Icon was not fired", propEvents.contains(Node.PROP_ICON));
    }

    public void testShortDescription() {
        ModelEvent e = new ModelEvent.NodeChanged(this, "Root", ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK);
        cm.fire(e);
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {}
            });
        } catch (InterruptedException iex) {
            fail(iex.toString());
        } catch (InvocationTargetException itex) {
            fail(itex.toString());
        }
        assertTrue("Short Description was not fired", propEvents.contains(Node.PROP_SHORT_DESCRIPTION));
    }

    public void testChildren() {
        n.getChildren().getNodes();
        /*
        ModelEvent e = new ModelEvent.NodeChanged(this, "Root", ModelEvent.NodeChanged.CHILDREN_MASK);
        cm.fire(e);
        try {
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {}
            });
        } catch (InterruptedException iex) {
            fail(iex.toString());
        } catch (InvocationTargetException itex) {
            fail(itex.toString());
        }
        //assertTrue("Short Description was not fired", propEvents.contains(Node.PROP_));
        assertNotNull("Children were not fired", this.event);
         */
    }

    public final class CompoundModel1 extends BasicTest.CompoundModel {
        
        int dn = 0;
        int ib = 0;
        int sd = 0;
        int cc = 0;
        
        @Override
        protected void addCall (String methodName, Object node) {
            // Ignore multiple calls
        }

        // init ....................................................................

        @Override
        public String getDisplayName (Object node) throws UnknownTypeException {
            String dns = super.getDisplayName(node);
            dns += (dn++);
            return dns;
        }
        
        @Override
        public String getIconBase (Object node) throws UnknownTypeException {
            String ibs = super.getIconBase(node);
            ibs += (ib++);
            return ibs;
        }
        
        @Override
        public String getShortDescription (Object node) throws UnknownTypeException {
            String sds = super.getShortDescription(node);
            sds += (sd++);
            return sds;
        }
        
        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        @Override
        public synchronized int getChildrenCount (Object node) throws UnknownTypeException {
            return super.getChildrenCount (node) + (cc++);
        }
        
        @Override
        public Object[] getChildren (Object parent, int from, int to) throws UnknownTypeException {
            //System.err.println("CompoundModel1.getChildren("+parent+", "+from+", "+to+")");
            //Thread.dumpStack();
            addCall ("getChildren", parent);
            Object[] ch = new Object[3 + (cc - 1)];
            if (parent == ROOT) {
                for (int i = 0; i < ch.length; i++) {
                    // Use  Character.valueOf() on 1.5
                    ch[i] = new Character((char) ('a' + i)).toString();
                }
                return ch;
            }
            if (parent instanceof String) {
                for (int i = 0; i < ch.length; i++) {
                    // Use  Character.valueOf() on 1.5
                    ch[i] = ((String) parent + new Character((char) ('a' + i)).toString());
                }
                return ch;
            }
            throw new UnknownTypeException (parent);
        }
    }
}
