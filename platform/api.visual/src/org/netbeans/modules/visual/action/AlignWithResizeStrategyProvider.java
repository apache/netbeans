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

import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.AlignWithWidgetCollector;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class AlignWithResizeStrategyProvider extends AlignWithSupport implements ResizeStrategy, ResizeProvider {

    private boolean outerBounds;

    public AlignWithResizeStrategyProvider (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator, boolean outerBounds) {
        super (collector, interractionLayer, decorator);
        this.outerBounds = outerBounds;
    }

    public Rectangle boundsSuggested (Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
        Insets insets = widget.getBorder ().getInsets ();
        int minx = insets.left + insets.right;
        int miny = insets.top + insets.bottom;

        suggestedBounds = widget.convertLocalToScene (suggestedBounds);

        Point suggestedLocation, point;
        int tempx, tempy;

        switch (controlPoint) {
            case BOTTOM_CENTER:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width / 2, suggestedBounds.y + suggestedBounds.height);
                if (! outerBounds)
                    suggestedLocation.y -= insets.bottom;

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, false, true, false, false);

                if (! outerBounds)
                    point.y += insets.bottom;

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);
                break;
            case BOTTOM_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y + suggestedBounds.height);
                if (! outerBounds) {
                    suggestedLocation.y -= insets.bottom;
                    suggestedLocation.x += insets.left;
                }

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                if (! outerBounds) {
                    point.y += insets.bottom;
                    point.x -= insets.left;
                }

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);

                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case BOTTOM_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y + suggestedBounds.height);
                if (! outerBounds) {
                    suggestedLocation.y -= insets.bottom;
                    suggestedLocation.x -= insets.right;
                }

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                if (! outerBounds) {
                    point.y += insets.bottom;
                    point.x += insets.right;
                }

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);

                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
            case CENTER_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y + suggestedBounds.height / 2);
                if (! outerBounds)
                    suggestedLocation.x += insets.left;

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, false, false, false);

                if (! outerBounds)
                    point.x -= insets.left;
                
                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case CENTER_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y + suggestedBounds.height / 2);
                if (! outerBounds)
                    suggestedLocation.x -= insets.right;

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, false, false, false);

                if (! outerBounds)
                    point.x += insets.right;
                
                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
            case TOP_CENTER:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width / 2, suggestedBounds.y);
                if (! outerBounds)
                    suggestedLocation.y += insets.top;

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, false, true, false, false);

                if (! outerBounds)
                    point.y -= insets.top;

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;
                break;
            case TOP_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y);
                if (! outerBounds) {
                    suggestedLocation.y += insets.top;
                    suggestedLocation.x += insets.left;
                }

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                if (! outerBounds) {
                    point.y -= insets.top;
                    point.x -= insets.left;
                }

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;

                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case TOP_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y);
                if (! outerBounds) {
                    suggestedLocation.y += insets.top;
                    suggestedLocation.x -= insets.right;
                }

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                if (! outerBounds) {
                    point.y -= insets.top;
                    point.x += insets.right;
                }

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;

                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
        }
        return widget.convertSceneToLocal (suggestedBounds);
    }

    public void resizingStarted (Widget widget) {
        show ();
    }

    public void resizingFinished (Widget widget) {
        hide ();
    }

}
