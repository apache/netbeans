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

import org.netbeans.api.visual.action.ConnectDecorator;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 *
 * @author alex
 */
public class ExtendedConnectAction extends ConnectAction {

    private long modifiers;
    private boolean macLocking;

    public ExtendedConnectAction(ConnectDecorator decorator, Widget interractionLayer, ConnectProvider provider, long modifiers) {
        super(decorator, interractionLayer, provider);
        this.modifiers = modifiers;
    }

    protected boolean isLocked () {
        return super.isLocked ()  ||  macLocking;
    }

    public WidgetAction.State mousePressed(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (isLocked ())
            return State.createLocked (widget, this);
        if ((event.getModifiers () & modifiers) == modifiers) {
            if ((Utilities.getOperatingSystem () & Utilities.OS_MAC) != 0)
                macLocking = true;
            return super.mousePressedCore(widget,event);
        }
        return State.REJECTED;
    }

    public WidgetAction.State mouseReleased(Widget widget, WidgetAction.WidgetMouseEvent event) {
        macLocking = false;
        if (isLocked ())
            return super.mouseReleased(widget,event);
        else
            return State.REJECTED;
    }

    public State mouseMoved (Widget widget, WidgetMouseEvent event) {
        if (macLocking)
            return super.mouseDragged (widget, event);
        return super.mouseMoved (widget, event);
    }
}
