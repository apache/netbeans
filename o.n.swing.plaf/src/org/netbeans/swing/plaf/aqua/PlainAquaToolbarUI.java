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
 * PlainAquaToolbarUI.java
 *
 * Created on January 17, 2004, 3:00 AM
 */

package org.netbeans.swing.plaf.aqua;


import javax.swing.*;
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
public class PlainAquaToolbarUI extends BasicToolBarUI implements ContainerListener {
        
    
    /** Creates a new instance of PlainAquaToolbarUI */
    public PlainAquaToolbarUI() {
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new PlainAquaToolbarUI();
    }
    
    @Override
    public void installUI( JComponent c ) {
        super.installUI(c);
        c.addContainerListener(this);
        boolean isEditorToolbar = "editorToolbar".equals (c.getName());
        c.setBackground(UIManager.getColor("NbExplorerView.background"));
        c.setOpaque(true);
        installButtonUIs (c, isEditorToolbar);
    }
    
    @Override
    public void uninstallUI (JComponent c) {
        super.uninstallUI (c);
        c.setBorder (null);
        c.removeContainerListener(this);
    }
    
    @Override
    public void setFloating(boolean b, Point p) {
        //nobody wants this
    }
    
    private void installButtonUI (Component c, boolean isEditorToolbar) {
        if (c instanceof AbstractButton) {
            ((AbstractButton) c).setUI(isEditorToolbar ? buttonui : mainButtonui);
        }
        if (c instanceof JComponent) {
            ((JComponent) c).setOpaque(false);
        }
    }
    
    private void installButtonUIs (Container parent, boolean isEditorToolbar) {
        Component[] c = parent.getComponents();
        for (int i=0; i < c.length; i++) {
            installButtonUI(c[i], isEditorToolbar);
        }
    }
    
    private static final ButtonUI mainButtonui = new AquaToolBarButtonUI(true);
    private static final ButtonUI buttonui = new AquaToolBarButtonUI(false);
    public void componentAdded(ContainerEvent e) {
        Container c = (Container) e.getSource();
        boolean isEditorToolbar = "editorToolbar".equals (c.getName());
        installButtonUI (e.getChild(), isEditorToolbar);
        if (isEditorToolbar) {
            //It's an editor toolbar.  Aqua's combo box ui paints outside
            //of its literal component bounds, and doesn't honor opacity.
            //Need to ensure the toolbar is tall enough that its border is
            //not hidden.
            Dimension min = new Dimension (32, 34);
            ((JComponent)e.getContainer()).setPreferredSize(min);
        }
    }
    
    public void componentRemoved(ContainerEvent e) {
        //do nothing
    }
}
