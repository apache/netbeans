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

import java.awt.*;

/**
 * RADMenuItemComponent represents one menu item component in the Form.
 *
 * @author Petr Hamernik, Ian Formanek
 */

public class RADMenuItemComponent extends RADComponent {

    /** Type of menu */
    private int type;

    /** Possible constants for type variable */
    static final int T_MENUBAR              = 0x01110;
    static final int T_MENUITEM             = 0x00011;
    static final int T_CHECKBOXMENUITEM     = 0x00012;
    static final int T_MENU                 = 0x00113;
    static final int T_POPUPMENU            = 0x01114;


    static final int T_SEPARATOR            = 0x1001B;

    /** Masks for the T_XXX constants */
    static final int MASK_AWT               = 0x00010;
//    static final int MASK_SWING             = 0x00020;
    static final int MASK_CONTAINER         = 0x00100;
    static final int MASK_ROOT              = 0x01000;
    static final int MASK_SEPARATOR         = 0x10000;


    @Override
    public Object initInstance(Class<? extends Object> beanClass) throws Exception {
        type = recognizeType(beanClass);
        return super.initInstance(beanClass);
    }

    @Override
    protected org.openide.nodes.Node.Property[] createSyntheticProperties() {
        // no synthetic properties for AWT Separator
        if (type == T_SEPARATOR)
            return RADComponent.NO_PROPERTIES;
        else
            return super.createSyntheticProperties();
    }

    int getMenuItemType() {
        return type;
    }

    /** Recognizes type of the menu from its class.
     * @return adequate T_XXX constant
     */
    static int recognizeType(Class cl) {
        if (org.netbeans.modules.form.Separator.class.isAssignableFrom(cl))
            return T_SEPARATOR;
        if (PopupMenu.class.isAssignableFrom(cl))
            return T_POPUPMENU;
        if (Menu.class.isAssignableFrom(cl))
            return  T_MENU;
        if (CheckboxMenuItem.class.isAssignableFrom(cl))
            return T_CHECKBOXMENUITEM;
        if (MenuItem.class.isAssignableFrom(cl))
            return  T_MENUITEM;
        if (MenuBar.class.isAssignableFrom(cl))
            return T_MENUBAR;


        throw new IllegalArgumentException("Cannot create RADMenuItemComponent for class: "+cl.getName()); // NOI18N
    }

    @Override
    public Object cloneBeanInstance(java.util.Collection<RADProperty> relativeProperties) {
        if (type == T_SEPARATOR)
            return null; // don't clone artificial org.netbeans.modules.form.Separator
        return super.cloneBeanInstance(relativeProperties);
    }

}
