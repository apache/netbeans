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
package org.netbeans.modules.search.ui;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.search.MatchingObject;
import org.netbeans.modules.search.ResultModel;
import org.netbeans.modules.search.SearchTestUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;

/**
 *
 * @author jhavlin
 */
public class MatchingObjectNodeTest extends NbTestCase {

    public MatchingObjectNodeTest(String name) {
        super(name);
    }

    /**
     * Test that bug 213441 is fixed.
     */
    @RandomlyFails
    public void testBug213441() throws IOException, InterruptedException,
            InvocationTargetException {

        Node n = new AbstractNode(Children.LEAF);
        Semaphore s = new Semaphore(0);
        ResultModel rm = SearchTestUtils.createResultModelWithOneMatch();
        MatchingObject mo = rm.getMatchingObjects().get(0);
        MatchingObjectNode mon = new MatchingObjectNode(n, Children.LEAF, mo,
                false);
        mon.addNodeListener(new DisplayNameChangeListener(s));
        mon.getDisplayName();
        mo.getFileObject().delete();
        rm.close();
        assertTrue("Display name change event has not been fired!", //NOI18N
                s.tryAcquire(10, TimeUnit.SECONDS));
    }

    /**
     * Test for bug 217984.
     */
    public void testCreateNodeForInvalidDataObject() throws IOException {
        ResultModel rm = SearchTestUtils.createResultModelWithOneMatch();
        MatchingObject mo = rm.getMatchingObjects().get(0);
        DataObject dob = mo.getDataObject();
        FileObject fob = mo.getFileObject();
        Node original = dob.getNodeDelegate();
        fob.delete();
        // No exception should be thrown from the constructor.
        Node n = new MatchingObjectNode(original, Children.LEAF, mo, false);
        assertEquals("test.txt", n.getDisplayName());
    }

    /**
     * Listener that releases a semaphore when display name is changed.
     */
    private class DisplayNameChangeListener implements NodeListener {

        private Semaphore semaphore;

        public DisplayNameChangeListener(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(Node.PROP_DISPLAY_NAME)) {
                semaphore.release();
            }
        }

        @Override
        public void childrenAdded(NodeMemberEvent ev) {
        }

        @Override
        public void childrenRemoved(NodeMemberEvent ev) {
        }

        @Override
        public void childrenReordered(NodeReorderEvent ev) {
        }

        @Override
        public void nodeDestroyed(NodeEvent ev) {
        }
    }
}
