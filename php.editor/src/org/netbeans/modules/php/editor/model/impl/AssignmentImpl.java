/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
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
    //TODO: typeName should be list or array to keep mixed types
    private Union2<String, Collection<? extends TypeScope>> typeNameScopes;
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

    AssignmentImpl(Container container, Scope scope, OffsetRange scopeRange, OffsetRange nameRange, String typeName, boolean isDeprecated) {
        super(scope, container.getName(), container.getFile(), nameRange, container.getPhpElementKind(), isDeprecated);
        this.container = container;
        String modifiedTypeName = typeName;
        boolean isNullableType = CodeUtils.isNullableType(modifiedTypeName);
        if (isNullableType) {
            modifiedTypeName = modifiedTypeName.substring(1);
        }
        if (modifiedTypeName != null && !VariousUtils.isSemiType(modifiedTypeName)) {
            QualifiedName qualifiedName = QualifiedName.create(modifiedTypeName);
            QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(qualifiedName, nameRange.getStart(), scope);
            if (qualifiedName.getSegments().size() != fullyQualifiedName.getSegments().size()) {
                modifiedTypeName = fullyQualifiedName.toString();
            }
        }
        if (isNullableType) {
            modifiedTypeName = CodeUtils.NULLABLE_TYPE_PREFIX + modifiedTypeName;
        }
        this.typeNameScopes = Union2.<String, Collection<? extends TypeScope>>createFirst(modifiedTypeName);
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
     * Get the type name from Union.
     * <b>Note:</b> If a type is a nullable type, it has "?" as a prefix. e.g.
     * ?\Foo, ?string
     *
     * @return the type name
     */
    String typeNameFromUnion() {
        if (typeNameScopes != null) {
            if (typeNameScopes.hasFirst() && typeNameScopes.first() != null) {
                return typeNameScopes.first();
            } else if (typeNameScopes.hasSecond() && typeNameScopes.second() != null) {
                TypeScope type = ModelUtils.getFirst(typeNameScopes.second());
                return type != null ? type.getName() : null;
            }
        }
        return null;
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
        final String tName = typeNameFromUnion();
        if (tName != null) {
            return Collections.singleton(tName);
        }
        return Collections.emptyList();
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
            typeNameScopes = Union2.<String, Collection<? extends TypeScope>>createSecond(types);
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
