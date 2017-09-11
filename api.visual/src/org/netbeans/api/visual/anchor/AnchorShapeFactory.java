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

import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.anchor.AttachableAnchorShape;
import org.netbeans.modules.visual.anchor.DefaultAnchorShapeResolver;
import org.netbeans.modules.visual.anchor.ImageAnchorShape;
import org.netbeans.modules.visual.anchor.TriangleAnchorShape;
import org.netbeans.modules.visual.anchor.ArrowAnchorShape;

import java.awt.*;

/**
 * The factory class of all built-in anchor shapes.
 * The instances of all built-in anchor shapes can be used multiple connection widgets.
 *
 * @author David Kaspar
 */
public class AnchorShapeFactory {

    public enum ConnectionEnd 
    {
        /** The source end of a connection */
        SOURCE, 
        
        /** The target end of a connection */
        TARGET 
    };
    
    private AnchorShapeFactory () {
    }

    /**
     * Creates an image anchor shape.
     * @param image the image
     * @return the anchor shape
     */
    public static AnchorShape createImageAnchorShape (Image image) {
        return createImageAnchorShape (image, false);
    }

    /**
     * Creates an image anchor shape with ability to specify line orientation.
     * @param image the image
     * @param lineOriented if true, then the image is line oriented
     * @return the anchor shape
     */
    public static AnchorShape createImageAnchorShape (Image image, boolean lineOriented) {
        return new ImageAnchorShape (image, lineOriented);
    }

    /**
     * Creates a triangular anchor shape.
     * @param size the size of triangle
     * @param filled if true, then the triangle is filled
     * @param output if true, then it is output triangle
     * @return the anchor shape
     */
    public static AnchorShape createTriangleAnchorShape (int size, boolean filled, boolean output) {
        return new TriangleAnchorShape (size, filled, output, false, 0.0);
    }

    /**
     * Creates a triangular anchor shape.
     * @param size the size of triangle
     * @param filled if true, then the triangle is filled
     * @param output if true, then it is output triangle
     * @param cutDistance the distance where the related line is cut (usually 1px smaller than the size)
     * @return the anchor shape
     */
    public static AnchorShape createTriangleAnchorShape (int size, boolean filled, boolean output, int cutDistance) {
        return new TriangleAnchorShape (size, filled, output, false, cutDistance);
    }

    /**
     * Creates an arrow anchor shape.
     * @param degrees the angle of the arrow in degrees (not radians)
     * @param size the size of the arrow
     * @return the anchor shape
     * @since 2.4
     */
    public static AnchorShape createArrowAnchorShape (int degrees, int size) {
        return new ArrowAnchorShape (degrees, size);
    }
    
    /**
     * Creates a proxy AnchorShape that is used to adjust the location of an AnchorShape.
     * The associated AnchorShape will fit on the outside of the attached widget.
     * The connection end and the attached widget is used to determine the
     * location of the location of the AnchorShape.
     * 
     * @param shape The shape that will be adjusted.
     * @param owner The owner of the AnchorShape.
     * @param referencingEnd The end of the connection to place the shape.
     * @param attachedWidget The widget on the shapes end.
     */
    public static AnchorShape createAdjustableAnchorShape(AnchorShape shape,
                                                          ConnectionWidget owner,
                                                          ConnectionEnd referencingEnd,
                                                          Widget attachedWidget)
    {
        AnchorShapeLocationResolver resolver = createWidgetResolver(owner, referencingEnd, attachedWidget);
        return createAdjustableAnchorShape(shape, resolver);
    }
    
    /**
     * Creates a proxy AnchorShape that is used to adjust the location of an AnchorShape.  
     * the location of the associated AnchorShape will be determined by a
     * AnchorShapeLocationResolver.
     * 
     * @param The shape that will be adjusted.
     * @param resolver The AnchorShapeLocationResolver used to determine where to place the shape.
     */
    public static AnchorShape createAdjustableAnchorShape(AnchorShape shape, AnchorShapeLocationResolver resolver)
    {
        return new AttachableAnchorShape(shape, resolver);
    }
    
    /**
     * Creates a AnchorShapeLocationResolver that uses a widget to resolve the 
     * AnchorsShapes location.  The connection end and the attached widget
     * is used to determine the location of the location of the AnchorShape.
     * 
     * @param owner The owner of the AnchorShape.
     * @param referencingEnd The end of the connection to place the shape.
     * @param attachedWidget The widget on the shapes end.
     */
    public static AnchorShapeLocationResolver createWidgetResolver(ConnectionWidget owner,
                                                                   ConnectionEnd referencingEnd,
                                                                   Widget attachedWidget)
    {
        return new DefaultAnchorShapeResolver(owner, referencingEnd, attachedWidget);
    }
    
}
