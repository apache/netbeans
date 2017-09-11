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
