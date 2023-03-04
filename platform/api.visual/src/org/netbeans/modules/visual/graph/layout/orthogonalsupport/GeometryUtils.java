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
package org.netbeans.modules.visual.graph.layout.orthogonalsupport;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 *
 * @author ptliu
 */
public class GeometryUtils {

    /**
     * 
     * @param line1
     * @param line2
     * @param extrapolate
     * @return
     */
    public static Point2D getIntersectionPoint(Line2D line1, Line2D line2,
            boolean extrapolate) {
        if (extrapolate || line1.intersectsLine(line2)) {
            float x1 = (float) line2.getX1();
            float y1 = (float) line2.getY1();
            float x2 = (float) line2.getX2();
            float y2 = (float) line2.getY2();

            float xp1 = (float) line1.getX1();
            float yp1 = (float) line1.getY1();
            float xp2 = (float) line1.getX2();
            float yp2 = (float) line1.getY2();

            float y = 0;
            float x = 0;
            float dy = y2 - y1;
            float s = (x2 - x1) / dy;

            float dpy = yp2 - yp1;
            float sp = (xp2 - xp1) / dpy;

            if (y1 == y2) {
                if (dpy == 0) {
                    return null;
                }
                y = y1;
                x = xp1 + sp * (y - yp1);
            } else if (yp1 == yp2) {
                if (dy == 0) {
                    return null;
                }
                y = yp1;
                x = x1 + s * (y - y1);
            } else {
                if (dy == 0 || dpy == 0 || (s - sp) == 0) {
                    return null;
                }
                y = (xp1 - x1 + s * y1 - sp * yp1) / (s - sp);
                x = x1 + s * (y - y1);
            }

            return new Point2D.Float(x, y);
        }

        return null;
    }

    /**
     * 
     * @param line1
     * @param line2
     * @return
     */
    public static boolean isParallel(Line2D line1, Line2D line2) {
        float x1 = (float) line1.getX1();
        float y1 = (float) line1.getY1();
        float x2 = (float) line1.getX2();
        float y2 = (float) line1.getY2();
        float dx = x2 - x1;
        float dy = y1 - y2;
        float d = (float) Math.sqrt((double) (dx * dx + dy * dy));

        float slope1 = Math.abs(dx / d);


        x1 = (float) line2.getX1();
        y1 = (float) line2.getY1();
        x2 = (float) line2.getX2();
        y2 = (float) line2.getY2();
        dx = x2 - x1;
        dy = y1 - y2;
        d = (float) Math.sqrt((double) (dx * dx + dy * dy));

        float slope2 = Math.abs(dx / d);

        return (slope1 == slope2);
    }
}
