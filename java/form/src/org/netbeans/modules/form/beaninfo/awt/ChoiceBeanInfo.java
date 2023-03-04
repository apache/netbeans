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
import java.awt.Choice;

/** A BeanInfo for java.awt.Choice.
*
* @author Ales Novak
*/
public class ChoiceBeanInfo extends ComponentBeanInfo.Support {

    public ChoiceBeanInfo() {
        super("choice", java.awt.Choice.class); // NOI18N
    }

    /** @return Propertydescriptors */
    @Override
    protected PropertyDescriptor[] createPDs() throws IntrospectionException {
        return new PropertyDescriptor[] {
            new PropertyDescriptor("selectedObjects", Choice.class, "getSelectedObjects", null), // NOI18N
            new PropertyDescriptor("selectedIndex", Choice.class, "getSelectedIndex", null), // NOI18N
            new PropertyDescriptor("itemCount", Choice.class, "getItemCount", null), // NOI18N
            new PropertyDescriptor("item", Choice.class, "getItem", null), // NOI18N
            new PropertyDescriptor("selectedItem", Choice.class, "getSelectedItem", null), // NOI18N
        };
    }

}
