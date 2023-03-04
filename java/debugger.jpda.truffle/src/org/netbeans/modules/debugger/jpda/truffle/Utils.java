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

package org.netbeans.modules.debugger.jpda.truffle;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.UIManager;

public final class Utils {
    
    private Utils() {}
    
    public static String toHTML (
        String text,
        boolean bold,
        boolean italics,
        Color color
    ) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder ();
        sb.append ("<html>");
        if (bold) sb.append ("<b>");
        if (italics) sb.append ("<i>");
        if (color == null) {
            color = UIManager.getColor("Table.foreground");
            if (color == null) {
                color = new JTable().getForeground();
            }
        }
        sb.append ("<font color=\"#");
        String hexColor = Integer.toHexString ((color.getRGB () & 0xffffff));
        for (int i = hexColor.length(); i < 6; i++) {
            sb.append("0"); // Prepend zeros to length of 6
        }
        sb.append(hexColor);
        sb.append ("\">");
        text = text.replace ("&", "&amp;");
        text = text.replace ("<", "&lt;");
        text = text.replace (">", "&gt;");
        sb.append (text);
        sb.append ("</font>");
        if (italics) sb.append ("</i>");
        if (bold) sb.append ("</b>");
        sb.append ("</html>");
        return sb.toString ();
    }

    public static String stringOrNull(String str) {
        if ("null".equals(str)) {
            return null;
        } else {
            return str;
        }
    }
    
}
