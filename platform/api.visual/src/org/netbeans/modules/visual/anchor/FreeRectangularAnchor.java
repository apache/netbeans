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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * @author Alex
 */
public final class FreeRectangularAnchor extends Anchor {

    private boolean includeBorders;

    public FreeRectangularAnchor(Widget widget, boolean includeBorders) {
        super(widget);
        this.includeBorders = includeBorders;
    }

    public Result compute(Entry entry) {
        ConnectionWidget fcw = entry.getAttachedConnectionWidget ();
        assert fcw != null;
        Point relatedLocation = getRelatedSceneLocation();
        Widget widget = getRelatedWidget();
        List<Point> fcwControlPoints = fcw.getControlPoints ();

        Point oppositeLocation;
        if (fcwControlPoints.size () < 2)
            oppositeLocation = getOppositeSceneLocation (entry);
        else if (entry.isAttachedToConnectionSource ())
            oppositeLocation = fcwControlPoints.get (1);
        else
            oppositeLocation = fcwControlPoints.get (fcwControlPoints.size () - 2);

        Rectangle bounds = widget.getBounds();
        if (! includeBorders) {
            Insets insets = widget.getBorder().getInsets();
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        bounds = widget.convertLocalToScene(bounds);

        if (bounds.isEmpty()  || relatedLocation.equals(oppositeLocation))
            return new Anchor.Result(relatedLocation, Anchor.DIRECTION_ANY);

        float dx = oppositeLocation.x - relatedLocation.x;
        float dy = oppositeLocation.y - relatedLocation.y;

        float ddx = Math.abs(dx) / (float) bounds.width;
        float ddy = Math.abs(dy) / (float) bounds.height;

        Anchor.Direction direction;

        if (ddx >= ddy) {
            direction = dx >= 0.0f ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = dy >= 0.0f ? Direction.BOTTOM : Direction.TOP;
        }

        float scale = 0.5f / Math.max(ddx, ddy);

        Point point = new Point(Math.round(relatedLocation.x + scale * dx), Math.round(relatedLocation.y + scale * dy));
        return new Anchor.Result(point, direction);
    }

}
