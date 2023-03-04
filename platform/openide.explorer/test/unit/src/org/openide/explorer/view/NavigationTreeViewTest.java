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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/** Check the behaviour of the view when navigating in it.
 *
 * @author  Jaroslav Tulach
 */
@RandomlyFails // NB-Core-Build #1187
public class NavigationTreeViewTest extends NbTestCase {
    
    private TreeView treeView;
    private ExplorerWindow testWindow;
    private CharSequence log;
    private Object enter;
    private Logger LOG;
    
    public NavigationTreeViewTest(String testName) {
        super(testName);
        LOG = Logger.getLogger(NavigationTreeViewTest.class.getName() + "." + getName());
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected boolean lazy() {
        return false;
    }

    @Override
    protected void runTest() throws Throwable {
        log = Log.enable(VisualizerNode.LOG.getName(), Level.FINEST);
        super.runTest();
        if (log.length() > 0 && log.toString().indexOf("Children.MUTEX") >= 0) {
            fail("something has been logged:\n" + log);
        }
    }

    @Override
    protected void setUp() throws Exception {
        assertFalse("Cannot run in AWT thread", EventQueue.isDispatchThread());
        treeView = new BeanTreeView();
        testWindow = new ExplorerWindow(treeView);
        testWindow.pack();
        testWindow.setVisible(true);

        for (int i = 0; i < 10; i++) {
            LOG.log(Level.INFO, "Is showing {0}", i);
            if (testWindow.isShowing()) {
                break;
            }
            Thread.sleep(200);
        }

        assertTrue("Tree is visible", testWindow.isShowing());
        enter = treeView.tree.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        assertNotNull("Enter has assigned key", enter);
        LOG.info("Setup is over");
    }

    @Override
    protected void tearDown() throws Exception {
        testWindow.setVisible(false);
    }



    public void testStructureFullOfFormFiles() throws Exception {
        if ((
            Utilities.getOperatingSystem() & 
            (Utilities.OS_SOLARIS | Utilities.OS_SUNOS)
        ) != 0) {
            LOG.log(Level.CONFIG, "Giving up, this test fails too randomly on Solaris");
            return;
        }
        
        Children ch = new Children.Array();
        Node root = new AbstractNode(ch);
        root.setName(getName());

        ch.add(nodeWith("A", "-A", "-B", "B"));
        ch.add(nodeWith("X", "Y", "Z"));

        final Node first = ch.getNodes()[0];

        LOG.log(Level.INFO, "Nodes are ready: {0}", root);
        final ExplorerManager em = testWindow.getExplorerManager();
        em.setRootContext(root);
        LOG.info("setRootContext done");
        em.setSelectedNodes(new Node[] { first });
        LOG.log(Level.INFO, "setSelectedNodes to {0}", first);
        LOG.log(Level.INFO, "Verify setSelectedNodes: {0}", Arrays.asList(em.getSelectedNodes()));

        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                TreePath path = treeView.tree.getSelectionPath();
                LOG.log(Level.INFO, "getSelectionPath {0}", path);
                LOG.log(Level.INFO, "getSelectedNodes {0}", Arrays.toString(em.getSelectedNodes()));
                assertNotNull("Something is selected", path);
                Node node = Visualizer.findNode(path.getLastPathComponent());
                assertEquals("It is the first node", first, node);
            }
        });
        
        sendAction("expand");
        sendAction("selectNext");

        assertEquals("Explored context is N0", first, em.getExploredContext());
        assertEquals("Selected node is A", 1, em.getSelectedNodes().length);
        assertEquals("Selected node is A", "A", em.getSelectedNodes()[0].getName());

        sendAction(enter);

        Keys keys = (Keys)first.getChildren();
        assertEquals("One invocation", 1, keys.actionPerformed);
        assertFalse("No write access", keys.writeAccess);
        assertFalse("No read access", keys.readAccess);
    }

    private void sendAction(final Object key) throws Exception {
        class Process implements Runnable {
            @Override
            public void run() {
                final ActionMap map = treeView.tree.getActionMap();
                Action a = map.get(key);
                String all = Arrays.toString(map.allKeys()).replace(',', '\n');
                
                assertNotNull("Action for key " + key + " found: " + all, a);
                a.actionPerformed(new ActionEvent(treeView.tree, 0, null));
            }
        }
        Process processEvent = new Process();
        LOG.log(Level.INFO, "Sending action {0}", key);
        SwingUtilities.invokeAndWait(processEvent);
        LOG.log(Level.INFO, "Action {0} send", key);
    }
    
    private int cnt;
    private Node[] nodeWith(String... arr) {
        AbstractNode an = new AbstractNode(new Keys(arr));
        an.setName("N" + cnt++);
        return new Node[] { an };
    }


    /** Sample keys.
    */
    private class Keys extends Children.Keys<String> {
        public int actionPerformed;
        public boolean writeAccess;
        public boolean readAccess;

        /** Constructor.
         */
        public Keys (String... args) {
            super(lazy());
            if (args != null && args.length > 0) {
                setKeys (args);
            }
        }

        /** Changes the keys.
         */
        public void keys (String... args) {
            super.setKeys (args);
        }

        /** Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *   nodes for this key
         */
        @Override
        protected Node[] createNodes(String key) {
            if (key.startsWith("-")) {
                return null;
            }

            class An extends AbstractNode implements Action {
                public An() {
                    super(Children.LEAF);
                }

                @Override
                public Action getPreferredAction() {
                    return this;
                }

                @Override
                public void putValue(String key, Object value) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setEnabled(boolean b) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isEnabled() {
                    return true;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    actionPerformed++;
                    readAccess = Children.MUTEX.isReadAccess();
                    writeAccess = Children.MUTEX.isWriteAccess();
                }
            }
            AbstractNode an = new An();
            an.setName (key.toString ());

            return new Node[] { an };
        }

    }
    private static final class ExplorerWindow extends JFrame
                               implements ExplorerManager.Provider {
        
        private final ExplorerManager explManager = new ExplorerManager();
        
        ExplorerWindow(JComponent content) {
            super("TreeView test"); //NOI18N
            getContentPane().add(content, BorderLayout.CENTER);
        }
        
        @Override
        public ExplorerManager getExplorerManager() {
            return explManager;
        }
        
    } // end of ExplorerManager
    
}
