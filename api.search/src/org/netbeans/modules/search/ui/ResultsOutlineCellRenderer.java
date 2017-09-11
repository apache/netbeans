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
package org.netbeans.modules.search.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import org.netbeans.modules.search.ResultView;
import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

/**
 *
 * @author jhavlin
 */
public class ResultsOutlineCellRenderer extends DefaultOutlineCellRenderer {

    private static final Logger LOG =
            Logger.getLogger(ResultsOutlineCellRenderer.class.getName());
    private static final long MINUTE = 60000;
    private static final long HOUR = 60 * MINUTE;
    private long todayStart = getMidnightTime();

    public ResultsOutlineCellRenderer() {
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component renderer = null;
        if ((value instanceof Property)) {
            Property<?> property = (Property<?>) value;
            try {
                String valueString = getDisplayValue(property);
                if (property.getName().equals("path")) { //NOI18N
                    renderer = super.getTableCellRendererComponent(table,
                            computeFitText(table, row, column, valueString),
                            isSelected, hasFocus, row, column);
                    setToolTip(renderer, property);
                } else if (property.getName().equals("size")) {         //NOI18N
                    renderer = super.getTableCellRendererComponent(table,
                            formatFileSize((Long) property.getValue()),
                            isSelected, hasFocus, row, column);
                    setToolTip(renderer, property);
                } else if (property.getName().equals("lastModified")) { //NOI18N
                    renderer = super.getTableCellRendererComponent(table,
                            formatDate((Date) property.getValue()),
                            isSelected, hasFocus, row, column);
                    setToolTip(renderer, property);
                } else {
                    renderer = super.getTableCellRendererComponent(table,
                            valueString, isSelected, hasFocus, row, column);
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
        if (renderer == null) {
            renderer = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
        }
        if (renderer instanceof JLabel) {
            ((JLabel) renderer).setHorizontalAlignment(SwingConstants.RIGHT);
            ((JLabel) renderer).setHorizontalTextPosition(SwingConstants.RIGHT);
        }
        renderer.setForeground(Color.BLACK);
        return renderer;
    }

    String getDisplayValue(Property<?> p) throws IllegalAccessException,
            InvocationTargetException {
        Object value = p.getValue();
        return value != null ? value.toString() : ""; // NOI18N
    }

    private String computeFitText(JTable table, int rowIdx, int columnIdx,
            String text) {
        if (text == null) {
            text = ""; // NOI18N
        }
        if (text.length() <= 3) {
            return text;
        }

        FontMetrics fm = table.getFontMetrics(table.getFont());
        int width = table.getCellRect(rowIdx, columnIdx, false).width;

        String prefix = "...";                                          //NOI18N
        int sufixLength = fm.stringWidth(prefix + "  ");                 //NOI18
        int desired = width - sufixLength - 15;
        if (desired <= 0) {
            return text;
        }

        for (int i = 1; i <= text.length() - 1; i++) {
            String part = text.substring(text.length() - i, text.length());
            int swidth = fm.stringWidth(part);
            if (swidth >= desired) {
                return part.length() > 0 ? prefix + part + " " : text;  //NOI18N
            }
        }
        return text;
    }

    private String formatFileSize(Long value) {
        if (value < (1 << 10)) {
            return NbBundle.getMessage(ResultView.class, //NOI18N
                    "TXT_FILE_SIZE_B", value);
        } else if (value < (1 << 20)) {
            return NbBundle.getMessage(ResultView.class, //NOI18N
                    "TXT_FILE_SIZE_KB", value >> 10);
        } else if (value < (1 << 30)) {
            return NbBundle.getMessage(ResultView.class, //NOI18N
                    "TXT_FILE_SIZE_MB", value >> 20);
        } else {
            return NbBundle.getMessage(ResultView.class, //NOI18N
                    "TXT_FILE_SIZE_GB", value >> 30);
        }
    }

    private String formatDate(Date date) {
        long time = date.getTime();
        long now = System.currentTimeMillis();
        if (now - time < HOUR) {
            return NbBundle.getMessage(ResultView.class,
                    "TXT_LAST_MODIFIED_RECENT", (now - time) / MINUTE); //NOI18N
        } else if (time > todayStart) {
            return NbBundle.getMessage(ResultView.class,
                    "TXT_LAST_MODIFIED_TODAY", date);                   //NOI18N
        } else {
            return NbBundle.getMessage(ResultView.class,
                    "TXT_LAST_MODIFIED_OLD", date);                     //NOI18N
        }
    }

    private long getMidnightTime() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.HOUR, 0);
        return c.getTimeInMillis();
    }

    private void setToolTip(Component renderer, Property<?> property)
            throws IllegalAccessException, InvocationTargetException {
        if (renderer instanceof JLabel) {
            Object val = property.getValue();
            if (val != null) {
                ((JLabel) renderer).setToolTipText(val.toString());
            }
        }
    }
}
