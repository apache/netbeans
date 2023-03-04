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

package org.netbeans.modules.profiler.heapwalk.ui;

import org.netbeans.lib.profiler.ui.components.tree.EnhancedTreeCellRenderer;
import org.netbeans.modules.profiler.heapwalk.model.HeapWalkerNode;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;


/**
 *
 * @author Jiri Sedlacek
 */
final class FieldTreeCellRenderer extends EnhancedTreeCellRenderer {
    
    private final boolean showsDetails;
    
    public FieldTreeCellRenderer(boolean showsDetails) {
        this.showsDetails = showsDetails;
        
        ((JLabel)getComponents()[0]).setIconTextGap(1);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus) {
        if (value instanceof HeapWalkerNode) {
            setupCellRendererIcon(((HeapWalkerNode) value).getIcon());
        }

        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }

    public Component getTreeCellRendererComponentPersistent(JTree tree, Object value, boolean sel, boolean expanded,
                                                            boolean leaf, int row, boolean hasFocus) {
        FieldTreeCellRenderer renderer = new FieldTreeCellRenderer(showsDetails);
        if (value instanceof HeapWalkerNode) {
            setupCellRendererIcon(((HeapWalkerNode) value).getIcon());
        }

        return renderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }
    
    protected String getLabel3Text(Object node, String value) {
        if (showsDetails && node instanceof HeapWalkerNode) {
            String details = ((HeapWalkerNode)node).getDetails();
            if (details != null && !details.isEmpty()) return "  " + details;  // NOI18N
        }
        return super.getLabel3Text(node, value);
    }

    private void setupCellRendererIcon(Icon icon) {
        setLeafIcon(icon);
        setOpenIcon(icon);
        setClosedIcon(icon);
    }
}
