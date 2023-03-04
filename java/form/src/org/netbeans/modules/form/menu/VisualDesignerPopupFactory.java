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
