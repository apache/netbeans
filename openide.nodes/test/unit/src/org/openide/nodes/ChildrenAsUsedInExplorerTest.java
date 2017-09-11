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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
