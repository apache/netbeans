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
import org.netbeans.api.visual.action.PopupMenuProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

/**
 * @author William Headrick, David Kaspar
 */
public final class PopupMenuAction extends WidgetAction.Adapter {

    private PopupMenuProvider provider;

    public PopupMenuAction (PopupMenuProvider provider) {
        this.provider = provider;
    }

    /**
     * Conditionally display a {@link JPopupMenu} for the given Widget if
     * the WidgetMouseEvent is a popup trigger.  Delegates check code
     * to {@link #handleMouseEvent(Widget, WidgetMouseEvent)}.
     * @param widget
     * @param event
     * @return {@link State#REJECTED} if no JPopupMenu is displayed,
     *         or {@link State#CONSUMED} if a JPopupMenu is displayed.
     * @see #handleMouseEvent(Widget, WidgetMouseEvent)
     */
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        return handleMouseEvent (widget, event);
    }

    /**
     * Conditionally display a {@link JPopupMenu} for the given Widget if
     * the WidgetMouseEvent is a popup trigger.  Delegates check code
     * to {@link #handleMouseEvent(Widget, WidgetMouseEvent)}.
     * @param widget
     * @param event
     * @return {@link State#REJECTED} if no JPopupMenu is displayed,
     *         or {@link State#CONSUMED} if a JPopupMenu is displayed.
     * @see #handleMouseEvent(Widget, WidgetMouseEvent)
     */
    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        return handleMouseEvent (widget, event);
    }

    /**
     * Conditionally display a {@link JPopupMenu} for the given Widget if
     * the WidgetMouseEvent is a popup trigger.  This method is called
     * by both {@link #mousePressed(Widget, WidgetMouseEvent)} and
     * {@link #mouseReleased(Widget, WidgetMouseEvent)} methods to handle
     * displaying a popup menu for the given widget and event.  Uses
     * {@link WidgetMouseEvent#isPopupTrigger() event.isPopupTrigger()} to
     * determine whether or not a popup menu should be displayed.
     * @param widget
     * @param event
     * @return {@link State#REJECTED} if no JPopupMenu is displayed,
     *         or {@link State#CONSUMED} if a JPopupMenu is displayed.
     * @see #mousePressed(Widget, WidgetMouseEvent)
     * @see #mouseReleased(Widget, WidgetMouseEvent)
     */
    protected State handleMouseEvent (Widget widget, WidgetMouseEvent event) {
        // Different OSes use different MouseEvents (Pressed/Released) to
        // signal that an event is a PopupTrigger.  So, the mousePressed(...)
        // and mouseReleased(...) methods delegate to this method to
        // handle the MouseEvent.
        if (event.isPopupTrigger ()) {
            JPopupMenu popupMenu = provider.getPopupMenu (widget, event.getPoint ());
            if (popupMenu != null) {
                Scene scene = widget.getScene ();
                Point point = scene.convertSceneToView (widget.convertLocalToScene (event.getPoint ()));
                popupMenu.show (scene.getView (), point.x, point.y);
            }
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        if (event.getKeyCode () == KeyEvent.VK_CONTEXT_MENU  ||  ((event.getModifiers () & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK  &&  event.getKeyCode () == KeyEvent.VK_F10)) {
            JPopupMenu popupMenu = provider.getPopupMenu (widget, null);
            if (popupMenu != null) {
                JComponent view = widget.getScene ().getView ();
                if (view != null) {
//                    Rectangle visibleRect = view.getVisibleRect ();
//                    popupMenu.show (view, visibleRect.x + 10, visibleRect.y + 10);
                    Rectangle bounds = widget.getBounds ();
                    Point location = new Point (bounds.x + 5, bounds.y + 5);
                    location = widget.convertLocalToScene (location);
                    location = widget.getScene ().convertSceneToView (location);
                    popupMenu.show (view, location.x, location.y);
                }
            }
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

}
