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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.Icon;
import org.openide.util.VectorIcon;

/* For use by CloseButtonFactory only. The "mac_close_(enabled|pressed|rollover).png" files were
confirmed to be identical to the mac_bigclose_(enabled|pressed|rollover).png ones, so the same
vector icons can be used here for either case. */
final class AquaVectorCloseButton extends VectorIcon {
    public static final Icon DEFAULT = new AquaVectorCloseButton(State.DEFAULT);
    public static final Icon PRESSED = new AquaVectorCloseButton(State.PRESSED);
    public static final Icon ROLLOVER = new AquaVectorCloseButton(State.ROLLOVER);
    private final State state;

    private enum State { DEFAULT, PRESSED, ROLLOVER}

    private AquaVectorCloseButton(State state) {
        super(14, 12);
        this.state = state;
    }

    @Override
    protected void paintIcon(Component c, Graphics2D g, int width, int height, double scaling) {
        /* Identical logic to that in o.n.swing.tabcontrol.plaf.AquaVectorTabControlIcon for the
        TabControlButton.ID_CLOSE_BUTTON case. We can't depend on that module, however, and it makes
        little sense for this module to expose a new API just to share this little piece of
        platform-dependent code. */
        double d = Math.min(width, height);
        Color bgColor = new Color(0, 0, 0, 0);
        Color fgColor = new Color(0, 0, 0, 168);
        if (state == State.ROLLOVER) {
            fgColor = Color.WHITE;
            bgColor = new Color(255, 35, 25, 215);
        } else if (state == State.PRESSED) {
            fgColor = Color.WHITE;
            bgColor = new Color(185, 43, 33, 215);
        }
        if (bgColor.getAlpha() > 0) {
            double circPosX = (width - d) / 2.0;
            double circPosY = (height - d) / 2.0;
            Shape bgCircle = new Ellipse2D.Double(circPosX, circPosY, d, d);
            g.setColor(bgColor);
            g.fill(bgCircle);
        }
        g.setColor(fgColor);
        double strokeWidth = 1.4 * scaling;
        double mx = width / 2.0;
        double my = height / 2.0;
        double cr = 0.45 * (d / 2.0);
        Stroke stroke = new BasicStroke(
                (float) strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Area area = new Area();
        area.add(new Area(stroke.createStrokedShape(
                new Line2D.Double(mx - cr, my - cr, mx + cr, my + cr))));
        area.add(new Area(stroke.createStrokedShape(
                new Line2D.Double(mx + cr, my - cr, mx - cr, my + cr))));
        g.fill(area);
    }
}
