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

package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class  AssignmentImpl<Container extends ModelElementImpl>  extends ScopeImpl {
    private Container container;
    @NullAllowed
    private Union2<List<String>, Collection<? extends TypeScope>> typeNameScopes;
    private OffsetRange scopeRange;
    private boolean arrayAccess;
    private boolean conditionalBlock;
    private boolean catchClause;

    AssignmentImpl(
            Container container,
            Scope scope,
            OffsetRange scopeRange,
            OffsetRange nameRange,
            Assignment assignment,
            Map<String, AssignmentImpl> allAssignments,
            boolean isDeprecated) {
        this(container, scope, scopeRange, nameRange, VariousUtils.extractVariableTypeFromAssignment(assignment, allAssignments), isDeprecated);
        if (assignment.getLeftHandSide() instanceof ArrayAccess) {
            arrayAccess = true;
        }
    }

    AssignmentImpl(Container container, Scope scope, OffsetRange scopeRange, OffsetRange nameRange, @NullAllowed String typeName, boolean isDeprecated) {
        super(scope, container.getName(), container.getFile(), nameRange, container.getPhpElementKind(), isDeprecated);
        this.container = container;
        List<String> types = new ArrayList<>();
        if (typeName != null) {
            List<String> typeNames = StringUtils.explode(typeName, Type.SEPARATOR);
            typeNames.forEach(type -> {
                String modifiedTypeName = type != null ? type.trim() : null;
                boolean isNullableType = CodeUtils.isNullableType(modifiedTypeName);
                if (isNullableType) {
                    modifiedTypeName = modifiedTypeName.substring(1);
                }
                if (modifiedTypeName != null && !VariousUtils.isSemiType(type)) {
                    QualifiedName qualifiedName = QualifiedName.create(modifiedTypeName);
                    QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(qualifiedName, nameRange.getStart(), scope);
                    if (qualifiedName.getSegments().size() != fullyQualifiedName.getSegments().size()) {
                        modifiedTypeName = fullyQualifiedName.toString();
                    }
                }
                if (isNullableType) {
                    modifiedTypeName = CodeUtils.NULLABLE_TYPE_PREFIX + modifiedTypeName;
                }
                types.add(modifiedTypeName);
            });
        }
        this.typeNameScopes = Union2.<List<String>, Collection<? extends TypeScope>>createFirst(types);
        this.scopeRange = scopeRange;
    }

    boolean canBeProcessed(String tName) {
        return canBeProcessed(tName, getName()) && canBeProcessed(tName, getName().substring(1));
    }

    static boolean canBeProcessed(String tName, String name) {
        if (tName.length() > 0 && tName.indexOf(name) == -1) {
            return true;
        } else {
            String varThis = VariousUtils.VAR_TYPE_PREFIX + "$this"; // NOI18N
            int indexOfVarThis = tName.indexOf(varThis);
            if (indexOfVarThis != -1 && !name.equals(varThis)) {
                tName = tName.substring(0, indexOfVarThis) + tName.substring(indexOfVarThis + varThis.length());
                return tName.length() > 0 && tName.indexOf(name) == -1;
            }
            return false;
        }
    }

    @CheckForNull
    private Collection<? extends TypeScope> typesFromUnion() {
        if (typeNameScopes != null) {
            if (typeNameScopes.hasSecond() && typeNameScopes.second() != null) {
                return typeNameScopes.second();
            }
        }
        return null;
    }

    /**
     * Get the type name(s) from Union.
     *
     * <b>Note:</b> If a type is a nullable type, it has "?" as a prefix. e.g.
     * ?\Foo, ?string. in the case of the union type, type names separated with
     * "|".
     *
     * @return the type name(s)
     */
    @CheckForNull
    String typeNameFromUnion() {
        List<String> typeNames = typeNamesFromUnion();
        return !typeNames.isEmpty() ? Type.asUnionType(typeNames) : null;
    }

    private List<String> typeNamesFromUnion() {
        if (typeNameScopes != null) {
            if (typeNameScopes.hasFirst() && !typeNameScopes.first().isEmpty()) {
                return typeNameScopes.first();
            } else if (typeNameScopes.hasSecond() && typeNameScopes.second() != null) {
                TypeScope type = ModelUtils.getFirst(typeNameScopes.second());
                return type != null ? Collections.singletonList(type.getName()) : Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
    /**
     * Get the type name from Union.
     *
     * @param withoutNullableTypePrefix {@code true} if remove "?" from type
     * name, otherwise {@code false}
     * @return the type name
     */
    String typeNameFromUnion(boolean withoutNullableTypePrefix) {
        if (withoutNullableTypePrefix) {
            return CodeUtils.removeNullableTypePrefix(typeNameFromUnion());
        }
        return typeNameFromUnion();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" == ").append(typeNameScopes);
        return sb.toString();
    }

    public Collection<? extends String> getTypeNames() {
        return typeNamesFromUnion();
    }

    public Collection<? extends TypeScope> getTypes() {
        List<? extends TypeScope> empty = Collections.emptyList();
        Collection<? extends TypeScope> types = typesFromUnion();
        if (types != null) {
            return types;
        }
        String tName = typeNameFromUnion(true);
        if (tName != null) {
            //StackOverflow prevention
            if (canBeProcessed(tName)) {
                types = VariousUtils.getType((VariableScope) getInScope(), tName, getOffset(), false);
            }
        }
        if (types != null) {
            if (types.isEmpty() && tName != null && !VariousUtils.isSemiType(tName)) {
                return empty;
            }
            typeNameScopes = Union2.<List<String>, Collection<? extends TypeScope>>createSecond(types);
            return types;
        } else {
            typeNameScopes = null;
        }
        return empty;
    }

    Container getContainer() {
        return container;
    }

    @Override
    public OffsetRange getBlockRange() {
        return scopeRange;
    }

    @Override
    public String getNormalizedName() {
        return getClass().getName() + ":" + toString() + ":" + String.valueOf(getOffset()); //NOI18N
    }

    public boolean isArrayAccess() {
        final String tpName = typeNameFromUnion(true);
        return arrayAccess || (tpName != null && tpName.equals(Type.ARRAY));
    }

    public void setAsArrayAccess(boolean arrayAccess) {
        this.arrayAccess = arrayAccess;
    }

    /**
     * @return the conditionalBlock
     */
    public boolean isConditionalBlock() {
        return conditionalBlock;
    }

    /**
     * @param conditionalBlock the conditionalBlock to set
     */
    public void setConditionalBlock(boolean conditionalBlock) {
        this.conditionalBlock = conditionalBlock;
    }

    public boolean isCatchClause() {
        return catchClause;
    }

    void setCatchClause(boolean catchClause) {
        this.catchClause = catchClause;
    }
}
