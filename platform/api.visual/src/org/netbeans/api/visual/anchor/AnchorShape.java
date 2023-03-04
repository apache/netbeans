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

import org.netbeans.modules.visual.anchor.TriangleAnchorShape;

import java.awt.*;

/**
 * Represents an anchor shape which is rendered at the source and the target point of a connection widget where the shape is used.
 * The same instance of a shape could be shared by multiple connection widgets.
 * @author David Kaspar
 */
public interface AnchorShape {

    /**
     * Returns whether the shape is oriented by the line path of a connection.
     * @return true if it is line-oriented
     */
    public boolean isLineOriented ();

    /**
     * Returns a radius of a shape that the shape used for rendering.
     * @return the radius
     */
    public int getRadius ();

    /**
     * Returns a distance by which a line at particular source or target point should be cut (not rendered).
     * This is used for hollow-triangle shapes, to not paint the connection-line within the triangle.
     * @return the cut distance in pixels;
     *     if positive, then the line is cut by specified number of pixels, the line could be cut by radius pixels only;
     *     if 0.0, then the line is not cut;
     *     if negative, then the line is extended by specified number of pixels, the line could be extended by radius pixels only
     */
    public double getCutDistance ();

    /**
     * Renders the shape into a graphics instance
     * @param graphics the graphics
     * @param source true, if the shape is used for a source point; false if the shape is used for a target point.
     */
    public void paint (Graphics2D graphics, boolean source);

    /**
     * The empty anchor shape.
     */
    public static final AnchorShape NONE = new AnchorShape() {
        public boolean isLineOriented () { return false; }
        public int getRadius () { return 0; }
        public double getCutDistance () { return 0; }
        public void paint (Graphics2D graphics, boolean source) { }
    };

    /**
     * The hollow-triangle anchor shape.
     */
    public static final AnchorShape TRIANGLE_HOLLOW = new TriangleAnchorShape (12, false, false, true, 12.0);

    /**
     * The filled-triangle anchor shape.
     */
    public static final AnchorShape TRIANGLE_FILLED = new TriangleAnchorShape (12, true, false, false, 11.0);

    /**
     * The output-triangle anchor shape.
     */
    public static final AnchorShape TRIANGLE_OUT = new TriangleAnchorShape (12, true, true, false, 11.0);

}
