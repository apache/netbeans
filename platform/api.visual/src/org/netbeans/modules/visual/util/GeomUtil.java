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
package org.netbeans.modules.visual.util;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

/**
 * @author David Kaspar
 */
public final class GeomUtil {

    public static final Logger LOG = Logger.getLogger ("org.netbeans.api.visual"); // NOI18N 
    
    public static final double M_PI_2 = Math.PI / 2;

    private GeomUtil () {
    }

    /**
     * Rounds Rectangle2D to Rectangle.
     * @param rectangle the rectangle2D
     * @return the rectangle
     */
    public static Rectangle roundRectangle (Rectangle2D rectangle) {
        int x1 = (int) Math.floor (rectangle.getX ());
        int y1 = (int) Math.floor (rectangle.getY ());
        int x2 = (int) Math.ceil (rectangle.getMaxX ());
        int y2 = (int) Math.ceil (rectangle.getMaxY ());
        return new Rectangle (x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Returns a center point of a rectangle.
     * @param rectangle the rectangle
     * @return the center point
     */
    public static Point center (Rectangle rectangle) {
        return new Point (rectangle.x + rectangle.width / 2, rectangle.y + rectangle.height / 2);
    }

    /**
     * Returns a x-axis center of rectangle.
     * @param rectangle the rectangle
     * @return the x-axis center
     */
    public static int centerX (Rectangle rectangle) {
        return rectangle.x + rectangle.width / 2;
    }

    /**
     * Returns a y-axis center of rectangle.
     * @param rectangle the rectangle
     * @return the y-axis center
     */
    public static int centerY (Rectangle rectangle) {
        return rectangle.y + rectangle.height / 2;
    }

    /**
     * Returns a square distance of two points.
     * @param p1 the first point
     * @param p2 the second point
     * @return the square distance
     */
    public static double distanceSq (Point p1, Point p2) {
        int w = p2.x - p1.x;
        int h = p2.y - p1.y;
        return Math.sqrt (w * w + h * h);
    }

    /**
     * Returns whether two objects are equal
     * @param o1 the first object; cound be null
     * @param o2 the second object; cound be null
     * @return true, if they are equal
     */
    public static boolean equals (Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals (o2);
    }

}
