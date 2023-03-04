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
package org.netbeans.modules.visual.animator;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.animator.Animator;
import org.netbeans.api.visual.animator.SceneAnimator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Kaspar
 */
public final class PreferredLocationAnimator extends Animator {

    private HashMap<Widget, Point> sourceLocations = new HashMap<Widget, Point> ();
    private HashMap<Widget, Point> targetLocations = new HashMap<Widget, Point> ();

    public PreferredLocationAnimator (SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    public void setPreferredLocation (Widget widget, Point preferredLocation) {
        assert widget != null;
        assert preferredLocation != null;
        if (!sourceLocations.isEmpty()) {
            sourceLocations.clear ();
        }
        targetLocations.put (widget, preferredLocation);
        start ();
    }

    protected void tick (double progress) {
        for (Map.Entry<Widget, Point> entry : targetLocations.entrySet ()) {
            Widget widget = entry.getKey ();
            Point sourceLocation = sourceLocations.get (widget);
            if (sourceLocation == null) {
                sourceLocation = widget.getPreferredLocation ();
                if (sourceLocation == null) {
                    sourceLocation = widget.getLocation ();
                    if (sourceLocation == null) {
                        sourceLocation = new Point ();
                    }
                }
                sourceLocations.put (widget, sourceLocation);
            }
            Point targetLocation = entry.getValue ();
            if (targetLocation == null)
                continue;
            Point point;
            if (progress >= 1.0)
                point = targetLocation;
            else
                point = new Point (
                        (int) (sourceLocation.x + progress * (targetLocation.x - sourceLocation.x)),
                        (int) (sourceLocation.y + progress * (targetLocation.y - sourceLocation.y)));
            widget.setPreferredLocation (point);
        }
        if (progress >= 1.0) {
            sourceLocations.clear ();
            targetLocations.clear ();
        }
    }

}
