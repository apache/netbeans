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
