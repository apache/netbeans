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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.elements.ParameterElementImpl;
import org.netbeans.modules.php.editor.elements.TypeResolverImpl;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.IntersectionType;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.openide.util.Pair;

/**
 *
 * @author Radek Matous
 */
public final class FormalParameterInfo extends ASTNodeInfo<FormalParameter> {
    private final ParameterElement parameter;

    private FormalParameterInfo(FormalParameter node, Map<String, Pair<String, List<Pair<QualifiedName, Boolean>>>> paramDocTypes) {
        super(node);
        FormalParameter formalParameter = getOriginalNode();
        String name = getName();
        String defVal = CodeUtils.getParamDefaultValue(formalParameter);
        Expression parameterType = formalParameter.getParameterType();
        final boolean isRawType = parameterType != null;
        final boolean isNullableType = parameterType instanceof NullableType;
        final boolean isUnionType = parameterType instanceof UnionType;
        final boolean isIntersectionType = parameterType instanceof IntersectionType;
        QualifiedName parameterTypeName = QualifiedName.create(parameterType);
        List<Pair<QualifiedName, Boolean>> types;
        final String declaredType = isRawType ? CodeUtils.extractQualifiedName(parameterType) : null;
        final String phpDocType = (!paramDocTypes.isEmpty() && paramDocTypes.get(name) != null)
                ? paramDocTypes.get(name).first()
                : null;
        if (isRawType && parameterTypeName != null) {
            if (!Type.isPrimitive(parameterTypeName.toString()) || paramDocTypes.isEmpty()) {
                types = Collections.singletonList(Pair.of(parameterTypeName, isNullableType));
            } else {
                types = getParamDocTypes(paramDocTypes, name);
            }
        } else if (isUnionType) {
            types = VariousUtils.getParamTypesFromUnionTypes((UnionType) parameterType);
        } else if (isIntersectionType) {
            types = VariousUtils.getParamTypesFromIntersectionTypes((IntersectionType) parameterType);
        } else {
            types = getParamDocTypes(paramDocTypes, name);
        }
        if (types == null) {
            types = Collections.emptyList();
        }
        this.parameter = new ParameterElementImpl(
                name,
                defVal,
                getRange().getStart(),
                declaredType,
                phpDocType,
                TypeResolverImpl.forNames(types),
                formalParameter.isMandatory(),
                isRawType,
                formalParameter.isReference(),
                formalParameter.isVariadic(),
                formalParameter.isUnionType(),
                formalParameter.getModifier(),
                formalParameter.isIntersectionType()
        );
    }

    private List<Pair<QualifiedName, Boolean>> getParamDocTypes(Map<String, Pair<String, List<Pair<QualifiedName, Boolean>>>> paramDocTypes, String name) {
        return paramDocTypes.get(name) != null ? paramDocTypes.get(name).second() : Collections.emptyList();
    }

    public static FormalParameterInfo create(FormalParameter node, Map<String, Pair<String, List<Pair<QualifiedName, Boolean>>>> paramDocTypes) {
        return new FormalParameterInfo(node, paramDocTypes);
    }

    @Override
    public Kind getKind() {
        return Kind.PARAMETER;
    }

    @Override
    public String getName() {
        FormalParameter formalParameter = getOriginalNode();
        return ASTNodeInfo.toName(formalParameter.getParameterName());
    }

    @Override
    public QualifiedName getQualifiedName() {
        QualifiedName qName = QualifiedName.create(getOriginalNode().getParameterName());
        return qName != null ? qName : QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        FormalParameter formalParameter = getOriginalNode();
        return ASTNodeInfo.toOffsetRange(formalParameter.getParameterName());
    }

    public ParameterElement toParameter() {
        return parameter;
    }
}
