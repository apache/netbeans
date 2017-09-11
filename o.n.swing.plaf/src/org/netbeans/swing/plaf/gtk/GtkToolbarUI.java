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
    
    public void installUI( JComponent c ) {
        super.installUI(c);
        //c.setBorder(b);
        c.setOpaque(false);
        c.addContainerListener(this);
        installButtonUIs (c);
    }
    
    public void uninstallUI (JComponent c) {
        super.uninstallUI (c);
        c.setBorder (null);
        c.removeContainerListener(this);
    }
    
    public void paint(Graphics g, JComponent c) {
        GradientPaint gp = new GradientPaint (0f, 0f, 
            UIManager.getColor("controlHighlight"), //NOI18N
            0f, c.getHeight(), 
            UIManager.getColor("control")); //NOI18N
        ((Graphics2D) g).setPaint (gp);
        Insets ins = c.getInsets();
        g.fillRect (ins.left, ins.top, c.getWidth() - (ins.left + ins.top), c.getHeight() - (ins.top + ins.bottom));
    }
    
    
    protected Border createRolloverBorder() {
        return BorderFactory.createEmptyBorder(2,2,2,2);
    }
    
    protected Border createNonRolloverBorder() {
        return createRolloverBorder();
    }
    
    private Border createNonRolloverToggleBorder() {
        return createRolloverBorder();
    }
    
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
