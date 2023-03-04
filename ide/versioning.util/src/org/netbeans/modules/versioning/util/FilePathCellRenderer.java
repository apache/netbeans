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

package org.netbeans.modules.versioning.util;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * Treats values in cells as file paths and renders them so that the end of the path is always visible.
 *
 * @author Maros Sandor
 */
public class FilePathCellRenderer extends DefaultTableCellRenderer {

    private static final int VISIBLE_START_CHARS = 0;

    private String computeFitText(String text) {
        if (text == null || text.length() <= VISIBLE_START_CHARS + 3) return text;

        FontMetrics fm = getFontMetrics(getFont());
        int width = getSize().width;
            
        String prefix = text.substring(0, VISIBLE_START_CHARS) + "...";
        int prefixLength = fm.stringWidth(prefix);
        int desired = width - prefixLength - 2;
        if (desired <= 0) return text;
        
        for (int i = text.length() - 1; i >= 0; i--) {
            String suffix = text.substring(i);
            int swidth = fm.stringWidth(suffix);
            if (swidth >= desired) {
                return suffix.length() > 0 ? prefix + suffix.substring(1) : text;
            }
        }
        return text;
    }
    
    protected void paintComponent(Graphics g) {
        setText(computeFitText(getText()));
        super.paintComponent(g);
    }
}
