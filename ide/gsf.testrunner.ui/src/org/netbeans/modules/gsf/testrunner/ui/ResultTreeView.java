/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

        @Override
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
    @Override
    public void run() {
        tree.setScrollsOnExpand(true);
    }
    
}
