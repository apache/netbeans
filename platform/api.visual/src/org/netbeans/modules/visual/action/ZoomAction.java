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

import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.WidgetAction;

/**
 * @author David Kaspar
 */
public final class ZoomAction extends WidgetAction.Adapter {

    private double zoomMultiplier;
    private boolean useAnimator;

    public ZoomAction (double zoomMultiplier, boolean useAnimator) {
        this.zoomMultiplier = zoomMultiplier;
        this.useAnimator = useAnimator;
    }

    public State mouseWheelMoved (Widget widget, WidgetMouseWheelEvent event) {
        Scene scene = widget.getScene ();

        int modifiers = scene.getInputBindings ().getZoomActionModifiers ();
        if ((event.getModifiers () & modifiers) != modifiers)
            return State.REJECTED;

        int amount = event.getWheelRotation ();

        if (useAnimator) {
            SceneAnimator sceneAnimator = scene.getSceneAnimator ();
            synchronized (sceneAnimator) {
                double zoom = sceneAnimator.isAnimatingZoomFactor () ? sceneAnimator.getTargetZoomFactor () : scene.getZoomFactor ();
                while (amount > 0) {
                    zoom /= zoomMultiplier;
                    amount --;
                }
                while (amount < 0) {
                    zoom *= zoomMultiplier;
                    amount ++;
                }
                sceneAnimator.animateZoomFactor (zoom);
            }
        } else {
            double zoom = scene.getZoomFactor ();
            while (amount > 0) {
                zoom /= zoomMultiplier;
                amount --;
            }
            while (amount < 0) {
                zoom *= zoomMultiplier;
                amount ++;
            }
            scene.setZoomFactor (zoom);
        }

        return WidgetAction.State.CONSUMED;
    }

}
