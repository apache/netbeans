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

import java.awt.*;


/** Custom Table cell renderer that paints a bar based on numerical value within min/max bounds.
 *
 * @author Jiri Sedlacek
 */
public class DiffBarCellRenderer extends CustomBarCellRenderer {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final Color BAR_FOREGROUND2_COLOR = new Color(41, 195, 41);

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public DiffBarCellRenderer(long min, long max) {
        super(min, max);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        Insets insets = getInsets();
        int clientWidth = getWidth() - insets.right - insets.left;
        int horizCenter = insets.left + (clientWidth / 2);
        int barExtent = (int) Math.ceil((Math.abs(relValue) * ((double) clientWidth)) / 2d);

        if (relValue > 0) {
            g.setColor(BAR_FOREGROUND_COLOR);
            g.fillRect(horizCenter, insets.top, barExtent, getHeight() - insets.bottom - insets.top);
        } else if (relValue < 0) {
            g.setColor(BAR_FOREGROUND2_COLOR);
            g.fillRect(horizCenter - barExtent, insets.top, barExtent, getHeight() - insets.bottom - insets.top);
        }
    }

    protected double calculateViewValue(long n) {
        long absMax = Math.max(Math.abs(min), max);

        return (double) (n) / (double) (absMax);
    }

    protected double calculateViewValue(double n) {
        long absMax = Math.max(Math.abs(min), max);

        return (double) (n) / (double) (absMax);
    }
}
