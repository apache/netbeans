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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.swing.plaf.gtk;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonListener;
import java.awt.*;

/** A GTK-style toolbar button UI
 *
 * @author  Tim Boudreau
 */
class GtkToolBarButtonUI extends ButtonUI implements ChangeListener {
    private static BasicButtonListener listener =
        new BasicButtonListener(null);

    /** Creates a new instance of AquaToolBarButtonUI */
    public GtkToolBarButtonUI() {
    }
    
    public void installUI (JComponent c) {
        AbstractButton b = (AbstractButton) c;
        b.addMouseListener (listener);
        b.addChangeListener(this);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setFocusable(false);
        b.setBorderPainted(false);
        b.setBorder (BorderFactory.createEmptyBorder());
        b.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
    }
    
    public void uninstallUI(JComponent c) {
        c.removeMouseListener (listener);
    }
    
    public void stateChanged(ChangeEvent e) {
        ((AbstractButton) e.getSource()).repaint();
    }
    
    private final Rectangle scratch = new Rectangle();
    public void paint (Graphics g, JComponent c) {
        Rectangle r = c.getBounds(scratch);
        AbstractButton b = (AbstractButton) c;
        r.x = 0;
        r.y = 0;
        Paint temp = ((Graphics2D) g).getPaint();
        paintBackground ((Graphics2D)g, b, r);
        paintIcon (g, b, r);
        ((Graphics2D) g).setPaint(temp);
    }
    
    private void paintBackground (Graphics2D g, AbstractButton b, Rectangle r) {
        if (!b.isEnabled()) {
        } else if (b.getModel().isPressed()) {
            compositeColor (g, r, Color.BLUE, 0.3f);
        } else if (b.getModel().isSelected()) {
            compositeColor (g, r, new Color (0, 120, 255), 0.2f);;
        } 
    }
    
    private void compositeColor (Graphics2D g, Rectangle r, Color c, float alpha) {
        g.setColor (c);
        Composite comp = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, alpha));

        g.fillRect (r.x, r.y, r.width, r.height);
        g.setComposite(comp);
    }
    
    private static boolean isFirst (AbstractButton b) {
        if (b.getParent() != null && b.getParent().getComponentCount() > 1) {
            //The grip is always component 0, so see if the button
            //is component 1
            return b == b.getParent().getComponent(1);
        } else {
            return false;
        }
    }
    
    private void paintIcon (Graphics g, AbstractButton b, Rectangle r) {
        Icon ic = getIconForState (b);
        if (ic != null) {
            int iconX = 0;
            int iconY = 0;
            int iconW = ic.getIconWidth();
            int iconH = ic.getIconHeight();
            
            if (iconW <= r.width) {
                iconX = (r.width / 2) - (iconW / 2);
            }
            if (iconH <= r.height) {
                iconY = (r.height / 2) - (iconH / 2);
            }
            ic.paintIcon(b, g, iconX, iconY);
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
    
    private static final int minButtonSize = 32;
    public Dimension getPreferredSize(JComponent c) {
        if (c instanceof AbstractButton) {
            Icon ic = getIconForState((AbstractButton) c);
            Dimension result;
            int minSize = isFirst((AbstractButton)c) ? 0 : minButtonSize;
            if (ic != null) {
                result = new Dimension(Math.max(minSize, ic.getIconWidth()+1), 
                    Math.max(minButtonSize,ic.getIconHeight() + 1));
            } else {
                result = new Dimension (minButtonSize, minButtonSize);
            }
            result.width += 4;
            return result;
        } else {
            if (c.getLayout() != null) {
                return c.getLayout().preferredLayoutSize(c);
            }
        }
        return null;
    }    
}
