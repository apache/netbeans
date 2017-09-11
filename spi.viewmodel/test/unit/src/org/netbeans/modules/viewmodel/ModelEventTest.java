/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
