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
