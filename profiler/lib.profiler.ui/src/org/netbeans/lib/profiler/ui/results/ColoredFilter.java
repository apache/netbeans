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
package org.netbeans.lib.profiler.ui.results;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.Objects;
import java.util.Properties;
import javax.swing.Icon;
import org.netbeans.lib.profiler.filters.GenericFilter;

/**
 *
 * @author Jiri Sedlacek
 */
public class ColoredFilter extends GenericFilter {
    
    private static final String PROP_COLOR = "COLOR"; // NOI18N
    
    private Color color;
    private transient Icon icon;
    
    
    public ColoredFilter(ColoredFilter other) {
        super(other);
        
        this.color = other.color;
    }
    
    public ColoredFilter(String name, String value, Color color) {
        super(name, value, TYPE_INCLUSIVE);
        
        this.color = color;
    }
    
    public ColoredFilter(Properties properties, String id) {
        super(properties, id);
        
        color = loadColor(properties, id);
    }
    
    
    public void copyFrom(ColoredFilter other) {
        super.copyFrom(other);
        
        color = other.color;
    }
    
    
    public final void setColor(Color color) {
        this.color = color;
    }
    
    public final Color getColor() {
        return color;
    }
    
    
    public final Icon getIcon(int width, int height) {
        if (icon == null || icon.getIconWidth() != width || icon.getIconHeight() != height) {
            final int w = Math.max(16, width);
            final int h = Math.max(16, height);
            final int ww = width;
            final int hh = height;
            final int wo = ww >= 16 ? 0 : (16 - ww) / 2;
            final int ho = hh >= 16 ? 0 : (16 - hh) / 2;
            icon = new Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (color == null) {
                        g.setColor(Color.BLACK);
                        g.drawLine(x + wo, y + ho, x + wo + ww, y + ho + hh);
                        g.drawLine(x + wo + ww, y + ho, x + wo, y + ho + hh);
                        g.drawRect(x + wo, y + ho, ww, hh);
                    } else {
                        g.setColor(color);
                        g.fillRect(x + wo, y + ho, ww, hh);
                        g.setColor(Color.BLACK);
                        g.drawRect(x + wo, y + ho, ww, hh);
                    }
                }
                public int getIconWidth() {
                    return w;
                }
                public int getIconHeight() {
                    return h;
                }
            };
        }
        return icon;
    }
    
    
    protected String[] computeValues(String value) {
        return super.computeValues(value.replace('*', ' ')); // NOI18N
    }
    
    
    public boolean passes(String string) {
        if (simplePasses(string)) return true;

        String[] values = getValues();
        for (int i = 0; i < values.length; i++)
            if (string.startsWith(values[i]))
                return true;
        
        return false;
    }
    
    
    protected boolean valuesEquals(Object obj) {
        if (!super.valuesEquals(obj)) return false;
        
        ColoredFilter other = (ColoredFilter)obj;
        if (!Objects.equals(color, other.color)) return false;
        
        return true;
    }
    
    protected int valuesHashCode(int hashBase) {
        hashBase = super.valuesHashCode(hashBase);
        
        if (color != null) hashBase = 67 * hashBase + color.hashCode();
        
        return hashBase;
    }
    
    
    public void store(Properties properties, String id) {
        super.store(properties, id);
        if (color != null) properties.put(id + PROP_COLOR, Integer.toString(color.getRGB()));
    }
    
    
    private static Color loadColor(Properties properties, String id) {
        String _color = properties.getProperty(id + PROP_COLOR);
        if (_color == null) return null;
        
        try {
            int _colorI = Integer.parseInt(_color);
            return new Color(_colorI);
        } catch (NumberFormatException e) {
            throw new InvalidFilterIdException("Bad color code specified", id); // NOI18N
        }
    }
    
}
