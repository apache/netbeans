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

package org.netbeans.modules.form.beaninfo.swing;

import java.beans.*;
import javax.swing.border.SoftBevelBorder;

public class SoftBevelBorderBeanInfo extends BISupport {

    public SoftBevelBorderBeanInfo() {
        super("softBevelBorder", javax.swing.border.SoftBevelBorder.class); // NOI18N
    }

    @Override
    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRO(SoftBevelBorder.class, "bevelType"), // NOI18N
            createRO(SoftBevelBorder.class, "highlightOuterColor"), // NOI18N
            createRO(SoftBevelBorder.class, "highlightInnerColor"), // NOI18N
            createRO(SoftBevelBorder.class, "shadowOuterColor"), // NOI18N
            createRO(SoftBevelBorder.class, "shadowInnerColor"), // NOI18N
        };
        pds[0].setPropertyEditorClass(BevelBorderBeanInfo.BevelTypePropertyEditor.class);
        return pds;
    }
    
}
