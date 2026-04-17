/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.jdk.image.ImageBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.Utils.InstanceBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.Utils.PlaceholderIcon;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jiri Sedlacek
 */
final class BaseBuilders {
    
    static final class PointBuilder extends InstanceBuilder<Point> {
        
        private final int x;
        private final int y;
        
        PointBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            x = DetailsUtils.getIntFieldValue(instance, "x", 0);
            y = DetailsUtils.getIntFieldValue(instance, "y", 0);
        }
        
        static PointBuilder fromField(Instance instance, String field, Heap heap) {
            Object point = instance.getValueOfField(field);
            if (!(point instanceof Instance)) return null;
            return new PointBuilder((Instance)point, heap);
        }
        
        protected Point createInstanceImpl() {
            return new Point(x, y);
        }
        
    }
    
    static final class DimensionBuilder extends InstanceBuilder<Dimension> {
        
        private final int width;
        private final int height;
        
        DimensionBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            width = DetailsUtils.getIntFieldValue(instance, "width", 0);
            height = DetailsUtils.getIntFieldValue(instance, "height", 0);
        }
        
        static DimensionBuilder fromField(Instance instance, String field, Heap heap) {
            Object dimension = instance.getValueOfField(field);
            if (!(dimension instanceof Instance)) return null;
            return new DimensionBuilder((Instance)dimension, heap);
        }
        
        protected Dimension createInstanceImpl() {
            return new Dimension(width, height);
        }
        
    }
    
    static final class RectangleBuilder extends InstanceBuilder<Rectangle> {
        
        private final PointBuilder point;
        private final DimensionBuilder dimension;
        
        RectangleBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            point = new PointBuilder(instance, heap);
            dimension = new DimensionBuilder(instance, heap);
        }
        
        static RectangleBuilder fromField(Instance instance, String field, Heap heap) {
            Object rectangle = instance.getValueOfField(field);
            if (!(rectangle instanceof Instance)) return null;
            return new RectangleBuilder((Instance)rectangle, heap);
        }
        
        protected Rectangle createInstanceImpl() {
            return new Rectangle(point.createInstance(), dimension.createInstance());
        }
        
    }
    
    static final class InsetsBuilder extends InstanceBuilder<Insets> {
        
        private final int top;
        private final int left;
        private final int bottom;
        private final int right;
        
        InsetsBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            top = DetailsUtils.getIntFieldValue(instance, "top", 0);
            left = DetailsUtils.getIntFieldValue(instance, "left", 0);
            bottom = DetailsUtils.getIntFieldValue(instance, "bottom", 0);
            right = DetailsUtils.getIntFieldValue(instance, "right", 0);
        }
        
        static InsetsBuilder fromField(Instance instance, String field, Heap heap) {
            Object insets = instance.getValueOfField(field);
            if (!(insets instanceof Instance)) return null;
            return new InsetsBuilder((Instance)insets, heap);
        }
        
        protected Insets createInstanceImpl() {
            return new Insets(top, left, bottom, right);
        }
        
    }
    
    static final class FontBuilder extends InstanceBuilder<Font> {
        
        private final String name;
        private final int style;
        private final int size;
        private final boolean isUIResource;
        
        FontBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            name = Utils.getFontName(instance, heap);
            style = DetailsUtils.getIntFieldValue(instance, "style", 0);
            size = DetailsUtils.getIntFieldValue(instance, "size", 10);
            isUIResource = DetailsUtils.isSubclassOf(instance, "javax.swing.plaf.FontUIResource");
        }
        
        boolean isUIResource() {
            return isUIResource;
        }
        
        static FontBuilder fromField(Instance instance, String field, Heap heap) {
            Object font = instance.getValueOfField(field);
            if (!(font instanceof Instance)) return null;
            return new FontBuilder((Instance)font, heap);
        }
        
        protected Font createInstanceImpl() {
            return new Font(name, style, size);
        }
        
    }
    
    static final class ColorBuilder extends InstanceBuilder<Color> {
        
        private final int value;
        private final boolean isUIResource;
        
        ColorBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            value = DetailsUtils.getIntFieldValue(instance, "value", 0);
            isUIResource = DetailsUtils.isSubclassOf(instance, "javax.swing.plaf.ColorUIResource") ||
                           DetailsUtils.isSubclassOf(instance, "javax.swing.plaf.nimbus.DerivedColor$UIResource");
        }
        
        boolean isUIResource() {
            return isUIResource;
        }
        
        static ColorBuilder fromField(Instance instance, String field, Heap heap) {
            Object color = instance.getValueOfField(field);
            if (!(color instanceof Instance)) return null;            
            return new ColorBuilder((Instance)color, heap);
        }
        
        protected Color createInstanceImpl() {
            return new Color(value);
        }
        
    }
    
    static final class IconBuilder extends InstanceBuilder<Icon> {
        
        private final int width;
        private final int height;
        private final Image image;
        
        IconBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            width = DetailsUtils.getIntFieldValue(instance, "width", 0);
            height = DetailsUtils.getIntFieldValue(instance, "height", 0);
            image = ImageBuilder.buildImage(instance, heap);
        }
        
        static IconBuilder fromField(Instance instance, String field, Heap heap) {
            Object icon = instance.getValueOfField(field);
            if (!(icon instanceof Instance)) return null;
            if (!DetailsUtils.isSubclassOf((Instance)icon, ImageIcon.class.getName())) return null;
            return new IconBuilder((Instance)icon, heap);
        }
        
        protected Icon createInstanceImpl() {
            if(image == null) {
                    return new PlaceholderIcon(width, height);
            }
            return ImageUtilities.image2Icon(image);
        }
        
    }
    
}
