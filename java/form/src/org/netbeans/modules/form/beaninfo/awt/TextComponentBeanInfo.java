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

/** A BeanInfo for java.awt.TextComponent.
*
* @author Ales Novak
*/
class TextComponentBeanInfo extends ComponentBeanInfo.Support {

    /** no-arg */
    TextComponentBeanInfo() {
        super(null, java.awt.TextComponent.class);
    }

    /** @return Propertydescriptors */
    @Override
    public PropertyDescriptor[] createPDs() throws IntrospectionException {
        return new PropertyDescriptor[] {
            new PropertyDescriptor("selectionStart", java.awt.TextComponent.class), // NOI18N
            new PropertyDescriptor("text", java.awt.TextComponent.class), // NOI18N
            new PropertyDescriptor("caretPosition", java.awt.TextComponent.class), // NOI18N
            new PropertyDescriptor("selectionEnd", java.awt.TextComponent.class), // NOI18N
            new PropertyDescriptor("editable", java.awt.TextComponent.class), // NOI18N
        };
    }
}
