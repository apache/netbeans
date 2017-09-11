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

import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for issue #95364.
 */
public class VisualizerSpeed95364Test extends NbTestCase {
    private int size;
    private TreeNode toCheck;
    private MyKeys chK;
    
    public VisualizerSpeed95364Test(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        return NbTestSuite.speedSuite(
            VisualizerSpeed95364Test.class, /* what tests to run */
            10 /* ten times slower */,
            17 /* try seventeen times if it fails */
        );
    }
    
    @Override
    protected void setUp() {
        size = getTestNumber();
        chK = new MyKeys();
        final AbstractNode root = new AbstractNode(chK);
        root.setName("test root");
        
        final String[] childrenNames = new String[size];
        for (int i = 0; i < size; i++) {
            childrenNames[i] = "test"+i;
        }
        chK.mySetKeys(childrenNames);
        toCheck = Visualizer.findVisualizer(root);
    }
    
    private void doTest() {
        TreeNode tn = null;
        for (int i = 0; i < 100000; i++) {
            tn = toCheck.getChildAt(size/2);
        }
        assertEquals("One node created + 50 at begining", 51, chK.cnt);
    }
    
    public void test100() { doTest(); }
    public void test1000() { doTest(); }
    public void test10000() { doTest(); }
    public void test100000() { doTest(); }
    
    
    private static Node createLeaf(String name) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(name);
        return n;
    }
    
    private static final class MyKeys extends Children.Keys<Object> {
        int cnt;
        
        public MyKeys() {
            super(true);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            cnt++;
            return new Node[] { createLeaf(key.toString())};
        }
        
        public void mySetKeys(Object[] newKeys) {
            super.setKeys(newKeys);
        }
    }
}
