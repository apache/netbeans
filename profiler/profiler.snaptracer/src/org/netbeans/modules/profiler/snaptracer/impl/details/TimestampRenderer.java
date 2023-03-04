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

package org.netbeans.modules.profiler.snaptracer.impl.details;

import java.awt.Component;
import java.text.Format;
import java.text.SimpleDateFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.lib.profiler.charts.axis.TimeAxisUtils;

/**
 *
 * @author Jiri Sedlacek
 */
final class TimestampRenderer extends DetailsTableCellRenderer {

    // Fri Mar 19 11:59:59.999 AM 2010
    static final long REFERENCE_TIMESTAMP = 1268996399999l;

    private String formatString;
    private Format format;

    TimestampRenderer(TableCellRenderer renderer) {
        super(renderer);
    }

    protected Object formatValue(JTable table, Object value, boolean isSelected,
                                 boolean hasFocus, int row, int column) {
        String valueString = format.format(value);
        // Improve spacing of the text
        return " " + valueString + " "; // NOI18N
    }

    protected void updateRenderer(Component c, JTable table, Object value,
                                  boolean isSelected, boolean hasFocus, int row,
                                  int column) {
        super.updateRenderer(c, table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel) ((JLabel)c).setHorizontalAlignment(JLabel.TRAILING);
    }

    boolean updateFormat(TableModel model) {
        int rowCount = model.getRowCount();

        long first = rowCount > 0 ? (Long)model.getValueAt(0, 1) : REFERENCE_TIMESTAMP;
        long last  = rowCount > 0 ? (Long)model.getValueAt(rowCount - 1, 1) :
                                    REFERENCE_TIMESTAMP + 1;
        
        String newFormatString = TimeAxisUtils.getFormatString(1, first, last);
        if (!newFormatString.equals(formatString)) {
            formatString = newFormatString;
            format = new SimpleDateFormat(formatString);
            return true;
        } else {
            return false;
        }
    }

}
