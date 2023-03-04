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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.Container;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import org.netbeans.modules.form.FormModelEvent;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.openide.nodes.Node;

/**
 * {@code GridInfoProvider} for {@code GrigBagLayout} layout manager.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class GridBagInfoProvider implements GridInfoProvider {
    /** Texture to mark components with size set to REMAINDER */
    private static final BufferedImage TILE_REMAINDER = GridUtils.loadBufferedImage("org/netbeans/modules/form/layoutsupport/griddesigner/resources/tile_remainder.png", false); // NOI18N
    /** Texture to mark components with size set to RELATIVE */
    private static final BufferedImage TILE_RELATIVE = GridUtils.loadBufferedImage("org/netbeans/modules/form/layoutsupport/griddesigner/resources/tile_relative.png", false); // NOI18N
    /** Thickness of textured line to mark components with size set to REMAINDER */
    private static final int THICKNESS_REMAINDER = 3;
    /** Thickness of textured line to mark components with size set to RELATIVE */
    private static final int THICKNESS_RELATIVE = 3;
    /** Transparency of color used to emphasize insets area, from 0=transparent to 255=opaque */
    private static final int INSETS_COLOR_ALPHA = 128;
    /** Transparency of color used to emphasize internal padding area, from 0=transparent to 255=opaque */
    private static final int IPADDING_COLOR_ALPHA = 64;
    
    private Container container;
    private LayoutSupportManager layoutManager;
    /**
     * {@code tempX} field of GridBagConstraints used to get real grid X
     * coordinate. We cannot use {@code gridx} field because it can contain
     * {@code RELATIVE} value.
     */
    private Field tempXField;
    /**
     * {@code tempY} field of GridBagConstraints used to get real grid Y
     * coordinate. We cannot use {@code gridy} field because it can contain
     * {@code RELATIVE} value.
     */
    private Field tempYField;
    /**
     * {@code tempWidth} field of GridBagConstraints used to get real grid width.
     * We cannot use {@code gridwidth} field because it can contain {@code RELATIVE}
     * or {@code REMAINDER} value.
     */
    private Field tempWidthField;
    /**
     * {@code tempHeight} field of GridBagConstraints used to get real grid height.
     * We cannot use {@code gridheight} field because it can contain {@code RELATIVE}
     * or {@code REMAINDER} value.
     */
    private Field tempHeightField;
    /**
     * Color to be used when painting insets etc. To be derived from current background.
     */
    private Color containerEmphColor;

    public GridBagInfoProvider(Container container, LayoutSupportManager layoutManager) {
        this.container = container;
        this.layoutManager = layoutManager;
        LayoutManager containerLayout = container.getLayout();
        if (!(containerLayout instanceof GridBagLayout)) {
            throw new IllegalArgumentException();
        }
        try {
            tempXField = GridBagConstraints.class.getDeclaredField("tempX"); // NOI18N
            tempXField.setAccessible(true);
            tempYField = GridBagConstraints.class.getDeclaredField("tempY"); // NOI18N
            tempYField.setAccessible(true);
            tempHeightField = GridBagConstraints.class.getDeclaredField("tempHeight"); // NOI18N
            tempHeightField.setAccessible(true);
            tempWidthField = GridBagConstraints.class.getDeclaredField("tempWidth"); // NOI18N
            tempWidthField.setAccessible(true);
        } catch (NoSuchFieldException nsfex) {
            FormUtils.LOGGER.log(Level.INFO, nsfex.getMessage(), nsfex);
        }
        containerEmphColor = deriveEmphColor(container);
    }

    private GridBagLayout getLayout() {
        return (GridBagLayout)container.getLayout();
    }

    public Object getLayoutProperty(String propertyName) {
        Node.Property property = layoutManager.getLayoutProperty(propertyName);
        Object layoutProp = null;
        try {
            layoutProp = property.getValue();
        } catch (IllegalAccessException iaex) {
            FormUtils.LOGGER.log(Level.WARNING, iaex.getMessage(), iaex);
        } catch (InvocationTargetException itex) {
            FormUtils.LOGGER.log(Level.WARNING, itex.getMessage(), itex);
        }
        return layoutProp;
    }

    public int getGapXArrayLength() {
        Object objXArray = getLayoutProperty("columnWidths");
        if(objXArray == null) {
            return 0;
        }
        if(objXArray instanceof int[]) {
            int gapXArray[] = (int[]) objXArray;
            return gapXArray.length;
        }
        return 0;
    }
    
    public int getGapYArrayLength() {
        Object objYArray = getLayoutProperty("rowHeights");
        if(objYArray == null) {
            return 0;
        }
        if(objYArray instanceof int[]) {
            int gapYArray[] = (int[]) objYArray;
            return gapYArray.length;
        }
        return 0;
    }
    
    /**
     * Checks whether GridBagLayout property columnWidths or rowHeights
     * is set to a valid value (i.e., value that can be decoded as representation
     * of gaps)
     *
     * @param obj current value of columnWidths or rowHeights GridBagLayout property
     * @returns true if obj is of type int[] and contains at least one value
     */
    public boolean isGapArray(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj instanceof int[]) {
            int arr[] = (int[]) obj;
            if(arr.length == 0) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hasGaps() {
        Object objXArray = getLayoutProperty("columnWidths");
        if(objXArray == null) return false;
        Object objYArray = getLayoutProperty("rowHeights");
        if(objYArray == null) return false;
        if(!isGapArray(objXArray)) return false;
        if(!isGapArray(objYArray)) return false;
        return true;
    }

    @Override
    public int getGapWidth() {
        Object objXArray = getLayoutProperty("columnWidths");
        if(objXArray == null) {
            return -1;
        }
        if(objXArray instanceof int[]) {
            int gapXArray[] = (int[]) objXArray;
            if(gapXArray.length < 2) {
                return -1;
            }
            return gapXArray[1];
        }
        return -1;
    }

    @Override
    public int getGapHeight() {
        Object objYArray = getLayoutProperty("rowHeights");
        if(objYArray == null) {
            return -1;
        }
        if(objYArray instanceof int[]) {
            int gapYArray[] = (int[]) objYArray;
            if(gapYArray.length < 2) {
                return -1;
            }
            return gapYArray[1];
        }
        return -1;
    }

    @Override
    public boolean isGapColumn(int columnIndex) {
        if( hasGaps() && (columnIndex % 2) == 1 && columnIndex >= 0 ) {
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isGapRow(int rowIndex) {
        if( hasGaps() && (rowIndex % 2) == 1 && rowIndex >= 0 ) {
            return true;
        }
        return false;
    }

    @Override
    public int getLastGapColumn() {
        int gapXArrayLength = getGapXArrayLength();
        return gapXArrayLength >=3 ? gapXArrayLength - 1 : -1;
    }

    @Override
    public int getLastGapRow() {
        int gapYArrayLength = getGapYArrayLength();
        return gapYArrayLength >=3 ? gapYArrayLength - 1 : -1;
    }
    
    @Override
    public boolean isGapEvent(FormModelEvent event) {
        if(event.getChangeType() == FormModelEvent.CONTAINER_LAYOUT_CHANGED) {
            String propName = event.getPropertyName();
            if ((propName != null) && (propName.equals("columnWidths") || propName.equals("rowHeights"))) { // NOI18N
                return true;
            }
        }
        return false;
    }

    @Override
    public int getX() {
        return getLayout().getLayoutOrigin().x;
    }

    @Override
    public int getY() {
        return getLayout().getLayoutOrigin().y;
    }

    @Override
    public int getWidth() {
        int[] widths = getLayout().getLayoutDimensions()[0];
        int sum = 0;
        for (int width : widths) {
            sum += width;
        }
        return sum;
    }

    @Override
    public int getHeight() {
        int[] heights = getLayout().getLayoutDimensions()[1];
        int sum = 0;
        for (int height : heights) {
            sum += height;
        }
        return sum;    }

    @Override
    public int getColumnCount() {
        return getLayout().getLayoutDimensions()[0].length;
    }

    @Override
    public int getRowCount() {
        return getLayout().getLayoutDimensions()[1].length;
    }

    @Override
    public int[] getColumnBounds() {
        int[] widths = getLayout().getLayoutDimensions()[0];
        int[] bounds = new int[widths.length+1];
        bounds[0] = getX();
        for (int i=0; i<widths.length; i++) {
            bounds[i+1] = bounds[i] + widths[i];
        }
        return bounds;
    }

    @Override
    public int[] getRowBounds() {
        int[] heights = getLayout().getLayoutDimensions()[1];
        int[] bounds = new int[heights.length+1];
        bounds[0] = getY();
        for (int i=0; i<heights.length; i++) {
            bounds[i+1] = bounds[i] + heights[i];
        }
        return bounds;
    }

    private int getIntFieldValue(Field intField, Object object) {
        int value = -1;
        try {
            value = intField.getInt(object);
        } catch (IllegalAccessException iaex) {
            FormUtils.LOGGER.log(Level.INFO, iaex.getMessage(), iaex);
        }
        return value;
    }

    @Override
    public int getGridX(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        int gridx = getIntFieldValue(tempXField, constraints);
        int columns = getColumnCount();
        return Math.min(gridx, columns-1); // See Issue 198519
    }

    public boolean getGridXRelative(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridx == GridBagConstraints.RELATIVE;
    }

    @Override
    public int getGridY(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        int gridy = getIntFieldValue(tempYField, constraints);
        int rows = getRowCount();
        return Math.min(gridy, rows-1); // See Issue 198519
    }

    public boolean getGridYRelative(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridy == GridBagConstraints.RELATIVE;
    }

    @Override
    public int getGridWidth(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        int gridWidth = getIntFieldValue(tempWidthField, constraints);
        int columns = getColumnCount();
        int gridx = getGridX(component);
        return Math.min(gridWidth, columns-gridx); // See Issue 198519
    }

    public boolean getGridWidthRelative(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridwidth == GridBagConstraints.RELATIVE;
    }

    public boolean getGridWidthRemainder(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridwidth == GridBagConstraints.REMAINDER;
    }

    @Override
    public int getGridHeight(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        int gridHeight = getIntFieldValue(tempHeightField, constraints);
        int rows = getRowCount();
        int gridy = getGridY(component);
        return Math.min(gridHeight, rows-gridy); // See Issue 198519
    }

    public boolean getGridHeightRelative(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridheight == GridBagConstraints.RELATIVE;
    }

    public boolean getGridHeightRemainder(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.gridheight == GridBagConstraints.REMAINDER;
    }

    public int getAnchor(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.anchor;
    }

    public int getFill(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.fill;
    }

    public double getWeightX(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.weightx;
    }

    public double getWeightY(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.weighty;
    }

    public int getIPadX(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.ipadx;
    }

    public int getIPadY(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.ipady;
    }

    public Insets getInsets(Component component) {
        GridBagConstraints constraints = getLayout().getConstraints(component);
        return constraints.insets;
    }

    @Override
    public void paintConstraints(Graphics g, Component component, boolean selected) {
        assert g instanceof Graphics2D;
        Graphics2D gg = (Graphics2D) g.create();
        Color emphColor = containerEmphColor;

        // component proxy position and size
        Rectangle inner = component.getBounds();
        // grid cell position and size
        int[] columnBounds = getColumnBounds();
        int[] rowBounds = getRowBounds();
        int gridX = getGridX(component);
        int gridY = getGridY(component);
        int gridWidth = getGridWidth(component);
        int gridHeight = getGridHeight(component);
        Rectangle outer = new Rectangle();
        outer.x = columnBounds[gridX];
        outer.width = columnBounds[gridX + gridWidth] - outer.x;
        outer.y = rowBounds[gridY];
        outer.height = rowBounds[gridY + gridHeight] - outer.y;
        
        // display the area covered by insets
        Insets insets = getInsets(component);
        if ( insets.top > 0 || insets.left > 0 || insets.bottom > 0 || insets.right > 0 ) {
            Color emphColorTransparent = new Color(emphColor.getRed(), emphColor.getGreen(), emphColor.getBlue(), INSETS_COLOR_ALPHA);
            if( insets.top > 0 ) {
                gg.setColor(emphColorTransparent);
                gg.fillRect(inner.x - insets.left, inner.y - insets.top, inner.width + insets.left + insets.right, insets.top);
                gg.setColor(emphColor);
                gg.drawLine(inner.x - insets.left, inner.y - insets.top, inner.x + inner.width + insets.right, inner.y - insets.top);
                gg.drawLine(inner.x - insets.left, inner.y - insets.top, inner.x, inner.y);
                gg.drawLine(inner.x + inner.width + insets.right, inner.y - insets.top, inner.x + inner.width, inner.y);
            }
            if( insets.bottom > 0 ) {
                gg.setColor(emphColorTransparent);
                gg.fillRect(inner.x - insets.left, inner.y + inner.height, inner.width + insets.left + insets.right, insets.bottom);
                gg.setColor(emphColor);
                gg.drawLine(inner.x - insets.left, inner.y + inner.height + insets.bottom, inner.x + inner.width + insets.right, inner.y + inner.height + insets.bottom);
                gg.drawLine(inner.x - insets.left, inner.y + inner.height + insets.bottom, inner.x, inner.y + inner.height);
                gg.drawLine(inner.x + inner.width + insets.right, inner.y + inner.height + insets.bottom, inner.x + inner.width, inner.y + inner.height);
            }
            if( insets.left > 0 ) {
                gg.setColor(emphColorTransparent);
                gg.fillRect(inner.x - insets.left, inner.y, insets.left, inner.height);
                gg.setColor(emphColor);
                gg.drawLine(inner.x - insets.left, inner.y - insets.top, inner.x - insets.left, inner.y + inner.height + insets.bottom);
                if( insets.top <= 0 ) gg.drawLine(inner.x - insets.left, inner.y, inner.x, inner.y);
                if( insets.bottom <= 0 ) gg.drawLine(inner.x - insets.left, inner.y + inner.height, inner.x, inner.y + inner.height);
            }
            if( insets.right > 0 ) {
                gg.setColor(emphColorTransparent);
                gg.fillRect(inner.x + inner.width, inner.y, insets.right, inner.height);
                gg.setColor(emphColor);
                gg.drawLine(inner.x + inner.width + insets.right, inner.y - insets.top, inner.x + inner.width + insets.right, inner.y + inner.height + insets.bottom);
                if( insets.top <= 0 ) gg.drawLine(inner.x + inner.width + insets.right, inner.y, inner.x + inner.width, inner.y);
                if( insets.bottom <= 0 ) gg.drawLine(inner.x + inner.width + insets.right, inner.y + inner.height, inner.x + inner.width, inner.y + inner.height);
            }
        }
        
        // mark REMAINDER component size
        boolean hRemainder = getGridWidthRemainder(component);
        boolean vRemainder = getGridHeightRemainder(component);
        if( hRemainder || vRemainder ) {
            Rectangle remainderTextureRectangle = new Rectangle(0, 0, TILE_REMAINDER.getWidth(), TILE_REMAINDER.getHeight());
            Paint remainderTexture = new TexturePaint(TILE_REMAINDER, remainderTextureRectangle);
            gg.setPaint(remainderTexture);
            if( hRemainder && ( outer.width >= THICKNESS_REMAINDER ) ) {
                gg.fillRect(outer.x + outer.width - THICKNESS_REMAINDER, outer.y, THICKNESS_REMAINDER, outer.height);
            }
            if( vRemainder && ( outer.height >= THICKNESS_REMAINDER ) ) {
                gg.fillRect(outer.x, outer.y + outer.height - THICKNESS_REMAINDER, outer.width, THICKNESS_REMAINDER);
            }
        }

        // mark RELATIVE component size
        boolean hRelative = getGridWidthRelative(component);
        boolean vRelative = getGridHeightRelative(component);
        if( hRelative || vRelative ) {
            Rectangle relativeTextureRectangle = new Rectangle(0, 0, TILE_RELATIVE.getWidth(), TILE_RELATIVE.getHeight());
            Paint relativeTexture = new TexturePaint(TILE_RELATIVE, relativeTextureRectangle);
            gg.setPaint(relativeTexture);
            if( hRelative && ( outer.width >= THICKNESS_RELATIVE ) ) {
                gg.fillRect(outer.x + outer.width - THICKNESS_RELATIVE, outer.y, THICKNESS_RELATIVE, outer.height);
            }
            if( vRelative && ( outer.height >= THICKNESS_RELATIVE ) ) {
                gg.fillRect(outer.x, outer.y + outer.height - THICKNESS_RELATIVE, outer.width, THICKNESS_RELATIVE);
            }
        }

        // mark internal padding
        int hPad = getIPadX(component);
        int vPad = getIPadY(component);
        if( hPad > 0 || vPad > 0 ) {
            Color padEmphColor = deriveEmphColor(component);
            gg.setColor(new Color(padEmphColor.getRed(), padEmphColor.getGreen(), padEmphColor.getBlue(), IPADDING_COLOR_ALPHA));
            int padTop = vPad / 2;
            int padBottom = vPad - padTop;
            int padLeft = hPad / 2;
            int padRight = hPad - padLeft;
            if( padTop > 0 ) {
                gg.fillRect(inner.x, inner.y, inner.width, padTop);
            }
            if( padBottom > 0 ) {
                gg.fillRect(inner.x, inner.y + inner.height-padBottom, inner.width, padBottom);
            }
            if( padLeft > 0 ) {
                gg.fillRect(inner.x, inner.y + padTop, padLeft, inner.height - padTop - padBottom);
            }
            if( padRight > 0 ) {
                gg.fillRect(inner.x + inner.width - padRight, inner.y + padTop, padRight, inner.height - padTop - padBottom);
            }
        }
        gg.dispose();
    }
  
    private static Color deriveEmphColor(Component component) {
        // derive the color for emphasizing from background
        Color backColor = component.getBackground();
        Color emphColor;
        int backBrightness = (30*backColor.getRed()+59*backColor.getGreen()+11*backColor.getBlue())/100;
        if (backBrightness >= 128) {
            emphColor = backColor.darker();
        } else { // brightening a dark area seems visually less notable than darkening a bright area
            emphColor = backColor.brighter().brighter();
        }
        return emphColor;
    }

}
