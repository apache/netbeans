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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.MoveControlPointProvider;
import org.netbeans.api.visual.widget.ConnectionWidget;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public final class OrthogonalMoveControlPointProvider implements MoveControlPointProvider {

    public java.util.List<Point> locationSuggested (ConnectionWidget connectionWidget, int index, Point suggestedLocation) {
        java.util.List<Point> controlPoints = connectionWidget.getControlPoints ();
        int last = controlPoints.size () - 1;
        if (index <= 0 || index >= last)
            return null;

        Point pointPre = controlPoints.get (index - 1);
        Point pointIndex = controlPoints.get (index);
        Point pointPost = controlPoints.get (index + 1);

        boolean changeX = true;
        boolean changeY = true;
        if (index <= 1) {
            Point pointFirst = controlPoints.get (0);
            if (pointFirst.x == pointIndex.x)
                changeX = false;
            if (pointFirst.y == pointIndex.y)
                changeY = false;
        }
        if (index >= last - 1) {
            Point pointLast = controlPoints.get (last);
            if (pointLast.x == pointIndex.x)
                changeX = false;
            if (pointLast.y == pointIndex.y)
                changeY = false;
        }

        Point newPointPre = new Point (pointPre);
        Point newPointIndex = new Point (pointIndex);
        Point newPointPost = new Point (pointPost);

        if (changeX) {
            final int x = suggestedLocation.x;
            if (pointPre.x == pointIndex.x)
                newPointPre.x = x;
            newPointIndex.x = x;
            if (pointPost.x == pointIndex.x)
                newPointPost.x = x;
        }
        if (changeY) {
            final int y = suggestedLocation.y;
            if (pointPre.y == pointIndex.y)
                newPointPre.y = y;
            newPointIndex.y = y;
            if (pointPost.y == pointIndex.y)
                newPointPost.y = y;
        }

        ArrayList<Point> list = new ArrayList<Point> (controlPoints);
        list.set (index - 1, newPointPre);
        list.set (index, newPointIndex);
        list.set (index + 1, newPointPost);
        return list;
    }

}
