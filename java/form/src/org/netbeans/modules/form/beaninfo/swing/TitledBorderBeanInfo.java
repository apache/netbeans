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
import javax.swing.border.TitledBorder;

public class TitledBorderBeanInfo extends BISupport {

    public TitledBorderBeanInfo() {
        super("titledBorder", javax.swing.border.TitledBorder.class); // NOI18N
    }

    @Override
    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        PropertyDescriptor[] pds = new PropertyDescriptor[] {
            createRW(TitledBorder.class, "border"), // NOI18N
            createRW(TitledBorder.class, "title"), // NOI18N
            createRW(TitledBorder.class, "titleJustification"), // NOI18N
            createRW(TitledBorder.class, "titlePosition"), // NOI18N
            createRW(TitledBorder.class, "titleColor"), // NOI18N
            createRW(TitledBorder.class, "titleFont"), // NOI18N
        };
        pds[2].setPropertyEditorClass(JustificationPropertyEditor.class);
        pds[3].setPropertyEditorClass(PositionPropertyEditor.class);
        return pds;
    }    


    public static class PositionPropertyEditor extends BISupport.TaggedPropertyEditor {
        public PositionPropertyEditor() {
            super(
                new int[] {
                    TitledBorder.DEFAULT_POSITION,
                    TitledBorder.ABOVE_TOP,
                    TitledBorder.TOP,
                    TitledBorder.BELOW_TOP,
                    TitledBorder.ABOVE_BOTTOM,
                    TitledBorder.BOTTOM,
                    TitledBorder.BELOW_BOTTOM
                },
                new String[] {
                    "javax.swing.border.TitledBorder.DEFAULT_POSITION", // NOI18N
                    "javax.swing.border.TitledBorder.ABOVE_TOP", // NOI18N
                    "javax.swing.border.TitledBorder.TOP", // NOI18N
                    "javax.swing.border.TitledBorder.BELOW_TOP", // NOI18N
                    "javax.swing.border.TitledBorder.ABOVE_BOTTOM", // NOI18N
                    "javax.swing.border.TitledBorder.BOTTOM", // NOI18N
                    "javax.swing.border.TitledBorder.BELOW_BOTTOM" // NOI18N
                },
                new String[] {
                    "VALUE_PosDefault", // NOI18N
                    "VALUE_PosAboveTop", // NOI18N
                    "VALUE_PosTop", // NOI18N
                    "VALUE_PosBelowTop", // NOI18N
                    "VALUE_PosAboveBottom", // NOI18N
                    "VALUE_PosBottom", // NOI18N
                    "VALUE_PosBelowBottom", // NOI18N
                }
            );
        }
    }

    public static class JustificationPropertyEditor extends BISupport.TaggedPropertyEditor {
        public JustificationPropertyEditor() {
            super(
                new int[] {
                    TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.LEFT,
                    TitledBorder.CENTER,
                    TitledBorder.RIGHT,
                    TitledBorder.LEADING,
                    TitledBorder.TRAILING,
                },
                new String[] {
                    "javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION", // NOI18N
                    "javax.swing.border.TitledBorder.LEFT", // NOI18N
                    "javax.swing.border.TitledBorder.CENTER", // NOI18N
                    "javax.swing.border.TitledBorder.RIGHT", // NOI18N
                    "javax.swing.border.TitledBorder.LEADING", // NOI18N
                    "javax.swing.border.TitledBorder.TRAILING", // NOI18N
                },
                new String[] {
                    "VALUE_JustDefault", // NOI18N
                    "VALUE_JustLeft", // NOI18N
                    "VALUE_JustCenter", // NOI18N
                    "VALUE_JustRight", // NOI18N
                    "VALUE_JustLeading", // NOI18N
                    "VALUE_JustTrailing", // NOI18N
                }
            );
        }
    }

}
