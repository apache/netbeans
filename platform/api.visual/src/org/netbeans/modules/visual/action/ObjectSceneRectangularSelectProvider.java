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

import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.model.ObjectScene;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.*;

/**
 * @author David Kaspar
 */
public final class ObjectSceneRectangularSelectProvider implements RectangularSelectProvider {

    private ObjectScene scene;

    public ObjectSceneRectangularSelectProvider (ObjectScene scene) {
        this.scene = scene;
    }

    public void performSelection (Rectangle sceneSelection) {
        boolean entirely = sceneSelection.width > 0;
        int w = sceneSelection.width;
        int h = sceneSelection.height;
        Rectangle rect = new Rectangle (w >= 0 ? 0 : w, h >= 0 ? 0 : h, w >= 0 ? w : -w, h >= 0 ? h : -h);
        rect.translate (sceneSelection.x, sceneSelection.y);

        HashSet<Object> set = new HashSet<Object> ();
        Set<?> objects = scene.getObjects ();
        for (Object object : objects) {
            Widget widget = scene.findWidget (object);
            if (widget == null)
                continue;
            if (entirely) {
                Rectangle widgetRect = widget.convertLocalToScene (widget.getBounds ());
                if (rect.contains (widgetRect))
                    set.add (object);
            } else {
                if (widget instanceof ConnectionWidget) {
                    ConnectionWidget conn = (ConnectionWidget) widget;
                    java.util.List<Point> points = conn.getControlPoints ();
                    for (int i = points.size () - 2; i >= 0; i --) {
                        Point p1 = widget.convertLocalToScene (points.get (i));
                        Point p2 = widget.convertLocalToScene (points.get (i + 1));
                        if (new Line2D.Float (p1, p2).intersects (rect))
                            set.add (object);
                    }
                } else {
                    Rectangle widgetRect = widget.convertLocalToScene (widget.getBounds ());
                    if (rect.intersects (widgetRect))
                        set.add (object);
                }
            }
        }
        Iterator<Object> iterator = set.iterator ();
        scene.setFocusedObject (iterator.hasNext () ? iterator.next () : null);
        scene.userSelectionSuggested (set, false);
    }

}
