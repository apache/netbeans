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
package org.netbeans.swing.plaf.aqua;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * SplitPane UI that paints the splitter using the same color as NetBeans window system splitter.
 * 
 * @author M. Fukala
 */
public class CleanSplitPaneUI extends BasicSplitPaneUI {

    @Override
    protected void installDefaults() {
        super.installDefaults();
        divider.setBorder(new SplitBorder());
    }

    public static ComponentUI createUI(JComponent x) {
        return new CleanSplitPaneUI();
    }

    private static class SplitBorder implements Border {

        private Color bkColor = UIManager.getColor("NbSplitPane.background"); //NOI18N

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(bkColor);
            g.fillRect(x,y,width,height);
        }
    }
}
