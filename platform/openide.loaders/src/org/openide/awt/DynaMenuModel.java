/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.awt;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 * @author mkleint
 */
class DynaMenuModel {
    private static final Icon BLANK_ICON = new ImageIcon(ImageUtilities.loadImage("org/openide/loaders/empty.gif")); // NOI18N
    
    private List<JComponent> menuItems;
    private HashMap<DynamicMenuContent, JComponent[]> actionToMenuMap;
    private boolean isWithIcons = false;
    /** Creates a new instance of DynaMenuModel */
    public DynaMenuModel() {
        actionToMenuMap = new HashMap<DynamicMenuContent, JComponent[]>();
    }
    
    public void loadSubmenu(List<Object> cInstances, JMenu m, boolean remove, Map<Object,FileObject> cookiesToFiles) {
        // clear first - refresh the menu's content
        boolean addSeparator = false;
        Icon curIcon = null;
        Iterator it = cInstances.iterator();
        menuItems = new ArrayList<JComponent>(cInstances.size());
        actionToMenuMap.clear();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof Action) {
                FileObject file = cookiesToFiles.get(obj);
                if (file != null) {
                    AcceleratorBinding.setAccelerator((Action) obj, file);
                }
            }
            if (obj instanceof Presenter.Menu) {
                // does this still apply??
                obj = ((Presenter.Menu)obj).getMenuPresenter();
            }
            if (obj instanceof DynamicMenuContent) {
                if(addSeparator) {
                    menuItems.add(null);
                    addSeparator = false;
                }
                DynamicMenuContent mn = (DynamicMenuContent)obj;
                JComponent[] itms = convertArray(mn.getMenuPresenters());
                actionToMenuMap.put(mn, itms);
                Iterator itx = Arrays.asList(itms).iterator();
                while (itx.hasNext()) {
                    JComponent comp = (JComponent)itx.next();
                    menuItems.add(comp);
                    // check icon
                    isWithIcons = checkIcon(comp, isWithIcons);
                }
                continue;
            } 
            
            
            if (obj instanceof JMenuItem) {
                if(addSeparator) {
                    menuItems.add(null);
                    addSeparator = false;
                }
                // check icon
                isWithIcons = checkIcon(obj, isWithIcons);
                menuItems.add((JMenuItem)obj);
            } else if (obj instanceof JSeparator) {
                addSeparator = menuItems.size() > 0;
            } else if (obj instanceof Action) {
                if(addSeparator) {
                    menuItems.add(null);
                    addSeparator = false;
                }
                Action a = (Action)obj;
                Actions.MenuItem item = new Actions.MenuItem(a, true);
                // check icon
                isWithIcons = checkIcon(item, isWithIcons);
                actionToMenuMap.put(item, new JComponent[] {item});
                menuItems.add(item);
            }
        }
        
        if (isWithIcons) {
            menuItems = alignVertically(menuItems);
        }
        
        if (remove) {
            m.removeAll();
        }
        
        // fill menu with built items
        JComponent curItem = null;
        boolean wasSeparator = false;
        for (Iterator<JComponent> iter = menuItems.iterator(); iter.hasNext(); ) {
            curItem = iter.next();
            if (curItem == null) {
                // null means separator
                curItem = createSeparator();
            }
            m.add(curItem);
            boolean isSeparator = curItem instanceof JSeparator;
            if (isSeparator && wasSeparator) {
                curItem.setVisible(false);
            }
            if (!(curItem instanceof InvisibleMenuItem)) {
                wasSeparator = isSeparator;
            }
        }
    }
    
    
    private boolean checkIcon(Object obj, boolean isWithIconsAlready) {
        if (isWithIconsAlready) {
            return isWithIconsAlready;
        }
        if (obj instanceof JMenuItem) {
            if (((JMenuItem)obj).getIcon() != null && !BLANK_ICON.equals(((JMenuItem)obj).getIcon())) {
                return true;
            }
        }
        return false;
    }
    
    public void checkSubmenu(JMenu menu) {
        boolean oldisWithIcons = isWithIcons;
        boolean changed = false;
        for (Map.Entry<DynamicMenuContent, JComponent[]> entry: actionToMenuMap.entrySet()) {
            DynamicMenuContent pres = entry.getKey();
            JComponent[] old = entry.getValue();
            int oldIndex = 0;
            Component[] menuones = menu.getPopupMenu().getComponents();
            int menuIndex = old.length > 0 ? findFirstItemIndex(old[0], menuones) : -1;
            JComponent[] newones = convertArray(pres.synchMenuPresenters(unconvertArray(old)));
            if (!compareEqualArrays(old, newones)) {
                if (menuIndex < 0) {
                    menuIndex = 0;
                } else {
                    for (int i = 0; i < old.length; i++) {
                        if (old[i] != null) {
                            menu.getPopupMenu().remove(old[i]);
                            menuItems.remove(old[i]);
                        }
                    }
                }
                for (int i = 0; i < newones.length; i++) {
                    ///TODO now what to do with icon alignments..
                    JComponent one = newones[i];
                    menu.getPopupMenu().add(one, i + menuIndex);
                    changed = true;
                    menuItems.add(one);
                    boolean thisOneHasIcon = checkIcon(one, false);
                    if (!thisOneHasIcon && isWithIcons) {
                        alignVertically(Collections.singletonList(one));
                    }
                    if (thisOneHasIcon && !isWithIcons) {
                        isWithIcons = true;
                    }
                }
                entry.setValue(newones);
            }
            
        }
        boolean hasAnyIcons = false;
        Component[] menuones = menu.getPopupMenu().getComponents();
        for (int i = 0; i < menuones.length; i++) {
            if (menuones[i] != null) {
                hasAnyIcons = checkIcon(menuones[i], hasAnyIcons);
                if (hasAnyIcons) {
                    break;
                }
            }
        }
        checkSeparators(menuones, menu.getPopupMenu());
        if (!hasAnyIcons && isWithIcons) {
            isWithIcons = false;
        }
        if (oldisWithIcons != isWithIcons) {
            menuItems = alignVertically(menuItems);
        }
        if (changed && Utilities.isWindows()) {
            //#67847 on windows, we need revalidation otherwise strange effects kick in..
            menu.getPopupMenu().revalidate();
        }
    }
    
    static void checkSeparators(Component[] menuones, JPopupMenu parent) {
        boolean wasSeparator = false;
        for (int i = 0; i < menuones.length; i++) {
            Component curItem = menuones[i];
            if (curItem != null) {
                boolean isSeparator = curItem instanceof JSeparator;
                if (isSeparator) {
                    boolean isVisible = curItem.isVisible();
                    if (isVisible != !wasSeparator) {
                        //MACOSX whenever a property like enablement or visible is changed, need to remove and add.
                        // could be possibly split to work differetly on other platform..
                        parent.remove(i);
                        JSeparator newOne = createSeparator();
                        newOne.setVisible(!wasSeparator);
                        parent.add(newOne, i);
                    }
                }
                if (!(curItem instanceof InvisibleMenuItem)) {
                    wasSeparator = isSeparator;
                }
            }
        }
    }
    
    private JComponent[] convertArray(JComponent[] arr) {
        if (arr == null || arr.length == 0) {
            return new JComponent[] { new InvisibleMenuItem() };
        }
        JComponent[] toRet = new JComponent[arr.length];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                toRet[i] = createSeparator();
            } else {
                toRet[i] = arr[i];
            }
        }
        return toRet;
    }
    
    private JComponent[] unconvertArray(JComponent[] arr) {
        if (arr.length == 1 && arr[0] instanceof InvisibleMenuItem) {
            return new JComponent[0];
        } else {
            return arr;
        }
    }
        
    private int findFirstItemIndex(JComponent first, Component[] menuItems) {
        for (int i = 0; i < menuItems.length; i++) {
            if (first == menuItems[i]) {
                return i;
            }
        }
        return -1;
    }
    
    private boolean compareEqualArrays(JComponent[] one, JComponent[] two) {
        if (one.length != two.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (one[i] != two[i]) {
                return false;
            }
        }
        return true;
    }
    
    /** Removes icons from all direct menu items of this menu.
     * Not recursive, */
    private List<JComponent> alignVertically(List<JComponent> menuItems) {
        //#204646 - some L&Fs show check boxes and action icons in the same menu column (e.g. Vista l&f)
        //so do not use blank icons in such cases
        if( !UIManager.getBoolean( "Nb.MenuBar.VerticalAlign" ) ) //NOI18N
            return menuItems;
        List<JComponent> result = new ArrayList<JComponent>(menuItems.size());
        JMenuItem curItem = null;
        for (JComponent obj: menuItems) {
            if (obj instanceof JMenuItem) {
                curItem = (JMenuItem)obj;
                if (isWithIcons && curItem != null && curItem.getIcon() == null) {
                    curItem.setIcon(BLANK_ICON);
                } else if (!isWithIcons && curItem != null) {
                    curItem.setIcon(null);
                }
            }
            result.add(obj);
        }
        return result;
    }
    
    static final class InvisibleMenuItem extends JMenuItem {

        @Override
        public boolean isVisible() {
            return false;
        }
        
    }
    
    private static JSeparator createSeparator() {
        JMenu menu = new JMenu();
        menu.addSeparator();
        return (JSeparator)menu.getPopupMenu().getComponent(0);
    }
}
