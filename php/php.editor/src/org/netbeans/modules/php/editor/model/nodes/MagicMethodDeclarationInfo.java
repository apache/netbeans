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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.elements.TypeResolverImpl;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
public class MagicMethodDeclarationInfo extends ASTNodeInfo<PHPDocMethodTag> {
    private final List<ParameterElement> parameters = new LinkedList<>();
    private String returnType;
    private String methodName;
    private int offset;
    private int typeOffset;
    private final boolean isStatic;
    private static final Pattern SPLIT_METHOD_NAMES_PATTERN = Pattern.compile("[(, ]"); // NOI18N

    MagicMethodDeclarationInfo(PHPDocMethodTag node) {
        super(node);
        // @method int get(Type $object) message
        // @method static int staticGet(Type $object) message
        String[] parts = CodeUtils.WHITE_SPACES_PATTERN.split(node.getValue().trim(), 3);
        isStatic = parts.length >= 1 && parts[0].equals(Type.STATIC); // NETBEANS-1861
        // the method is already checked whether it is static when PHPDocMethodTag is created
        // So, they should be the same result
        // see: PHPDocCommentParser.createTag()
        assert isStatic == node.isStatic() : "PHPDocMethodTag static: " + node.isStatic(); // NOI18N
        if (isStatic) {
            parts = Arrays.copyOfRange(parts, 1, parts.length);
        }
        String mtdName = node.getMethodName().getValue();
        if (parts.length == 1 || (parts.length > 0 && parts[0].trim().startsWith(mtdName))) { // don't check '(' because DNF return type has '(' e.g. (X&Y)|Z method()
            // expect that the type is void
            returnType = Type.VOID;
            String[] methodNames = SPLIT_METHOD_NAMES_PATTERN.split(parts[0], 2);
            if (methodNames.length > 0) {
                methodName = methodNames[0];
                offset = getOriginalNode().getStartOffset() + PHPDocTag.Type.METHOD.toString().length() + 1 + node.getValue().indexOf(methodName);
            }
        } else if (parts.length >= 2) {
            String[] methodNames = SPLIT_METHOD_NAMES_PATTERN.split(parts[1], 2);
            if (parts[0].length() > 0 && methodNames.length > 0) {
                returnType = parts[0];
                methodName = methodNames[0];
                offset = getOriginalNode().getStartOffset() + PHPDocTag.Type.METHOD.toString().length() + 1 + node.getValue().indexOf(methodName);
                typeOffset = getOriginalNode().getStartOffset() + PHPDocTag.Type.METHOD.toString().length() + 1 + node.getValue().indexOf(returnType);
            }
        }

        for (PHPDocVarTypeTag parameter : node.getParameters()) {
            Collection<Pair<QualifiedName, Boolean>> names = new LinkedList<>();
            String declaredType = null;
            if (!parameter.getTypes().isEmpty()) {
                // e.g. (X&Y)|Z $param = null
                String[] paramValues = CodeUtils.WHITE_SPACES_PATTERN.split(parameter.getValue().trim(), 2);
                if (!paramValues[0].trim().startsWith("$")) { // NOI18N
                    declaredType = paramValues[0].trim();
                    assert !declaredType.isEmpty() : parameter.getValue();
                }
            }
            for (PHPDocTypeNode type : parameter.getTypes()) {
                String typeName = type.getValue();
                boolean isNullableType = CodeUtils.isNullableType(typeName);
                if (isNullableType) {
                    typeName = typeName.substring(1);
                }
                QualifiedName qualifiedName = QualifiedName.create(typeName);
                names.add(Pair.of(qualifiedName, isNullableType));
            }
            Set<TypeResolver> types = TypeResolverImpl.forNames(names);
            String name = parameter.getVariable().getValue();
            String[] split = parameter.getValue().split("="); // NOI18N
            String defaultValue = null;
            if (split.length > 1) {
                defaultValue = split[1].trim();
            }
            boolean isMandatory = defaultValue == null;
            boolean isReference = name.startsWith("&"); // NOI18N
            boolean isVariadic = name.startsWith("..."); // NOI18N
            boolean isRawType = declaredType != null;
            parameters.add(new ParameterElementImpl(name, defaultValue, 0, declaredType, declaredType, types, isMandatory, isRawType, isReference, isVariadic, false, 0, false));
        }
    }

    @CheckForNull
    public static MagicMethodDeclarationInfo create(PHPDocMethodTag node) {
        MagicMethodDeclarationInfo retval = new MagicMethodDeclarationInfo(node);
        return (retval.methodName != null && retval.returnType != null) ? retval : null;
    }

    public ASTNodeInfo<PHPDocMethodTag> getClassInfo() {
        return new ASTNodeInfo<PHPDocMethodTag>(getOriginalNode()) {

            @Override
            public String getName() {
                return MagicMethodDeclarationInfo.this.getReturnType();
            }

            @Override
            public OffsetRange getRange() {
                return MagicMethodDeclarationInfo.this.getTypeRange();
            }


            @Override
            public Kind getKind() {
                return Kind.CLASS;
            }

            @Override
            public QualifiedName getQualifiedName() {
                return QualifiedName.create(getName());
            }

            @Override
            public PhpElementKind getPhpElementKind() {
                return PhpElementKind.CLASS;
            }
        };
    }

    @Override
    public Kind getKind() {
        return isStatic ? Kind.STATIC_METHOD : Kind.METHOD;
    }

    @Override
    public String getName() {
        return getQualifiedName().toName().toString();
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.create(methodName).toName();
    }

    @Override
    public OffsetRange getRange() {
        return new OffsetRange(offset, offset + getName().length());
    }

    public OffsetRange getTypeRange() {
        return new OffsetRange(typeOffset, typeOffset + getReturnType().length());
    }

    public List<? extends ParameterElement> getParameters() {
        return new ArrayList<>(parameters);
    }

    public PhpModifiers getAccessModifiers() {
        int modifiers = isStatic ? (PhpModifiers.PUBLIC | PhpModifiers.STATIC) : PhpModifiers.PUBLIC;
        return PhpModifiers.fromBitMask(modifiers);
    }
}
