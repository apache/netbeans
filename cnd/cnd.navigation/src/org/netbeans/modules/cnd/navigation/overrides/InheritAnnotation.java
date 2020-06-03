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
import java.util.Collections;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.openide.util.NbBundle;

/**
 *
 */
/*package*/ class InheritAnnotation extends BaseAnnotation {

    public InheritAnnotation(StyledDocument document, CsmClass decl,
            Collection<? extends CsmOffsetableDeclaration> descDecls,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates,
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations) {
        super(document, decl, Collections.<CsmOffsetableDeclaration>emptyList(), descDecls, baseTemplates, templateSpecializations);
    }


    @Override
    public String getShortDescription() {
        String out = descUIDs.isEmpty() ? "" : NbBundle.getMessage(getClass(), "LAB_Extended");
        out = addTemplateAnnotation(out);
        return out;
    }

    @Override
    protected CharSequence debugTypeString() {
        switch (type) {
            case OVERRIDES:
                return "INHERITS"; // NOI18N
            case IS_OVERRIDDEN:
                return "INHERITED"; // NOI18N
            case SPECIALIZES:
                return "SPECIALIZES"; // NOI18N
            case IS_SPECIALIZED:
                return "IS_SPECIALIZED"; // NOI18N
            case OVERRIDEN_COMBINED:
                return "INHERITS_AND_INHERITED"; // NOI18N
            case EXTENDED_SPECIALIZES:
                return "EXTENDED_SPECIALIZES"; // NOI18N
            case EXTENDED_IS_SPECIALIZED:
                return "EXTENDED_IS_SPECIALIZED"; // NOI18N
            default:
                return "???"; // NOI18N
        }
    }

}
