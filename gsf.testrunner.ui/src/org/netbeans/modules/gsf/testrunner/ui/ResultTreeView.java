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

package org.netbeans.modules.gsf.testrunner.ui;

import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.netbeans.modules.gsf.testrunner.ui.api.CallstackFrameNode;
import java.awt.Component;
import java.awt.EventQueue;
import javax.accessibility.AccessibleContext;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import org.openide.awt.HtmlRenderer;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.Visualizer;
import org.openide.util.NbBundle;


/**
 *
 * @author Marian Petras
 */
final class ResultTreeView extends BeanTreeView implements Runnable {

    /** */
    private final TreeCellRenderer defaultTreeCellRenderer;
    /** */
    private final TreeCellRenderer noIconTreeCellRenderer;
    
    /** Creates a new instance of ResultTree */
    ResultTreeView() {
        super();
        defaultTreeCellRenderer = tree.getCellRenderer();
        noIconTreeCellRenderer = createNoIconTreeCellRenderer();
        tree.setCellRenderer(new DelegatingTreeCellRenderer());
        tree.setDragEnabled(false);
        
        initAccessibility();
    }

    @NbBundle.Messages({"ACSN_ResultPanelTree=Information about passed and failed tests",
    "ACSD_ResultPanelTree=Displays in a tree structure information about passed, failed and erroneous tests"})
    private void initAccessibility() {
        AccessibleContext accContext = tree.getAccessibleContext();
        accContext.setAccessibleName(Bundle.ACSN_ResultPanelTree());
        accContext.setAccessibleDescription(Bundle.ACSD_ResultPanelTree());
    }
    
    /**
     *
     */
    private final class DelegatingTreeCellRenderer implements TreeCellRenderer {

        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean selected,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {
            boolean isResultRootNode =
                            (value instanceof TreeNode)
                            && (((TreeNode) value).getParent() == null);
	    // render no icon space an empty icon of a callStackFrame
            boolean isCallstackFrame = false;
            if (null != value) {
                isCallstackFrame = (Visualizer.findNode(value) instanceof CallstackFrameNode);
            }

            TreeCellRenderer renderer = (isResultRootNode || isCallstackFrame)
                                        ? noIconTreeCellRenderer
                                        : defaultTreeCellRenderer;
            return renderer.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);
        }

    }
    
    /**
     */
    private TreeCellRenderer createNoIconTreeCellRenderer() {
        HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();
        renderer.setHtml(false);
        renderer.setIcon(null);
        renderer.setIconTextGap(0);
        renderer.setIndent(2);
        return renderer;
    }
    
    /**
     */
    void expandReportNode(TestsuiteNode node) {
        final boolean wasScrollsOnExpand = tree.getScrollsOnExpand();
        
        tree.setScrollsOnExpand(false);
        try {
            expandNode(node);
        } finally {
            if (wasScrollsOnExpand) {
                
                /*
                 * We must post the scrolling-enabling routine to the end of the
                 * event queue, after all the requests for expansion of nodes:
                 */
                EventQueue.invokeLater(this);
            }
        }
    }
    
    /**
     */
    public void run() {
        tree.setScrollsOnExpand(true);
    }
    
}
