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

package org.netbeans.swing.plaf.metal;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 *
 * @author  Dafe Simonek
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
    public StatusLineBorder (int type) {
        this.type = type;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y,
    int w, int h) {
        g.translate(x, y);
        Color shadowC = UIManager.getColor("controlShadow"); //NOI18N
        Color highlightC = UIManager.getColor("controlHighlight"); //NOI18N
        Color middleC = UIManager.getColor("control"); //NOI18N
        // top
        if ((type & TOP) != 0) {
            g.setColor(shadowC);
            g.drawLine(0, 0, w - 1, 0);
            g.drawLine(0, 3, w - 1, 3);
            g.setColor(highlightC);
            g.drawLine(0, 1, w - 1, 1);
            g.setColor(middleC);
            g.drawLine(0, 2, w - 1, 2);
        }
        // left side
        if ((type & LEFT) != 0) {
            g.setColor(middleC);
            g.drawLine(0, 2, 0, h - 1);
            g.setColor(shadowC);
            g.drawLine(1, 3, 1, h - 1);
        }
        // right side
        if ((type & RIGHT) != 0) {
            g.setColor(shadowC);
            g.drawLine(w - 2, 3, w - 2, h - 1);
            g.setColor(highlightC);
            g.drawLine(w - 1, 4, w - 1, h - 1);
            g.setColor(middleC);
            g.drawLine(w - 1, 3, w - 1, 3);
        }

        g.translate(-x, -y);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        if (insets == null) {
            insets = getBorderInsets(c, new Insets(0, 0, 0, 0));
        }
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = (type & LEFT) != 0 ? 2 : 0;
        insets.top = (type & TOP) != 0 ? 4 : 0;
        insets.right = (type & RIGHT) != 0 ? 2 : 0;
        insets.bottom = 0;
        return insets;
    }    

}
