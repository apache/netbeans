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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;

import java.awt.*;
import java.util.*;

/**
 * @author David Kaspar
 */
public final class FlowLayout implements Layout {

    private boolean verticalOrientation;
    private LayoutFactory.SerialAlignment alignment;
    private int gap;

    public FlowLayout (boolean verticalOrientation, LayoutFactory.SerialAlignment alignment, int gap) {
        this.verticalOrientation = verticalOrientation;
        this.alignment = alignment;
        this.gap = gap;
    }

    public void layout (Widget widget) {
        int max = 0;
        Collection<Widget> children = widget.getChildren ();
        if (verticalOrientation) {
            for (Widget child : children) {
                if (! child.isVisible ())
                    continue;
                Rectangle preferredBounds = child.getPreferredBounds ();
                int i = preferredBounds.width;
                if (i > max)
                    max = i;
            }
            int pos = 0;
            for (Widget child : children) {
                Rectangle preferredBounds = child.getPreferredBounds ();
                int x = preferredBounds.x;
                int y = preferredBounds.y;
                int width = preferredBounds.width;
                int height = preferredBounds.height;
                int lx = -x;
                int ly = pos - y;
                switch (alignment) {
                    case CENTER:
                        lx += (max - width) / 2;
                        break;
                    case JUSTIFY:
                        width = max;
                        break;
                    case LEFT_TOP:
                        break;
                    case RIGHT_BOTTOM:
                        lx += max - width;
                        break;
                }
                if (child.isVisible ()) {
                    child.resolveBounds (new Point (lx, ly), new Rectangle (x, y, width, height));
                    pos += height + gap;
                } else
                    child.resolveBounds (new Point (lx, ly), new Rectangle (x, y, 0, 0));
            }
        } else {
            for (Widget child : children) {
                if (!child.isVisible ())
                    continue;
                Rectangle preferredBounds = child.getPreferredBounds ();
                int i = preferredBounds.height;
                if (i > max)
                    max = i;
            }
            int pos = 0;
            for (Widget child : children) {
                Rectangle preferredBounds = child.getPreferredBounds ();
                int x = preferredBounds.x;
                int y = preferredBounds.y;
                int width = preferredBounds.width;
                int height = preferredBounds.height;
                int lx = pos - x;
                int ly = -y;
                switch (alignment) {
                    case CENTER:
                        ly += (max - height) / 2;
                        break;
                    case JUSTIFY:
                        height = max;
                        break;
                    case LEFT_TOP:
                        break;
                    case RIGHT_BOTTOM:
                        ly += max - height;
                        break;
                }
                if (child.isVisible ()) {
                    child.resolveBounds (new Point (lx, ly), new Rectangle (x, y, width, height));
                    pos += width + gap;
                } else
                    child.resolveBounds (new Point (lx, ly), new Rectangle (x, y, 0, 0));
            }
        }
    }

    public boolean requiresJustification (Widget widget) {
        return true;
    }

    public void justify (Widget widget) {
        Rectangle parentBounds = widget.getClientArea ();
        int totalWeight = 0;
        int totalGap = 0;
        java.util.List<Widget> children = widget.getChildren ();
        for (int a = 0; a < children.size (); a ++) {
            Widget child = children.get (a);
            if (! child.isVisible ())
                continue;
            totalWeight += resolveWeight (widget, child);

            if (a > 0)
                totalGap -= gap;
            if (verticalOrientation)
                totalGap -= child.getBounds ().height;
            else
                totalGap -= child.getBounds ().width;
        }
        totalGap += verticalOrientation ? parentBounds.height : parentBounds.width;
        if (totalGap < 0)
            totalWeight = totalGap = 0;

        int gapAdd = 0;
        int weightAdd = 0;

        int parentX1 = parentBounds.x;
        int parentX2 = parentX1 + parentBounds.width;
        int parentY1 = parentBounds.y;
        int parentY2 = parentY1 + parentBounds.height;

        for (Widget child : widget.getChildren ()) {
            Point childLocation = child.getLocation ();
            Rectangle childBounds = child.getBounds ();

            if (verticalOrientation) {
                switch (alignment) {
                    case CENTER:
                        childLocation.x = (parentX1 + parentX2 - childBounds.width) / 2;
                        break;
                    case JUSTIFY:
                        childLocation.x = parentX1;
                        childBounds.width = parentX2 - parentX1;
                        break;
                    case LEFT_TOP:
                        childLocation.x = parentX1;
                        break;
                    case RIGHT_BOTTOM:
                        childLocation.x = parentX2 - childBounds.width;
                        break;
                }
                if (totalWeight > 0  &&  child.isVisible ()) {
                    childLocation.y += gapAdd;
                    int weight = resolveWeight (widget, child);
                    int gap = (weightAdd + weight) * totalGap / totalWeight;
                    childBounds.height += gap - gapAdd;
                    gapAdd = gap;
                    weightAdd += weight;
                }
                childLocation.x -= childBounds.x;
                childLocation.y += parentY1;
            } else {
                switch (alignment) {
                    case CENTER:
                        childLocation.y = (parentY1 + parentY2 - childBounds.height) / 2;
                        break;
                    case JUSTIFY:
                        childLocation.y = parentY1;
                        childBounds.height = parentY2 - parentY1;
                        break;
                    case LEFT_TOP:
                        childLocation.y = parentY1;
                        break;
                    case RIGHT_BOTTOM:
                        childLocation.y = parentY2 - childBounds.height;
                        break;
                }
                if (totalWeight > 0  &&  child.isVisible ()) {
                    childLocation.x += gapAdd;
                    int weight = resolveWeight (widget, child);
                    int gap = (weightAdd + weight) * totalGap / totalWeight;
                    childBounds.width += gap - gapAdd;
                    gapAdd = gap;
                    weightAdd += weight;
                }
                childLocation.y -= childBounds.y;
                childLocation.x += parentX1;
            }

            child.resolveBounds (childLocation, childBounds);
        }
    }

     private static int resolveWeight (Widget widget, Widget child) {
         Object o = widget.getChildConstraint (child);
         if (o instanceof Number) {
             int weight = ((Number) o).intValue ();
             if (weight > 0)
                 return weight;
         }
         return 0;
     }

}
