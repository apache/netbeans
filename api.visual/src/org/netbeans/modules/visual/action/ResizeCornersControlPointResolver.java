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

import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.util.GeomUtil;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class ResizeCornersControlPointResolver implements ResizeControlPointResolver {

    public ResizeProvider.ControlPoint resolveControlPoint (Widget widget, Point point) {
        Rectangle bounds = widget.getBounds ();
        Insets insets = widget.getBorder ().getInsets ();
        Point center = GeomUtil.center (bounds);
        Dimension centerDimension = new Dimension (Math.max (insets.left, insets.right), Math.max (insets.top, insets.bottom));
        if (point.y >= bounds.y + bounds.height - insets.bottom && point.y < bounds.y + bounds.height) {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.BOTTOM_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.BOTTOM_LEFT;
            else
            if (point.x >= center.x - centerDimension.height / 2 && point.x < center.x + centerDimension.height - centerDimension.height / 2)
                return ResizeProvider.ControlPoint.BOTTOM_CENTER;

        } else if (point.y >= bounds.y && point.y < bounds.y + insets.top) {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.TOP_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.TOP_LEFT;
            else
            if (point.x >= center.x - centerDimension.height / 2 && point.x < center.x + centerDimension.height - centerDimension.height / 2)
                return ResizeProvider.ControlPoint.TOP_CENTER;

        } else
        if (point.y >= center.y - centerDimension.width / 2 && point.y < center.y + centerDimension.width - centerDimension.width / 2)
        {

            if (point.x >= bounds.x + bounds.width - insets.right && point.x < bounds.x + bounds.width)
                return ResizeProvider.ControlPoint.CENTER_RIGHT;
            else if (point.x >= bounds.x && point.x < bounds.x + insets.left)
                return ResizeProvider.ControlPoint.CENTER_LEFT;

        }
        // TODO - resolve CENTER points
        return null;
    }

}
