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
package org.netbeans.swing.laf.flatlaf.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.UIManager;
import com.formdev.flatlaf.util.UIScale;

/**
 * Toolbar grip consistent with FlatLaf style.
 * Paint code is modeled after {@code com.formdev.flatlaf.ui.FlatToolBarBorder}.
 */
public class FlatToolbarDragger extends JPanel {
	private static final int DOT_COUNT = 4;
	private static final int DOT_SIZE = 2;
	private static final int GRIP_WIDTH = DOT_SIZE * 3;
    private final Dimension min;
    private final Dimension max;
    protected final Color gripColor = UIManager.getColor("ToolBar.gripColor"); //NOI18N

    public FlatToolbarDragger() {
        min = new Dimension(GRIP_WIDTH, GRIP_WIDTH);
        max = new Dimension(GRIP_WIDTH, Integer.MAX_VALUE);
    }

    @Override
    public void paint (Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        try {
            Utils.setRenderingHints(g2);
            g2.setColor(gripColor);
            int dotSize = UIScale.scale(DOT_SIZE);
            int gapSize = dotSize;
            int gripSize = (dotSize * DOT_COUNT) + ((gapSize * (DOT_COUNT - 1)));
            // paint dots
            int y = Math.round((getHeight() - gripSize) / 2f);
            for(int i = 0; i < DOT_COUNT; i++) {
                g2.fillOval(dotSize, y, dotSize, dotSize);
                y += dotSize + gapSize;
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getMinimumSize () {
        return UIScale.scale(min);
    }

    @Override
    public Dimension getPreferredSize () {
        return this.getMinimumSize ();
    }

    @Override
    public Dimension getMaximumSize () {
        return UIScale.scale(max);
    }

}