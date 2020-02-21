/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.navigation.overrides;

import java.util.Collection;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.openide.util.NbBundle;

/**
 *
 */
/*package*/ class OverrideAnnotation extends BaseAnnotation {

    public OverrideAnnotation(StyledDocument document, CsmFunction decl, CsmVirtualInfoQuery.CsmOverrideInfo thisMethod,
            Collection<CsmVirtualInfoQuery.CsmOverrideInfo> baseDecls, 
            Collection<CsmVirtualInfoQuery.CsmOverrideInfo> descDecls,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates, 
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations) {
        super(document, decl, thisMethod, baseDecls, descDecls, baseTemplates, templateSpecializations);
    }

    @Override
    public String getShortDescription() {
        String out = "";
        if (baseUIDs.isEmpty() && pseudoBaseUIDs.isEmpty() && (!descUIDs.isEmpty() || !pseudoDescUIDs.isEmpty())) {
            out = NbBundle.getMessage(getClass(), "LAB_IsOverriden");
        } else if ((!baseUIDs.isEmpty() || !pseudoBaseUIDs.isEmpty()) && descUIDs.isEmpty() && pseudoDescUIDs.isEmpty()) {
            CharSequence text = "..."; //NOI18N
            if (baseUIDs.size() == 1) {
                CsmOffsetableDeclaration obj = baseUIDs.iterator().next().getObject();
                if (obj != null) {
                    text = obj.getQualifiedName();
                }
            } else if (pseudoBaseUIDs.size() == 1) {
                CsmOffsetableDeclaration obj = pseudoBaseUIDs.iterator().next().getObject();
                if (obj != null) {
                    text = obj.getQualifiedName();
                }
            }
            out = NbBundle.getMessage(getClass(), "LAB_Overrides", text);
        } else if ((!baseUIDs.isEmpty() || !pseudoBaseUIDs.isEmpty()) && (!descUIDs.isEmpty() || !pseudoDescUIDs.isEmpty())) {
            out = NbBundle.getMessage(getClass(), "LAB_OverridesAndIsOverriden");
        } else if (baseTemplateUIDs.isEmpty() && specializationUIDs.isEmpty()) { //both are empty
            throw new IllegalArgumentException("Either overrides or overridden should be non empty"); //NOI18N
        }
        out = addTemplateAnnotation(out);
        return out;
    }

    @Override
    protected CharSequence debugTypeString() {
        switch (type) {
            case OVERRIDES:
                return "OVERRIDES"; // NOI18N
            case OVERRIDES_PSEUDO:
                return "OVERRIDES_PSEUDO"; // NOI18N
            case IS_OVERRIDDEN:
                return "OVERRIDDEN"; // NOI18N
            case IS_OVERRIDDEN_PSEUDO:
                return "OVERRIDDEN_PSEUDO"; // NOI18N
            case SPECIALIZES:
                return "SPECIALIZES"; // NOI18N
            case IS_SPECIALIZED:
                return "IS_SPECIALIZED"; // NOI18N
            case OVERRIDEN_COMBINED:
                return "OVERRIDES_AND_OVERRIDDEN"; // NOI18N
            case OVERRIDEN_COMBINED_PSEUDO:
                return "OVERRIDES_AND_OVERRIDDEN_PSEUDO"; // NOI18N
            case EXTENDED_SPECIALIZES:
                return "EXTENDED_SPECIALIZES_FUNCTION"; // NOI18N
            case EXTENDED_IS_SPECIALIZED:
                return "EXTENDED_IS_SPECIALIZED_FUNCTION"; // NOI18N
            default:
                return "???"; // NOI18N
        }
    }
}
