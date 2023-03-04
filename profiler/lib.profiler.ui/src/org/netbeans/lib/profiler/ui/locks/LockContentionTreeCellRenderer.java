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
package org.netbeans.lib.profiler.ui.locks;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import org.netbeans.lib.profiler.results.locks.LockCCTNode;
import org.netbeans.lib.profiler.ui.components.tree.EnhancedTreeCellRenderer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
public class LockContentionTreeCellRenderer extends EnhancedTreeCellRenderer {
    
    protected String getLabel1Text(Object node, String value) {
        LockCCTNode n = (LockCCTNode)node;
        String name = n.getNodeName();
        if (n.isThreadLockNode()) return n.getParent().getParent() == null ? "" : name; // NOI18N
        
        int bracketIndex = name.indexOf('('); // NOI18N
        int dotIndex = name.lastIndexOf('.'); // NOI18N

        if ((dotIndex == -1) && (bracketIndex == -1)) return name;

        if (bracketIndex != -1) {
            name = name.substring(0, bracketIndex);
            dotIndex = name.lastIndexOf('.'); // NOI18N
        }

        return name.substring(0, dotIndex + 1);
    }

    protected String getLabel2Text(Object node, String value) {
        LockCCTNode n = (LockCCTNode)node;
        String name = n.getNodeName();
        if (n.isThreadLockNode()) return n.getParent().getParent() == null ? name : ""; // NOI18N
        
        int bracketIndex = name.indexOf('('); // NOI18N
        int dotIndex = name.lastIndexOf('.'); // NOI18N

        if ((dotIndex == -1) && (bracketIndex == -1)) return ""; // NOI18N

        if (bracketIndex != -1) {
            name = name.substring(0, bracketIndex);
            dotIndex = name.lastIndexOf('.'); // NOI18N
        }

        return name.substring(dotIndex + 1);
    }

    protected String getLabel3Text(Object node, String value) {
        LockCCTNode n = (LockCCTNode)node;
        if (n.isThreadLockNode()) return ""; // NOI18N
        
        String name = n.getNodeName();
        int bracketIndex = name.indexOf('('); // NOI18N
        return bracketIndex != -1 ? " " + name.substring(bracketIndex) : ""; // NOI18N
    }
    
    private Icon getIcon(Object node) {
        LockCCTNode n = (LockCCTNode)node;
        if (n.isThreadLockNode()) return Icons.getIcon(ProfilerIcons.THREAD);
        else if (n.isMonitorNode()) return Icons.getIcon(ProfilerIcons.WINDOW_LOCKS);
        return null;
    }
    
    protected Icon getLeafIcon(Object value) {
        return getIcon(value);
    }

    protected Icon getOpenIcon(Object value) {
        return getIcon(value);
    }
    
    protected Icon getClosedIcon(Object value) {
        return getIcon(value);
    }
    
    
    public Component getTreeCellRendererComponentPersistent(JTree tree, Object value, boolean sel, boolean expanded,
                                                            boolean leaf, int row, boolean hasFocus) {
        LockContentionTreeCellRenderer renderer = new LockContentionTreeCellRenderer();
//        renderer.setLeafIcon(leafIcon);
//        renderer.setClosedIcon(closedIcon);
//        renderer.setOpenIcon(openIcon);

        return renderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }
    
}
