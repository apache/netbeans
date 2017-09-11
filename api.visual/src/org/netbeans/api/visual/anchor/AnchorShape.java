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
package org.netbeans.api.visual.anchor;

import org.netbeans.modules.visual.anchor.TriangleAnchorShape;

import java.awt.*;

/**
 * Represents an anchor shape which is rendered at the source and the target point of a connection widget where the shape is used.
 * The same instance of a shape could be shared by multiple connection widgets.
 * @author David Kaspar
 */
public interface AnchorShape {

    /**
     * Returns whether the shape is oriented by the line path of a connection.
     * @return true if it is line-oriented
     */
    public boolean isLineOriented ();

    /**
     * Returns a radius of a shape that the shape used for rendering.
     * @return the radius
     */
    public int getRadius ();

    /**
     * Returns a distance by which a line at particular source or target point should be cut (not rendered).
     * This is used for hollow-triangle shapes, to not paint the connection-line within the triangle.
     * @return the cut distance in pixels;
     *     if positive, then the line is cut by specified number of pixels, the line could be cut by radius pixels only;
     *     if 0.0, then the line is not cut;
     *     if negative, then the line is extended by specified number of pixels, the line could be extended by radius pixels only
     */
    public double getCutDistance ();

    /**
     * Renders the shape into a graphics instance
     * @param graphics the graphics
     * @param source true, if the shape is used for a source point; false if the shape is used for a target point.
     */
    public void paint (Graphics2D graphics, boolean source);

    /**
     * The empty anchor shape.
     */
    public static final AnchorShape NONE = new AnchorShape() {
        public boolean isLineOriented () { return false; }
        public int getRadius () { return 0; }
        public double getCutDistance () { return 0; }
        public void paint (Graphics2D graphics, boolean source) { }
    };

    /**
     * The hollow-triangle anchor shape.
     */
    public static final AnchorShape TRIANGLE_HOLLOW = new TriangleAnchorShape (12, false, false, true, 12.0);

    /**
     * The filled-triangle anchor shape.
     */
    public static final AnchorShape TRIANGLE_FILLED = new TriangleAnchorShape (12, true, false, false, 11.0);

    /**
     * The output-triangle anchor shape.
     */
    public static final AnchorShape TRIANGLE_OUT = new TriangleAnchorShape (12, true, true, false, 11.0);

}
