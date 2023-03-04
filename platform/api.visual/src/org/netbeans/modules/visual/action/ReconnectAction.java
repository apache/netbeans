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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Override at least isReplacementWidget and setConnectionAnchor methods. reconnectingFinished is always called before setConnectionAnchor.
 *
 * @author David Kaspar
 */
public final class ReconnectAction extends WidgetAction.LockedAdapter {

    private static final int MIN_DIFFERENCE = 5;

    private ReconnectDecorator decorator;
    private ReconnectProvider provider;

    private ConnectionWidget connectionWidget = null;
    private boolean reconnectingSource = false;
    private Point floatPoint = null;
    private Widget replacementWidget = null;
    private Anchor originalAnchor = null;

    public ReconnectAction (ReconnectDecorator decorator, ReconnectProvider provider) {
        this.decorator = decorator;
        this.provider = provider;
    }

    protected boolean isLocked () {
        return connectionWidget != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 1) {
            if (widget instanceof ConnectionWidget) {
                ConnectionWidget conn = (ConnectionWidget) widget;
                int index = conn.getControlPointHitAt (event.getPoint ());
                List<Point> controlPoints = conn.getControlPoints ();
                if (index == 0  &&  provider.isSourceReconnectable (conn)) {
                    reconnectingSource = true;
                } else if (controlPoints != null  &&  index == controlPoints.size () - 1  && provider.isTargetReconnectable (conn)) {
                    reconnectingSource = false;
                } else {
                    return State.REJECTED;
                }

                floatPoint = new Point (event.getPoint ());
                replacementWidget = null;
                connectionWidget = conn;
                provider.reconnectingStarted (conn, reconnectingSource);
                if (reconnectingSource)
                    originalAnchor = connectionWidget.getSourceAnchor ();
                else
                    originalAnchor = connectionWidget.getTargetAnchor ();
                return State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        Point point = event.getPoint ();
        boolean state = move (widget, point);
        if (state) {
            cancel (point);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    private void cancel (Point point) {
        if (reconnectingSource)
            connectionWidget.setSourceAnchor (originalAnchor);
        else
            connectionWidget.setTargetAnchor (originalAnchor);
        provider.reconnectingFinished (connectionWidget, reconnectingSource);
        if (point != null)
            if (Math.abs (floatPoint.x - point.x) >= ReconnectAction.MIN_DIFFERENCE  ||  Math.abs (floatPoint.y - point.y) >= ReconnectAction.MIN_DIFFERENCE)
                provider.reconnect (connectionWidget, replacementWidget, reconnectingSource);
        replacementWidget = null;
        floatPoint = null;
        connectionWidget = null;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return move (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean move (Widget widget, Point point) {
        if (connectionWidget != widget)
            return false;

        Point replacementSceneLocation = widget.convertLocalToScene (point);
        replacementWidget = resolveReplacementWidgetCore (connectionWidget.getScene (), replacementSceneLocation);
        Anchor replacementAnchor = null;
        if (replacementWidget != null)
            replacementAnchor = decorator.createReplacementWidgetAnchor (replacementWidget);
        if (replacementAnchor == null)
            replacementAnchor = decorator.createFloatAnchor (replacementSceneLocation);

        if (reconnectingSource)
            connectionWidget.setSourceAnchor (replacementAnchor);
        else
            connectionWidget.setTargetAnchor (replacementAnchor);

        return true;
    }

    protected Widget resolveReplacementWidgetCore (Scene scene, Point sceneLocation) {
        if (provider != null)
            if (provider.hasCustomReplacementWidgetResolver (scene))
                return provider.resolveReplacementWidget (scene, sceneLocation);
        Point sceneOrigin = scene.getLocation ();
        sceneLocation = new Point (sceneLocation.x + sceneOrigin.x, sceneLocation.y + sceneOrigin.y);
        Widget[] result = new Widget[]{null};
        resolveReplacementWidgetCoreDive (result, scene, sceneLocation);
        return result[0];
    }

    private boolean resolveReplacementWidgetCoreDive (Widget[] result, Widget widget, Point parentLocation) {
        if (widget == connectionWidget)
            return false;

        Point widgetLocation = widget.getLocation ();
        Point location = new Point (parentLocation.x - widgetLocation.x, parentLocation.y - widgetLocation.y);

        if (! widget.getBounds ().contains (location))
            return false;

        java.util.List<Widget> children = widget.getChildren ();
        for (int i = children.size () - 1; i >= 0; i --) {
            if (resolveReplacementWidgetCoreDive (result, children.get (i), location))
                return true;
        }

        if (! widget.isHitAt (location))
            return false;

        ConnectorState state = provider.isReplacementWidget (connectionWidget, widget, reconnectingSource);
        if (state == ConnectorState.REJECT)
            return false;
        if (state == ConnectorState.ACCEPT)
            result[0] = widget;
        return true;
    }


    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        if (isLocked ()  &&  event.getKeyCode () == KeyEvent.VK_ESCAPE) {
            cancel (null);
            return State.CONSUMED;
        }
        return super.keyPressed (widget, event);
    }

}
