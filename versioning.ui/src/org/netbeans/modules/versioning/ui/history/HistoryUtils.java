/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
