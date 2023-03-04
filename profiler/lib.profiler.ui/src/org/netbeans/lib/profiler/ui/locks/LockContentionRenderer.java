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

import javax.swing.Icon;
import org.netbeans.lib.profiler.results.locks.LockCCTNode;
import org.netbeans.lib.profiler.ui.results.PackageColorer;
import org.netbeans.lib.profiler.ui.swing.renderer.NormalBoldGrayRenderer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
public class LockContentionRenderer extends NormalBoldGrayRenderer {
    
    private static final Icon THREAD_ICON = Icons.getIcon(ProfilerIcons.THREAD);
    private static final Icon LOCK_ICON = Icons.getIcon(ProfilerIcons.WINDOW_LOCKS);
    
    public void setValue(Object value, int row) {
        if (value == null) {
            setNormalValue(""); // NOI18N
            setBoldValue(""); // NOI18N
            setGrayValue(""); // NOI18N
            setIcon(null);
        } else {
            LockCCTNode node = (LockCCTNode)value;

            boolean threadNode = node.isThreadLockNode();
            boolean monitorNode = node.isMonitorNode();

            String nodeName = node.getNodeName();
            int bracketIndex = nodeName.indexOf('('); // NOI18N
            int dotIndex = nodeName.lastIndexOf('.'); // NOI18N

            String normalValue = getNormalValue(node, nodeName, bracketIndex, dotIndex, threadNode);
            String boldValue = getBoldValue(node, nodeName, bracketIndex, dotIndex, threadNode);
            String grayValue = getGrayValue(node, nodeName, bracketIndex, dotIndex, threadNode);

            setNormalValue(normalValue);
            setBoldValue(boldValue);
            setGrayValue(grayValue);

            Icon icon = null;
            if (threadNode) icon = THREAD_ICON;
            else if (monitorNode) icon = LOCK_ICON;

            setIcon(icon);
            
            // TODO: optimize to not slow down sort/search/filter by resolving color!
            setCustomForeground(monitorNode ? PackageColorer.getForeground(normalValue) : null);
        }
    }
    
    private String getNormalValue(LockCCTNode node, String nodeName, int bracketIndex,
                                  int dotIndex, boolean threadNode) {
        
        if (threadNode) return node.getParent().getParent() == null ? "" : nodeName; // NOI18N
        
        if (dotIndex == -1 && bracketIndex == -1) return nodeName;

        if (bracketIndex != -1) nodeName = nodeName.substring(0, bracketIndex);
        return nodeName.substring(0, dotIndex + 1);
    }
    
    private String getBoldValue(LockCCTNode node, String nodeName, int bracketIndex,
                                int dotIndex, boolean threadNode) {
        
        if (threadNode) return node.getParent().getParent() == null ? nodeName : ""; // NOI18N
        
        if (dotIndex == -1 && bracketIndex == -1) return ""; // NOI18N

        if (bracketIndex != -1) nodeName = nodeName.substring(0, bracketIndex);
        return nodeName.substring(dotIndex + 1);
    }
    
    private String getGrayValue(LockCCTNode node, String nodeName, int bracketIndex,
                                int dotIndex, boolean threadNode) {
        
        if (threadNode) return ""; // NOI18N
        
        return bracketIndex != -1 ? " " + nodeName.substring(bracketIndex) : ""; // NOI18N
    }
    
}
