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

package org.netbeans.api.visual.export;

import java.awt.Polygon;
import org.netbeans.api.visual.widget.Widget;

/**
 * The purpose of this class is to provide a framework for holding a Widget  
 * and the Polygon that encompasses it. Can be used for creating HTML image
 * maps. For the ConnectionWidgets, a Polygon is created that follows the contour
 * of the connection with a pixel buffer equal to the defined margin.
 * @author krichard
 */
public final class WidgetPolygonalCoordinates {

    private final Widget widget ;
    private final Polygon polygon ;

    /**
     * Creates an instance of a WidgetPolygonalCoordinates object.
     * @param widget The Widget (may be a ConnectionWidget) being encompassed
     * by the polygon.
     * @param polygon The Polygon surrounding the respective Widget.
     */
    public WidgetPolygonalCoordinates (Widget widget, Polygon polygon) {
        this.widget = widget ;
        this.polygon = polygon ;
    }
    
    /**
     * Returns the Widget stored in this class. 
     * @return the Widget stored in this class.
     */
    public Widget getWidget() {
        return widget;
    }

    /**
     * Returns the Polygon stored in this class. 
     * @return the Polygon stored in this class.
     */
    public Polygon getPolygon() {
        return polygon;
    }
    
}
