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

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/** Simulate a deadlock.
 *
 * @author Jaroslav Tulach
 */
public final class TreeView72765Test extends NbTestCase {
    private TreeView ttv;

    public TreeView72765Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        ttv = new BeanTreeView();
    }

    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    public void testRedrawWhenOtherThreadHasChildrenLock() throws InterruptedException {

        class Block implements Runnable {
            public synchronized void run() {
                if (!Children.MUTEX.isWriteAccess()) {
                    Children.MUTEX.writeAccess(this);
                    return;
                }

                notifyAll();
                try {
                    wait(500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                synchronized (ttv.getTreeLock()) {
                }
            }
        }


        Block block = new Block();
        RequestProcessor.Task t;
        synchronized (block) {
            t = RequestProcessor.getDefault().post(block);
            block.wait();
        }

        ttv.addNotify();

        assertTrue("Initialize peer", ttv.isDisplayable());

        ttv.invalidate();
        ttv.validate();

    }
}
