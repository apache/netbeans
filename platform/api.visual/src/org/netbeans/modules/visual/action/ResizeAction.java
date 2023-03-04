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

import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Rezing a widget based on insets of the widget border.
 *
 * @author David Kaspar
 */
public final class ResizeAction extends WidgetAction.LockedAdapter {

    private ResizeStrategy strategy;
    private ResizeControlPointResolver resolver;
    private ResizeProvider provider;

    private Widget resizingWidget = null;
    private ResizeProvider.ControlPoint controlPoint;
    private Rectangle originalSceneRectangle = null;
    private Insets insets;
    private Point dragSceneLocation = null;

    public ResizeAction (ResizeStrategy strategy, ResizeControlPointResolver resolver, ResizeProvider provider) {
        this.strategy = strategy;
        this.resolver = resolver;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return resizingWidget != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            insets = widget.getBorder ().getInsets ();
            controlPoint = resolver.resolveControlPoint (widget, event.getPoint ());
            if (controlPoint != null) {
                resizingWidget = widget;
                originalSceneRectangle = null;
                if (widget.isPreferredBoundsSet ())
                    originalSceneRectangle = widget.getPreferredBounds ();
                if (originalSceneRectangle == null)
                    originalSceneRectangle = widget.getBounds ();
                if (originalSceneRectangle == null)
                    originalSceneRectangle = widget.getPreferredBounds ();
                dragSceneLocation = widget.convertLocalToScene (event.getPoint ());
                provider.resizingStarted (widget);
                return State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        boolean state = resize (widget, event.getPoint ());
        if (state) {
            resizingWidget = null;
            provider.resizingFinished (widget);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return resize (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean resize (Widget widget, Point newLocation) {
        if (resizingWidget != widget)
            return false;

        newLocation = widget.convertLocalToScene (newLocation);
        int dx = newLocation.x - dragSceneLocation.x;
        int dy = newLocation.y - dragSceneLocation.y;
        int minx = insets.left + insets.right;
        int miny = insets.top + insets.bottom;

        Rectangle rectangle = new Rectangle (originalSceneRectangle);
        switch (controlPoint) {
            case BOTTOM_CENTER:
                resizeToBottom (miny, rectangle, dy);
                break;
            case BOTTOM_LEFT:
                resizeToLeft (minx, rectangle, dx);
                resizeToBottom (miny, rectangle, dy);
                break;
            case BOTTOM_RIGHT:
                resizeToRight (minx, rectangle, dx);
                resizeToBottom (miny, rectangle, dy);
                break;
            case CENTER_LEFT:
                resizeToLeft (minx, rectangle, dx);
                break;
            case CENTER_RIGHT:
                resizeToRight (minx, rectangle, dx);
                break;
            case TOP_CENTER:
                resizeToTop (miny, rectangle, dy);
                break;
            case TOP_LEFT:
                resizeToLeft (minx, rectangle, dx);
                resizeToTop (miny, rectangle, dy);
                break;
            case TOP_RIGHT:
                resizeToRight (minx, rectangle, dx);
                resizeToTop (miny, rectangle, dy);
                break;
        }

        widget.setPreferredBounds (strategy.boundsSuggested (widget, originalSceneRectangle, rectangle, controlPoint));
        return true;
    }

    private static void resizeToTop (int miny, Rectangle rectangle, int dy) {
        if (rectangle.height - dy < miny)
            dy = rectangle.height - miny;
        rectangle.y += dy;
        rectangle.height -= dy;
    }

    private static void resizeToBottom (int miny, Rectangle rectangle, int dy) {
        if (rectangle.height + dy < miny)
            dy = miny - rectangle.height;
        rectangle.height += dy;
    }

    private static void resizeToLeft (int minx, Rectangle rectangle, int dx) {
        if (rectangle.width - dx < minx)
            dx = rectangle.width - minx;
        rectangle.x += dx;
        rectangle.width -= dx;
    }

    private static void resizeToRight (int minx, Rectangle rectangle, int dx) {
        if (rectangle.width + dx < minx)
            dx = minx - rectangle.width;
        rectangle.width += dx;
    }

//    public static class SnapToGridStrategy implements Strategy {
//
//        private int horizontalGridSize;
//        private int verticalGridSize;
//
//        public SnapToGridStrategy (int horizontalGridSize, int verticalGridSize) {
//            assert horizontalGridSize > 0  &&  verticalGridSize > 0;
//            this.horizontalGridSize = horizontalGridSize;
//            this.verticalGridSize = verticalGridSize;
//        }
//
//        public Rectangle boundsSuggested (Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
//            switch (controlPoint) {
//                case BOTTOM_CENTER:
//                    snapToBottom (suggestedBounds);
//                    break;
//                case BOTTOM_LEFT:
//                    snapToBottom (suggestedBounds);
//                    snapToLeft (suggestedBounds);
//                    break;
//                case BOTTOM_RIGHT:
//                    snapToBottom (suggestedBounds);
//                    snapToRight (suggestedBounds);
//                    break;
//                case CENTER_LEFT:
//                    snapToLeft (suggestedBounds);
//                    break;
//                case CENTER_RIGHT:
//                    snapToRight (suggestedBounds);
//                    break;
//                case TOP_CENTER:
//                    snapToTop (suggestedBounds);
//                    break;
//                case TOP_LEFT:
//                    snapToTop (suggestedBounds);
//                    snapToLeft (suggestedBounds);
//                    break;
//                case TOP_RIGHT:
//                    snapToTop (suggestedBounds);
//                    snapToRight (suggestedBounds);
//                    break;
//            }
//            return suggestedBounds;
//        }
//
//    }

}
