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

package org.netbeans.modules.form.beaninfo.awt;

import java.awt.Image;
import java.beans.*;
import org.openide.util.ImageUtilities;

/** Base class for awt components - toolbars, checkboxes...
*
* @author Ales Novak
* @see sun.java.beans.infos.... or
*/
public class ComponentBeanInfo extends SimpleBeanInfo {

    /** no-arg */
    ComponentBeanInfo() {
    }

    /** @return Propertydescriptors */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            return new PropertyDescriptor[] {
                new PropertyDescriptor("background", java.awt.Component.class, "getBackground", "setBackground"), // 0 // NOI18N
                new PropertyDescriptor("foreground", java.awt.Component.class, "getForeground", "setForeground"), //1 // NOI18N
                new PropertyDescriptor("enabled", java.awt.Component.class, "isEnabled", "setEnabled"), //2 // NOI18N
                new PropertyDescriptor("name", java.awt.Component.class), //3 // NOI18N
                new PropertyDescriptor("visible", java.awt.Component.class), //4 // NOI18N
                new PropertyDescriptor("cursor", java.awt.Component.class), // NOI18N
                new PropertyDescriptor("font", java.awt.Component.class) // NOI18N
            };
        } catch (IntrospectionException ex) {
            return super.getPropertyDescriptors();
        }
    }
    
    static class Support extends SimpleBeanInfo {

        String iconName;
        Class beanClass;
        
        Support(String icon, Class beanClass) {
            iconName = icon;
            this.beanClass = beanClass;
        }

        @Override
        public BeanDescriptor getBeanDescriptor() {
            return new BeanDescriptor(beanClass);
        }

        /**
        * Return the icon
        */
        @Override
        public Image getIcon(int type) {
            if (iconName == null) return null;
            return ImageUtilities.loadImage("org/netbeans/modules/form/beaninfo/awt/" + iconName + ".gif"); // NOI18N
        }

        @Override
        public BeanInfo[] getAdditionalBeanInfo() {
            return new BeanInfo[] { new ComponentBeanInfo() };
        }

        /** @return Propertydescriptors */
        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                return createPDs();
            } catch (IntrospectionException ex) {
                return null;
            }
        }
            
        protected PropertyDescriptor[] createPDs() throws IntrospectionException {
            return new PropertyDescriptor[0];
        }
    }
}
