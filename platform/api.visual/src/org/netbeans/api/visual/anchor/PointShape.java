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
package org.netbeans.api.visual.anchor;

import org.netbeans.modules.visual.anchor.SquarePointShape;

import java.awt.*;

/**
 * Represents a point shape. Usually used for control points and end points of a connection widget.
 * @author David Kaspar
 */
public interface PointShape {

    /**
     * Returns a radius of the shape.
     * @return the radius
     */
    public int getRadius ();

    /**
     * Renders a shape into the graphics instance
     * @param graphics
     */
    public void paint (Graphics2D graphics);

    /**
     * The empty point shape.
     */
    public static final PointShape NONE = new PointShape () {
        public int getRadius () { return 0; }
        public void paint (Graphics2D graphics) {}
    };

    /**
     * The 8px big filled-square shape.
     */
    public static final PointShape SQUARE_FILLED_BIG = new SquarePointShape (4, true);

    /**
     * The 6px big filled-square shape.
     */
    public static final PointShape SQUARE_FILLED_SMALL = new SquarePointShape (3, true);

}
