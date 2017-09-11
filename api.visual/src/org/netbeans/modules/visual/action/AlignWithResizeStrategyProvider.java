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

import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.AlignWithWidgetCollector;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class AlignWithResizeStrategyProvider extends AlignWithSupport implements ResizeStrategy, ResizeProvider {

    private boolean outerBounds;

    public AlignWithResizeStrategyProvider (AlignWithWidgetCollector collector, LayerWidget interractionLayer, AlignWithMoveDecorator decorator, boolean outerBounds) {
        super (collector, interractionLayer, decorator);
        this.outerBounds = outerBounds;
    }

    public Rectangle boundsSuggested (Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
        Insets insets = widget.getBorder ().getInsets ();
        int minx = insets.left + insets.right;
        int miny = insets.top + insets.bottom;

        suggestedBounds = widget.convertLocalToScene (suggestedBounds);

        Point suggestedLocation, point;
        int tempx, tempy;

        switch (controlPoint) {
            case BOTTOM_CENTER:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width / 2, suggestedBounds.y + suggestedBounds.height);
                if (! outerBounds)
                    suggestedLocation.y -= insets.bottom;

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, false, true, false, false);

                if (! outerBounds)
                    point.y += insets.bottom;

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);
                break;
            case BOTTOM_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y + suggestedBounds.height);
                if (! outerBounds) {
                    suggestedLocation.y -= insets.bottom;
                    suggestedLocation.x += insets.left;
                }

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                if (! outerBounds) {
                    point.y += insets.bottom;
                    point.x -= insets.left;
                }

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);

                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case BOTTOM_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y + suggestedBounds.height);
                if (! outerBounds) {
                    suggestedLocation.y -= insets.bottom;
                    suggestedLocation.x -= insets.right;
                }

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                if (! outerBounds) {
                    point.y += insets.bottom;
                    point.x += insets.right;
                }

                suggestedBounds.height = Math.max (miny, point.y - suggestedBounds.y);

                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
            case CENTER_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y + suggestedBounds.height / 2);
                if (! outerBounds)
                    suggestedLocation.x += insets.left;

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, false, false, false);

                if (! outerBounds)
                    point.x -= insets.left;
                
                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case CENTER_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y + suggestedBounds.height / 2);
                if (! outerBounds)
                    suggestedLocation.x -= insets.right;

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, false, false, false);

                if (! outerBounds)
                    point.x += insets.right;
                
                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
            case TOP_CENTER:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width / 2, suggestedBounds.y);
                if (! outerBounds)
                    suggestedLocation.y += insets.top;

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, false, true, false, false);

                if (! outerBounds)
                    point.y -= insets.top;

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;
                break;
            case TOP_LEFT:
                suggestedLocation = new Point (suggestedBounds.x, suggestedBounds.y);
                if (! outerBounds) {
                    suggestedLocation.y += insets.top;
                    suggestedLocation.x += insets.left;
                }

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                if (! outerBounds) {
                    point.y -= insets.top;
                    point.x -= insets.left;
                }

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;

                tempx = Math.min (point.x, suggestedBounds.x + suggestedBounds.width - minx);
                suggestedBounds.width = suggestedBounds.x + suggestedBounds.width - tempx;
                suggestedBounds.x = tempx;
                break;
            case TOP_RIGHT:
                suggestedLocation = new Point (suggestedBounds.x + suggestedBounds.width, suggestedBounds.y);
                if (! outerBounds) {
                    suggestedLocation.y += insets.top;
                    suggestedLocation.x -= insets.right;
                }

                point = super.locationSuggested (widget, new Rectangle (suggestedLocation), suggestedLocation, true, true, false, false);

                if (! outerBounds) {
                    point.y -= insets.top;
                    point.x += insets.right;
                }

                tempy = Math.min (point.y, suggestedBounds.y + suggestedBounds.height - miny);
                suggestedBounds.height = suggestedBounds.y + suggestedBounds.height - tempy;
                suggestedBounds.y = tempy;

                suggestedBounds.width = Math.max (minx, point.x - suggestedBounds.x);
                break;
        }
        return widget.convertSceneToLocal (suggestedBounds);
    }

    public void resizingStarted (Widget widget) {
        show ();
    }

    public void resizingFinished (Widget widget) {
        hide ();
    }

}
