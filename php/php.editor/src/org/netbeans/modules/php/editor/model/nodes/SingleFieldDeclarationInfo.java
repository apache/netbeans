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

package org.netbeans.modules.php.editor.model.nodes;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;

/**
 *
 * @author Radek Matous
 */
public final class SingleFieldDeclarationInfo extends ASTNodeInfo<SingleFieldDeclaration> {

    private final FieldsDeclaration fieldsDeclaration;

    private SingleFieldDeclarationInfo(FieldsDeclaration fieldsDeclaration, SingleFieldDeclaration node) {
        super(node);
        this.fieldsDeclaration = fieldsDeclaration;
    }

    public static List<? extends SingleFieldDeclarationInfo> create(FieldsDeclaration fd) {
        List<SingleFieldDeclarationInfo> retval = new ArrayList<>();
        List<SingleFieldDeclaration> fields = fd.getFields();
        for (SingleFieldDeclaration singleFieldDeclaration : fields) {
            retval.add(new SingleFieldDeclarationInfo(fd, singleFieldDeclaration));
        }
        return retval;
    }

    @Override
    public Kind getKind() {
        return getAccessModifiers().isStatic() ? Kind.STATIC_FIELD : Kind.FIELD;
    }

    @Override
    public String getName() {
        return ASTNodeInfo.toNameField(getOriginalNode().getName());
    }

    @Override
    public OffsetRange getRange() {
        return ASTNodeInfo.toOffsetRangeVar(getOriginalNode().getName());
    }

    public PhpModifiers getAccessModifiers() {
        return PhpModifiers.fromBitMask(fieldsDeclaration.getModifier());
    }

    @CheckForNull
    public String getFieldType() {
        return VariousUtils.getDeclaredType(fieldsDeclaration.getFieldType());
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

}
