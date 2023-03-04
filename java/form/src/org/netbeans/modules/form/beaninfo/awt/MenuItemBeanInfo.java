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

import java.beans.*;

/** A BeanInfo for java.awt.MenuItem.
*
* @author Ales Novak
*/
abstract class MenuItemBeanInfo extends MenuComponentBeanInfo {

    /** no-arg */
    MenuItemBeanInfo() {
    }

    /** @return Propertydescriptors */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] inh = super.getPropertyDescriptors();
        PropertyDescriptor[] desc = new PropertyDescriptor[inh.length + 3];
        System.arraycopy(inh, 0, desc, 0, inh.length);
        try {
            desc[inh.length] = new PropertyDescriptor("actionCommand", java.awt.MenuItem.class); // NOI18N
            desc[inh.length + 1] = new PropertyDescriptor("label", java.awt.MenuItem.class); // NOI18N
            desc[inh.length + 2] = new PropertyDescriptor("enabled", java.awt.MenuItem.class); // NOI18N
            return desc;
        } catch (IntrospectionException ex) {
            return inh;
        }
    }
}
