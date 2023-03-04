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
/*
 * AquaToolBarButtonUI.java
 *
 * Created on January 17, 2004, 1:54 PM
 */

package org.netbeans.swing.plaf.aqua;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.plaf.basic.BasicButtonUI;

/** A finder-style aqua toolbar button UI
 *
 * @author  Tim Boudreau
 */
class AquaToolBarButtonUI extends BasicButtonUI implements ChangeListener {
    private final boolean isMainToolbarButtonUI;
    private static BasicButtonListener listener = new BasicButtonListener(null);
    
    /** Creates a new instance of AquaToolBarButtonUI */
    public AquaToolBarButtonUI( boolean isMainToolbar ) {
        this.isMainToolbarButtonUI = isMainToolbar;
    }
    
    @Override
    public void installUI (JComponent c) {
        AbstractButton b = (AbstractButton) c;
        b.addMouseListener (listener);
        b.addChangeListener(this);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusable(false);
        b.setBorderPainted(true);
        if( isMainToolbarButtonUI )
            b.setBorder( BorderFactory.createEmptyBorder(4,6,4,6) );
        else
            b.setBorder( BorderFactory.createEmptyBorder(2,6,2,6) );
        b.setRolloverEnabled(isMainToolbarButtonUI);
   }

    @Override
    public void uninstallUI(JComponent c) {
        c.removeMouseListener (listener);
        if( c instanceof AbstractButton )
            ((AbstractButton)c).removeChangeListener(this);
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        ((AbstractButton) e.getSource()).repaint();
    }
    
    private final Rectangle scratch = new Rectangle();
    @Override
    public void paint (Graphics g, JComponent c) {
        Rectangle r = c.getBounds(scratch);
        AbstractButton b = (AbstractButton) c;
        r.x = 0;
        r.y = 0;
        Paint temp = ((Graphics2D) g).getPaint();
        paintBackground ((Graphics2D)g, b, r);
        paintIcon (g, b, r);
        paintText (g, b, r);
        ((Graphics2D) g).setPaint(temp);
    }
    
    
    private FontMetrics fm = null; //We are not setting any custom fonts, can use one
    private void paintText (Graphics g, AbstractButton b, Rectangle r) {
        String s = b.getText();
        if (s == null || s.length() == 0) {
            return;
        }
        g.setColor (b.getForeground());
        Font f = b.getFont();
        if (b.isSelected()) {
            // don't use deriveFont() - see #49973 for details
            f = new Font(f.getName(), Font.BOLD, f.getSize());
        }
        g.setFont (f);
        if( g instanceof Graphics2D ) {
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        FontMetrics fontMetrics = g.getFontMetrics();
        if (this.fm == null) {
            this.fm = fontMetrics;
        }        
        int x = 0;
        Icon ic = b.getIcon();
        if (ic != null) {
            x = ic.getIconWidth() + b.getIconTextGap() + 2;
        } else {
            int w = fontMetrics.stringWidth (s);
            if (w <= r.width) {
                x = (r.width / 2) - (w / 2);
            }
        }
        int h = fontMetrics.getHeight();
        int y = fontMetrics.getMaxAscent();
        if (h <= r.height) {
            y += (r.height / 2) - (h / 2);
        }
        g.drawString (s, x, y);
    }
    
    private void paintBackground (Graphics2D g, AbstractButton b, Rectangle r) {
        if( !(b.isSelected() || b.getModel().isPressed()) )
            return;
        Color c = isMainToolbarButtonUI 
                ? UIManager.getColor("NbBrushedMetal.lightShadow")
                : b.getParent().getBackground();

        Color darker = makeDarker( c );
        Paint p = g.getPaint();
        g.setPaint( new GradientPaint(r.x, r.y, c, r.x, r.height/2, darker));
        g.fillRect(r.x, r.y, r.width, r.height/2);
        g.setPaint( new GradientPaint(r.x, r.y+r.height/2-1, darker, r.x, r.height, c));
        g.fillRect(r.x, r.y+r.height/2-1, r.width, r.height);

        Color evenDarker = makeDarker(darker);
        g.setPaint( new GradientPaint(r.x, r.y, darker, r.x, r.height/2, evenDarker));
        g.fillRect(r.x, r.y, 1, r.height/2);
        g.fillRect(r.x+r.width-1, r.y, 1, r.height/2);

        g.setPaint( new GradientPaint(r.x, r.y+r.height/2-1, evenDarker, r.x, r.height, darker));
        g.fillRect(r.x, r.y+r.height/2-1, 1, r.height);
        g.fillRect(r.x+r.width-1, r.y+r.height/2-1, 1, r.height);

        g.setPaint( new GradientPaint(r.x, r.y, c, r.x, r.height/2, evenDarker));
        g.fillRect(r.x+1, r.y, 1, r.height/2);
        g.fillRect(r.x+r.width-2, r.y, 1, r.height/2);

        g.setPaint( new GradientPaint(r.x, r.y+r.height/2-1, evenDarker, r.x, r.height, c));
        g.fillRect(r.x+1, r.y+r.height/2-1, 1, r.height);
        g.fillRect(r.x+r.width-2, r.y+r.height/2-1, 1, r.height);
    }

    private Color makeDarker( Color c ) {
        int factor = 30;
        return new Color( Math.max(c.getRed()-factor, 0),
                Math.max(c.getGreen()-factor, 0),
                Math.max(c.getRed()-factor, 0));
    }
    
    private void paintIcon (Graphics g, AbstractButton b, Rectangle r) {
        Icon ic = getIconForState (b);
        boolean noText = b.getText() == null || b.getText().length() == 0;
        if (ic != null) {
            int iconX = 0;
            int iconY = 0;
            int iconW = ic.getIconWidth();
            int iconH = ic.getIconHeight();
            
            if (iconW <= r.width && noText) {
                iconX = (r.width / 2) - (iconW / 2);
            }
            if (iconH <= r.height) {
                iconY = (r.height / 2) - (iconH / 2);
            }
            Graphics2D g2d = (Graphics2D) g;
            ic.paintIcon(b, g, iconX, iconY);
            if( isMainToolbarButtonUI && b.getModel().isRollover() && !b.getModel().isPressed()) {
                Composite composite = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, 0.2f));
                ic.paintIcon(b, g, iconX, iconY);
                g2d.setComposite(composite);
            }
        }
    }
    
    private Icon getIconForState (AbstractButton b) {
        ButtonModel mdl = b.getModel();
        Icon result = null;
        if (!b.isEnabled()) {
            result = mdl.isSelected() ? b.getDisabledSelectedIcon() : b.getDisabledIcon();
            if (result == null && mdl.isSelected()) {
                result = b.getDisabledIcon();
            }
        } else {
            if (mdl.isArmed() && !mdl.isPressed()) {
                result = mdl.isSelected() ? b.getRolloverSelectedIcon() : b.getRolloverIcon();
                if (result == null & mdl.isSelected()) {
                    result = b.getRolloverIcon();
                }
            }
            if (mdl.isPressed()) {
                result = b.getPressedIcon();
            } else if (mdl.isSelected()) {
                result = b.getSelectedIcon();
            }
        }
        if (result == null) {
            result = b.getIcon();
        }
        return result;
    }
    
    private static final int minButtonSize = 16;
    @Override
    public Dimension getPreferredSize(JComponent c) {
        AbstractButton b = (AbstractButton) c;
        
        boolean noText = 
            b.getText() == null || 
            b.getText().length() == 0;
            
        Icon ic = getIconForState((AbstractButton) c);
        int w = minButtonSize;
        Dimension result = ic == null ? new Dimension (noText ? 32 : 0, minButtonSize) :
                new Dimension(Math.max(w, ic.getIconWidth()+1), 
                Math.max(minButtonSize,ic.getIconHeight() + 1));
        
        if (!noText) {
            FontMetrics fm = this.fm;
            if (fm == null && c.getGraphicsConfiguration() != null) {
                fm = c.getGraphicsConfiguration().createCompatibleImage(1,1)
                     .getGraphics().getFontMetrics(c.getFont());
            }
            if (fm == null) {
                //init
                fm = new BufferedImage(1, 1, 
                BufferedImage.TYPE_INT_RGB).getGraphics().getFontMetrics(c.getFont());
            }
            result.width += fm.stringWidth(b.getText());
        }
        Insets in = b.getInsets();
        if( null != in ) {
            result.width += in.left + in.right;
            result.height += in.top + in.bottom;
        }
        return result;
    }    
}
