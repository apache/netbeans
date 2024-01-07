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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;

class ClassConstantElementImpl extends ModelElementImpl implements ClassConstantElement {

    private final String typeName;
    private final String value;
    private final String declaredType;

    ClassConstantElementImpl(Scope inScope, TypeConstantElement indexedConstant) {
        super(inScope, indexedConstant, PhpElementKind.TYPE_CONSTANT);
        assert inScope instanceof TypeScope;
        String in = indexedConstant.getIn();
        if (in != null) {
            typeName = in;
        } else {
            typeName = inScope.getName();
        }
        value = indexedConstant.getValue();
        declaredType = indexedConstant.getDeclaredType();
    }

    ClassConstantElementImpl(Scope inScope, ClassConstantDeclarationInfo clsConst, boolean isDeprecated) {
        super(inScope, clsConst, clsConst.getAccessModifiers(), isDeprecated);
        typeName = inScope.getName();
        value = clsConst.getValue();
        declaredType = clsConst.getDeclaredType();
    }

    @Override
    public String getNormalizedName() {
        return typeName + super.getNormalizedName();
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_CLASS_CONST, getIndexSignature(), true, true);
    }

    private String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(getName()).append(Signature.ITEM_DELIMITER);
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        sb.append(getValue() != null ? Signature.encodeItem(getValue()) : "?").append(Signature.ITEM_DELIMITER); //NOI18N
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        sb.append(getPhpModifiers().toFlags()).append(Signature.ITEM_DELIMITER);
        sb.append((getDeclaredType() == null) ? "" : getDeclaredType()).append(Signature.ITEM_DELIMITER); // NOI18N
        return sb.toString();
    }

    @Override
    public String getValue() {
        return value;
    }

    @CheckForNull
    @Override
    public String getDeclaredType() {
        return declaredType;
    }
}
