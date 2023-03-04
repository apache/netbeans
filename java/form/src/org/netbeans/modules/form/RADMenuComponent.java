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

package org.netbeans.modules.form;

import org.openide.util.datatransfer.NewType;

import java.awt.*;
import java.util.*;

import org.netbeans.modules.form.project.ClassSource;

/**
 *
 * @author Ian Formanek
 */

public class RADMenuComponent extends RADMenuItemComponent implements ComponentContainer {

    /** Map of possible combinations of menus in menus. Menu types (as Integer)
     * are mapped to supported (sub)menu types (as Class[]).
     */
    static Map<Integer,Class[]> supportedMenus;

    /** Initialization of supportedMenus map. */
    static {
        supportedMenus = new HashMap<Integer,Class[]>();
        supportedMenus.put(Integer.valueOf(T_MENUBAR),
                           new Class[] { Menu.class });
        supportedMenus.put(Integer.valueOf(T_MENU),
                           new Class[] { MenuItem.class,
                                         CheckboxMenuItem.class,
                                         Menu.class,
                                         Separator.class });
        supportedMenus.put(Integer.valueOf(T_POPUPMENU),
                           new Class[] { MenuItem.class,
                                         CheckboxMenuItem.class,
                                         Menu.class,
                                         Separator.class });
//        supportedMenus.put(new Integer(T_JMENUBAR),
//                           new Class[] { JMenu.class });
//        supportedMenus.put(new Integer(T_JMENU),
//                           new Class[] { JMenuItem.class,
//                                         JCheckBoxMenuItem.class,
//                                         JRadioButtonMenuItem.class,
//                                         JMenu.class,
//                                         JSeparator.class });
//        supportedMenus.put(new Integer(T_JPOPUPMENU),
//                           new Class[] { JMenuItem.class,
//                                         JCheckBoxMenuItem.class,
//                                         JRadioButtonMenuItem.class,
//                                         JMenu.class,
//                                         JSeparator.class });
    }

    // -----------------------------------------------------------------------------
    // Private variables

    private ArrayList<RADComponent> subComponents;

    // -----------------------------------------------------------------------------
    // Initialization

    /** Support for new types that can be created in this node.
     * @return array of new type operations that are allowed
     */
    @Override
    public NewType[] getNewTypes() {
        if (isReadOnly())
            return RADComponent.NO_NEW_TYPES;

        Class[] classes = supportedMenus.get(Integer.valueOf(getMenuItemType()));

        if (classes == null)
            return RADComponent.NO_NEW_TYPES;

        NewType[] types = new NewType[classes.length];
        for (int i = 0; i < types.length; i++)
            types[i] = new NewMenuType(classes[i]);

        return types;
    }

    public boolean canAddItem(Class itemType) {
        Class[] classes = supportedMenus.get(Integer.valueOf(getMenuItemType()));

        if (classes != null)
            for (int i=0; i < classes.length; i++)
                if (classes[i] == itemType) // or more general isAssignableFrom ??
                    return true;

        return false;
    }

    // -----------------------------------------------------------------------------
    // SubComponents Management

    @Override
    public RADComponent[] getSubBeans() {
        RADComponent[] components = new RADComponent [subComponents.size()];
        subComponents.toArray(components);
        return components;
    }

    @Override
    public void initSubComponents(RADComponent[] initComponents) {
        if (subComponents == null)
            subComponents = new ArrayList<RADComponent>(initComponents.length);
        else {
            subComponents.clear();
            subComponents.ensureCapacity(initComponents.length);
        }

        for (int i = 0; i < initComponents.length; i++) {
            RADComponent comp = initComponents[i];
            if (comp instanceof RADMenuItemComponent) {
                subComponents.add(comp);
                comp.setParentComponent(this);
            }
        }
    }

    @Override
    public void reorderSubComponents(int[] perm) {
        RADComponent[] components = new RADComponent[subComponents.size()];
        for (int i=0; i < perm.length; i++)
            components[perm[i]] = subComponents.get(i);

        subComponents.clear();
        subComponents.addAll(Arrays.asList(components));
    }

    @Override
    public void add(RADComponent comp) {
        if (comp instanceof RADMenuItemComponent) {
            subComponents.add(comp);
            comp.setParentComponent(this);
//            getNodeReference().updateChildren();
        }
    }

    @Override
    public void remove(RADComponent comp) {
        if (subComponents.remove(comp))
            comp.setParentComponent(null);
//        getNodeReference().updateChildren();
    }

    @Override
    public int getIndexOf(RADComponent comp) {
        return subComponents.indexOf(comp);
    }

    // -------------
    // Innerclasses

    /** NewType for creating sub-MenuItem. */
    class NewMenuType extends NewType {
        /** Class which represents the menu class for this NewType */
        Class item;

        /** Constructs new NewType for the given menu class */
        public NewMenuType(Class item) {
            this.item = item;
        }

        /** Display name for the creation action. This should be
         * presented as an item in a menu.
         *
         * @return the name of the action
         */
        @Override
        public String getName() {
            String s = item.getName();

            int index = s.lastIndexOf('.');
            if (index != -1)
                return s.substring(index + 1);
            else
                return s;
        }

        /** Create the object.
         * @exception IOException if something fails
         */
        @Override
        public void create() throws java.io.IOException {
            getFormModel().getComponentCreator()
                .createComponent(new ClassSource(item.getName()),
                                 RADMenuComponent.this,
                                 null);
        }
    }
}
