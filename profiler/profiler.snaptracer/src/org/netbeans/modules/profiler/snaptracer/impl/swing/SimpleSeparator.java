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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author Jiri Sedlacek
 */
public final class SimpleSeparator extends JPanel implements SwingConstants {

    private final int orientation;
    private final Dimension preferredSize = new Dimension(1, 1);


    public SimpleSeparator() {
        this(HORIZONTAL);
    }

    public SimpleSeparator(int orientation) {
        super(null);
        this.orientation = orientation;
    }


    public void setPreferredSize(Dimension preferredSize) {
        this.preferredSize.width = preferredSize.width;
        this.preferredSize.height = preferredSize.height;
    }


    public Dimension getMinimumSize() {
        Insets insets = getInsets();
        if (orientation == HORIZONTAL)
            return new Dimension(insets.left + insets.right,
                                 insets.top + insets.bottom + 1);
        else
            return new Dimension(insets.left + insets.right + 1,
                                 insets.top + insets.bottom);
    }

    public Dimension getMaximumSize() {
        Insets insets = getInsets();
        if (orientation == HORIZONTAL)
            return new Dimension(Integer.MAX_VALUE,
                                 insets.top + insets.bottom + 1);
        else
            return new Dimension(insets.left + insets.right + 1,
                                 Integer.MAX_VALUE);
    }

    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        if (orientation == HORIZONTAL)
            return new Dimension(Math.max(insets.left + insets.right, preferredSize.width),
                                 insets.top + insets.bottom + 1);
        else
            return new Dimension(insets.left + insets.right + 1,
                                 Math.max(insets.top + insets.bottom, preferredSize.height));
    }


    public void paint(Graphics g) {
        g.setColor(new Color(192, 192, 192));
        Insets insets = getInsets();
        if (orientation == HORIZONTAL)
            g.drawLine(insets.left, insets.top, getWidth() - insets.right, insets.top);
        else
            g.drawLine(insets.left, insets.top, insets.left, getHeight() - insets.bottom);
    }

}
