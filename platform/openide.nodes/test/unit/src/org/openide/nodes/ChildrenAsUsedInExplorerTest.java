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

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.*;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * If filter node is asked for children (under MUTEX.readAccess) while
 * original node's children are initializing (so filter node cannot wait)
 * it has to be guaranteed that filter node is notified (after original's children
 * initialization) to be able to refresh
 */
public class ChildrenAsUsedInExplorerTest extends NbTestCase {

    public ChildrenAsUsedInExplorerTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.WARNING;
    }

    protected boolean lazy() {
        return false;
    }

    public void testGetNodesInReadAccessInitializeInAnotherThread() throws Exception {
        final Logger logger = Logger.getLogger("test.org.openide.nodes");
        class K extends Children.Keys<Object> implements Runnable {

            K() {
                super(lazy());
            }
            Node[] mainNodes;
            Node[] sndNodes;
            RequestProcessor.Task sndTask;

            public void run() {
                if (Children.MUTEX.isReadAccess()) {
                    sndTask = RequestProcessor.getDefault().post(this);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    logger.warning("Before getNodes");
                    mainNodes = getNodes();
                    logger.warning("After getNodes: " + Arrays.asList(mainNodes));
                } else {
                    logger.warning("Before getNodes");
                    sndNodes = getNodes();
                    logger.warning("After getNodes: " + Arrays.asList(sndNodes));
                }
            }

            @Override
            protected synchronized void addNotify() {
                logger.warning("Before setKeys()");
                setKeys(new String[]{"1", "2"});
                logger.warning("After setKeys()");
            }

            protected Node[] createNodes(Object key) {
                AbstractNode n = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                logger.warning("returning node " + key);
                return new Node[]{n};
            }
        }


        K keys = new K();
        Node n = new AbstractNode(keys);
        Listener l = new Listener();
        n.addNodeListener(l);


        Children.MUTEX.readAccess(keys);

        keys.sndTask.waitFinished();

        assertEquals("First thread saw no children", 0, keys.mainNodes.length);
        assertEquals("Snd thread saw them all", 2, keys.sndNodes.length);

        assertTrue("Children notified to be added", l.added);
        assertEquals("Now we have two children", 2, keys.getNodesCount());
    }
    
   /** test whether filter node will have the same children as original
    * on getNodes() (under described circumstances)
    */
    public void testChildrenAsUsedInExplorer () throws Exception {
        final Logger logger = Logger.getLogger("test.org.openide.nodes");
        final AtomicBoolean b = new AtomicBoolean(false);
        class K extends Children.Keys<Object> implements Runnable {
            K() { super(lazy()); }
            Node node;
            Node[] nodes;
            public void run () {
                if (!MUTEX.isReadAccess()) {
                    MUTEX.readAccess (this);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    logger.warning("getNodes() (without read access)");
                    nodes = node.getChildren().getNodes();
                    logger.warning("Result (without read access): " + Arrays.asList(nodes));
                    b.set(true);
                    return;
                }
                logger.warning("In read access waiting");
                
                try {
                    Thread.sleep (100);
                } catch (InterruptedException ex) {
                    fail ("No interrupts");
                }
                logger.warning("getNodes() (read access)");
                nodes = node.getChildren().getNodes();
                logger.warning("Result (read access): " + Arrays.asList(nodes));
            }
            
            @Override
            protected synchronized void addNotify () {
                logger.warning("Before setKeys()");
                setKeys(new String[] {"1", "2"});
                logger.warning("After setKeys()");
            }
            
            protected Node[] createNodes (Object key) {
                AbstractNode n  = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                logger.warning("returning node " + key);
                return new Node[] {n};
            }

        }
        
        K keys = new K ();
        AbstractNode anode = new AbstractNode(keys);
        keys.node = new FilterNode(anode);
        
        SwingUtilities.invokeLater(keys);
        Thread.sleep(30);
        logger.warning("Main getNodes()");
        Node[] nodes = anode.getChildren().getNodes(true);
        
        logger.warning("Main getNodes() result: " + Arrays.asList(nodes));
        while (!b.get()) {
            Thread.sleep(100);
        }
        assertEquals(2, keys.node.getChildren().getNodes().length);
        assertEquals(Arrays.asList(nodes), Arrays.asList(keys.nodes));
        //fail("OK");
    }

        
   /** test whether Listener on filter node will be notified after original's
    * children are initialized (under described circumstances)
    */
    public void testChildrenAsUsedInExplorerWithListener () throws Exception {
        final Logger logger = Logger.getLogger("test.org.openide.nodes");

        class K extends Children.Keys<Object> implements Runnable {
            K() { super(lazy()); }
            
            Node node;
            Node[] nodes;
            public void run () {
                if (!MUTEX.isReadAccess()) {
                    MUTEX.readAccess (this);
                    return;
                }
                logger.warning("In read access waiting");
                
                try {
                    Thread.sleep (100);
                } catch (InterruptedException ex) {
                    fail ("No interrupts");
                }
                logger.warning("getNodes() (read access)");
                nodes = node.getChildren().getNodes();
                logger.warning("Result (read access): " + Arrays.asList(nodes));
            }
            
            @Override
            protected synchronized void addNotify () {
                logger.warning("Before setKeys()");
                setKeys(new String[] {"1", "2"});
                logger.warning("After setKeys()");
            }
            
            protected Node[] createNodes (Object key) {
                AbstractNode n  = new AbstractNode(Children.LEAF);
                n.setName(key.toString());
                logger.warning("returning node " + key);
                return new Node[] {n};
            }

        }
        
        K keys = new K ();
        Listener listener = new Listener();
        AbstractNode anode = new AbstractNode(keys);
        keys.node = new FilterNode(anode);
        keys.node.addNodeListener(listener);
        
        SwingUtilities.invokeLater(keys);
        Thread.sleep(30);
        logger.warning("Main getNodes()");
        Node[] nodes = anode.getChildren().getNodes(true);
        
        logger.warning("Main getNodes() result: " + Arrays.asList(nodes));
        assertTrue("Listener should be notified", listener.added);
        assertEquals(2, keys.node.getChildren().getNodes().length);
        assertEquals(Arrays.asList(nodes), Arrays.asList(keys.node.getChildren().getNodes()));
        //fail("OK");
    }
    private class Listener implements NodeListener {
        boolean added;

        public void childrenAdded(NodeMemberEvent ev) {
            added = true;
        }

        public void childrenRemoved(NodeMemberEvent ev) {
        }

        public void childrenReordered(NodeReorderEvent ev) {
        }

        public void nodeDestroyed(NodeEvent ev) {
        }

        public void propertyChange(PropertyChangeEvent evt) {
        }

    }
}
