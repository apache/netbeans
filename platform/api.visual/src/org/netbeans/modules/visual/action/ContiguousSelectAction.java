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

import org.netbeans.api.visual.action.ContiguousSelectEvent;
import org.netbeans.api.visual.action.ContiguousSelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author David Kaspar
 */
public final class ContiguousSelectAction extends WidgetAction.Adapter {

    private ContiguousSelectProvider provider;
    private Widget previousWidget;
    private Point previousLocalLocation;

    public ContiguousSelectAction (ContiguousSelectProvider provider) {
        this.provider = provider;
    }

    @Override
    public State mousePressed (Widget widget, WidgetMouseEvent event) {
        Point localLocation = event.getPoint();
        if ((event.getButton() & (MouseEvent.BUTTON1  | MouseEvent.BUTTON2  | MouseEvent.BUTTON3)) != 0) {
            if (process (widget, localLocation, event.getModifiersEx ()))
                return State.CHAIN_ONLY;
        }
        return State.REJECTED;
    }

    private boolean process (Widget widget, Point localLocation, int modifiers) {
        boolean ctrl = (modifiers & MouseEvent.CTRL_DOWN_MASK) != 0;
        boolean shift = (modifiers & MouseEvent.SHIFT_DOWN_MASK) != 0;
        ContiguousSelectEvent.SelectionType type = ctrl
                ? (shift ? ContiguousSelectEvent.SelectionType.ADDITIVE_CONTIGUOUS : ContiguousSelectEvent.SelectionType.ADDITIVE_NON_CONTIGUOUS)
                : (shift ? ContiguousSelectEvent.SelectionType.REPLACE_CONTIGUOUS : ContiguousSelectEvent.SelectionType.REPLACE_NON_CONTIGUOUS);
        ContiguousSelectEvent providerEvent = ContiguousSelectEvent.create (previousWidget, previousLocalLocation, widget, localLocation, type);
        if (provider.isSelectionAllowed (providerEvent)) {
            provider.select(providerEvent);
            if (! shift) {
                previousWidget = widget;
                previousLocalLocation = localLocation;
            }
            return true;
        }
        return false;
    }

    @Override
    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        if (event.getKeyChar () == KeyEvent.VK_SPACE)
            if (process (widget, null, event.getModifiers ()))
                return State.CONSUMED;
        return State.REJECTED;
    }

}
