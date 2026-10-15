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
                switch (property.getName()) {
                    case "path": //NOI18N
                        renderer = super.getTableCellRendererComponent(table,
                                computeFitText(table, row, column, valueString),
                                isSelected, hasFocus, row, column);
                        setToolTip(renderer, property);
                        break;
                    case "size": //NOI18N
                        renderer = super.getTableCellRendererComponent(table,
                                formatFileSize((Long) property.getValue()),
                                isSelected, hasFocus, row, column);
                        setToolTip(renderer, property);
                        break;
                    case "lastModified": //NOI18N
                        renderer = super.getTableCellRendererComponent(table,
                                formatDate((Date) property.getValue()),
                                isSelected, hasFocus, row, column);
                        setToolTip(renderer, property);
                        break;
                    default:
                        renderer = super.getTableCellRendererComponent(table,
                            valueString, isSelected, hasFocus, row, column);
                        break;
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
            String part = text.substring(text.length() - i);
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
