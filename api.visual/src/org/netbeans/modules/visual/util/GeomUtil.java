/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
