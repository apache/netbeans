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

package org.netbeans.lib.profiler.ui.components.table;

import org.netbeans.lib.profiler.ui.components.*;
import java.awt.*;
import javax.swing.*;


/** Custom Table cell renderer that paints a bar based on numerical value within min/max bounds.
 *
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
public class CustomBarCellRenderer extends EnhancedTableCellRenderer {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final Color BAR_FOREGROUND_COLOR = new Color(195, 41, 41);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected double relValue; // relative part of max - min, <0, 1>
    protected long max;
    protected long min;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CustomBarCellRenderer(long min, long max) {
        setMinimum(min);
        setMaximum(max);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setMaximum(long n) {
        max = n;
    }

    public void setMinimum(long n) {
        min = n;
    }

    public void setRelValue(double n) {
        relValue = n;
    }

    public Component getTableCellRendererComponentPersistent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                             int row, int column) {
        return null;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Insets insets = getInsets();
        g.setColor(BAR_FOREGROUND_COLOR);
        g.fillRect(insets.left, insets.top, (int) Math.round(relValue * (getWidth() - insets.right - insets.left)),
                   getHeight() - insets.bottom - insets.top);
    }

    /**
     * Called each time this renderer is to be used to render a specific value on specified row/column.
     * Subclasses need to implement this method to render the value.
     *
     * @param table  the table in which the rendering occurs
     * @param value  the value to be rendered
     * @param row    the row at which the value is located
     * @param column the column at which the value is located
     */
    protected void setValue(JTable table, Object value, int row, int column) {
        if (value instanceof Long) {
            //multiplying by 10 to allow displaying graphs for values < 1
            // - same done for maxi and min values of progress bar, should be ok
            setRelValue(calculateViewValue(((Long) value).longValue()));
        } else if (value instanceof Number) {
            //multiplying by 10 to allow displaying graphs for values < 1
            // - same done for maxi and min values of progress bar, should be ok
            setRelValue(calculateViewValue(((Number) value).doubleValue()));
        } else if (value instanceof String) {
            //multiplying by 10 to allow displaying graphs for values < 1
            // - same done for maxi and min values of progress bar, should be ok
            setRelValue(calculateViewValue(Double.parseDouble((String) value)));
        } else {
            setRelValue(min);
        }
    }

    protected double calculateViewValue(long n) {
        return (double) (n - min) / (double) (max - min);
    }

    protected double calculateViewValue(double n) {
        return (double) (n - min) / (double) (max - min);
    }
}
