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

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.*;

/**
 * @author David Kaspar
 */
public class WheelPanAction extends WidgetAction.Adapter {

    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        JComponent view = widget.getScene ().getView ();
        Rectangle visibleRect = view.getVisibleRect ();
        int amount = event.getWheelRotation () * 64;

        switch (event.getModifiers () & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.ALT_MASK)) {
            case InputEvent.SHIFT_MASK:
                visibleRect.x += amount;
                break;
            case 0:
                visibleRect.y += amount;
                break;
            default:
                return State.REJECTED;
        }

        view.scrollRectToVisible (visibleRect);
        return State.CONSUMED;
    }

}
