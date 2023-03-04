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

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
public final class PanAction extends WidgetAction.LockedAdapter {

    private Scene scene;
    private JScrollPane scrollPane;
    private Point lastLocation;

    protected boolean isLocked () {
        return scrollPane != null;
    }

    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        if (isLocked()) {
            return State.createLocked(widget, this);
        }
        scene = widget.getScene();
        if (event.getButton() == scene.getInputBindings().getPanActionButton()) {
            scrollPane = findScrollPane (scene.getView ());
            if (scrollPane != null) {
                lastLocation = scene.convertSceneToView (widget.convertLocalToScene (event.getPoint ()));
                SwingUtilities.convertPointToScreen (lastLocation, scene.getView ());
                return State.createLocked (widget, this);
            }
        }
        return State.REJECTED;
    }

    private JScrollPane findScrollPane (JComponent component) {
        for (;;) {
            if (component == null)
                return null;
            if (component instanceof JScrollPane)
                return ((JScrollPane) component);
            Container parent = component.getParent ();
            if (! (parent instanceof JComponent))
                return null;
            component = (JComponent) parent;
        }
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        boolean state = pan (widget, event.getPoint ());
        if (state)
            scrollPane = null;
        return state ? State.createLocked (widget, this) : State.REJECTED;
    }

    public State mouseDragged (Widget widget, WidgetMouseEvent event) {
        return pan (widget, event.getPoint ()) ? State.createLocked (widget, this) : State.REJECTED;
    }

    private boolean pan (Widget widget, Point newLocation) {
        if (scrollPane == null  ||  scene != widget.getScene ())
            return false;
        newLocation = scene.convertSceneToView (widget.convertLocalToScene (newLocation));
        SwingUtilities.convertPointToScreen (newLocation, scene.getView ());
        JComponent view = scene.getView ();
        Rectangle rectangle = view.getVisibleRect ();
        rectangle.x += lastLocation.x - newLocation.x;
        rectangle.y += lastLocation.y - newLocation.y;
        view.scrollRectToVisible (rectangle);
        lastLocation = newLocation;
        return true;
    }

}
