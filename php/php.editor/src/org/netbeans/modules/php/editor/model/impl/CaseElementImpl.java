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
package org.netbeans.modules.php.editor.model.impl;

import java.util.Locale;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.EnumCaseElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.CaseElement;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.CaseDeclarationInfo;

class CaseElementImpl extends ModelElementImpl implements CaseElement {

    private final String typeName;
    private final String value;
    private final boolean isBacked;
    private static final String UNKOWN_VALUE = "?"; // NOI18N

    CaseElementImpl(Scope inScope, EnumCaseElement indexedCase) {
        super(inScope, indexedCase, PhpElementKind.ENUM_CASE);
        assert inScope instanceof TypeScope;
        String in = indexedCase.getIn();
        if (in != null) {
            typeName = in;
        } else {
            typeName = inScope.getName();
        }
        value = indexedCase.getValue();
        isBacked = indexedCase.isBacked();
    }

    CaseElementImpl(Scope inScope, CaseDeclarationInfo info, boolean isDeprecated) {
        super(inScope, info, info.getAccessModifiers(), isDeprecated);
        typeName = inScope.getName();
        value = info.getValue();
        if (inScope instanceof EnumScope) {
            EnumScope enumScope = (EnumScope) inScope;
            isBacked = enumScope.getBackingType() != null;
        } else {
            isBacked = false;
        }
    }

    @Override
    public String getNormalizedName() {
        return typeName + super.getNormalizedName();
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_ENUM_CASE, getIndexSignature(), true, true);
    }

    private String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase(Locale.ROOT)).append(Signature.ITEM_DELIMITER); // 0: lower case name
        sb.append(getName()).append(Signature.ITEM_DELIMITER); // 1: name
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER); // 2: offset
        sb.append(getValue() != null ? Signature.encodeItem(getValue()) : UNKOWN_VALUE).append(Signature.ITEM_DELIMITER); // 3: value
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER); // 4: deprecated
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER); // 5: file name url
        sb.append(getPhpModifiers().toFlags()).append(Signature.ITEM_DELIMITER); // 6: modifiers
        sb.append(isBacked() ? 1 : 0).append(Signature.ITEM_DELIMITER); // 7: isBacked
        return sb.toString();
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isBacked() {
        return isBacked;
    }
}
