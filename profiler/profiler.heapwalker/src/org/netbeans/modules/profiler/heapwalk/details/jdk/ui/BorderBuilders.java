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
package org.netbeans.modules.profiler.heapwalk.details.jdk.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.ColorBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.FontBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.IconBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.InsetsBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.Utils.InstanceBuilder;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;

/**
 *
 * @author Jiri Sedlacek
 */
final class BorderBuilders {
    
    static BorderBuilder fromField(Instance instance, String field, boolean uiresource, Heap heap) {
        Object _border = instance.getValueOfField(field);
        if (!(_border instanceof Instance)) return null;

        Instance border = (Instance)_border;

        // Make sure subclasses are listed before base class if using isSubclassOf
        if (DetailsUtils.isSubclassOf(border, BevelBorder.class.getName())) {
            return new BevelBorderBuilder(border, heap);
        } else if (DetailsUtils.isSubclassOf(border, MatteBorder.class.getName())) { // Must be before EmptyBorder (extends EmptyBorder)
            return new EmptyBorderBuilder(border, heap);
        } else if (DetailsUtils.isSubclassOf(border, EmptyBorder.class.getName())) {
            return new MatteBorderBuilder(border, heap);
        } else if (DetailsUtils.isSubclassOf(border, EtchedBorder.class.getName())) {
            return new EtchedBorderBuilder(border, heap);
        } else if (DetailsUtils.isSubclassOf(border, LineBorder.class.getName())) {
            return new LineBorderBuilder(border, heap);
        } else if (DetailsUtils.isSubclassOf(border, TitledBorder.class.getName())) {
            return new TitledBorderBuilder(border, heap);
        } else if (DetailsUtils.isSubclassOf(border, CompoundBorder.class.getName())) {
            return new CompoundBorderBuilder(border, heap);
        }

        return null;
    }
    
    abstract static class BorderBuilder extends InstanceBuilder<Border> {
        private final boolean isUIResource;
        BorderBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            this.isUIResource = instance.getJavaClass().getName().
                    startsWith("javax.swing.plaf.BorderUIResource$");
        }
        boolean isUIResource() {
            return isUIResource;
        }
    }
    
    private static class BevelBorderBuilder extends BorderBuilder {
        
        private final int bevelType;
        private final ColorBuilder highlightOuter;
        private final ColorBuilder highlightInner;
        private final ColorBuilder shadowInner;
        private final ColorBuilder shadowOuter;
        
        BevelBorderBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            bevelType = DetailsUtils.getIntFieldValue(instance, "bevelType", BevelBorder.LOWERED);
            highlightOuter = ColorBuilder.fromField(instance, "highlightOuter", heap);
            highlightInner = ColorBuilder.fromField(instance, "highlightInner", heap);
            shadowInner = ColorBuilder.fromField(instance, "shadowInner", heap);
            shadowOuter = ColorBuilder.fromField(instance, "shadowOuter", heap);
        }
        
        protected Border createInstanceImpl() {
            if (highlightOuter == null && shadowInner == null) {
                if (highlightInner == null && shadowOuter == null) {
                    return BorderFactory.createBevelBorder(bevelType);
                } else {
                    return BorderFactory.createBevelBorder(bevelType,
                            highlightInner.createInstance(), shadowOuter.createInstance());
                }
            } else {
                return BorderFactory.createBevelBorder(bevelType,
                        highlightOuter.createInstance(), highlightInner.createInstance(),
                        shadowOuter.createInstance(), shadowInner.createInstance());
            }
        }
        
    }
    
    private static class EmptyBorderBuilder extends BorderBuilder {
        
        private final InsetsBuilder insets;
        
        EmptyBorderBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            insets = new InsetsBuilder(instance, heap);
        }
        
        protected Border createInstanceImpl() {
            Insets i = insets.createInstance();
            if (i.top == 0 && i.left == 0 && i.bottom == 0 && i.right == 0) {
                return BorderFactory.createEmptyBorder();
            } else {
                return BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
            }
        }
        
    }
    
    private static class MatteBorderBuilder extends BorderBuilder {
        
        private final InsetsBuilder insets;
        private final ColorBuilder color;
        private final IconBuilder tileIcon;
        
        MatteBorderBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            insets = new InsetsBuilder(instance, heap);
            color = ColorBuilder.fromField(instance, "color", heap);
            tileIcon = IconBuilder.fromField(instance, "tileIcon", heap);
        }
        
        protected Border createInstanceImpl() {
            Insets i = insets.createInstance();
            if (color == null) {
                return BorderFactory.createMatteBorder(i.top, i.left, i.bottom,
                        i.right, tileIcon == null ? null : tileIcon.createInstance());
            } else {
                return BorderFactory.createMatteBorder(i.top, i.left, i.bottom,
                        i.right, color.createInstance());
            }
        }
        
    }
    
    private static class EtchedBorderBuilder extends BorderBuilder {
        
        private final int etchType;
        private final ColorBuilder highlight;
        private final ColorBuilder shadow;
        
        EtchedBorderBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            etchType = DetailsUtils.getIntFieldValue(instance, "etchType", EtchedBorder.LOWERED);
            highlight = ColorBuilder.fromField(instance, "highlight", heap);
            shadow = ColorBuilder.fromField(instance, "shadow", heap);
        }
        
        protected Border createInstanceImpl() {
            if (highlight == null && shadow == null) {
                return BorderFactory.createEtchedBorder(etchType);
            } else {
                return BorderFactory.createEtchedBorder(etchType,
                        highlight == null ? null : highlight.createInstance(),
                        shadow == null ? null : shadow.createInstance());
            }
        }
        
    }
    
    private static class LineBorderBuilder extends BorderBuilder {
        
        private final int thickness;
        private final ColorBuilder lineColor;
        private final boolean roundedCorners;
        
        LineBorderBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            thickness = DetailsUtils.getIntFieldValue(instance, "thickness", 1);
            lineColor = ColorBuilder.fromField(instance, "lineColor", heap);
            roundedCorners = DetailsUtils.getBooleanFieldValue(instance, "roundedCorners", false);
        }
        
        protected Border createInstanceImpl() {
            Color c = lineColor == null ? null : lineColor.createInstance();
            if (c == null) c = Color.BLACK;
            if (roundedCorners) {
                return new LineBorder(c, thickness, roundedCorners);
            } else if (thickness == 1) {
                return BorderFactory.createLineBorder(c);
            } else {
                return BorderFactory.createLineBorder(c, thickness);
            }
        }
        
    }
    
    private static class TitledBorderBuilder extends BorderBuilder {
        
        private final String title;
        private final BorderBuilder border;
        private final int titlePosition;
        private final int titleJustification;
        private final FontBuilder titleFont;
        private final ColorBuilder titleColor;
        
        TitledBorderBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            title = Utils.getFieldString(instance, "title");
            border = fromField(instance, "border", false, heap);
            titlePosition = DetailsUtils.getIntFieldValue(instance, "titlePosition", TitledBorder.DEFAULT_POSITION);
            titleJustification = DetailsUtils.getIntFieldValue(instance, "titleJustification", TitledBorder.LEADING);
            titleFont = FontBuilder.fromField(instance, "titleFont", heap);
            titleColor = ColorBuilder.fromField(instance, "titleColor", heap);
        }
        
        protected Border createInstanceImpl() {
            Font font = titleFont == null || titleFont.isUIResource() ?
                        null : titleFont.createInstance();
            Color color = titleColor == null || titleColor.isUIResource() ?
                        null : titleColor.createInstance();
            
            return new TitledBorder(border == null ? null : border.createInstance(),
                    title, titleJustification, titlePosition, font, color);
        }
        
    }
    
    private static class CompoundBorderBuilder extends BorderBuilder {
        
        private final BorderBuilder outsideBorder;
        private final BorderBuilder insideBorder;
        
        CompoundBorderBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            outsideBorder = fromField(instance, "outsideBorder", true, heap);
            insideBorder = fromField(instance, "insideBorder", true, heap);
        }
        
        protected Border createInstanceImpl() {
            Border outside = outsideBorder == null || outsideBorder.isUIResource() ?
                             null : outsideBorder.createInstance();
            Border inside = insideBorder == null || insideBorder.isUIResource() ?
                             null : insideBorder.createInstance();
            if (outside == null && inside == null) {
                return BorderFactory.createEmptyBorder();
            } else if (outside == null || inside == null) {
                if (outside == null) return inside;
                else return outside;
            } else {
                return BorderFactory.createCompoundBorder(outside, inside);
            }
        }
        
    }
    
}
