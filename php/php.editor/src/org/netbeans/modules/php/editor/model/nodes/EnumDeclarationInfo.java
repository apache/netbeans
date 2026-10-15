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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;

public class EnumDeclarationInfo extends ASTNodeInfo<EnumDeclaration> {

    EnumDeclarationInfo(EnumDeclaration node) {
        super(node);
    }

    public static EnumDeclarationInfo create(EnumDeclaration enumDeclaration) {
        return new EnumDeclarationInfo(enumDeclaration);
    }

    @Override
    public Kind getKind() {
        return Kind.ENUM;
    }

    @Override
    public String getName() {
        EnumDeclaration enumDeclaration = getOriginalNode();
        return enumDeclaration.getName().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.create(getOriginalNode().getName());
    }

    @CheckForNull
    public QualifiedName getBackingType() {
        return QualifiedName.create(getOriginalNode().getBackingType());
    }

    public List<? extends Expression> getInterfaces() {
        return getOriginalNode().getInterfaces();
    }

    public Set<QualifiedName> getInterfaceNames() {
        final Set<QualifiedName> retval = new HashSet<>();
        final List<Expression> interfaes = getOriginalNode().getInterfaces();
        for (Expression iface : interfaes) {
            QualifiedName ifaceName = QualifiedName.create(iface);
            if (ifaceName != null) {
                retval.add(ifaceName);
            }
        }
        return retval;
    }

    @Override
    public OffsetRange getRange() {
        EnumDeclaration enumDeclaration = getOriginalNode();
        Identifier name = enumDeclaration.getName();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public Collection<QualifiedName> getUsedTraits() {
        final UsedTraitsVisitor visitor = new UsedTraitsVisitor();
        getOriginalNode().getBody().accept(visitor);
        return visitor.getUsedTraits();
    }

}
