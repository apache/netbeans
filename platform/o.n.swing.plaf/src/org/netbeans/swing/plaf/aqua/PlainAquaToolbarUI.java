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
