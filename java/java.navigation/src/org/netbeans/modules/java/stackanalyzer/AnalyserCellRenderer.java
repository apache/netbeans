/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.stackanalyzer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.netbeans.modules.java.stackanalyzer.StackLineAnalyser.Link;


/**
 *
 * @author hanz
 */
class AnalyserCellRenderer extends DefaultListCellRenderer {

    public AnalyserCellRenderer () {
    }

    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    private Border getNoFocusBorder() {
        Border border = UIManager.getBorder("List.cellNoFocusBorder");
        if (System.getSecurityManager() != null) {
            if (border != null) return border;
            return SAFE_NO_FOCUS_BORDER;
        } else {
            if (border != null &&
                    (noFocusBorder == null ||
                    noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
                return border;
            }
            return noFocusBorder;
        }
    }


    @Override
    public Component getListCellRendererComponent (
        JList                   list,
        Object                  value,
        int                     index,
        boolean                 isSelected,
        boolean                 cellHasFocus
    ) {
        setComponentOrientation (list.getComponentOrientation ());

        Color bg = null;
        Color fg = null;

        JList.DropLocation dropLocation = list.getDropLocation ();
        if (dropLocation != null && !dropLocation.isInsert () && dropLocation.getIndex () == index) {

            bg = UIManager.getColor ("List.dropCellBackground");
            fg = UIManager.getColor ("List.dropCellForeground");

            isSelected = true;
        }

        String line = (String) value;
        Link link = StackLineAnalyser.analyse (line);

        if (isSelected) {
            setBackground(bg == null ? list.getSelectionBackground() : bg);
            setForeground(fg == null ? list.getSelectionForeground() : fg);
        } else {
            setBackground (list.getBackground ());
//            if (link != null)
//                setForeground (foreground);
//            else
                setForeground (list.getForeground ());
        }

        if (link != null) {
            StringBuilder sb = new StringBuilder ();
            sb.append ("<html>");
            if (isSelected) {
                sb.append("<style> a.val {text-decoration: underline; color: "+toRgbText(getForeground())+"} </style><body>");
            }
            sb.append(indentAt(escapeAngleBrackets(line.substring(0, link.getStartOffset())), "&nbsp;"));
            sb.append("<a class=\"val\" href=\"\">");
            sb.append(escapeAngleBrackets(line.substring(link.getStartOffset(), link.getEndOffset())));
            sb.append("</a>");
            sb.append(escapeAngleBrackets(line.substring(link.getEndOffset())));
            sb.append("</body></html>");
            setText(sb.toString ());
        } else {
            setText(indentAt(line.strip(), " "));
        }

        setEnabled (list.isEnabled ());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder ("List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder ("List.focusCellHighlightBorder");
            }
        } else {
            border = getNoFocusBorder ();
        }
        setBorder (border);

        return this;
    }

//    private boolean hasSource (String line) {
//        Matcher m = AnalyzeStackTopComponent.STACK_LINE_PATTERN.matcher (line);
//        if (!m.matches ()) return false;
//        String pkg = m.group (3);
//        String filename = m.group (4);
//        String resource = pkg.replace ('.', '/') + filename;
//        int lineNumber = Integer.parseInt (m.group (5));
//        ClassPath cp = ClassPathSupport.createClassPath (GlobalPathRegistry.getDefault ().getSourceRoots ().toArray (new FileObject[0]));
//        return cp.findResource (resource) != null;
//    }

    private String toRgbText(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private String escapeAngleBrackets(String str) {
        return str.replace("<", "&lt;");
    }

    private String indentAt(String str, String indent) {
        return str.startsWith("at ") ? indent.repeat(8) + str : str;
    }
}

