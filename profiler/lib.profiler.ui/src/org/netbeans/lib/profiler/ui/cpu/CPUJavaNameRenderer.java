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

package org.netbeans.lib.profiler.ui.cpu;

import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.ui.swing.renderer.JavaNameRenderer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
public class CPUJavaNameRenderer extends JavaNameRenderer {
    
    private static final Icon THREAD_ICON = Icons.getIcon(ProfilerIcons.THREAD);
    private static final Icon THREAD_ICON_DISABLED = UIManager.getLookAndFeel().getDisabledIcon(null, THREAD_ICON);
    private static final Icon LEAF_ICON = Icons.getIcon(ProfilerIcons.NODE_LEAF);
    private static final Icon LEAF_ICON_DISABLED = UIManager.getLookAndFeel().getDisabledIcon(null, LEAF_ICON);
    
    private final Icon icon;
    private final Icon iconDisabled;
    
    public CPUJavaNameRenderer() {
        this(ProfilerIcons.NODE_FORWARD);
    }
    
    public CPUJavaNameRenderer(String iconKey) {
        this.icon = Icons.getIcon(iconKey);
        this.iconDisabled = UIManager.getLookAndFeel().getDisabledIcon(null, icon);
    }
    
    public void setValue(Object value, int row) {
        if (value instanceof PrestimeCPUCCTNode) {
            PrestimeCPUCCTNode node = (PrestimeCPUCCTNode)value;
            
            if (node.isSelfTimeNode()) {
                setNormalValue(node.getNodeName());
                setBoldValue(""); // NOI18N
                setGrayValue(""); // NOI18N
            } else if (node.isThreadNode()) {
                setNormalValueEx(""); // NOI18N
                setBoldValue(node.getNodeName());
                setGrayValue(""); // NOI18N
            } else if (node.isFiltered()) {
                setNormalValue(""); // NOI18N
                setBoldValue("");
                setGrayValue(node.getNodeName()); // NOI18N
            } else {
                super.setValue(node.getNodeName(), row);
            }
            
            if (node.isThreadNode()) {
                setIcon(node.isFiltered() ? THREAD_ICON_DISABLED : THREAD_ICON);
            } else if (node.isLeaf()) {
                setIcon(node.isFiltered() ? LEAF_ICON_DISABLED : LEAF_ICON);
            } else {
                setIcon(node.isFiltered() ? iconDisabled : icon);
            }
        } else {
            super.setValue(value, row);
        }
    }
    
    
    // TODO: optimize to not slow down sort/search/filter by resolving color!
    private void setNormalValueEx(String value) {
        super.setNormalValue(value);
        setCustomForeground(null);
    }
    
}
