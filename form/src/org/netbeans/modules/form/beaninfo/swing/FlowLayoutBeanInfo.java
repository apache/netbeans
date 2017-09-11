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
