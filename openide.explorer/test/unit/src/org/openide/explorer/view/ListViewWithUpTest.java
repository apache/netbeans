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
