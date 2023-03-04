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
package org.openide.awt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;
import org.openide.util.VectorIcon;

// For use by CloseButtonFactory only.
final class Windows8VectorCloseButton extends VectorIcon {
    public static final Icon DEFAULT = new Windows8VectorCloseButton(false);
    public static final Icon PRESSED = new Windows8VectorCloseButton(true);
    private final boolean pressed;

    private Windows8VectorCloseButton(boolean pressed) {
        super(14, 15);
        this.pressed = pressed;
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g, int width, int height, double scaling) {
        /* Identical logic to that in o.n.swing.tabcontrol.plaf.Windows8VectorTabControlIcon for the
        TabControlButton.ID_CLOSE_BUTTON case. We can't depend on that module, however, and it makes
        little sense for this module to expose a new API just to share this little piece of
        platform-dependent code. */
        if (pressed) {
            g.setColor(new Color(199, 79, 80, 255));
            g.fillRect(0, 0, width, height);
        }
        g.setColor(pressed ? Color.WHITE : new Color(86, 86, 86, 255));
        if (getIconWidth() == width && getIconHeight() == height)
            setAntiAliasing(g, false);
        double strokeWidth = (pressed ? 1.0 : 0.8) * scaling;
        if (scaling > 1.0)
            strokeWidth *= 1.5f;
        double marginX = 3.5 * scaling;
        int topMarginY = round(4 * scaling);
        int botMarginY = round(4 * scaling);
        g.clip(new Rectangle2D.Double(0, topMarginY, width, height - topMarginY - botMarginY));
        g.setStroke(new BasicStroke((float) strokeWidth));
        g.draw(new Line2D.Double(marginX, topMarginY, width - marginX, height - botMarginY));
        g.draw(new Line2D.Double(width - marginX, topMarginY, marginX, height - botMarginY));
    }
}
