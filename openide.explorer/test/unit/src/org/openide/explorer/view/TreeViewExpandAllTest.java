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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Denis Sepanov, Tomas Holy
 */

public class TreeViewExpandAllTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeViewExpandAllTest.class);
    }

    public TreeViewExpandAllTest(String name) {
        super(name);
    }
    Set<Integer> expandedNodesIndexes = new HashSet<Integer>();

    boolean lazy;
    public void testExpandAllEager() throws InterruptedException, InvocationTargetException {
        lazy = false;
        doTestExpandAll();
    }

    public void testExpandAllLazy() throws InterruptedException, InvocationTargetException {
        lazy = true;
        doTestExpandAll();
    }

    public void doTestExpandAll() throws InterruptedException, InvocationTargetException {

        EventQueue.invokeAndWait(new Runnable() {

            public void run() {
                final BeanTreeView beanTreeView = new BeanTreeView();
                final ExplorerWindow testWindow = new ExplorerWindow();
                testWindow.getContentPane().add(beanTreeView);
                // Node which has 7 levels 0-6
                testWindow.getExplorerManager().setRootContext(new LevelNode(6));

                testWindow.pack();
                testWindow.setVisible(true);
                beanTreeView.expandAll();
            }
        });
        // Whole expanded tree should have nodes O-6
        assertEquals(new HashSet<Integer>(Arrays.asList(0, 1, 2, 3, 4, 5, 6)), expandedNodesIndexes);
    }

    private static final class ExplorerWindow extends JFrame implements ExplorerManager.Provider {

        private final ExplorerManager explManager = new ExplorerManager();

        ExplorerWindow() {
            super("TreeView expandAll test");
        }

        public ExplorerManager getExplorerManager() {
            return explManager;
        }
    }

    class LevelNode extends AbstractNode {

        public LevelNode(int level) {
            super(new LevelNodeChildren(level));
            expandedNodesIndexes.add(level);
        }
    }

    class LevelNodeChildren extends Children.Keys<Integer> {

        int level;

        public LevelNodeChildren(int level) {
            super(lazy);
            this.level = level;
        }

        @Override
        protected void addNotify() {
            if (level > 0) {
                setKeys(new Integer[]{--level});
            }
        }

        @Override
        protected Node[] createNodes(Integer i) {
            return new Node[]{new LevelNode(i)};
        }
    }
}
