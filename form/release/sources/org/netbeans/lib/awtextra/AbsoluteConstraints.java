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

import java.awt.Dimension;
import java.awt.Point;

/** An object that encapsulates position and (optionally) size for
* Absolute positioning of components.
*
* @see AbsoluteLayout
* @version 1.01, Aug 19, 1998
*/
public class AbsoluteConstraints implements java.io.Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5261460716622152494L;

    /** The X position of the component */
    public int x;
    /** The Y position of the component */
    public int y;
    /** The width of the component or -1 if the component's preferred width should be used */
    public int width = -1;
    /** The height of the component or -1 if the component's preferred height should be used */
    public int height = -1;

    /** Creates a new AbsoluteConstraints for specified position.
    * @param pos The position to be represented by this AbsoluteConstraints
    */
    public AbsoluteConstraints(Point pos) {
        this (pos.x, pos.y);
    }

    /** Creates a new AbsoluteConstraints for specified position.
    * @param x The X position to be represented by this AbsoluteConstraints
    * @param y The Y position to be represented by this AbsoluteConstraints
    */
    public AbsoluteConstraints(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Creates a new AbsoluteConstraints for specified position and size.
    * @param pos  The position to be represented by this AbsoluteConstraints
    * @param size The size to be represented by this AbsoluteConstraints or null
    *             if the component's preferred size should be used
    */
    public AbsoluteConstraints(Point pos, Dimension size) {
        this.x = pos.x;
        this.y = pos.y;
        if (size != null) {
            this.width = size.width;
            this.height = size.height;
        }
    }

    /** Creates a new AbsoluteConstraints for specified position and size.
    * @param x      The X position to be represented by this AbsoluteConstraints
    * @param y      The Y position to be represented by this AbsoluteConstraints
    * @param width  The width to be represented by this AbsoluteConstraints or -1 if the 
    *               component's preferred width should be used  
    * @param height The height to be represented by this AbsoluteConstraints or -1 if the
    *               component's preferred height should be used  
    */
    public AbsoluteConstraints(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** @return The X position represented by this AbsoluteConstraints */
    public int getX () {
        return x;
    }

    /** @return The Y position represented by this AbsoluteConstraints */
    public int getY () {
        return y;
    }

    /** @return The width represented by this AbsoluteConstraints or -1 if the
    * component's preferred width should be used 
    */
    public int getWidth () {
        return width;
    }

    /** @return The height represented by this AbsoluteConstraints or -1 if the
    * component's preferred height should be used 
    */
    public int getHeight () {
        return height;
    }

    public String toString () {
        return super.toString () +" [x="+x+", y="+y+", width="+width+", height="+height+"]";
    }

}

