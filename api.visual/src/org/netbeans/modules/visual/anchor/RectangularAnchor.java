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
package org.netbeans.modules.visual.anchor;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.anchor.Anchor;

import java.awt.*;

/**
 * @author David Kaspar
 */
// TODO - scene component location is not 100% attach to the bounding rectangle when the line goes far to the bottom-left-bottom direction
public final class RectangularAnchor extends Anchor {

    private boolean includeBorders;

    public RectangularAnchor (Widget widget, boolean includeBorders) {
        super (widget);
//        assert widget != null;
        this.includeBorders = includeBorders;
    }
    
    public boolean allowsArbitraryConnectionPlacement() {
        return false;
    }
        //this is used by the Orthogonal Router. This router routes to the center of
    //of the anchors and then allows the anchor to adjust the end points.
    @Override
    public List<Point> compute(List<Point> points) {
        ArrayList<Point> bestPoints = new ArrayList<Point> (points) ;
        Point relatedLocation = getRelatedSceneLocation();

        int direction = 1 ;
        int index = 0 ;
        
        //the related location is the center of this anchor. It is possible that
        //the list of points started at the opposite anchor (other end of connection).
        Point endPoint = bestPoints.get(index);
        if (!endPoint.equals(relatedLocation)) {
            index = bestPoints.size() - 1 ;
            endPoint = bestPoints.get(index);
            direction = -1 ;
        }
        
        Widget widget = getRelatedWidget();
        Rectangle bounds = widget.getBounds();
        bounds = widget.convertLocalToScene(bounds);
        
        Point neighbor = bestPoints.get (index+direction) ;
        
        //moving the end point to the end of the anchor from the interior
        while (bounds.contains(neighbor)) {
            bestPoints.remove(index) ;
            endPoint = bestPoints.get (index);
            neighbor = bestPoints.get (index+direction);
        }
        
        Result intersection = this.computeBoundaryIntersectionPoint(endPoint, neighbor);
                
        bestPoints.remove(index) ;
        bestPoints.add(index, intersection.getAnchorSceneLocation());
        
        return bestPoints ;
    }

    public Result compute(Entry entry) {
        Point relatedLocation = getRelatedSceneLocation();
        Point oppositeLocation = null;

        if (oppositeLocation == null) {
            oppositeLocation = getOppositeSceneLocation(entry);
        }
        
        Result boundaryIntersection =
                computeBoundaryIntersectionPoint(relatedLocation, oppositeLocation);

        if (boundaryIntersection == null) {
            return new Anchor.Result(relatedLocation, Anchor.DIRECTION_ANY);
        }
        
        return boundaryIntersection ;
    }

    private Result computeBoundaryIntersectionPoint(Point relatedLocation, Point oppositeLocation) {
        
        Widget widget = getRelatedWidget();
        Rectangle bounds = widget.getBounds();
        if (!includeBorders) {
            Insets insets = widget.getBorder().getInsets();
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        bounds = widget.convertLocalToScene(bounds);

        if (bounds.isEmpty() || relatedLocation.equals(oppositeLocation)) {
            return null;
        }
        float dx = oppositeLocation.x - relatedLocation.x;
        float dy = oppositeLocation.y - relatedLocation.y;

        float ddx = Math.abs(dx) / (float) bounds.width;
        float ddy = Math.abs(dy) / (float) bounds.height;

        Anchor.Direction direction;

        if (ddx >= ddy) {
            direction = dx >= 0.0f ? Direction.RIGHT : Direction.LEFT;
        } else {
            direction = dy >= 0.0f ? Direction.BOTTOM : Direction.TOP;
        }

        float scale = 0.5f / Math.max(ddx, ddy);

        Point point = new Point(Math.round(relatedLocation.x + scale * dx), 
                Math.round(relatedLocation.y + scale * dy));
        
        return new Anchor.Result(point, direction);
    }

}
