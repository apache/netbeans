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
import javax.swing.border.BevelBorder;

public class BevelBorderBeanInfo extends BISupport {

    public BevelBorderBeanInfo() {
        super("bevelBorder", javax.swing.border.BevelBorder.class); // NOI18N
    }

    @Override
    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRO(BevelBorder.class, "bevelType"), // NOI18N
            createRO(BevelBorder.class, "highlightOuterColor"), // NOI18N
            createRO(BevelBorder.class, "highlightInnerColor"), // NOI18N
            createRO(BevelBorder.class, "shadowOuterColor"), // NOI18N
            createRO(BevelBorder.class, "shadowInnerColor"), // NOI18N
        };
        pds[0].setPropertyEditorClass(BevelTypePropertyEditor.class);
        return pds;
    }    

    public static class BevelTypePropertyEditor extends BISupport.TaggedPropertyEditor {
        public BevelTypePropertyEditor() {
            super(
                new int[] {
                    BevelBorder.RAISED,
                    BevelBorder.LOWERED,
                },
                new String[] {
                    "javax.swing.border.BevelBorder.RAISED", // NOI18N
                    "javax.swing.border.BevelBorder.LOWERED", // NOI18N
                },
                new String[] {
                    "VALUE_BevelRaised",  // NOI18N
                    "VALUE_BevelLowered", // NOI18N
                }
            );
        }
    }

}
