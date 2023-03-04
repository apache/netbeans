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
package org.netbeans.lib.profiler.ui.memory;

import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.results.memory.PresoObjAllocCCTNode;
import org.netbeans.lib.profiler.ui.swing.renderer.JavaNameRenderer;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
public class MemoryJavaNameRenderer extends JavaNameRenderer {
    
    private static final Icon CLASS_ICON = Icons.getIcon(LanguageIcons.CLASS);
    private static final Icon REVERSE_ICON = Icons.getIcon(ProfilerIcons.NODE_REVERSE);
    private static final Icon REVERSE_ICON_DISABLED = UIManager.getLookAndFeel().getDisabledIcon(null, REVERSE_ICON);
    
    public void setValue(Object value, int row) {
        if (value instanceof PresoObjAllocCCTNode) {
            PresoObjAllocCCTNode node = (PresoObjAllocCCTNode)value;
            
            if (node.isFiltered()) {
                setNormalValue(""); // NOI18N
                setBoldValue("");
                setGrayValue(node.getNodeName()); // NOI18N
            } else {
                super.setValue(value, row);
            }
            
            if (node.isFiltered()) setIcon(REVERSE_ICON_DISABLED);
            else if (node.getMethodClassNameAndSig()[2] == null) setIcon(CLASS_ICON); // class name
            else setIcon(REVERSE_ICON); // method name
        } else {
            super.setValue(value, row);
        }
        
//        // TODO: <clinit> methods should be displayed with "()" similar to <init>
//        // PlainFormattableMethodName.getFullFormattedMethod()
//        
//        if (getGrayValue().isEmpty()) System.err.println(">> value: " + ((PresoObjAllocCCTNode)value).getMethodClassNameAndSig()[2]);
//        // TODO: also "Objects allocated by reflection" should be excluded to display icon
//        if (getGrayValue().isEmpty()) setIcon(null); // class name
//        else setIcon(Icons.getIcon(ProfilerIcons.NODE_REVERSE)); // method name
    }
    
}
