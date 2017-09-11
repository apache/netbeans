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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.form.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualContainer;

/** A PopupFactory which returns VisualDesignerJPanelPopup's
 *
 * @author joshua.marinacci@sun.com
 */
class VisualDesignerPopupFactory extends PopupFactory {
    public Map<JMenu, JPanel> containerMap;
    private Map<JMenu, VisualDesignerJPanelPopup> popupMap;
    
    private MenuEditLayer canvas;
    
    public VisualDesignerPopupFactory(MenuEditLayer canvas) {
        containerMap = new HashMap<JMenu, JPanel>();
        popupMap = new HashMap<JMenu, VisualDesignerJPanelPopup>();
        this.canvas = canvas;
    }
    
    @Override
    public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
        final JMenu menu = (JMenu) owner;
        JPanel cont = containerMap.get(menu);
        
        if (cont == null) {
            cont = new VisualDesignerJPanelContainer(menu,this);
            cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));

            RADVisualContainer menuRAD = (RADVisualContainer) canvas.formDesigner.getMetaComponent(menu);
            for(RADComponent c : menuRAD.getSubBeans()) {
                JComponent comp = (JComponent) canvas.formDesigner.getComponent(c);
                cont.add(comp);
            }
            
            cont.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            containerMap.put(menu,cont);
            canvas.layers.add(cont, JLayeredPane.DEFAULT_LAYER);
        }
        
        cont.setSize(cont.getLayout().preferredLayoutSize(cont));
        canvas.validate();
        canvas.setVisible(true);
        final JPanel fcont = cont;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationFromMenu(menu, fcont);
            }
        });
        
        canvas.validate();
        canvas.repaint();
        VisualDesignerJPanelPopup popup = new VisualDesignerJPanelPopup(cont, menu, this);
        popupMap.put(menu,popup);
        return popup;
    }
    
    VisualDesignerJPanelPopup getPopup(JMenu menu) {
        return popupMap.get(menu);
    }
    
    private void setLocationFromMenu(final JMenu menu, final JPanel cont) {
        Point pt = SwingUtilities.convertPoint(menu, new Point(0,0), canvas);
        
        JComponent parent = canvas.getMenuParent(menu);
        if(parent instanceof JMenu) {
            // get this menu's location in local coords
            // move to the right edge of the menu
            pt = new Point(pt.x + menu.getWidth(), pt.y);
        } else {
            // if parent isn't a jmenu the this must be a toplevel,
            // so we must position below the menu instead of next to it
            pt = new Point(pt.x, pt.y + menu.getHeight());
        }
        cont.setLocation(pt);
    }
    
    void hideOtherMenus(JMenu menu) {
        for(JMenu m : containerMap.keySet()) {
            if(m != menu) {
                // hide if not an ancestor of this menu
                if(!isAncestor(m,menu)) {/* && 
                        (canvas.isTopLevelMenu(m) ||
                         canvas.hasSelectedDescendants(m))
                        ) {*/
                    JPanel popup = containerMap.get(m);
                    popup.setVisible(false);
                }
            }
        }
    }
    
    private boolean isAncestor(JMenu m, JMenu menu) {
        return canvas.isAncestor(menu, m);
    }
    
    private static class VisualDesignerJPanelContainer extends JPanel {
        private JMenu menu;
        private VisualDesignerPopupFactory fact;
        VisualDesignerJPanelContainer(JMenu menu, VisualDesignerPopupFactory fact) {
            this.menu = menu;
            this.fact = fact;
        }
        @Override
        public void setVisible(boolean visible) {
            // if making visible
            if(visible) {
                // make sure the other menus are hidden
                fact.hideOtherMenus(menu);
                // make sure this menu popup is at the right place
                fact.setLocationFromMenu(menu,this);
                // repack?
            }
            super.setVisible(visible);
        }
    }
    
}
