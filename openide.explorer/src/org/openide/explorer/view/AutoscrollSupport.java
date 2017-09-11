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
package org.openide.explorer.view;

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
    AutoscrollSupport(Component comp, Insets insets) {
        this.comp = comp;
        this.insets = insets;
        this.scrollUnits = insets;
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
                curComp = comp.getParent();
            }

            viewport = (JViewport) curComp;
        }

        return viewport;
    }
}
