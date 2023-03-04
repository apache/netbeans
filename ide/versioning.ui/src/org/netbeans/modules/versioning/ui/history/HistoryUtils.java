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
package org.netbeans.modules.versioning.ui.history;

import java.awt.FontMetrics;
import javax.swing.JTable;

/**
 *
 * @author Tomas Stupka
 */
public class HistoryUtils {
    
    private static final int VISIBLE_START_CHARS = 0;

    private HistoryUtils() {}
    
    public static String escapeForHTMLLabel(String text) {
        if(text == null) {
            return "";                                                          // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break;                             // NOI18N
                case '>': sb.append("&gt;"); break;                             // NOI18N
                default: sb.append(c);
            }
        }
        return sb.toString();
    }  
    
    public static String computeFitText(JTable table, int rowIdx, int columnIdx, String text) {
        if(text == null) text = "";                                             // NOI18N
        if (text.length() <= VISIBLE_START_CHARS + 3) return text;

        FontMetrics fm = table.getFontMetrics(table.getFont());
        int width = table.getCellRect(rowIdx, columnIdx, false).width;

        String sufix = "...";                                                   // NOI18N
        int sufixLength = fm.stringWidth(sufix + " ");                          // NOI18N
        int desired = width - sufixLength;
        if (desired <= 0) return text;

        for (int i = 0; i <= text.length() - 1; i++) {
            String prefix = text.substring(0, i);
            int swidth = fm.stringWidth(prefix);
            if (swidth >= desired) {
                return prefix.length() > 0 ? prefix + sufix: text;
            }
        }
        return text;
    }        
}
