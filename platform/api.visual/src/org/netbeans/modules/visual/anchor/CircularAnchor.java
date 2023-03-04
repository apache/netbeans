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
package org.netbeans.modules.visual.anchor;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class CircularAnchor extends Anchor {

    private int radius;

    public CircularAnchor (Widget widget, int radius) {
        super (widget);
//        assert widget != null;
        this.radius = radius;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        double angle = Math.atan2 (oppositeLocation.y - relatedLocation.y, oppositeLocation.x - relatedLocation.x);

        Point location = new Point (relatedLocation.x + (int) (radius * Math.cos (angle)), relatedLocation.y + (int) (radius * Math.sin (angle)));
        return new Anchor.Result (location, Anchor.DIRECTION_ANY); // TODO - resolve direction
    }

}
