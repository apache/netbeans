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

package org.netbeans.swing.plaf.gtk;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * A trivial border class which centered dividers shorter than the component
 *
 * @author  Tim Boudreau
 */
public class InsetBorder implements Border {
    private boolean left;
    private boolean right;

    /** Creates a new instance of InsetBorder */
    public InsetBorder(boolean left, boolean right) {
        this.left = left;
        this.right = right;
    }
    
    public java.awt.Insets getBorderInsets(java.awt.Component c) {
        return new Insets (2, left ? 6 : 2, 0, right ? 6 : 2);
    }
    
    public boolean isBorderOpaque() {
        return false;
    }
    
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int h = c.getHeight();
        Color col = g.getColor();
        g.setColor (UIManager.getColor("controlShadow")); //NOI18N
        if (left) {
            g.drawLine (x + 3, y, x + 3, y + h - 1);
        }
        if (right) {
            g.drawLine (x + width - 3, y, x + width - 3, y + h - 1);
        }
        g.drawLine (x, y, x + width - 1, y);
    }
    
}
