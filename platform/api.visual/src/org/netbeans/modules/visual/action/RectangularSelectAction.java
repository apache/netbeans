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

import org.netbeans.api.visual.action.RectangularSelectDecorator;
import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
public final class RectangularSelectAction extends WidgetAction.LockedAdapter {

    private RectangularSelectDecorator decorator;
    private LayerWidget interractionLayer;
    private RectangularSelectProvider provider;

    private Widget selectionWidget;
    private Rectangle selectionSceneRectangle;

    public RectangularSelectAction (RectangularSelectDecorator decorator, LayerWidget interractionLayer, RectangularSelectProvider provider) {
        this.decorator = decorator;
        this.interractionLayer = interractionLayer;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return selectionSceneRectangle != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            selectionWidget = decorator.createSelectionWidget ();
            assert selectionWidget != null;
            interractionLayer.addChild (selectionWidget);
            selectionSceneRectangle = new Rectangle (widget.convertLocalToScene (event.getPoint ()));
            move (widget, event.getPoint ());
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (selectionSceneRectangle != null) {
            move (widget, event.getPoint ());
            selectionWidget.getParentWidget ().removeChild (selectionWidget);
            provider.performSelection (selectionSceneRectangle);
            selectionWidget = null;
            selectionSceneRectangle = null;
        }
        return State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        if (selectionSceneRectangle != null) {
            move (widget, event.getPoint ());
            return State.createLocked (widget, this);
        }
        return State.REJECTED;
    }

    private void resolveSelectionWidgetLocationBounds () {
        selectionWidget.setPreferredLocation (selectionSceneRectangle.getLocation ());
        int w = selectionSceneRectangle.width;
        int h = selectionSceneRectangle.height;
        selectionWidget.setPreferredBounds (new Rectangle (w >= 0 ? 0 : w, h >= 0 ? 0 : h, w >= 0 ? w : -w, h >= 0 ? h : -h));
    }

    private void move (Widget widget, Point newLocation) {
        Point sceneLocation = widget.convertLocalToScene (newLocation);
        selectionSceneRectangle.width = sceneLocation.x - selectionSceneRectangle.x;
        selectionSceneRectangle.height = sceneLocation.y - selectionSceneRectangle.y;
        resolveSelectionWidgetLocationBounds ();
    }

}
