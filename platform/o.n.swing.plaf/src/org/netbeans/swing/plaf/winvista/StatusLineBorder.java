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

package org.netbeans.swing.plaf.winvista;

import org.netbeans.swing.plaf.LFCustoms;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * (copy & paste of XP StatusLineBorder)
 * @author  S. Aubrecht
 */
class StatusLineBorder extends AbstractBorder {

    /** Constants for sides of status line border */
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 4;

    private Insets insets;

    private int type;

    /** Constructs new status line border of specified type. Type is bit
     * mask specifying which sides of border should be visible */
    public StatusLineBorder(int type) {
        this.type = type;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y,
    int w, int h) {
        g.translate(x, y);
        Color borderC = UIManager.getColor (LFCustoms.SCROLLPANE_BORDER_COLOR);
        g.setColor(borderC);
        // top
        if ((type & TOP) != 0) {
            g.drawLine(0, 0, w - 1, 0);
        }
        // left side
        if ((type & LEFT) != 0) {
            g.drawLine(0, 0, 0, h - 1);
        }
        // right side
        if ((type & RIGHT) != 0) {
            g.drawLine(w - 1, 0, w - 1, h - 1);
        }

        g.translate(-x, -y);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        if (insets == null) {
            insets = new Insets((type & TOP) != 0 ? 1 : 0,
            (type & LEFT) != 0 ? 1 : 0, 0,
            (type & RIGHT) != 0 ? 1 : 0);
        }
        return insets;
    }

}
