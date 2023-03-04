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

/**
 * @author David Kaspar
 */
public final class ForwardKeyEventsAction extends WidgetAction.Adapter {

    private Widget forwardToWidget;
    private String forwardedToTool;

    public ForwardKeyEventsAction (Widget forwardToWidget, String forwardedToTool) {
        this.forwardToWidget = forwardToWidget;
        this.forwardedToTool = forwardedToTool;
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        WidgetAction.Chain actions = forwardedToTool != null ? widget.getActions (forwardedToTool) : widget.getActions ();
        return actions != null ? actions.keyTyped (forwardToWidget, event) : State.REJECTED;
    }

    public State keyPressed (Widget widget, WidgetKeyEvent event) {
        WidgetAction.Chain actions = forwardedToTool != null ? widget.getActions (forwardedToTool) : widget.getActions ();
        return actions != null ? actions.keyPressed (forwardToWidget, event) : State.REJECTED;
    }

    public State keyReleased (Widget widget, WidgetKeyEvent event) {
        WidgetAction.Chain actions = forwardedToTool != null ? widget.getActions (forwardedToTool) : widget.getActions ();
        return actions != null ? actions.keyReleased (forwardToWidget, event) : State.REJECTED;
    }

}
