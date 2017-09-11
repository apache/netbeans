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

package org.openide.actions;

import org.netbeans.junit.*;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import java.util.Arrays;
import org.openide.windows.TopComponent;

/** Test behavior of MoveUpAction (also MoveDownAction and ReorderAction).
 * @author Jesse Glick
 */
public class MoveUpActionTest extends NbTestCase {

    static {
        // Get Lookup right to begin with.
        ActionsInfraHid.class.getName();
    }
    
    public MoveUpActionTest(String name) {
        super(name);
    }
    
    private Node n, n1, n2, n3;
    
    protected @Override void setUp() throws Exception {
        n1 = new AbstractNode(Children.LEAF);
        n1.setName("n1");
        n2 = new AbstractNode(Children.LEAF);
        n2.setName("n2");
        n3 = new AbstractNode(Children.LEAF);
        n3.setName("n3");
        final Index.ArrayChildren c = new Index.ArrayChildren() {
            {
                add(new Node[] {n1, n2, n3});
            }
            public @Override void reorder() {
                reorder(new int[] {1, 2, 0});
            }
        };
        n = new AbstractNode(c) {
            {
                getCookieSet().add(c);
            }
        };
        n.setName("n");
    }
    
    /**
     * in order to run in awt event queue
     * fix for #39789
     */
    protected @Override boolean runInEQ()
    {
        return true;
    }
    
    public void testBasicUsage() throws Exception {
        SystemAction mua = SystemAction.get(MoveUpAction.class);
        SystemAction mda = SystemAction.get(MoveDownAction.class);
        SystemAction roa = SystemAction.get(ReorderAction.class);
        ActionsInfraHid.WaitPCL l = null;
        TopComponent tc = new TopComponent();
        tc.requestActive();
        try {
            assertNull(tc.getActivatedNodes());
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            l = new ActionsInfraHid.WaitPCL(SystemAction.PROP_ENABLED);
            mua.addPropertyChangeListener(l);
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertTrue(roa.isEnabled());
            assertEquals(Arrays.asList(new Node[] {n1, n2, n3}), Arrays.asList(n.getChildren().getNodes()));
            roa.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n3, n1, n2}), Arrays.asList(n.getChildren().getNodes()));
            assertTrue(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n1, n2});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n1});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue("MoveUp is enabled on a node in the middle of its parents", mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            mua.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n1, n3, n2}), Arrays.asList(n.getChildren().getNodes()));
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue("MoveUp is turned off after a node is moved to the very top", !mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n2});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
            tc.setActivatedNodes(new Node[] {n3});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertTrue(mda.isEnabled());
            assertFalse(roa.isEnabled());
            mda.actionPerformed(null);
            assertEquals(Arrays.asList(new Node[] {n1, n2, n3}), Arrays.asList(n.getChildren().getNodes()));
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(mua.isEnabled());
            assertFalse(mda.isEnabled());
            assertFalse(roa.isEnabled());
        } finally {
            if (l != null) {
                mua.removePropertyChangeListener(l);
                mda.removePropertyChangeListener(l);
                roa.removePropertyChangeListener(l);
            }
            tc.setActivatedNodes(new Node[0]);
            tc.setActivatedNodes(null);
        }
    }
    
}
