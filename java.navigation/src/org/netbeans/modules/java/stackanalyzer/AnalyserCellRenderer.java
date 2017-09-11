/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
            setBackground (bg == null ? list.getSelectionBackground () : bg);
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
            if (isSelected)
                sb.append("<style> a.val {text-decoration: underline; color: " + toRgbText(getForeground().getRGB()) + "} </style><body>");
            sb.append (line.substring (0, link.getStartOffset ()));
            sb.append ("<a class=\"val\" href=\"\">");
            sb.append (line.substring (link.getStartOffset (), link.getEndOffset ()));
            sb.append ("</a>");
            sb.append (line.substring (link.getEndOffset ()));
            sb.append ("</body></html>");
            setText (sb.toString ());
        } else
            setText (line.trim ());

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

    private static String toRgbText(int rgb) {
        if (rgb > 0xFFFFFF)
            rgb = 0xFFFFFF;
        if (rgb < 0)
            rgb = 0;
        String str = "000000" + Integer.toHexString(rgb); //NOI18N
        return "#" + str.substring(str.length() - 6); //NOI18N
    }
}

