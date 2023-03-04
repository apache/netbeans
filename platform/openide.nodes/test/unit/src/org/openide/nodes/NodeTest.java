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
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import junit.textui.TestRunner;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

/** Checking some of the behaviour of Node (and AbstractNode).
 * @author Jaroslav Tulach, Jesse Glick
 */
public class NodeTest extends NbTestCase {

    public NodeTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NodeTest.class));
    }

    public void testGetActions () throws Exception {
        final SystemAction[] arr1 = {
            SystemAction.get (PropertiesAction.class)
        };
        final SystemAction[] arr2 = {
        };
        
        AbstractNode an = new AbstractNode (Children.LEAF) {
            @Override
            public SystemAction[] getActions () {
                return arr1;
            }
            
            @Override
            public SystemAction[] getContextActions () {
                return arr2;
            }
        };
        
        
        assertEquals ("getActions(false) properly delegates to getActions()", arr1, an.getActions (false));
        assertEquals ("getActions(true) properly delegates to getContextActions()", arr2, an.getActions (true));
        
    }
    
    public void testCanCallNodeSetChildrenFromReadAccess() throws Exception {
        CharSequence log = Log.enable("global`", Level.WARNING);
        class Mn extends AbstractNode implements Runnable {
            public Mn() {
                super(Children.LEAF);
            }

            public void run() {
                setChildren(new Children.Array());
            }
        }
        Mn mn = new Mn();
        
        Children.MUTEX.readAccess(mn);
        
        assertEquals("Log is empty", "", log.toString());
    }

    public void testCanCreateNodeWithoutChildrenMutex() {
        final CountDownLatch l1 = new CountDownLatch(1);
        final CountDownLatch l2 = new CountDownLatch(1);

        Thread t1 = new Thread() {

            @Override
            public void run() {
                if (!Children.MUTEX.isWriteAccess()){
                    Children.MUTEX.writeAccess(this);
                    return;
                }
                try {
                    l2.countDown();
                    l1.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        Thread t2 = new Thread() {

            @Override
            public void run() {
                try {
                    l2.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                Node n = new AbstractNode(new Children.Array());
                l1.countDown();
            }
        };

        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        for (int i = 0; i < 1000; i++) {
            try {
                t1.join(10);
                t2.join(10);
                if (!t1.isAlive() && !t2.isAlive()) {
                    return;
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        fail("It seems Node creation requires Children.MUTEX");
    }

    public void testPreferredAction() throws Exception {
        final SystemAction a1 = SystemAction.get(PropertiesAction.class);
        final Action a2 = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {}
        };
        final SystemAction a3 = SystemAction.get(OpenAction.class);
        final Action a4 = new AbstractAction() {
            public void actionPerformed(ActionEvent ev) {}
        };
        // Old code:
        Node n1 = new AbstractNode(Children.LEAF) {
            {
                setDefaultAction(a1);
            }
        };
        Node n2 = new AbstractNode(Children.LEAF) {
            @Override
            public SystemAction getDefaultAction() {
                return a1;
            }
        };
        // New code:
        Node n4 = new AbstractNode(Children.LEAF) {
            @Override
            public Action getPreferredAction() {
                return a1;
            }
        };
        // emulation of DataNode
        Node n5 = new AbstractNode(Children.LEAF) {
            {
                setDefaultAction (a1);
            }
            
            @Override
            public SystemAction getDefaultAction () {
                return super.getDefaultAction ();
            }
        };
        Node n6 = new AbstractNode(Children.LEAF) {
            @Override
            public Action getPreferredAction() {
                return a2;
            }
        };
        // Wacko code:
        Node n7 = new AbstractNode(Children.LEAF) {
            {
                setDefaultAction(a1);
            }
            @Override
            public SystemAction getDefaultAction() {
                return a3;
            }
        };
        assertEquals(a1, n1.getDefaultAction());
        assertEquals(a1, n1.getPreferredAction());
        assertEquals(a1, n2.getDefaultAction());
        assertEquals(a1, n2.getPreferredAction());
        assertEquals(a1, n4.getDefaultAction());
        assertEquals(a1, n4.getPreferredAction());
        assertEquals(a1, n5.getPreferredAction());
        assertEquals(a1, n5.getDefaultAction());
        assertEquals(null, n6.getDefaultAction());
        assertEquals(a2, n6.getPreferredAction());
        assertEquals(a3, n7.getDefaultAction());
        assertEquals(a3, n7.getPreferredAction());
    }

    public void testShortDescriptionCanBeSetToNull () {
        class PCL extends NodeAdapter {
            public int cnt;
            
            @Override
            public void propertyChange (PropertyChangeEvent ev) {
                if (Node.PROP_SHORT_DESCRIPTION.equals (ev.getPropertyName ())) {
                    cnt++;
                }
            }
        }
        
        AbstractNode an = new AbstractNode (Children.LEAF);
        an.setDisplayName ("My name");
        
        PCL pcl = new PCL ();
        an.addNodeListener (pcl);
        assertEquals ("My name", an.getShortDescription ());
        
        an.setShortDescription ("Ahoj");
        assertEquals ("Ahoj", an.getShortDescription ());
        assertEquals ("One change", 1, pcl.cnt);
        
        an.setShortDescription (null);
        assertEquals ("My name", an.getShortDescription ());
        assertEquals ("Second change", 2, pcl.cnt);
    }
    
    /** Another sample action */
    public static final class PropertiesAction extends OpenAction {
        
    }
}
