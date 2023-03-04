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
import java.awt.FlowLayout;

public class FlowLayoutBeanInfo extends BISupport {

    public FlowLayoutBeanInfo() {
        super("flowLayout", java.awt.FlowLayout.class); // NOI18N
    }

    @Override
    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRW(FlowLayout.class, "alignment"), // NOI18N
            createRW(FlowLayout.class, "alignOnBaseline"), // NOI18N
            createRW(FlowLayout.class, "hgap"), // NOI18N
            createRW(FlowLayout.class, "vgap"), // NOI18N
        };
        pds[0].setPropertyEditorClass(AlignmentPropertyEditor.class);
        return pds;
    }

    
    
    public static class AlignmentPropertyEditor extends BISupport.TaggedPropertyEditor {
        public AlignmentPropertyEditor() {
            super(
                new int[] {
                    FlowLayout.CENTER,
                    FlowLayout.LEFT,
                    FlowLayout.RIGHT,
                    FlowLayout.LEADING,
                    FlowLayout.TRAILING
                },
                new String[] {
                    "java.awt.FlowLayout.CENTER", // NOI18N
                    "java.awt.FlowLayout.LEFT", // NOI18N
                    "java.awt.FlowLayout.RIGHT", // NOI18N
                    "java.awt.FlowLayout.LEADING", // NOI18N
                    "java.awt.FlowLayout.TRAILING" // NOI18N
                },
                new String[] {
                    "VALUE_AlignmentCenter", // NOI18N
                    "VALUE_AlignmentLeft", // NOI18N
                    "VALUE_AlignmentRight", // NOI18N
                    "VALUE_AlignmentLeading", // NOI18N
                    "VALUE_AlignmentTrailing" // NOI18N
                }
            );
        }
    }

}
