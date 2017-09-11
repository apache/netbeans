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

package org.netbeans.lib.awtextra;

import java.awt.*;

/** AbsoluteLayout is a LayoutManager that works as a replacement for "null" layout to
* allow placement of components in absolute positions.
*
* @see AbsoluteConstraints
* @version 1.01, Aug 19, 1998
*/
public class AbsoluteLayout implements LayoutManager2, java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1919857869177070440L;

    /** Adds the specified component with the specified name to
    * the layout.
    * @param name the component name
    * @param comp the component to be added
    */
    public void addLayoutComponent(String name, Component comp) {
        throw new IllegalArgumentException();
    }

    /** Removes the specified component from the layout.
    * @param comp the component to be removed
    */
    public void removeLayoutComponent(Component comp) {
        constraints.remove(comp);
    }

    /** Calculates the preferred dimension for the specified
    * panel given the components in the specified parent container.
    * @param parent the component to be laid out
    *
    * @see #minimumLayoutSize
    */
    public Dimension preferredLayoutSize(Container parent) {
        int maxWidth = 0;
        int maxHeight = 0;
        for (java.util.Enumeration e = constraints.keys(); e.hasMoreElements();) {
            Component comp = (Component)e.nextElement();
            AbsoluteConstraints ac = (AbsoluteConstraints)constraints.get(comp);
            Dimension size = comp.getPreferredSize();

            int width = ac.getWidth ();
            if (width == -1) width = size.width;
            int height = ac.getHeight ();
            if (height == -1) height = size.height;

            if (ac.x + width > maxWidth)
                maxWidth = ac.x + width;
            if (ac.y + height > maxHeight)
                maxHeight = ac.y + height;
        }
        return new Dimension (maxWidth, maxHeight);
    }

    /** Calculates the minimum dimension for the specified
    * panel given the components in the specified parent container.
    * @param parent the component to be laid out
    * @see #preferredLayoutSize
    */
    public Dimension minimumLayoutSize(Container parent) {
        int maxWidth = 0;
        int maxHeight = 0;
        for (java.util.Enumeration e = constraints.keys(); e.hasMoreElements();) {
            Component comp = (Component)e.nextElement();
            AbsoluteConstraints ac = (AbsoluteConstraints)constraints.get(comp);

            Dimension size = comp.getMinimumSize();

            int width = ac.getWidth ();
            if (width == -1) width = size.width;
            int height = ac.getHeight ();
            if (height == -1) height = size.height;

            if (ac.x + width > maxWidth)
                maxWidth = ac.x + width;
            if (ac.y + height > maxHeight)
                maxHeight = ac.y + height;
        }
        return new Dimension (maxWidth, maxHeight);
    }

    /** Lays out the container in the specified panel.
    * @param parent the component which needs to be laid out
    */
    public void layoutContainer(Container parent) {
        for (java.util.Enumeration e = constraints.keys(); e.hasMoreElements();) {
            Component comp = (Component)e.nextElement();
            AbsoluteConstraints ac = (AbsoluteConstraints)constraints.get(comp);
            Dimension size = comp.getPreferredSize();
            int width = ac.getWidth ();
            if (width == -1) width = size.width;
            int height = ac.getHeight ();
            if (height == -1) height = size.height;

            comp.setBounds(ac.x, ac.y, width, height);
        }
    }

    /** Adds the specified component to the layout, using the specified
    * constraint object.
    * @param comp the component to be added
    * @param constr  where/how the component is added to the layout.
    */
    public void addLayoutComponent(Component comp, Object constr) {
        if (!(constr instanceof AbsoluteConstraints))
            throw new IllegalArgumentException();
        constraints.put(comp, constr);
    }

    /** Returns the maximum size of this component.
    * @see java.awt.Component#getMinimumSize()
    * @see java.awt.Component#getPreferredSize()
    * @see LayoutManager
    */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /** Returns the alignment along the x axis.  This specifies how
    * the component would like to be aligned relative to other
    * components.  The value should be a number between 0 and 1
    * where 0 represents alignment along the origin, 1 is aligned
    * the furthest away from the origin, 0.5 is centered, etc.
    */
    public float getLayoutAlignmentX(Container target) {
        return 0;
    }

    /** Returns the alignment along the y axis.  This specifies how
    * the component would like to be aligned relative to other
    * components.  The value should be a number between 0 and 1
    * where 0 represents alignment along the origin, 1 is aligned
    * the furthest away from the origin, 0.5 is centered, etc.
    */
    public float getLayoutAlignmentY(Container target) {
        return 0;
    }

    /** Invalidates the layout, indicating that if the layout manager
    * has cached information it should be discarded.
    */
    public void invalidateLayout(Container target) {
    }


    /** A mapping <Component, AbsoluteConstraints> */
    protected java.util.Hashtable constraints = new java.util.Hashtable();
}

