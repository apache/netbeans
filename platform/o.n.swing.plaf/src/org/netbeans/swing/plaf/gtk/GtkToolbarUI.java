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
 * GtkToolbarUI.java
 *
 * Created on January 17, 2004, 3:00 AM
 */

package org.netbeans.swing.plaf.gtk;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolBarUI;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

/** A ToolbarUI subclass that gets rid of all borders
 * on buttons and provides a finder-style toolbar look.
 *
 * @author  Tim Boudreau
 */
public class GtkToolbarUI extends BasicToolBarUI implements ContainerListener {
    //private Border b = new AdaptiveMatteBorder (true, true, true, true, 3, true);
    /** Creates a new instance of PlainGtkToolbarUI */
    private GtkToolbarUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new GtkToolbarUI();
    }

    @Override
    public void installUI( JComponent c ) {
        super.installUI(c);
        //c.setBorder(b);
        c.setOpaque(false);
        c.addContainerListener(this);
        installButtonUIs (c);
    }

    @Override
    public void uninstallUI (JComponent c) {
        super.uninstallUI (c);
        c.setBorder (null);
        c.removeContainerListener(this);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        GradientPaint gp = new GradientPaint (0f, 0f, 
            UIManager.getColor("controlHighlight"), //NOI18N
            0f, c.getHeight(), 
            UIManager.getColor("control")); //NOI18N
        ((Graphics2D) g).setPaint (gp);
        Insets ins = c.getInsets();
        g.fillRect (ins.left, ins.top, c.getWidth() - (ins.left + ins.top), c.getHeight() - (ins.top + ins.bottom));
    }


    @Override
    protected Border createRolloverBorder() {
        return BorderFactory.createEmptyBorder(2,2,2,2);
    }

    @Override
    protected Border createNonRolloverBorder() {
        return createRolloverBorder();
    }
    
    private Border createNonRolloverToggleBorder() {
        return createRolloverBorder();
    }

    @Override
    protected void setBorderToRollover(Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setBorderPainted(false);
            ((AbstractButton) c).setBorder(BorderFactory.createEmptyBorder());
            ((AbstractButton) c).setContentAreaFilled(false);
            ((AbstractButton) c).setOpaque(false);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }

    @Override
    protected void setBorderToNormal(Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setBorderPainted(false);
            ((AbstractButton) c).setContentAreaFilled(false);
            ((AbstractButton) c).setOpaque(false);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }

    @Override
    public void setFloating(boolean b, Point p) {
        //nobody wants this
    }
    
    private void installButtonUI (Component c) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setUI(buttonui);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }
    
    private void installButtonUIs (Container parent) {
        Component[] c = parent.getComponents();
        for (int i=0; i < c.length; i++) {
            installButtonUI(c[i]);
        }
    }
    
    private static final ButtonUI buttonui = new GtkToolBarButtonUI();
    public void componentAdded(ContainerEvent e) {
        installButtonUI (e.getChild());
    }
    
    public void componentRemoved(ContainerEvent e) {
        //do nothing
    }
}
