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
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.util.GeomUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author David Kaspar
 */
public final class CenteredZoomAction extends WidgetAction.Adapter {

    private double zoomMultiplier;

    public CenteredZoomAction (double zoomMultiplier) {
        this.zoomMultiplier = zoomMultiplier;
    }

    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        Scene scene = widget.getScene ();

        int modifiers = scene.getInputBindings ().getZoomActionModifiers ();
        if ((event.getModifiers () & modifiers) != modifiers)
            return State.REJECTED;

        int amount = event.getWheelRotation ();

        double scale = 1.0;
        while (amount > 0) {
            scale /= zoomMultiplier;
            amount --;
        }
        while (amount < 0) {
            scale *= zoomMultiplier;
            amount ++;
        }

        JComponent view = scene.getView ();
        if (view != null) {
            Rectangle viewBounds = view.getVisibleRect ();

            Point center = GeomUtil.center (viewBounds);
            center = scene.convertViewToScene (center);

            scene.setZoomFactor (scale * scene.getZoomFactor ());
            scene.validate (); // HINT - forcing to change preferred size of the JComponent view
            
            center = scene.convertSceneToView (center);

            view.scrollRectToVisible (new Rectangle (
                center.x - viewBounds.width / 2,
                center.y - viewBounds.height / 2,
                viewBounds.width,
                viewBounds.height
            ));
        } else
            scene.setZoomFactor (scale * scene.getZoomFactor ());

        return State.CONSUMED;
    }

}
