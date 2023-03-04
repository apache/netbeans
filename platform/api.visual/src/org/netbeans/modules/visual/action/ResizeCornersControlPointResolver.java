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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.util.GeomUtil;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ResizeCornersControlPointResolver implements ResizeControlPointResolver {

    public ResizeProvider.ControlPoint resolveControlPoint (Widget widget, Point point) {
        Rectangle bounds = widget.getBounds ();
        Insets insets = widget.getBorder ().getInsets ();
        Point center = GeomUtil.center (bounds);
        Dimension centerDimension = new Dimension (Math.max (insets.left, insets.right), Math.max (insets.top, insets.bottom));
        if (point.y >= bounds.y + bounds.height - insets.bottom && point.y < bounds.y + bounds.height) {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.BOTTOM_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.BOTTOM_LEFT;
            else
            if (point.x >= center.x - centerDimension.height / 2 && point.x < center.x + centerDimension.height - centerDimension.height / 2)
                return ResizeProvider.ControlPoint.BOTTOM_CENTER;

        } else if (point.y >= bounds.y && point.y < bounds.y + insets.top) {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.TOP_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.TOP_LEFT;
            else
            if (point.x >= center.x - centerDimension.height / 2 && point.x < center.x + centerDimension.height - centerDimension.height / 2)
                return ResizeProvider.ControlPoint.TOP_CENTER;

        } else
        if (point.y >= center.y - centerDimension.width / 2 && point.y < center.y + centerDimension.width - centerDimension.width / 2)
        {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.CENTER_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.CENTER_LEFT;

        }
        // TODO - resolve CENTER points
        return null;
    }

}
