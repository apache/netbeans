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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class CardLayout implements Layout {

    private static final Point POINT_EMPTY = new Point ();
    private static final Rectangle RECTANGLE_EMPTY = new Rectangle ();

    private Widget cardLayoutWidget;
    private Widget activeChildWidget;

    public CardLayout (Widget cardLayoutWidget) {
        assert cardLayoutWidget != null;
        this.cardLayoutWidget = cardLayoutWidget;
        cardLayoutWidget.setCheckClipping (true);
    }

    public Widget getActiveChildWidget () {
        return activeChildWidget;
    }

    public void setActiveChildWidget (Widget activeChildWidget) {
        this.activeChildWidget = activeChildWidget;
        cardLayoutWidget.revalidate ();
    }

    public void layout (Widget widget) {
        assert widget == cardLayoutWidget;

        Point preferredLocation = null;
        Rectangle preferredBounds = null;

        if (activeChildWidget != null  &&  activeChildWidget.isVisible ())
            for (Widget child : cardLayoutWidget.getChildren ())
                if (child == activeChildWidget) {
                    preferredLocation = child.getPreferredLocation ();
                    preferredBounds = child.getPreferredBounds ();
                    break;
                }

        if (preferredLocation == null)
            preferredLocation = POINT_EMPTY;
        if (preferredBounds == null)
            preferredBounds = RECTANGLE_EMPTY;
        Rectangle otherBounds = new Rectangle (preferredBounds.x, preferredBounds.y, 0, 0);

        for (Widget child : cardLayoutWidget.getChildren ())
            child.resolveBounds (preferredLocation, child == activeChildWidget ? preferredBounds : otherBounds);
    }

    public boolean requiresJustification (Widget widget) {
        return true;
    }

    public void justify (Widget widget) {
        assert widget == cardLayoutWidget;

        if (activeChildWidget != null  &&  activeChildWidget.isVisible ())
            for (Widget child : cardLayoutWidget.getChildren ())
                if (child == activeChildWidget) {
                    Rectangle bounds = widget.getClientArea ();
                    Point location = child.getLocation ();
                    Rectangle childBounds = child.getBounds ();

                    bounds.translate (- location.x, - location.y);
                    childBounds.add (bounds);

                    child.resolveBounds (location, bounds);
                    return;
                }
    }

}
