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

package org.openide.awt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Parameters;
import org.openide.util.VectorIcon;

/**
 * An icon that paints a small arrow to the right of the provided icon.
 * 
 * @author S. Aubrecht
 * @since 6.11
 */
class IconWithArrow implements Icon {
    private Icon orig;
    private Icon arrow;
    private boolean paintRollOver;
    
    private static final int GAP = 6;
    
    /** Creates a new instance of IconWithArrow */
    public IconWithArrow(  Icon orig, boolean paintRollOver, boolean disabledArrow ) {
        Parameters.notNull("original icon", orig); //NOI18N
        this.orig = orig;
        this.paintRollOver = paintRollOver;
        this.arrow = disabledArrow ? ArrowIcon.INSTANCE_DISABLED : ArrowIcon.INSTANCE_DEFAULT;
    }

    @Override
    public void paintIcon( Component c, Graphics g, int x, int y ) {
        int height = getIconHeight();
        orig.paintIcon( c, g, x, y+(height-orig.getIconHeight())/2 );
        
        arrow.paintIcon( c, g, x+GAP+orig.getIconWidth(), y+(height-arrow.getIconHeight())/2 );
        
        if( paintRollOver ) {
            Color brighter = UIManager.getColor( "controlHighlight" ); //NOI18N
            Color darker = UIManager.getColor( "controlShadow" ); //NOI18N
            if( null == brighter || null == darker ) {
                brighter = c.getBackground().brighter();
                darker = c.getBackground().darker();
            }
            if( null != brighter && null != darker ) {
                g.setColor( brighter );
                g.drawLine( x+orig.getIconWidth()+1, y, 
                            x+orig.getIconWidth()+1, y+getIconHeight() );
                g.setColor( darker );
                g.drawLine( x+orig.getIconWidth()+2, y, 
                            x+orig.getIconWidth()+2, y+getIconHeight() );
            }
        }
    }

    @Override
    public int getIconWidth() {
        return orig.getIconWidth() + GAP + arrow.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return Math.max( orig.getIconHeight(), arrow.getIconHeight() );
    }

    public static int getArrowAreaWidth() {
        return GAP/2 + 5;
    }

    static class ArrowIcon extends VectorIcon {
        public static final Icon INSTANCE_DEFAULT = new ArrowIcon(false);
        public static final Icon INSTANCE_DISABLED = new ArrowIcon(true);
        private final boolean disabled;

        private ArrowIcon(boolean disabled) {
          super(5, 4);
          this.disabled = disabled;
        }

        @Override
        protected void paintIcon(Component c, Graphics2D g, int width, int height, double scaling) {
            final Color color;
            if (UIManager.getBoolean("nb.dark.theme")) {
              // Foreground brightness level taken from the combobox dropdown on Darcula.
              color = disabled ? new Color(90, 90, 90, 255) : new Color(187, 187, 187, 255);
            } else {
              color = disabled ? new Color(201, 201, 201, 255) : new Color(86, 86, 86, 255);
            }
            g.setColor(color);
            final double overshoot = 2.0 / scaling;
            final double arrowWidth = width + overshoot * scaling;
            final double arrowHeight = height - 0.2 * scaling;
            final double arrowMidX = arrowWidth / 2.0 - (overshoot / 2.0) * scaling;
            g.clipRect(0, 0, width, height);
            Path2D.Double arrowPath = new Path2D.Double();
            arrowPath.moveTo(arrowMidX - arrowWidth / 2.0, 0);
            arrowPath.lineTo(arrowMidX, arrowHeight);
            arrowPath.lineTo(arrowMidX + arrowWidth / 2.0, 0);
            arrowPath.closePath();
            g.fill(arrowPath);
        }
    }
}
