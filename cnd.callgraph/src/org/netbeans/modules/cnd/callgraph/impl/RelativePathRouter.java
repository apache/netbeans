/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.cnd.callgraph.impl;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 */
public class RelativePathRouter implements Router {

    private static final int SHIFT_DISTANCE = 15;

    private final LayerWidget connectionLayer;

    public RelativePathRouter(LayerWidget connectionLayer) {
        this.connectionLayer = connectionLayer;
    }

    @Override
    public List<Point> routeConnection(ConnectionWidget widget) {

        ArrayList<Point> list = new ArrayList<Point>();

        Anchor sourceAnchor = widget.getSourceAnchor();
        Anchor targetAnchor = widget.getTargetAnchor();

        if (sourceAnchor == null || targetAnchor == null) {
            return Collections.emptyList();
        }

        Point p0 = sourceAnchor.compute(widget.getSourceAnchorEntry()).getAnchorSceneLocation();
        Point p1 = targetAnchor.compute(widget.getTargetAnchorEntry()).getAnchorSceneLocation();

        if (p0.x == p1.x && p0.y == p1.y) {
            return Collections.emptyList();
        }

        list.add(p0);
        Point medium = new Point((p0.x + p1.x) / 2, (p0.y + p1.y) / 2);

        ConnectionWidget opposite = oppositeConnection(widget);
        if (opposite != null) {
//             TODO need a more reliable way, this is a fast workaround
            int h0 = widget.hashCode();
            int h1 = opposite.hashCode();
            boolean positiveShift = (h0 >= h1);

            Point shiftedMedium;
//            // vertical line
            if (p1.x - p0.x == 0) {
                shiftedMedium = new Point((positiveShift) ? medium.x + SHIFT_DISTANCE : medium.x - SHIFT_DISTANCE, medium.y);
            } // horizontal line
            else if (p1.y - p0.y == 0) {
                shiftedMedium = new Point(medium.x, (positiveShift) ? medium.y + SHIFT_DISTANCE : medium.y - SHIFT_DISTANCE);
            } else {
                double k = (double) (p1.y - p0.y) / (p1.x - p0.x);
                double invK = -1d / k;
                double x2;
                double y2;
                if (Math.abs(invK) >= 1) {
                    y2 = (positiveShift) ? medium.y + SHIFT_DISTANCE : medium.y - SHIFT_DISTANCE;
                    x2 = medium.x + (y2 - medium.y) / invK;
                } else {
                    x2 = (positiveShift) ? medium.x + SHIFT_DISTANCE : medium.x - SHIFT_DISTANCE;
                    y2 = invK * (x2 - medium.x) + medium.y;
                }
                shiftedMedium = new Point((int) x2, (int) y2);
            }
            list.add(shiftedMedium);
        } else {
            list.add(medium);
        }
        list.add(p1);

        return list;
    }

    private ConnectionWidget oppositeConnection(ConnectionWidget c0) {
        Widget sourceAnchorWidget = c0.getSourceAnchor().getRelatedWidget();
        Widget targetAnchorWidget = c0.getTargetAnchor().getRelatedWidget();

        for (Widget w : connectionLayer.getChildren()) {
            if (w instanceof ConnectionWidget) {
                ConnectionWidget c1 = (ConnectionWidget) w;
                Anchor sourceAnchor = c1.getSourceAnchor();
                Anchor targetAnchor = c1.getTargetAnchor();
                if (sourceAnchor == null || targetAnchor == null) {
                    continue;
                }
                if (sourceAnchor.getRelatedWidget().equals(targetAnchorWidget) && targetAnchor.getRelatedWidget().equals(sourceAnchorWidget)) {
                    return c1;
                }
            }
        }
        return null;
    }

}
