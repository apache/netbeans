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

import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class DirectionalAnchor extends Anchor {

    private AnchorFactory.DirectionalAnchorKind kind;
    private int gap;

    public DirectionalAnchor (Widget widget, AnchorFactory.DirectionalAnchorKind kind, int gap) {
        super (widget);
//        assert widget != null;
        this.kind = kind;
        this.gap = gap;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        Widget widget = getRelatedWidget ();
        Rectangle bounds = widget.convertLocalToScene (widget.getBounds ());
        Point center = GeomUtil.center (bounds);

        switch (kind) {
            case HORIZONTAL:
                if (relatedLocation.x >= oppositeLocation.x)
                    return new Anchor.Result (new Point (bounds.x - gap, center.y), Direction.LEFT);
                else
                    return new Anchor.Result (new Point (bounds.x + bounds.width + gap, center.y), Direction.RIGHT);
            case VERTICAL:
                if (relatedLocation.y >= oppositeLocation.y)
                    return new Anchor.Result (new Point (center.x, bounds.y - gap), Direction.TOP);
                else
                    return new Anchor.Result (new Point (center.x, bounds.y + bounds.height + gap), Direction.BOTTOM);
        }
        return null;
    }

}
