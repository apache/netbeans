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

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.List;

/**
 * @author Alex
 */
public final class FreeRectangularAnchor extends Anchor {

    private boolean includeBorders;

    public FreeRectangularAnchor(Widget widget, boolean includeBorders) {
        super(widget);
        this.includeBorders = includeBorders;
    }

    public Result compute(Entry entry) {
        ConnectionWidget fcw = entry.getAttachedConnectionWidget ();
        assert fcw != null;
        Point relatedLocation = getRelatedSceneLocation();
        Widget widget = getRelatedWidget();
        List<Point> fcwControlPoints = fcw.getControlPoints ();

        Point oppositeLocation;
        if (fcwControlPoints.size () < 2)
            oppositeLocation = getOppositeSceneLocation (entry);
        else if (entry.isAttachedToConnectionSource ())
            oppositeLocation = fcwControlPoints.get (1);
        else
            oppositeLocation = fcwControlPoints.get (fcwControlPoints.size () - 2);

        Rectangle bounds = widget.getBounds();
        if (! includeBorders) {
            Insets insets = widget.getBorder().getInsets();
            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= insets.left + insets.right;
            bounds.height -= insets.top + insets.bottom;
        }
        bounds = widget.convertLocalToScene(bounds);

        if (bounds.isEmpty()  || relatedLocation.equals(oppositeLocation))
            return new Anchor.Result(relatedLocation, Anchor.DIRECTION_ANY);

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

        Point point = new Point(Math.round(relatedLocation.x + scale * dx), Math.round(relatedLocation.y + scale * dy));
        return new Anchor.Result(point, direction);
    }

}
