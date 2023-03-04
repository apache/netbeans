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
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * A test that verifies few aspects of the ListView implementation.
 * 
 * @author  Petr Nejedly
 */
public final class ListViewWithUpTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ListViewWithUpTest.class);
    }

    private ListView view;
    private ExplorerWindow testWindow;
    
    public ListViewWithUpTest(String testName) {
        super(testName);
    }

    /**
     * Tests whether the ListView doesn't try to scroll to a changed node
     * on a change event. See issue 88209
     */
    public void testNoScrollOnIconChange() throws Exception {
        assert !EventQueue.isDispatchThread();
        
        testWindow = new ExplorerWindow();
        testWindow.getContentPane().add(view = new ListView());
        view.setShowParentNode(true);

        Ch20 ch = new Ch20("");
        Node root = new AbstractNode(ch);
        root.setName("Root");
        
        testWindow.getExplorerManager().setRootContext(root);
        awtRequest(new Callable<Void>() {
            @Override
            public Void call() {
                testWindow.pack();
                testWindow.setVisible(true);
                return null;
            }
        });

        while (!awtRequest(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return testWindow.isShowing();
            }
        })) {
            Thread.sleep(100);
        }

        assertEquals("Just 20 items for root", 20, view.model.getSize());
        testWindow.getExplorerManager().setExploredContext(root.getChildren().getNodeAt(10));
        awtRequest(new Callable<Void>() {
            @Override
            public Void call() {
                return null;
            }
        });
        assertEquals("21 items for non-root", 21, view.model.getSize());
        awtRequest(new Callable<Void>() {
            @Override
            public Void call() {
                view.performObjectAt(0, 0);
                return null;
            }
        });
        assertEquals("Moved back to root", root, testWindow.getExplorerManager().getExploredContext());
        
        // cleanup
        awtRequest(new Callable<Void>() {
            @Override
            public Void call() {
                testWindow.setVisible(false);
                return null;
            }
        });
    }

    private static final class Ch20 extends Children.Keys<Integer> {
        private final String prefix;

        public Ch20(String prefix) {
            this.prefix = prefix;
        }


        @Override
        protected void addNotify() {
            List<Integer> arr = new ArrayList<Integer>();
            for (int i = 0; i < 20; i++) {
                arr.add(i);
            }
            setKeys(arr);
        }


        @Override
        protected Node[] createNodes(Integer key) {
            AbstractNode an = new AbstractNode(new Ch20(prefix + key));
            an.setName(prefix + key);
            return new Node[] { an };
        }

    }
        
    
    private static final class ExplorerWindow extends JFrame
                               implements ExplorerManager.Provider {
        
        private final ExplorerManager explManager = new ExplorerManager();
        
        ExplorerWindow() {
            super("ListView test");                                     //NOI18N
        }
        
        @Override
        public ExplorerManager getExplorerManager() {
            return explManager;
        }
    }
    
    private static <T> T awtRequest(final Callable<T> call) throws Exception {
        final AtomicReference<T> value = new AtomicReference<T>();
        final Exception[] exc = new Exception[1];
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    value.set(call.call());
                } catch (Exception ex) {
                    exc[0] = ex;
                }
            }
        });
        
        if (exc[0] != null) throw exc[0];
        return value.get();
    }
    
}
