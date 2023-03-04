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
package org.netbeans.modules.php.editor.model.nodes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;

/**
 *
 * @author Radek Matous
 */
public final class PhpDocTypeTagInfo extends ASTNodeInfo<PHPDocNode> {
    private final PHPDocTypeTag typeTag;
    private final Kind kind;
    private final String typeName;

    private PhpDocTypeTagInfo(PHPDocTypeTag typeTag, PHPDocNode node, String typeName, Kind kind) {
        super(node);
        this.typeTag = typeTag;
        this.kind = kind;
        this.typeName = typeName;
    }

    public static List<? extends PhpDocTypeTagInfo> create(PHPDocTypeTag typeTag, Kind kind, Scope scope) {
        List<PhpDocTypeTagInfo> retval = new ArrayList<>();
        List<PHPDocTypeNode> types = typeTag.getTypes();
        if (!types.isEmpty()) {
            for (PHPDocNode docNode : types) {
                if (scope != null) {
                    QualifiedName qualifiedName = QualifiedName.create(docNode.getValue());
                    if (VariousUtils.isAliased(qualifiedName, docNode.getStartOffset(), scope)) {
                        LinkedList<String> segments = qualifiedName.getSegments();
                        retval.add(new PhpDocTypeTagInfo(typeTag, docNode, segments.getFirst(), Kind.USE_ALIAS));
                        if (segments.size() > 1) {
                            retval.add(new PhpDocTypeTagInfo(typeTag, docNode, docNode.getValue(), Kind.CLASS));
                        }
                    } else {
                        retval.add(new PhpDocTypeTagInfo(typeTag, docNode, docNode.getValue(), Kind.CLASS));
                    }
                }
            }
        }
        if (!kind.equals(Kind.CLASS) && typeTag instanceof PHPDocVarTypeTag) {
            PHPDocVarTypeTag varTypeTag = (PHPDocVarTypeTag) typeTag;
            if (types.isEmpty()) {
                retval.add(new PhpDocTypeTagInfo(typeTag, varTypeTag.getVariable(), varTypeTag.getVariable().getValue(), kind));
            } else {
                for (PHPDocNode docNode : types) {
                    retval.add(new PhpDocTypeTagInfo(typeTag, varTypeTag.getVariable(), docNode.getValue(), kind));
                }
            }
        }
        return retval;
    }

    public static List<? extends PhpDocTypeTagInfo> create(PHPDocTypeTag typeTag, Scope scope) {
        Kind kind;
        if (scope instanceof ClassScope || scope instanceof TraitScope) {
            kind = Kind.FIELD;
        } else {
            kind = Kind.VARIABLE;
        }
        return create(typeTag, kind, scope);
    }

    public PHPDocTypeTag getTypeTag() {
        return typeTag;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    /**
     * Get the type name.
     *
     * @return the type name (it can be contained the nullable type prefix("?"))
     */
    public String getTypeName() {
        return typeName != null ? typeName : null;
    }

    @Override
    public String getName() {
        PHPDocNode docNode = getOriginalNode();
        String value = docNode.getValue();
        int idx = value.indexOf("::");
        if (idx != -1) {
            value = value.substring(0, idx);
        }
        if (getKind().equals(Kind.CLASS)) {
            QualifiedName qn = QualifiedName.create(value);
            value = qn.toName().toString();
        }
        if (getKind().equals(Kind.USE_ALIAS)) {
            QualifiedName qn = QualifiedName.create(value);
            value = qn.getSegments().getFirst();
        }
        return value;
    }

    @Override
    public QualifiedName getQualifiedName() {
        if (Kind.VARIABLE.equals(getKind()) || Kind.FIELD.equals(getKind())) {
            QualifiedName.createUnqualifiedName(getName());
        }
        String type = CodeUtils.removeNullableTypePrefix(getTypeName());
        return QualifiedName.create(type);
    }

    @Override
    public OffsetRange getRange() {
        PHPDocNode node = getOriginalNode();
        if (Kind.VARIABLE.equals(getKind()) || Kind.FIELD.equals(getKind())) {
            int start = getName().length() > 0 ? node.getStartOffset() + 1 : node.getStartOffset();
            int end = node.getStartOffset() + getName().length();
            return new OffsetRange(start, end);
        }
        if (Kind.USE_ALIAS.equals(getKind())) {
            return new OffsetRange(node.getStartOffset(), node.getStartOffset() + getName().length());
        }
        String type = CodeUtils.removeNullableTypePrefix(getTypeName());
        QualifiedName typeQN = QualifiedName.create(type);
        final QualifiedName namespaceName = typeQN.toNamespaceName(typeQN.getKind().isFullyQualified());
        final int nsNameLength = namespaceName.toString().length();
        int startOffset = node.getStartOffset();
        if (CodeUtils.isNullableType(getTypeName())) {
            startOffset++;
        }
        if (nsNameLength > 0) {
            startOffset += nsNameLength;
            if (namespaceName.getSegments().size() > 0 && !namespaceName.isDefaultNamespace()) {
                startOffset += 1;
            }
        }
        return new OffsetRange(startOffset, startOffset + typeQN.toName().toString().length());
    }

    public PhpModifiers getAccessModifiers() {
        return PhpModifiers.fromBitMask(PhpModifiers.PUBLIC);
    }

}
