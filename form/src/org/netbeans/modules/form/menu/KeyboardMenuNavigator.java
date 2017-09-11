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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import org.netbeans.modules.form.InPlaceEditLayer;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;

/**
 * Handles navigation of menu items using the keyboard
 *
 * @author joshua.marinacci@sun.com
 */
public class KeyboardMenuNavigator extends KeyAdapter {
    MenuEditLayer menuEditLayer;
    private RADVisualContainer menuBarRAD;
    private RADVisualContainer currentMenuRAD;
    KeyboardFinishListener listener;
    
    public KeyboardMenuNavigator(MenuEditLayer menuEditLayer) {
        this.menuEditLayer = menuEditLayer;
        configure();
    }
    
    public void setCurrentMenuRAD(RADVisualContainer currentMenuRAD) {
        this.currentMenuRAD = currentMenuRAD;
        this.menuBarRAD = getMenuBarRad(currentMenuRAD);
    }
    
    private RADVisualContainer getMenuBarRad(RADComponent comp) {
        if(JMenuBar.class.isAssignableFrom(comp.getBeanClass())) {
            return (RADVisualContainer) comp;
        }
        if(comp.getParentComponent() == null) return null;
        return getMenuBarRad(comp.getParentComponent());
    }
    
    public void configure() {
        listener = new KeyboardFinishListener();
        menuEditLayer.formDesigner.getInPlaceEditLayer().addFinishListener(listener);
    }
    public void unconfigure() {
        menuEditLayer.formDesigner.getInPlaceEditLayer().removeFinishListener(listener);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            selectOffsetMenuItem(+1);
        }
        if(e.getKeyCode() == KeyEvent.VK_UP) {
            selectOffsetMenuItem(-1);
        }
        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            selectOffsetMenu(-1);
        }
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            selectOffsetMenu(+1);
        }
        
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            startEditing();
        }
        // #116961: start inplace editing when F2 key is pressed on a menu
        if(e.getKeyCode() == KeyEvent.VK_F2) {
            startEditing();
        }
        //we aren't getting tabs for some reason
        if(e.getKeyCode() == KeyEvent.VK_A) {
            if(e.isShiftDown()) {
                selectNextMenuItem(-1);
            } else {
                selectNextMenuItem(+1);
            }
        }
    }
    
    private void selectNextMenuItem(int offset) {
        //josh: do nothing here until i figure out why tab events aren't being called
        if(currentMenuRAD == null) return;
        if(!menuEditLayer.isComponentSelected()) {
            menuEditLayer.setSelectedRADComponent(currentMenuRAD.getSubComponent(0));
        }
        
        RADComponent selectedRADComponent = menuEditLayer.getSingleSelectedComponent();
        //if menu, descend into the menu
        if(isJMenu(selectedRADComponent) && offset == +1) {
            RADVisualContainer newMenu = (RADVisualContainer) selectedRADComponent;
            if(newMenu.getSubComponents().length > 0) {
                currentMenuRAD = newMenu;
                selectOffsetMenuItem(offset);
                return;
            }
        }
        
        //if already at the end of this menu
        if(isLastItem(selectedRADComponent, currentMenuRAD) && offset == +1) {
            goUpOneLevelAndNext();
            return;
        }
        if(isFirstItem(selectedRADComponent, currentMenuRAD) && offset == -1) {
            goUpOneLevel();
            return;
        }
        selectOffsetMenuItem(offset);
    }
    
    
    // select the next menu item offset from the current one.
    // pass in -1 and +1 to do prev and next menu items
    private void selectOffsetMenuItem(int offset) {
        if(currentMenuRAD == null) return;
        if(currentMenuRAD.getSubComponents().length == 0) {
            menuEditLayer.setSelectedRADComponent(null);//Component((JComponent)null);
            return;
        }
        if(!menuEditLayer.isComponentSelected()) {
            menuEditLayer.setSelectedRADComponent(currentMenuRAD.getSubComponent(0));
            return;
        }
        
        
        int index = currentMenuRAD.getIndexOf(menuEditLayer.getSingleSelectedComponent());
        if(index+offset >=0 && index+offset < currentMenuRAD.getSubComponents().length) {
            menuEditLayer.setSelectedRADComponent(currentMenuRAD.getSubComponent(index+offset));
        } else {
            if(index >= 0 && index < currentMenuRAD.getSubComponents().length) {
                menuEditLayer.setSelectedRADComponent(currentMenuRAD.getSubComponent(index));
            }
        }
        
    }
    
    private boolean isJMenu(RADComponent comp) {
        return menuEditLayer.formDesigner.getComponent(comp) instanceof JMenu;
    }
    
    
    // select the next menu offset from the current one
    // pass in -1 and + 1 to do prev and next menu items
    private void selectOffsetMenu(int offset) {
        //clear the selected component
        //menuEditLayer.setSelectedComponent(null);
        
        //if the current component is a JMenu
        if(isJMenu(menuEditLayer.getSingleSelectedComponent())) {
            RADVisualContainer menuRAD = (RADVisualContainer) menuEditLayer.getSingleSelectedComponent();//selectedRADComponent;
            // make it's first element be highlighted
            if(menuRAD.getSubComponents() != null &&
                    menuRAD.getSubComponents().length > 0 &&
                    menuRAD.getSubComponent(0) != null) {
                RADVisualComponent firstItemRad = menuRAD.getSubComponent(0);
                // open the menu
                menuEditLayer.showMenuPopup((JMenu)menuEditLayer.formDesigner.getComponent(menuEditLayer.getSingleSelectedComponent()));//selectedRADComponent));
                menuEditLayer.setSelectedRADComponent(firstItemRad);
                currentMenuRAD = menuRAD;
                return;
            }
        }
        
        // if not a toplevel menu
        int index = menuBarRAD.getIndexOf(currentMenuRAD);
        if(index < 0) {
            // if left then head back up the heirarchy
            if(offset < 0) {
                goUpOneLevel();
                return;
            }
            // if right then switch to the next a full toplevel menu
            if(offset > 0) {
                currentMenuRAD = getTopLevelMenu(currentMenuRAD);
                index = menuBarRAD.getIndexOf(currentMenuRAD);
                // now continue on as normal
            }
        }
        
        // set the current to the new one
        index = index+offset;
        // wrap around if necessary
        if(index <0) {
            index = menuBarRAD.getSubComponents().length-1;
        }
        if(index >= menuBarRAD.getSubComponents().length) {
            index = 0;
        }
        currentMenuRAD = (RADVisualContainer) menuBarRAD.getSubComponent(index);
        
        // show the new current menu
        JMenu menu = (JMenu) menuEditLayer.formDesigner.getComponent(currentMenuRAD);
        menuEditLayer.openMenu(currentMenuRAD,menu);
        
        // set the first item as selected
        if(currentMenuRAD.getSubComponents().length > 0) {
            menuEditLayer.setSelectedRADComponent(currentMenuRAD.getSubComponents()[0]);
        }
    }
    
    private void goUpOneLevel() {
        menuEditLayer.setSelectedRADComponent(currentMenuRAD);
        currentMenuRAD = currentMenuRAD.getParentContainer();
    }
    
    private void goUpOneLevelAndNext() {
        menuEditLayer.setSelectedRADComponent(currentMenuRAD);
        currentMenuRAD = currentMenuRAD.getParentContainer();
        if(isLastItem(menuEditLayer.getSingleSelectedComponent(), currentMenuRAD)) {
            goUpOneLevelAndNext();
            return;
        } else {
            selectOffsetMenuItem(+1);
        }
    }
    
    private boolean isFirstItem(RADComponent comp, RADVisualContainer cont) {
        int index = cont.getIndexOf(comp);
        if(index == 0) return true;
        return false;
    }
    
    private boolean isLastItem(RADComponent comp, RADVisualContainer cont) {
        int index = cont.getIndexOf(comp);
        if(index == cont.getSubComponents().length-1) {
            return true;
        }
        return false;
    }
    
    private RADVisualContainer getTopLevelMenu(RADVisualContainer currentMenuRAD) {
        if(menuBarRAD.getIndexOf(currentMenuRAD) >= 0) {
            return currentMenuRAD;
        }
        return getTopLevelMenu(currentMenuRAD.getParentContainer());
    }
    
    private void startEditing() {
        menuEditLayer.configureEditedComponent(menuEditLayer.getSingleSelectedComponent());//selectedRADComponent);
        menuEditLayer.formDesigner.startInPlaceEditing(menuEditLayer.getSingleSelectedComponent());//selectedRADComponent);
    }
    
    private class KeyboardFinishListener implements InPlaceEditLayer.FinishListener {
        @Override
        public void editingFinished(boolean changed) {
            if(menuEditLayer.isVisible()) {
                menuEditLayer.glassLayer.requestFocusInWindow();
            }
        }
    }

}
