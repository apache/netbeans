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

package org.netbeans.lib.profiler.ui.threads;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.netbeans.lib.profiler.results.threads.ThreadData;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;

/**
 *
 * @author Jiri Sedlacek
 */
public class NameStateRenderer extends LabelRenderer {
    
    public NameStateRenderer() {
        setOpaque(true);
        setMargin(3, 4, 3, 4);
    }
    
    public void setValue(Object value, int row) {
        if (value == null) {
            setText(""); // NOI18N
            setIcon(null);
        } else {
            ThreadData data = (ThreadData)value;
            setText(data.getName());
            setIcon(getIcon(data.getLastState()));
        }
    }
    
    private static final int THREAD_ICON_SIZE = 9;    
    private static final Map<Byte, Icon> STATE_ICONS_CACHE = new HashMap<>();
    private static Icon getIcon(byte state) {
        Icon icon = STATE_ICONS_CACHE.get(state);
        
        if (icon == null) {
            icon = new ThreadStateIcon(state, THREAD_ICON_SIZE, THREAD_ICON_SIZE);
            STATE_ICONS_CACHE.put(state, icon);
        }
        
        return icon;
    }
    
}
