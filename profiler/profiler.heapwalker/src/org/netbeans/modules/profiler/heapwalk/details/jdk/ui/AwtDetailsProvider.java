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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.ColorBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.DimensionBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.FontBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.InsetsBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.PointBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.RectangleBuilder;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=DetailsProvider.class)
public final class AwtDetailsProvider extends DetailsProvider.Basic {
    
    private static final String FONT_MASK = "java.awt.Font+";                   // NOI18N
    private static final String COLOR_MASK = "java.awt.Color+";                 // NOI18N
    private static final String POINT_MASK = "java.awt.Point+";                 // NOI18N
    private static final String DIMENSION_MASK = "java.awt.Dimension+";         // NOI18N
    private static final String RECTANGLE_MASK = "java.awt.Rectangle+";         // NOI18N
    private static final String INSETS_MASK = "java.awt.Insets+";               // NOI18N
    private static final String TEXTATTRIBUTE_MASK = "java.text.AttributedCharacterIterator$Attribute+"; // NOI18N
    private static final String CURSOR_MASK = "java.awt.Cursor+";               // NOI18N
    
    public AwtDetailsProvider() {
        super(FONT_MASK, COLOR_MASK, POINT_MASK, DIMENSION_MASK,
              RECTANGLE_MASK, INSETS_MASK, TEXTATTRIBUTE_MASK, CURSOR_MASK);
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (FONT_MASK.equals(className)) {                                      // Font+
            String name = Utils.getFontName(instance, heap);
            if (name == null) name = "Default";                                 // NOI18N
            int size = DetailsUtils.getIntFieldValue(instance, "size", 10);     // NOI18N // TODO: should use default font size
            name += ", " + size + "pt";                                         // NOI18N
            int style = DetailsUtils.getIntFieldValue(instance, "style", 0);    // NOI18N
            if ((style & 1) != 0) name += ", bold";                             // NOI18N
            if ((style & 2) != 0) name += ", italic";                           // NOI18N
            return name;
        } else if (COLOR_MASK.equals(className)) {                              // Color+
            Color color = new ColorBuilder(instance, heap).createInstance();
            return color.getRed() + ", " + color.getGreen() +                   // NOI18N
                   ", " + color.getBlue() + ", " + color.getAlpha();            // NOI18N
        } else if (POINT_MASK.equals(className)) {                              // Point+
            Point point = new PointBuilder(instance, heap).createInstance();
            return point.x + ", " + point.y;                                    // NOI18N
        } else if (DIMENSION_MASK.equals(className)) {                          // Dimension+
            Dimension dimension = new DimensionBuilder(instance, heap).createInstance();
            return dimension.width + ", " + dimension.height;                   // NOI18N
        } else if (RECTANGLE_MASK.equals(className)) {                          // Rectangle+
            Rectangle rectangle = new RectangleBuilder(instance, heap).createInstance();
            return rectangle.x + ", " + rectangle.y +                           // NOI18N
                   ", " + rectangle.width + ", " + rectangle.height;            // NOI18N
        } else if (INSETS_MASK.equals(className)) {                             // Insets+
            Insets insets = new InsetsBuilder(instance, heap).createInstance();
            return insets.top + ", " + insets.left +                            // NOI18N
                   ", " + insets.bottom + ", " + insets.right;                  // NOI18N
        } else if (TEXTATTRIBUTE_MASK.equals(className) ||                      // AttributedCharacterIterator$Attribute+
                   CURSOR_MASK.equals(className)) {                             // Cursor+
            return DetailsUtils.getInstanceFieldString(
                    instance, "name", heap);                                    // NOI18N
        }
        return null;
    }
    
    public View getDetailsView(String className, Instance instance, Heap heap) {
        if (FONT_MASK.equals(className)) {                                      // Font+
            return new FontView(instance, heap);
        } else if (COLOR_MASK.equals(className)) {                              // Color+
            return new ColorView(instance, heap);
        }
        return null;
    }
    
    @NbBundle.Messages({
        "FontView_Preview=ABCabc123"
    })
    private static class FontView extends Utils.View<FontBuilder> {
        
        FontView(Instance instance, Heap heap) {
            super(0, false, true, instance, heap);
        }
        
        protected FontBuilder getBuilder(Instance instance, Heap heap) {
            return new FontBuilder(instance, heap);
        }
        
        protected Component getComponent(FontBuilder builder) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(builder.createInstance());
            label.setText(Bundle.FontView_Preview());
            return label;
        }
        
    }
    
    private static class ColorView extends Utils.View<ColorBuilder> {
        
        ColorView(Instance instance, Heap heap) {
            super(0, true, true, instance, heap);
        }
        
        protected ColorBuilder getBuilder(Instance instance, Heap heap) {
            return new ColorBuilder(instance, heap);
        }
        
        protected Component getComponent(ColorBuilder builder) {
            final Color color = builder.createInstance();
            JPanel panel = new JPanel(null) {
                public void paint(Graphics g) {
                    g.setColor(color);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            panel.setOpaque(false);
            return panel;
        }
        
    }
    
}
