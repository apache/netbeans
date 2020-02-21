/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.modules.cnd.refactoring.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.refactoring.support.MemberInfo;
import org.openide.util.NbBundle;

/**
 *
 */
public final class InsertPoint {
    public static final InsertPoint DEFAULT = new InsertPoint(null, null, null, Integer.MIN_VALUE,
            NbBundle.getMessage(InsertPoint.class, "EncapsulateFieldPanel.jComboInsertPoint.default")); // NOI18N
    private final int index;
    private final String description;
    private final CsmOffsetable elemDecl;
    private final CsmOffsetable elemDef;
    private final CsmClass clazz;

    public static boolean initInsertPoints(JComboBox jComboInsertPoint, CsmClass encloser) {
        List<InsertPoint> result = new ArrayList<>();
        int idx = 0;
        boolean hasOutOfClassMemberDefinitions = false;
        for (CsmMember member : encloser.getMembers()) {
            if (CsmKindUtilities.isMethod(member)) {
                CsmMethod method = (CsmMethod) member;
                CsmFunction definition = ((CsmFunction)method).getDefinition();
                InsertPoint ip = new InsertPoint(encloser, method, definition, idx + 1, NbBundle.getMessage(
                        EncapsulateFieldPanel.class,
                        "MSG_EncapsulateFieldInsertPointMethod", // NOI18N
                        MemberInfo.create(method).getHtmlText()
                        ));
                if (definition != null && definition != method) {
                    hasOutOfClassMemberDefinitions = true;
                }
                result.add(ip);
            }
            ++idx;
        }
        jComboInsertPoint.addItem(InsertPoint.DEFAULT);
        if (!result.isEmpty()) {
            InsertPoint first = new InsertPoint(encloser, null, null, Integer.MIN_VALUE,
            NbBundle.getMessage(InsertPoint.class, "EncapsulateFieldPanel.jComboInsertPoint.first")); // NOI18N
            InsertPoint last = new InsertPoint(encloser, null, null, Integer.MAX_VALUE,
            NbBundle.getMessage(InsertPoint.class, "EncapsulateFieldPanel.jComboInsertPoint.last")); // NOI18N
            jComboInsertPoint.addItem(first); // NOI18N
            jComboInsertPoint.addItem(last); // NOI18N
            for (InsertPoint ip : result) {
                jComboInsertPoint.addItem(ip);
            }
        }
        jComboInsertPoint.setSelectedItem(InsertPoint.DEFAULT);
        return hasOutOfClassMemberDefinitions;
    }


    private InsertPoint(CsmClass clazz, CsmOffsetable elemDecl, CsmOffsetable elemDef, int index, String description) {
        this.index = index;
        this.description = description;
        this.elemDecl = elemDecl;
        this.elemDef = elemDef;
        this.clazz = clazz;
    }

    public CsmClass getContainerClass() {
        return clazz;
    }

    public CsmOffsetable getElementDeclaration() {
        return elemDecl;
    }

    public CsmOffsetable getElementDefinition() {
        return elemDef;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return description;
    }

}
