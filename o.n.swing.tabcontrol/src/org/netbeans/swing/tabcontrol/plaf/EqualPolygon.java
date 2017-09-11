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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
/*
 * EqualPolygon.java
 *
 * Created on February 10, 2004, 1:17 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;


/**
 * A Polygon which implements a proper equals/hashcode contract.  In order to
 * optimize drag and drop repainting, it is necessary that the Shape objects
 * returned by getTabIndication() be able to be compared properly.
 * <p/>
 * To ease migration of older code, this class also implements a couple methods
 * of GeneralPath, which was used before. These methods just delegate to
 * addPoint(), so the full functionality of GeneralPath is not replicated
 * (specifically, a polygon must be contiguous and closed).
 * <p/>
 *
 * @author Tim Boudreau
 */
public final class EqualPolygon extends Polygon {

    /**
     * Creates a new instance of EqualGeneralPath
     */
    public EqualPolygon() {
    }

    /**
     * Copy constructor will copy the xpoints/ypoints arrays so the caller can
     * later modify them without changing the polygon constructor here.
     */
    public EqualPolygon(int[] x, int[] y, int n) {
        //Must clone the arrays, or transforms on the source of the polygon
        //will also transform this one
        xpoints = new int[n];
        ypoints = new int[n];
        System.arraycopy(x, 0, xpoints, 0, xpoints.length);
        System.arraycopy(y, 0, ypoints, 0, ypoints.length);
        npoints = n;
    }

    /**
     * Copy constructor - takes either another EqualPolygon or a Polygon. Copies
     * the points arrays of the original polygon, so the passed polygon may be
     * modified without affecting the instance constructed here.
     *
     * @param p
     */
    public EqualPolygon(Polygon p) {
        super(p.xpoints, p.ypoints, p.npoints);
    }

    /** Convenience constructor which takes a Rectangle */
    public EqualPolygon(Rectangle r) {
        super (
            new int[] {r.x, r.x + r.width, r.x + r.width,  r.x},
            new int[] {r.y, r.y,           r.y + r.height, r.y + r.height},
            4
        );
    }

    /**
     * Non copy constructor based on fixed arrays.  Takes the point count
     * parameter from<code>x.length</code>.
     */
    public EqualPolygon(int[] x, int[] y) {
        super(x, y, x.length);
    }

    /**
     * Delegates to <code>Polygon.addPoint()</code>.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void moveTo(int x, int y) {
        addPoint(x, y);
    }

    /**
     * Delegates to <code>Polygon.addPoint()</code>.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void lineTo(int x, int y) {
        addPoint(x, y);
    }

    /**
     * Creates a new EqualPolygon using the copy constructor - the resulting
     * polygon may be modified without affecting the original.
     *
     * @return A new instance of EqualPolygon with the same point values
     */
    public Object clone() {
        return new EqualPolygon(xpoints, ypoints, xpoints.length);
    }

    /**
     * Overridden to produce a meaningful result.
     *
     * @return A string representation of the EqualPolygon
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("EqualPolygon: "); //NOI18N
        for (int i = 0; i < npoints; i++) {
            sb.append(' '); //NOI18N
            sb.append(xpoints[i]);
            sb.append(','); //NOI18N
            sb.append(ypoints[i]);
        }
        return sb.toString();
    }

    /**
     * Computes a hashCode based on the points arrays.
     *
     * @return The hash code
     */
    public int hashCode() {
        return arrayHashCode(xpoints) ^ arrayHashCode(ypoints);
    }

    private int arrayHashCode(int[] o) {
        int result = 0;
        for (int i = 0; i < npoints; i++) {
            result += o[i] ^ i;
        }
        return result;
    }

    /**
     * Returns true if the argument is a Polygon (does not need to be
     * EqualPolygon) and its point arrays and number of points matches.
     *
     * @param o Another polygon
     * @return whether or not they are equal
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Polygon) {
            Polygon p = (Polygon) o;
            int[] ox = p.xpoints;
            int[] oy = p.ypoints;
            boolean result = Arrays.equals(xpoints, ox)
                    && Arrays.equals(ypoints, oy);
            result &= p.npoints == npoints;
            return result;
        } else {
            return false;
        }
    }

    private Point[] sortPoints(Point[] p) {
        //Prune duplicates
        HashSet<Point> set = new HashSet<Point>(Arrays.asList(p));
        p = new Point[set.size()];
        p = set.toArray(p);
        //Then sort
        Arrays.sort(p, comparator);
        return p;
    }

    private static final Comparator<Point> comparator = new PointsComparator();

    private static class PointsComparator implements Comparator<Point> {
        public int compare(Point a, Point b) {
            int result = (a.y * (a.x - b.x)) - (b.y * (b.x - a.x));
            return result;
        }
    }

}
