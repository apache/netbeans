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
