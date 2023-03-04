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

package org.openide.explorer;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ExplorerActionsImplTest extends NbTestCase implements PropertyChangeListener {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ExplorerActionsImplTest.class);
    }

    private volatile AssertionFailedError err;
    private volatile int cnt;

    public ExplorerActionsImplTest(String s) {
        super(s);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testIllegalStateException() throws Exception {
        N root = new N();
        final N ch1 = new N();
        final N ch2 = new N();
        final N ch3 = new N();
        PT mockPaste = new PT();
        ch3.pasteTypes = Collections.<PasteType>singletonList(mockPaste);

        root.getChildren().add(new Node[] { ch1, ch2, ch3 });
        final ExplorerManager em = new ExplorerManager();
        em.setRootContext(root);
        em.setSelectedNodes(new Node[] { root });
        Action action = ExplorerUtils.actionPaste(em);
        Action cut = ExplorerUtils.actionCut(em);
        em.waitActionsFinished();
        assertFalse("Not enabled", action.isEnabled());
        
        action.addPropertyChangeListener(this);
        cut.addPropertyChangeListener(this);
        

        em.setSelectedNodes(new Node[] { ch3 });
        em.waitActionsFinished();
        assertFalse("Cut is not enabled", cut.isEnabled());
        assertTrue("Now enabled", action.isEnabled());
        action.actionPerformed(new ActionEvent(this, 0, ""));

        assertEquals("The paste type is going to be called", 1, mockPaste.cnt);
        
        if (err != null) {
            throw err;
        }
        if (cnt == 0) {
            fail("There should be some change in actions: " + cnt);
        }
    }
    
    @RandomlyFails // NB-Core-Build #9619, #9847, #9998, #10014
    public void testPasteActionGetDelegatesBlocks() throws Exception {
        N root = new N();
        final N ch1 = new N();
        final N ch2 = new N();
        final N ch3 = new N();
        PT mockPaste = new PT();
        ch3.pasteTypes = Collections.<PasteType>singletonList(mockPaste);

        root.getChildren().add(new Node[] { ch1, ch2, ch3 });
        final ExplorerManager em = new ExplorerManager();
        em.setRootContext(root);
        em.setSelectedNodes(new Node[] { root });
        Action action = ExplorerUtils.actionPaste(em);
        em.waitActionsFinished();
        assertFalse("Not enabled", action.isEnabled());
        
        action.addPropertyChangeListener(this);
        
        assertNull("No delegates yet", action.getValue("delegates"));

        em.setSelectedNodes(new Node[] { ch3 });
        Object ret = action.getValue("delegates");
        assertNotNull("Delegates are updated", ret);
        Object[] arr = (Object[])ret;
        assertEquals("One item in there", 1, arr.length);
        if (err != null) {
            throw err;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (err == null && !EventQueue.isDispatchThread()) {
            err = new AssertionFailedError("Properties should be delivered in AWT Event thread");
        }
        cnt++;
    }

    private static class PT extends PasteType {
        int cnt;

        @Override
        public Transferable paste() throws IOException {
            assertTrue("paste is performed synchronously", EventQueue.isDispatchThread());
            cnt++;
            return null;
        }
    }

    private static class N extends AbstractNode {
        List<PasteType> pasteTypes;

        public N() {
            super(new Children.Array());
        }

        @Override
        protected void createPasteTypes(Transferable t, List<PasteType> s) {
            assertFalse("Don't block AWT", EventQueue.isDispatchThread());
            if (pasteTypes != null) {
                s.addAll(pasteTypes);
            }
        }
    }
}