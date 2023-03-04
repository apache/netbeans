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
package org.netbeans.api.visual.border;

import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.border.*;
import org.netbeans.modules.visual.util.GeomUtil;

import java.awt.*;
import org.netbeans.modules.visual.laf.DefaultLookFeel;
import org.openide.util.Parameters;

/**
 * This class is a factory of all built-in implementation of borders.
 * Instances of built-in borders can be shared by multiple widgets.
 *
 * @author David Kaspar
 */
// TODO - check insets values
public final class BorderFactory {

    private static final Border BORDER_EMPTY = new EmptyBorder (0, 0, 0, 0, false);
    private static final Border BORDER_LINE = createLineBorder (1);

    private BorderFactory () {
    }

    /**
     * Creates an default empty border with 0px layout.
     * The instance can be shared by multiple widgets.
     * @return the empty border
     */
    public static Border createEmptyBorder () {
        return BORDER_EMPTY;
    }

    /**
     * Creates an empty border with specific thickness.
     * The instance can be shared by multiple widgets.
     * @param thickness the border thickness
     * @return the empty border
     */
    public static Border createEmptyBorder (int thickness) {
        return thickness > 0 ? createEmptyBorder (thickness, thickness, thickness, thickness) : BORDER_EMPTY;
    }

    /**
     * Creates an empty border with specific thickness.
     * The instance can be shared by multiple widgets.
    * @param horizontal the horizontal thickness
     * @param vertical the vertical thickness
     * @return the empty border
     */
    public static Border createEmptyBorder (int horizontal, int vertical) {
        return createEmptyBorder (vertical, horizontal, vertical, horizontal);
    }

    /**
     * Creates an empty border with specific thickness.
     * The instance can be shared by multiple widgets.
     * @param top the top inset
     * @param left the left inset
     * @param bottom the bottom inset
     * @param right the right inset
     * @return the empty border
     */
    public static Border createEmptyBorder (int top, int left, int bottom, int right) {
        return new EmptyBorder (top, left, bottom, right, false);
    }

    /**
     * Creates an opaque border with specific thickness.
     * The instance can be shared by multiple widgets.
     * @param top the top inset
     * @param left the left inset
     * @param bottom the bottom inset
     * @param right the right inset
     * @return the empty border
     */
    public static Border createOpaqueBorder (int top, int left, int bottom, int right) {
        return new EmptyBorder (top, left, bottom, right, true);
    }

    /**
     * Creates a composite border that consists of a list of specified borders - one embedded to another.
     * The instance can be shared by multiple widgets.
     * @param borders the list of borders
     * @return the composite border
     */
    public static Border createCompositeBorder (Border... borders) {
        return new CompositeBorder (borders);
    }

    /**
     * Creates a layout from a Swing border.
     * The instance can be shared by multiple widgets but cannot be used in multiple scenes.
     * @param scene the scene where the border is used.
     * @param border the Swing border
     * @return the border
     */
    public static Border createSwingBorder (Scene scene, javax.swing.border.Border border) {
        Parameters.notNull("scene", scene);
        // Makes SwingBorderGetterTest fail: Parameters.notNull("scene.view", scene.getView());
        Parameters.notNull("border", border);
        return new SwingBorder (scene, border);
    }

    /**
     * Creates a line border with default style.
     * The instance can be shared by multiple widgets.
     * @return the line border
     */
    public static Border createLineBorder () {
        return BORDER_LINE;
    }
    
    /**
     * Creates a line border with default style.
     * 
     * The instance can not be shared by multiple widgets.
     * 
     * @param property the property name
     * @param associated the widget used to retrieve the resource table.
     * @return the line border
     */
    public static Border createLineBorder (String property, Widget associated) {
        return createLineBorder (1, property, associated);
    }
    
    /**
     * Creates a line border with default style.
     * 
     * The instance can be shared by multiple widgets.
     * 
     * @param property the property name
     * @param table the resource table.
     * @return the line border
     */
    public static Border createLineBorder (String property, ResourceTable table) {
        return createLineBorder (1, property, table);
    }
    
    /**
     * Creates a line border with specific thickness. The line is still one pixel but the layout insets are calculated from thickness.
     * The instance can be shared by multiple widgets.
     * @param thickness the border thickness
     * @return the line border
     */
    public static Border createLineBorder (int thickness) {
        return createLineBorder (thickness, null);
    }

    /**
     * Creates a line border with specific thickness and color. The line is still one pixel but the layout insets are calculated from thickness.
     * The instance can be shared by multiple widgets.
     * @param thickness the border thickness
     * @param color     the line color
     * @return the line border
     */
    public static Border createLineBorder (int thickness, Color color) {
        return new LineBorder (thickness, thickness, thickness, thickness, color != null ? color : (new DefaultLookFeel()).getForeground()/*Color.BLACK*/);
    }
 
    /**
     * Creates a line border with specific thickness and color. The line is 
     * still one pixel but the layout insets are calculated from thickness.
     * The color is specified by a resource property that is retrieved from a   
     * resource table.  The resource table is retrieved from the associated 
     * widget.
     * 
     * The instance can not be shared by multiple widgets.
     * @param thickness the border thickness
     * @param property the property name
     * @param associated the widget used to retrieve the resource table.
     * @return the line border
     */
    public static Border createLineBorder (int thickness, String property, Widget associated) {
        return new LineBorder (thickness, thickness, thickness, thickness, property, associated);
    }
    
    /**
     * Creates a line border with specific thickness and color. The line is 
     * still one pixel but the layout insets are calculated from thickness.
     * The color is specified by a resource property that is retrieved from a   
     * resource table.  The resource table is retrieved from the associated 
     * widget.
     * 
     * The instance can not be shared by multiple widgets.
     * 
     * @param thickness the border thickness
     * @param property the property name
     * @param table the resource table.
     * @return the line border
     */
    public static Border createLineBorder (int thickness, String property, ResourceTable table) {
        return new LineBorder (thickness, thickness, thickness, thickness, property, table);
    }
    
    /**
     * Creates a line border with specific insets and color. The line is still one pixel but the layout insets are specified.
     * The instance can be shared by multiple widgets.
     * @param top the top inset
     * @param left the left inset
     * @param bottom the bottom inset
     * @param right the right inset
     * @param color the line color
     * @return the line border
     */
    public static Border createLineBorder (int top, int left, int bottom, int right, Color color) {
        return new LineBorder (top, left, bottom, right, color != null ? color : (new DefaultLookFeel()).getForeground()/*Color.BLACK*/);
    }
    
    /**
     * Creates a line border with specific insets and color. The line is still 
     * one pixel but the layout insets are specified.  The color is specified
     * by a resource property that is retrieved from a resource table.  
     * The resource table is retrieved from the associated widget.
     * 
     * The instance can not shared by multiple widgets.
     * @param top the top inset
     * @param left the left inset
     * @param bottom the bottom inset
     * @param right the right inset
     * @param property the property name
     * @param associated the widget used to retrieve the resource table.
     * @return the line border
     */
    public static Border createLineBorder (int top, int left, int bottom, int right, 
                                           String property, Widget associated) {
        return new LineBorder (top, left, bottom, right, property, associated);
    }
    
    /**
     * Creates a line border with specific insets and color. The line is still 
     * one pixel but the layout insets are specified.  The color is specified
     * by a resource property that is retrieved from a resource table. 
     * 
     * The instance can not shared by multiple widgets.
     * @param top the top inset
     * @param left the left inset
     * @param bottom the bottom inset
     * @param right the right inset
     * @param property the property name
     * @param table the resource table.
     * @return the line border
     */
    public static Border createLineBorder (int top, int left, int bottom, int right, 
                                           String property, ResourceTable table) {
        return new LineBorder (top, left, bottom, right, property, table);
    }
    
    /**
     * Creates a bevel border.
     * The instance can be shared by multiple widgets.
     * @param raised if true, then it is a raised-bevel border; if false, then it is a lowered-bevel layout
     * @return the bevel border
     */
    public static Border createBevelBorder (boolean raised) {
        return createBevelBorder (raised, null);
    }

    /**
     * Creates a bevel border.
     * The instance can be shared by multiple widgets.
     * @param raised if true, then it is a raised-bevel layout; if false, then it is a lowered-bevel border
     * @param color the border color
     * @return the bevel border
     */
    public static Border createBevelBorder (boolean raised, Color color) {
        return new BevelBorder (raised, color != null ? color : Color.GRAY);
    }
    
    /**
     * Creates a bevel border.
     * 
     * The instance can not be shared by multiple widgets.
     * @param raised if true, then it is a raised-bevel layout; if false, then 
     * it is a lowered-bevel border
     * @param property the property name
     * @param table the resource table.
     * @return the bevel border
     */
    public static Border createBevelBorder (boolean raised, 
                                            String property, 
                                            ResourceTable table) {
        return new BevelBorder (raised, property, table);
    }
    
    /**
     * Creates a bevel border.
     * 
     * The instance can not be shared by multiple widgets.
     * @param raised if true, then it is a raised-bevel layout; if false, then 
     * it is a lowered-bevel border
     * @param property the property name
     * @param associated the widget used to retrieve the resource table.
     * @return the bevel border
     */
    public static Border createBevelBorder (boolean raised, 
                                            String property, 
                                            Widget associated) {
        return new BevelBorder (raised, property, associated);
    }
    
    /**
     * Creates an image layout. The border is painted using a supplied Image. The image is split into 3x3 regions defined by insets.
     * The middle regions are tiled for supplying variable width and height of border. Central region is not painted.
     * The instance can be shared by multiple widgets.
     * @param insets the border insets
     * @param image the border image
     * @return the image border
     */
    public static Border createImageBorder (Insets insets, Image image) {
        return createImageBorder (insets, insets, image);
    }

    /**
     * Creates an image layout. The border is painted using a supplied Image. The image is split into 3x3 regions defined by imageInsets.
     * The middle regions are tiled for supplying variable width and height of border. Central region is not painted.
     * The insets of the border is specified by borderInsets.
     * The instance can be shared by multiple widgets.
     * @param borderInsets the border insets
     * @param imageInsets the image insets
     * @param image the border image
     * @return the image border
     */
    public static Border createImageBorder (Insets borderInsets, Insets imageInsets, Image image) {
        assert borderInsets != null  &&  imageInsets != null  &&  image != null;
        return new ImageBorder (borderInsets, imageInsets, image);
    }

    /**
     * Creates an rounded-rectangle border with a specified style. Insets are calculated from arcWidth and arcHeight.
     * The instance can be shared by multiple widgets.
     * @param arcWidth the arc width
     * @param arcHeight the arc height
     * @param fillColor the fill color
     * @param drawColor the draw color
     * @return the rounded border
     */
    public static Border createRoundedBorder (int arcWidth, int arcHeight, Color fillColor, Color drawColor) {
        return createRoundedBorder (arcWidth, arcHeight, arcWidth, arcHeight, fillColor, drawColor);
    }
    
    /**
     * Creates an rounded-rectangle border with a specified style. Insets are 
     * calculated from arcWidth and arcHeight.
     * 
     * The instance can not be shared by multiple widgets.
     * @param arcWidth the arc width
     * @param arcHeight the arc height
     * @param fillProperty the property name for the fill color
     * @param drawProperty the property name for the draw color
     * @param associated the widget used to retrieve the resource table.
     * @return the rounded border
     */
    public static Border createRoundedBorder (int arcWidth, int arcHeight, 
                                              String fillProperty,  String drawProperty,  
                                              Widget associated) {
        return createRoundedBorder (arcWidth, arcHeight, arcWidth, arcHeight, 
                                    fillProperty, drawProperty,
                                    associated);
    }
    
    /**
     * Creates an rounded-rectangle border with a specified style. Insets are 
     * calculated from arcWidth and arcHeight.
     * 
     * The instance can not be shared by multiple widgets.
     * @param arcWidth the arc width
     * @param arcHeight the arc height
     * @param fillProperty the property name for the fill color
     * @param drawProperty the property name for the draw color
     * @param table the resource table.
     * @return the rounded border
     */
    public static Border createRoundedBorder (int arcWidth, int arcHeight, 
                                              String fillProperty,  String drawProperty, 
                                              ResourceTable table) {
        return createRoundedBorder (arcWidth, arcHeight, arcWidth, arcHeight, 
                                    fillProperty, drawProperty,
                                    table);
    }
    
    /**
     * Creates an rounded-rectangle border with a specified style.
     * The instance can be shared by multiple widgets.
     * @param arcWidth the arc width
     * @param arcHeight the arc height
     * @param insetWidth the inset width
     * @param insetHeight the inset height
     * @param fillColor the fill color
     * @param drawColor the draw color
     * @return the rounded border
     */
    public static Border createRoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, Color fillColor, Color drawColor) {
        return new RoundedBorder (arcWidth, arcHeight, insetWidth, insetHeight, fillColor, drawColor);
    }
    
    /**
     * Creates an rounded-rectangle border with a specified style.
     * The instance can be shared by multiple widgets.
     * @param arcWidth the arc width
     * @param arcHeight the arc height
     * @param insetWidth the inset width
     * @param insetHeight the inset height
     * @param fillProperty the property name for the fill color
     * @param drawProperty the property name for the draw color
     * @param table the resource table.
     * @return the rounded border
     */
    public static Border createRoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, 
                                              String fillProperty,  String drawProperty, 
                                              ResourceTable table) 
    {
        return new RoundedBorder (arcWidth, arcHeight, insetWidth, insetHeight, 
                                  fillProperty, drawProperty,
                                  table);
    }
    
    /**
     * Creates an rounded-rectangle border with a specified style.
     * The instance can be shared by multiple widgets.
     * @param arcWidth the arc width
     * @param arcHeight the arc height
     * @param insetWidth the inset width
     * @param insetHeight the inset height
     * @param fillProperty the property name for the fill color
     * @param drawProperty the property name for the draw color
     * @param associated the widget used to retrieve the resource table.
     * @return the rounded border
     */
    public static Border createRoundedBorder (int arcWidth, int arcHeight, int insetWidth, int insetHeight, 
                                              String fillProperty,  String drawProperty,  
                                              Widget associated)
    {
        return new RoundedBorder (arcWidth, arcHeight, insetWidth, insetHeight, 
                                  fillProperty, drawProperty,
                                  associated);
    }
    
    /**
     * Creates a resize border. Usually used as resizing handles for ResizeAction. It renders a bounding rectangle with 8-direction squares.
     * The instance can be shared by multiple widgets.
     * @param thickness the thickness of the border
     * @return the resize border
     */
    public static Border createResizeBorder (int thickness) {
        return createResizeBorder (thickness, null, false);
    }
    
    /**
     * Creates a resize border. Usually used as resizing handles for ResizeAction. It renders a bounding rectangle with 8-direction squares.
     * The instance can be shared by multiple widgets.
     * @param thickness the thickness of the border
     * @param property the property name for the border color
     * @param associated the widget used to retrieve the resource table.
     * @return the resize border
     */
    public static Border createResizeBorder (int thickness, String property,  
                                              Widget associated) {
        return createResizeBorder (thickness, property, associated, false);
    }
    
    /**
     * Creates a resize border. Usually used as resizing handles for ResizeAction. It renders a bounding rectangle with 8-direction squares.
     * The instance can be shared by multiple widgets.
     * @param thickness the thickness of the border
     * @param property the property name for the border color
     * @param table the resource table.
     * @return the resize border
     */
    public static Border createResizeBorder (int thickness, String property,  
                                             ResourceTable table) {
        return createResizeBorder (thickness, property, table, false);
    }
    
    /**
     * Creates a resize border. Usually used as resizing handles for ResizeAction. It renders a bounding rectangle with 8-direction squares.
     * The instance can be shared by multiple widgets.
     * @param thickness the thickness of the border
     * @param color the border color
     * @param outer if true, then the rectangle encapsulate the squares too; if false, then the rectangle encapsulates the widget client area only
     * @return the resize border
     */
    public static Border createResizeBorder (int thickness, Color color, boolean outer) {
        return new ResizeBorder (thickness, color != null ? color : (new DefaultLookFeel()).getForeground()/*Color.BLACK*/, outer);
    }
    
    /**
     * Creates a resize border. Usually used as resizing handles for ResizeAction. It renders a bounding rectangle with 8-direction squares.
     * The instance can be shared by multiple widgets.
     * @param thickness the thickness of the border
     * @param property the property name for the border color
     * @param associated the widget used to retrieve the resource table.
     * @param outer if true, then the rectangle encapsulate the squares too; if false, then the rectangle encapsulates the widget client area only
     * @return the resize border
     */
    public static Border createResizeBorder (int thickness, String property,
                                             Widget associated, boolean outer) {
        return new ResizeBorder (thickness, property, associated, outer);
    }
    
    /**
     * Creates a resize border. Usually used as resizing handles for ResizeAction. It renders a bounding rectangle with 8-direction squares.
     * The instance can be shared by multiple widgets.
     * @param thickness the thickness of the border
     * @param property the property name for the border color
     * @param table the resource table.
     * @param outer if true, then the rectangle encapsulate the squares too; if false, then the rectangle encapsulates the widget client area only
     * @return the resize border
     */
    public static Border createResizeBorder (int thickness, String property,
                                             ResourceTable table, boolean outer) {
        return new ResizeBorder (thickness, property, table, outer);
    }
    
    /**
     * Creates a resize border rendered with dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param color the border color
     * @param width the inset width
     * @param height the inset height
     * @return the dashed border
     */
    public static Border createDashedBorder (Color color, int width, int height) {
        return createDashedBorder (color, width, height, false);
    }
    
    /**
     * Creates a resize border rendered with dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param property the property name for the border color
     * @param associated the widget used to retrieve the resource table.
     * @param width the inset width
     * @param height the inset height
     * @return the dashed border
     */
    public static Border createDashedBorder (String property, Widget associated,
                                             int width, int height) {
        return createDashedBorder (property, associated, width, height, false);
    }
    
    /**
     * Creates a resize border rendered with dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param property the property name for the border color
     * @param table the resource table.
     * @param width the inset width
     * @param height the inset height
     * @return the dashed border
     */
    public static Border createDashedBorder (String property, ResourceTable table,
                                             int width, int height) {
        return createDashedBorder (property, table, width, height, false);
    }
    
    /**
     * Creates a resize border rendered with dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param color  the border color
     * @param width  the inset width
     * @param height the inset height
     * @param squares the
     * @return the dashed border
     */
    public static Border createDashedBorder (Color color, int width, int height, boolean squares) {
        if (! squares)
            return new FancyDashedBorder (color != null ? color : (new DefaultLookFeel()).getForeground()/*Color.BLACK*/, width, height);
        else
            return new DashedBorder (color != null ? color : (new DefaultLookFeel()).getForeground()/*Color.BLACK*/, width, height);
    }
/**
     * Creates a resize border rendered with dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param property the property name
     * @param associated the widget used
     * @param width  the inset width
     * @param height the inset height
     * @param squares the
     * @return the dashed border
     */
    public static Border createDashedBorder (String property, Widget associated,
                                             int width, int height, boolean squares) {
        if (! squares)
            return new FancyDashedBorder (property, associated, width, height);
        else
            return new DashedBorder (property, associated, width, height);
    }
    
    /**
     * Creates a resize border rendered with dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param property the property name
     * @param table the resource table
     * @param width  the inset width
     * @param height the inset height
     * @param squares the
     * @return the dashed border
     */
    public static Border createDashedBorder (String property, ResourceTable table,
                                             int width, int height, boolean squares) {
        if (! squares)
            return new FancyDashedBorder (property, table, width, height);
        else
            return new DashedBorder (property, table, width, height);
    }
    
    /**
     * Creates a resize border rendered with fancy dashed stroke.
     * The instance can be shared by multiple widgets.
     * @param color the border color
     * @param width the inset width
     * @param height the inset height
     * @return the fancy dashed border
     * @deprecated use createDashedBorder (color, width, height, true) method instead
     */
    @Deprecated
    public static Border createFancyDashedBorder (Color color, int width, int height) {
        GeomUtil.LOG.warning ("BorderFactory.createFancyDashedBorder() method is deprecated. Use BorderFactory.createDashedBorder(color,width,height,true) method instead."); // NOI18N
        return new FancyDashedBorder (color != null ? color : (new DefaultLookFeel()).getForeground()/*Color.BLACK*/, width, height);
    }

}
