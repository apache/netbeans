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

import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class SwitchCardProvider implements SelectProvider {

    private Widget cardLayoutWidget;

    public SwitchCardProvider (Widget cardLayoutWidget) {
        this.cardLayoutWidget = cardLayoutWidget;
    }

    public boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection) {
        return false;
    }

    public boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection) {
        return true;
    }

    public void select (Widget widget, Point localLocation, boolean invertSelection) {
        Widget currentActiveCard = LayoutFactory.getActiveCard (cardLayoutWidget);

        List<Widget> children = cardLayoutWidget.getChildren ();
        int i = children.indexOf (currentActiveCard);
        i ++;
        if (i >= children.size ())
            i = 0;
        Widget newActiveCard = children.get (i);

        if (currentActiveCard == newActiveCard)
            return;

        LayoutFactory.setActiveCard (cardLayoutWidget, newActiveCard);
//        notifyCardSwitched (currentActiveCard, newActiveCard);
    }

}
