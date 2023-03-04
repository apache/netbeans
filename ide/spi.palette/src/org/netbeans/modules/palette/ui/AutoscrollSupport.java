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

package org.netbeans.modules.palette.ui;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.dnd.Autoscroll;

import javax.swing.JViewport;


/** The support for autoscrolling in components contained in
* the viewport.
*
* @author Dafe Simonek
*/
final class AutoscrollSupport extends Object implements Autoscroll {
    // Attributes

    /** The component which we support with autoscrolling */
    Component comp;

    /** The viewport containing asociated component */
    JViewport viewport;

    /** The insets where autoscrolling is active */
    Insets insets;

    /** Base sizes of scrolling during one autoscroll operation */
    Insets scrollUnits;

    /** Insets to return from getAutoscrollInsets - insets
    * where autoscroll could potencionally occur */
    Insets autoscrollInsets;

    /** Creates a support for given component with given insets
    * where autoscrolling is active */
    AutoscrollSupport( Component comp ) {
        this.comp = comp;
        this.insets = new Insets(20, 10, 20, 10);
        this.scrollUnits = new Insets(20, 10, 20, 10);
    }

    /** Performs autoscroll operation.
    */
    public void autoscroll(Point cursorLoc) {
        JViewport viewport = getViewport();

        if (viewport == null) {
            return;
        }

        Point viewPos = viewport.getViewPosition();
        int viewHeight = viewport.getExtentSize().height;
        int viewWidth = viewport.getExtentSize().width;

        // perform scrolling
        if ((cursorLoc.y - viewPos.y) < insets.top) {
            // scroll up
            viewport.setViewPosition(new Point(viewPos.x, Math.max(viewPos.y - scrollUnits.top, 0)));
        } else if (((viewPos.y + viewHeight) - cursorLoc.y) < insets.bottom) {
            // scroll down
            viewport.setViewPosition(
                new Point(viewPos.x, Math.min(viewPos.y + scrollUnits.bottom, comp.getHeight() - viewHeight))
            );
        } else if ((cursorLoc.x - viewPos.x) < insets.left) {
            // scroll left
            viewport.setViewPosition(new Point(Math.max(viewPos.x - scrollUnits.left, 0), viewPos.y));
        } else if (((viewPos.x + viewWidth) - cursorLoc.x) < insets.right) {
            // scroll right
            viewport.setViewPosition(
                new Point(Math.min(viewPos.x + scrollUnits.right, comp.getWidth() - viewWidth), viewPos.y)
            );
        }
    }

    public Insets getAutoscrollInsets() {
        if (autoscrollInsets == null) {
            int height = comp.getHeight();
            int width = comp.getWidth();
            autoscrollInsets = new Insets(height, width, height, width);
        }

        return autoscrollInsets;
    }

    /** @return insets where autoscroll is active
    */
    public Insets getInsets() {
        return insets;
    }

    /** Sets new active autoscroll insets
    */
    public void setInsets(Insets insets) {
        this.insets = insets;
    }

    /** @return Scroll units for one autoscroll operation.
    */
    public Insets getScrollUnits() {
        return scrollUnits;
    }

    /** Sets autoscroll scroll units.
    * When autoscroll(..) method is called, it will scroll the
    * component accordign to scroll unit in appropriate direction.
    * So, scrollUnits.top says how much (in pixels) the component
    * will autoscroll up etc...
    */
    public void setScrollUnits(Insets scrollUnits) {
        this.scrollUnits = scrollUnits;
    }

    /** Getter for viewport of asociated component.
    * Can return null if component is not contained in any viewport.
    */
    JViewport getViewport() {
        if (viewport == null) {
            Component curComp = comp;

            while (!(curComp instanceof JViewport) && (curComp != null)) {
                curComp = curComp.getParent();
            }

            viewport = (JViewport) curComp;
        }

        return viewport;
    }
}
