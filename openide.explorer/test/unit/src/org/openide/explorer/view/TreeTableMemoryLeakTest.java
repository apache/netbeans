/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.openide.explorer.view;

import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Matteo Di Giovinazzo <digiovinazzo@streamsim.com>
 */
public class TreeTableMemoryLeakTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeTableMemoryLeakTest.class);
    }

    private Node root;
    private Children.Array children;
    private Node child;
    private TreeTableView ttv;

    public TreeTableMemoryLeakTest() {
        super("MainTest");
    }

    public void testMain() {
        child = new AbstractNode(Children.LEAF) {

            {
                setName("child");
            }
        };


        children = new Children.Array();
        children.add(new Node[]{child});
        root = new AbstractNode(children) {

            {
                setName("root");
            }
        };


        try {
            SwingUtilities.invokeAndWait(new MyRunnable());
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        Reference<Node> ref = new WeakReference<Node>(child);
        child = null;

        assertGC("child in TTV must be GCed", ref, Collections.singleton(ttv));
        assertGC("child in general must be GCed", ref);
    }

    private class MyRunnable implements Runnable {

        public MyRunnable() {
        }

        @Override
        public void run() {

            // create panel (implementing ExplorerManager.Provider) with TTV
            MyPanel panel = new MyPanel();
            panel.setLayout(new GridLayout(1, 2));
            ttv = new TreeTableView();
            panel.add(ttv);

            // set root and keep the same root
            panel.setExplorerManagerRoot(root);


            //comment the next line to make the test works
            ttv.expandNode(root);

            // remove child
            children.remove(new Node[]{child});
            root = null;
            children = null;
        }
    }

    private static class MyPanel extends JPanel implements ExplorerManager.Provider {

        private ExplorerManager manager;

        public MyPanel() {
            super();
            manager = new ExplorerManager();
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }

        private void setExplorerManagerRoot(Node root) {
            manager.setRootContext(root);
        }
    }
}
