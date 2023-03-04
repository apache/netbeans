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
import java.util.concurrent.Callable;
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
public final class ListViewTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ListViewTest.class);
    }

    private ListView view;
    private ExplorerWindow testWindow;
    
    public ListViewTest(String testName) {
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

        Children.Array ch = new Children.Array();
        Node root = new AbstractNode(ch);
        
        AbstractNode last = null;
        
        for (int i=1; i<20; i++) {
            last = new AbstractNode(Children.LEAF);
            last.setName("Node #" + i);
            ch.add(new Node[] {last});
        }

        testWindow.getExplorerManager().setRootContext(root);
        
        awtRequest(new Callable<Void>() {
            @Override
            public Void call() {
                testWindow.pack();
                testWindow.setVisible(true);
                return null;
            }
        });
        
        //Wait for the AWT thread to actually display the dialog:
        Thread.sleep(2000);
        
        // check the pane is not scrolled yet
        assertEquals(0, (int)awtRequest(new CallY()));

        // cause an icon change
        last.setIconBaseWithExtension("org/openide/nodes/beans.gif");
        Thread.sleep(1000);
        // check the pane is still not scrolled
        assertEquals(0, (int)awtRequest(new CallY()));

        // cleanup
        awtRequest(new Callable<Void>() {
            @Override
            public Void call() {
                testWindow.setVisible(false);
                return null;
            }
        });
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
        final Object[] value = new Object[1];
        final Exception[] exc = new Exception[1];
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    value[0] = call.call();
                } catch (Exception ex) {
                    exc[0] = ex;
                }
            }
        });
        
        if (exc[0] != null) throw exc[0];
        return (T) value[0];
    }
    
    private class CallY implements Callable<Integer> {
        @Override
        public Integer call() {
            return view.getViewport().getViewPosition().y;
        }
    };
    
}
