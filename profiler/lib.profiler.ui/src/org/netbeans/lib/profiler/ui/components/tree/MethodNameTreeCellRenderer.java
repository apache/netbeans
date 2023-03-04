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

package org.netbeans.lib.profiler.ui.components.tree;

import java.awt.Color;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import java.awt.Component;
import javax.swing.*;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;


/**
 * Formats the node as follows:
 *   - if node does not contain either '.' or '(', format the node in plain font (typically if not a class or method name)
 *   - anything after '(' is formatted using gray font                           (typically method arguments)
 *   - anything between last '.' and before '(' is formatted using bold font     (typically method name)
 */
public class MethodNameTreeCellRenderer extends EnhancedTreeCellRenderer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Icon allThreadsIcon = Icons.getIcon(ProfilerIcons.ALL_THREADS);
    private Icon threadIcon = Icons.getIcon(ProfilerIcons.THREAD);

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Component getTreeCellRendererComponentPersistent(JTree tree, Object value, boolean sel, boolean expanded,
                                                            boolean leaf, int row, boolean hasFocus) {
        MethodNameTreeCellRenderer renderer = new MethodNameTreeCellRenderer();
        renderer.setLeafIcon(getLeafIcon(value));
        renderer.setClosedIcon(getClosedIcon(value));
        renderer.setOpenIcon(getOpenIcon(value));
        Color backgroundColor = UIUtils.getProfilerResultsBackground();

        if ((row & 0x1) == 0) { //even row
            renderer.setBackgroundNonSelectionColor(UIUtils.getDarker(backgroundColor));
        } else {
            renderer.setBackgroundNonSelectionColor(backgroundColor);
        }

        return renderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }

    protected Icon getClosedIcon(Object value) {
        if (value instanceof PrestimeCPUCCTNode) {
            PrestimeCPUCCTNode cct = (PrestimeCPUCCTNode) value;

            if (cct.isThreadNode()) {
                if (cct.getThreadId() == -1) {
                    return allThreadsIcon;
                } else {
                    return threadIcon;
                }
            } else if (cct.isFiltered()) {
                return UIManager.getLookAndFeel().getDisabledIcon(this, super.getClosedIcon(value));
            }
        } else if (value instanceof PresoObjAllocCCTNode) {
            if (((PresoObjAllocCCTNode)value).isFiltered()) {
                return UIManager.getLookAndFeel().getDisabledIcon(this, super.getClosedIcon(value));
            }
        }

        // not a thread node or not instance of PrestimeCPUCCTNode
        return super.getClosedIcon(value);
    }

    protected String getLabel1Text(Object node, String value) {
        if (node instanceof PrestimeCPUCCTNode) {
            if (((PrestimeCPUCCTNode)node).isThreadNode() ||
                ((PrestimeCPUCCTNode)node).isFiltered())
                return ""; //NOI18N
        } else if (node instanceof PresoObjAllocCCTNode) {
            if (((PresoObjAllocCCTNode)node).isFiltered())
                return ""; //NOI18N
        }
        
        int bracketIndex = value.indexOf('('); //NOI18N
        int dotIndex = value.lastIndexOf('.'); //NOI18N

        if ((dotIndex == -1) && (bracketIndex == -1)) {
            return value; // not a method -> we will format it in plain text
        }

        if (bracketIndex != -1) {
            value = value.substring(0, bracketIndex);
            dotIndex = value.lastIndexOf('.'); //NOI18N
        }

        return value.substring(0, dotIndex + 1);
    }

    protected String getLabel2Text(Object node, String value) {
        if (node instanceof PrestimeCPUCCTNode) {
            if (((PrestimeCPUCCTNode)node).isThreadNode())
                return value;
            else if (((PrestimeCPUCCTNode)node).isFiltered())
                return ""; // NOI18N
        } else if (node instanceof PresoObjAllocCCTNode) {
            if (((PresoObjAllocCCTNode)node).isFiltered())
                return ""; //NOI18N
        }
        
        int bracketIndex = value.indexOf('('); //NOI18N
        int dotIndex = value.lastIndexOf('.'); //NOI18N

        if ((dotIndex == -1) && (bracketIndex == -1)) {
            return ""; //NOI18N // not a method -> we will format it in plain text
        }

        if (bracketIndex != -1) {
            value = value.substring(0, bracketIndex);
            dotIndex = value.lastIndexOf('.'); //NOI18N
        }

        return value.substring(dotIndex + 1);
    }

    protected String getLabel3Text(Object node, String value) {
        if (node instanceof PrestimeCPUCCTNode) {
            if (((PrestimeCPUCCTNode)node).isThreadNode())
                return ""; //NOI18N
            else if (((PrestimeCPUCCTNode)node).isFiltered())
                return value;
        } else if (node instanceof PresoObjAllocCCTNode) {
            if (((PresoObjAllocCCTNode)node).isFiltered())
                return value;
        }
        
        int bracketIndex = value.indexOf('('); //NOI18N

        if (bracketIndex != -1) {
            return " " + value.substring(bracketIndex); //NOI18N
        } else {
            return ""; //NOI18N
        }
    }

    protected Icon getLeafIcon(Object value) {
        if (value instanceof PrestimeCPUCCTNode) {
            PrestimeCPUCCTNode cct = (PrestimeCPUCCTNode) value;

            if (cct.isThreadNode()) {
                if (cct.getThreadId() == -1) {
                    return allThreadsIcon;
                } else {
                    return threadIcon;
                }
            } else if (cct.isFiltered()) {
                return UIManager.getLookAndFeel().getDisabledIcon(this, super.getLeafIcon(value));
            }
        } else if (value instanceof PresoObjAllocCCTNode) {
            if (((PresoObjAllocCCTNode)value).isFiltered()) {
                return UIManager.getLookAndFeel().getDisabledIcon(this, super.getLeafIcon(value));
            }
        }

        // not a thread node or not instance of PrestimeCPUCCTNode
        return super.getLeafIcon(value);
    }

    protected Icon getOpenIcon(Object value) {
        if (value instanceof PrestimeCPUCCTNode) {
            PrestimeCPUCCTNode cct = (PrestimeCPUCCTNode) value;

            if (cct.isThreadNode()) {
                if (cct.getThreadId() == -1) {
                    return allThreadsIcon;
                } else {
                    return threadIcon;
                }
            } else if (cct.isFiltered()) {
                return UIManager.getLookAndFeel().getDisabledIcon(this, super.getOpenIcon(value));
            }
        } else if (value instanceof PresoObjAllocCCTNode) {
            if (((PresoObjAllocCCTNode)value).isFiltered()) {
                return UIManager.getLookAndFeel().getDisabledIcon(this, super.getOpenIcon(value));
            }
        }

        // not a thread node or not instance of PrestimeCPUCCTNode
        return super.getOpenIcon(value);
    }
}
