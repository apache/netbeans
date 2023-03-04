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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.EditProvider;

import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

/**
 * @author David Kaspar
 */
public final class EditAction extends WidgetAction.Adapter {

    private EditProvider provider;

    public EditAction (EditProvider provider) {
        this.provider = provider;
    }

    public State mouseClicked (Widget widget, WidgetMouseEvent event) {
        if (event.getButton () == MouseEvent.BUTTON1  &&  event.getClickCount () == 2) {
            provider.edit (widget);
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        if (event.getKeyChar () == KeyEvent.VK_ENTER) {
            provider.edit (widget);
            return State.CONSUMED;
        }
        return State.REJECTED;
    }

}
