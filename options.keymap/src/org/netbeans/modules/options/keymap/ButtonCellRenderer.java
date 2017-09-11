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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.options.keymap;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;


/**
 * Renderer for table cells customizing shortcut.
 *
 * @author Max Sauer
 */
public class ButtonCellRenderer implements TableCellRenderer {

    private TableCellRenderer defaultRenderer;

    private static ShortcutCellPanel panel;

    public ButtonCellRenderer (TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent (
        JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column
    ) {
        JLabel c = (JLabel)defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (value instanceof String) {
            Rectangle cellRect = table.getCellRect(row, column, false);
            String scCell = (String) value;
            Dimension d = new Dimension((int) cellRect.getWidth(), (int) cellRect.getHeight());
            if (panel == null)
                panel = new ShortcutCellPanel(scCell);
            panel.setText(scCell);
            panel.setSize(d);

            if (isSelected) {
                panel.setBgColor(table.getSelectionBackground());
                if (UIManager.getLookAndFeel ().getID ().equals ("GTK"))
                    panel.setFgCOlor(table.getForeground(), true);
                else
                    panel.setFgCOlor(table.getSelectionForeground(), true);
            } else {
                panel.setBgColor(c.getBackground());
                panel.setFgCOlor(c.getForeground(), false);
            }
            if (hasFocus) {
                panel.setBorder(c.getBorder());
            } else {
                panel.setBorder(null);
            }

            return panel;
        }
        else {
            return c;
        }
    }

}
