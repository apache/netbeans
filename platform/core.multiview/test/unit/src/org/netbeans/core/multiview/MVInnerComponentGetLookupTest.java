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

package org.netbeans.core.multiview;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author Milos Kleint
 */
public class MVInnerComponentGetLookupTest extends org.openide.windows.TopComponentGetLookupTest {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MVInnerComponentGetLookupTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private TopComponent mvtc;
    private TopComponent top2;
    private TopComponent top3;
    MultiViewDescription desc1;
    MultiViewDescription desc2;
    MultiViewDescription desc3;    
    
    public MVInnerComponentGetLookupTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    protected int checkAtLeastCount() {
        return 0;
    }
    
    /** Setup component with lookup.
     */
    @Override
    protected void setUp () {
        final MVElemTopComponent elem1 = new MVElemTopComponent();
        final MVElemTopComponent elem2 = new MVElemTopComponent();
        final MVElemTopComponent elem3 = new MVElemTopComponent();
        desc1 = new MVDesc("desc1", null, 0, elem1);
        desc2 = new MVDesc("desc2", null, 0, elem2);
        desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent mvtop = MultiViewFactory.createMultiView(descs, desc1);
        top = (TopComponent)elem1;
        get = top;
        top2 = (TopComponent)elem2;
        top3 = (TopComponent)elem3;
        lookup = mvtop.getLookup();
        mvtop.open();
        mvtop.requestActive();
        mvtc = mvtop;
    }
    
    public void testMVTCActivatedNodes() throws Exception {
        ProChange change = new ProChange();
        
        TopComponent.getRegistry().addPropertyChangeListener(change);
        Node[] nodes = new Node[] {new N("one"), new N("two")};
        
        assertEquals(TopComponent.getRegistry().getActivated(), mvtc);
        top.setActivatedNodes(nodes);
        Node[] ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 2);
        
        assertTrue(ret[0] == nodes[1] || ret[0] == nodes[0]);
        assertTrue(ret[1] == nodes[0] || ret[1] == nodes[1]);
        Node[] activ = TopComponent.getRegistry().getActivatedNodes();
        assertEquals(activ.length, 2);
        assertTrue(activ[0] == nodes[1] || activ[0] == nodes[0]);
        assertTrue(activ[1] == nodes[0] || activ[1] == nodes[1]);
//        assertEquals(1, change.count);
        
        
        
        Node[] nodes2 = new Node[] {new N("three")};
        top.setActivatedNodes(nodes2);
        ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 1);
        assertEquals(ret[0], nodes2[0]);
        activ = TopComponent.getRegistry().getActivatedNodes();
        assertEquals(activ.length, 1);
        assertEquals(activ[0], nodes2[0]);
//        assertEquals(2, change.count);
        
    }
    
    public void testMVTCActivatedNodesOnElementChange() throws Exception {    
        Node[] nodes1 = new Node[] {new N("one"), new N("two")};
        Node[] nodes2 = new Node[] {new N("three"), new N("four"), new N("five")};
        Node[] nodes3 = new Node[] {new N("six")};
        top.setActivatedNodes(nodes1);
        top2.setActivatedNodes(nodes2);
        top3.setActivatedNodes(nodes3);

        assertEquals(TopComponent.getRegistry().getActivated(), mvtc);
        // first element selected now..
        Node[] ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 2);
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvtc);
        // test related hack, easy establishing a  connection from Desc->perspective
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc2));
        ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 3);
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc3));
        ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 1);
        handler.requestActive(Accessor.DEFAULT.createPerspective(desc1));
        ret = mvtc.getActivatedNodes();
        assertNotNull(ret);
        assertEquals(ret.length, 2);
        
    }

    
    private class ProChange implements PropertyChangeListener {
        public int count = 0;
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if ("activatedNodes".equals(evt.getPropertyName())) {
                count = count + 1;  
            }
        }
        
    }
    
}
