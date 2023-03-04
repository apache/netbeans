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

package org.netbeans.lib.profiler.ui.swing.renderer;

import javax.swing.Icon;
import org.netbeans.lib.profiler.ui.results.PackageColorer;

/**
 *
 * @author Jiri Sedlacek
 */
public class JavaNameRenderer extends NormalBoldGrayRenderer {
    
    private final Icon icon;
    
    public JavaNameRenderer() {
        this(null);
    }
    
    public JavaNameRenderer(Icon icon) {
        this.icon = icon;
    }
    
    public void setValue(Object value, int row) {
        if (value == null) {
            setNormalValue(""); // NOI18N
            setBoldValue(""); // NOI18N
            setGrayValue(""); // NOI18N
        } else {
            String name = value.toString();
            String gray = ""; // NOI18N

            int bracketIndex = name.indexOf('('); // NOI18N
            if (bracketIndex != -1) {
                gray = " " + name.substring(bracketIndex); // NOI18N
                name = name.substring(0, bracketIndex);
            }

            int dotIndex = name.lastIndexOf('.'); // NOI18N
            setNormalValue(name.substring(0, dotIndex + 1));
            setBoldValue(name.substring(dotIndex + 1));
            setGrayValue(gray);
        }
        setIcon(icon);
    }
    
    
    // TODO: optimize to not slow down sort/search/filter by resolving color!
    protected void setNormalValue(String value) {
        super.setNormalValue(value);
        setCustomForeground(PackageColorer.getForeground(value));
    }
    
}
