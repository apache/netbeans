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
package org.netbeans.swing.plaf.windows8;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import org.netbeans.swing.plaf.LFCustoms;

/**
 * (copy & paste of XP EditorToolbarBorder)
 * @author  S. Aubrecht
 * @since 1.30
 */
class EditorToolbarBorder extends AbstractBorder {
    // Add two pixels of left padding here, to give some space around the "Source" button.
    private static final Insets insets = new Insets(1, 2, 1, 0);

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
        Color borderC = UIManager.getColor (LFCustoms.SCROLLPANE_BORDER_COLOR);
        g.setColor(borderC);
        g.drawLine(x, y + h - 1, x + w - 1, y + h - 1);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }  
}
