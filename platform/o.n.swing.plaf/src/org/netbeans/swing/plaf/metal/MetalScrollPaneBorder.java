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
/*
 * MetalScrollPaneBorder.java
 *
 * Created on March 14, 2004, 4:33 AM
 */

package org.netbeans.swing.plaf.metal;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

/** Scroll pane border for Metal look and feel
 *
 * @author  Dafe Simonek
 */
class MetalScrollPaneBorder extends AbstractBorder {

    private static final Insets insets = new Insets(1, 1, 2, 2);

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y,
    int w, int h) {
        g.translate(x, y);

        Color color = UIManager.getColor("controlShadow");
        g.setColor(color == null ? Color.darkGray : color);
        g.drawRect(0, 0, w-2, h-2);
        color = UIManager.getColor("controlHighlight");
        g.setColor(color == null ? Color.white : color);
        g.drawLine(w-1, 1, w-1, h-1);
        g.drawLine(1, h-1, w-1, h-1);

        g.translate(-x, -y);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }
}
