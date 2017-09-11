/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.AlignWithWidgetCollector;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.Collection;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author David Kaspar
 */
public class AlignWithSupport {

    private static final int GRAVITY = 10;

    private AlignWithWidgetCollector collector;
    private LayerWidget interractionLayer;
    private AlignWithMoveDecorator decorator;

    private ConnectionWidget lineWidget1, lineWidget2;

    public AlignWithSupport (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator) {
        this.collector = collector;
        this.interractionLayer = interractionLayer;
        this.decorator = decorator;
    }

    protected Point locationSuggested (Widget widget, Rectangle sceneWidgetBounds, Point suggestedLocation, boolean horizontal, boolean vertical, boolean bothSides, boolean snapHack) {
        Point point = new Point (suggestedLocation);
        Collection<Rectangle> regions = collector.getRegions (widget);

        if (horizontal) {
            boolean snap = false;
            int xs = 0, x = 0, dx = 0, y1 = 0, y2 = 0;

            int b1 = sceneWidgetBounds.x;
            int b2 = sceneWidgetBounds.x + sceneWidgetBounds.width;
            int b3 = sceneWidgetBounds.x + (sceneWidgetBounds.width/2);

            for (Rectangle rectangle : regions) {
                int a1 = rectangle.x;
                int a2 = a1 + rectangle.width;
                int a3 = a1 +(rectangle.width/2);

                int d;
                boolean snapNow = false;

                d = Math.abs (a1 - b1);
                if ((snap && d < dx) || (!snap && d < GRAVITY)) {
                    snap = snapNow = true;
                    x = xs = a1;
                    dx = d;
                }

                if (bothSides) {
                    d = Math.abs (a1 - b2);
                    if ((snap && d < dx) || (!snap && d < GRAVITY)) {
                        snap = snapNow = true;
                        x = a1;
                        xs = a1 - sceneWidgetBounds.width;
                        dx = d;
                    }
                }

                d = Math.abs (a2 - b1);
                if ((snap && d < dx) || (!snap && d < GRAVITY)) {
                    snap = snapNow = true;
                    x = xs = a2;
                    dx = d;
                }

                if (bothSides) {
                    d = Math.abs (a2 - b2);
                    if ((snap && d < dx) || (!snap && d < GRAVITY)) {
                        snap = snapNow = true;
                        x = a2;
                        xs = a2 - sceneWidgetBounds.width;
                        dx = d;
                    }
                }

                d = Math.abs(a3 - b3);
                if ((snap && d < dx) || (!snap && d < GRAVITY)) {
                    snap = snapNow = true;
                    xs = a3 - (b3 - b1);
                    x = a3;
                    dx = d;
                }

                if (snapNow) {
                    y1 = rectangle.y;
                    y2 = rectangle.y + rectangle.height;
                }
            }

            if (snap) {
                point.x = xs;
                if (snapHack)
                    point.x -= widget.getBounds ().x;
            }


            if (interractionLayer != null)
                lineWidget1.setControlPoints (snap ? Arrays.asList (
                    new Point (x, Math.min (sceneWidgetBounds.y, y1)),
                    new Point (x, Math.max (sceneWidgetBounds.y + sceneWidgetBounds.height, y2))
                ) : Collections.<Point>emptyList (), true);
        }

        if (vertical) {
            boolean snap = false;
            int ys = 0, y = 0, dy = 0, x1 = 0, x2 = 0;

            int b1 = sceneWidgetBounds.y;
            int b2 = sceneWidgetBounds.y + sceneWidgetBounds.height;
            int b3 = sceneWidgetBounds.y + (sceneWidgetBounds.height/2);

            for (Rectangle rectangle : regions) {
                int a1 = rectangle.y;
                int a2 = a1 + rectangle.height;
                int a3 = a1 + (rectangle.height/2);

                int d;
                boolean snapNow = false;

                d = Math.abs (a1 - b1);
                if ((snap && d < dy) || (!snap && d < GRAVITY)) {
                    snap = snapNow = true;
                    y = ys = a1;
                    dy = d;
                }

                d = Math.abs (a1 - b2);
                if ((snap && d < dy) || (!snap && d < GRAVITY)) {
                    snap = snapNow = true;
                    ys = a1 - sceneWidgetBounds.height;
                    y = a1;
                    dy = d;
                }

                d = Math.abs (a2 - b1);
                if ((snap && d < dy) || (!snap && d < GRAVITY)) {
                    snap = snapNow = true;
                    y = ys = a2;
                    dy = d;
                }

                d = Math.abs (a2 - b2);
                if ((snap && d < dy) || (!snap && d < GRAVITY)) {
                    snap = snapNow = true;
                    ys = a2 - sceneWidgetBounds.height;
                    y = a2;
                    dy = d;
                }

                d = Math.abs(a3 - b3);
                if ((snap && d < dy) || (!snap && d < GRAVITY)) {
                    snap = snapNow = true;
                    ys = a3 - (b3 - b1);
                    y = a3;
                    dy = d;
                }

                if (snapNow) {
                    x1 = rectangle.x;
                    x2 = rectangle.x + rectangle.width;
                }
            }
            if (snap) {
                point.y = ys;
                if (snapHack)
                    point.y -= widget.getBounds ().y;
            }

            if (interractionLayer != null)
                lineWidget2.setControlPoints (snap ? Arrays.asList (
                    new Point (Math.min (sceneWidgetBounds.x, x1), y),
                    new Point (Math.max (sceneWidgetBounds.x + sceneWidgetBounds.width, x2), y)
                ) : Collections.<Point>emptyList (), true);
        }

        return point;
    }

    public void show () {
        if (interractionLayer != null) {
            if (lineWidget1 == null)
                lineWidget1 = decorator.createLineWidget (interractionLayer.getScene ());
            if (lineWidget2 == null)
                lineWidget2 = decorator.createLineWidget (interractionLayer.getScene ());
            interractionLayer.addChild (lineWidget1);
            interractionLayer.addChild (lineWidget2);
            lineWidget1.setControlPoints (Collections.<Point>emptySet (), true);
            lineWidget2.setControlPoints (Collections.<Point>emptySet (), true);
        }
    }

    public void hide () {
        if (interractionLayer != null) {
            interractionLayer.removeChild (lineWidget1);
            interractionLayer.removeChild (lineWidget2);
        }
    }

}
