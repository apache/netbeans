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
