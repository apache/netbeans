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
     * @param shape The shape that will be adjusted.
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
