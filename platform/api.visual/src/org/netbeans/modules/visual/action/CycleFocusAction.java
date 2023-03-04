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
import org.netbeans.api.visual.action.CycleFocusProvider;
import org.netbeans.api.visual.widget.Widget;

import java.awt.event.KeyEvent;

/**
 * @author David Kaspar
 */
public class CycleFocusAction extends WidgetAction.Adapter {

    private CycleFocusProvider provider;

    public CycleFocusAction (CycleFocusProvider provider) {
        this.provider = provider;
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        boolean state = false;
        if (event.getKeyChar () == KeyEvent.VK_TAB) {
            if ((event.getModifiers () & KeyEvent.SHIFT_MASK) == KeyEvent.SHIFT_MASK)
                state = provider.switchPreviousFocus (widget);
            else
                state = provider.switchNextFocus (widget);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

}
