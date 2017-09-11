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
 *//*
 * TabLayoutModel.java
 *
 * Created on May 16, 2003, 3:47 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.*;


/**
 * A model representing the visual layout of tabs in a TabDataModel as a set of
 * rectangles.  Used by BasicTabDisplayerUI and its subclasses to manage the layout of
 * tabs in the displayer.
 *
 * @author Tim Boudreau
 */
public interface TabLayoutModel {
    /**
     * Get the x coordinate of the tab rectangle for the tab at index
     * <code>index</code> in the data model.
     *
     * @param index The tab index
     * @return The coordinate
     */
    public int getX(int index);

    /**
     * Get the y coordinate of the tab rectangle for the tab at index
     * <code>index</code> in the data model.
     *
     * @param index The tab index
     * @return The coordinate
     */
    public int getY(int index);

    /**
     * Get the width of the tab rectangle for the tab at index
     * <code>index</code> in the data model.
     *
     * @param index The tab index
     * @return The width
     */
    public int getW(int index);

    /**
     * Get the height of the tab rectangle for the tab at index
     * <code>index</code> in the data model.
     *
     * @param index The tab index
     * @return The height
     */
    public int getH(int index);

    /**
     * Get the index of the tab in the data model for the supplied point.
     *
     * @param x X coordinate of a point representing a set of pixel coordinate in the space
     *          modeled by this layout model
     * @param y Y coordinate
     * @return The index into the data model of the tab displayed at the passed
     *         point or -1
     */
    public int indexOfPoint(int x, int y);

    // XXX DnD only
    /**
     * Gets the index of possibly dropped component (as a new tab).
     */
    public int dropIndexOfPoint(int x, int y);

    public void setPadding (Dimension d);
}
