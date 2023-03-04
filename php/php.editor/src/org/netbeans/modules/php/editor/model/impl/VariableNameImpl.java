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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.api.elements.TypedInstanceElement;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
class VariableNameImpl extends ScopeImpl implements VariableName {

    enum TypeResolutionKind {
        LAST_ASSIGNMENT,
        MERGE_ASSIGNMENTS
    };

    private TypeResolutionKind typeResolutionKind = TypeResolutionKind.LAST_ASSIGNMENT;
    final List<LazyFieldAssignment> assignmentDatas = new ArrayList<>();
    private boolean globallyVisible;

    VariableNameImpl(Scope inScope, VariableElement indexedVariable) {
        this(inScope, indexedVariable.getName(),
                Union2.<String/*url*/, FileObject>createFirst(indexedVariable.getFilenameUrl()),
                new OffsetRange(indexedVariable.getOffset(), indexedVariable.getOffset() + indexedVariable.getName().length()), true);
        indexedElement = indexedVariable;
    }
    VarAssignmentImpl createAssignment(
            Scope scope,
            boolean conditionalBlock,
            OffsetRange blockRange,
            OffsetRange nameRange,
            Assignment assignment,
            Map<String, AssignmentImpl> allAssignments) {
        VarAssignmentImpl retval = new VarAssignmentImpl(this, scope, conditionalBlock, blockRange, nameRange, assignment, allAssignments);
        return retval;
    }

    VarAssignmentImpl createAssignment(final Scope scope, final boolean conditionalBlock, final OffsetRange blockRange, final OffsetRange nameRange, final String typeName) {
        VarAssignmentImpl retval = new VarAssignmentImpl(this, scope, conditionalBlock, blockRange, nameRange, typeName);
        return retval;
    }

    @Override
    public FileObject getRealFileObject() {
        return (indexedElement != null) ? indexedElement.getFileObject() : null;
    }

    VariableNameImpl(Scope inScope, Variable variable, boolean globallyVisible) {
        this(inScope,
                toName(variable), inScope.getFile(), toOffsetRange(variable), globallyVisible);
    }
    VariableNameImpl(Scope inScope, String name, Union2<String/*url*/, FileObject> file, OffsetRange offsetRange, boolean globallyVisible) {
        super(inScope, name, file, offsetRange, PhpElementKind.VARIABLE, false);
        this.globallyVisible = globallyVisible;
    }

    void setTypeResolutionKind(TypeResolutionKind typeResolutionKind) {
        this.typeResolutionKind = typeResolutionKind;
    }

    static String toName(Variable node) {
        return CodeUtils.extractVariableName(node);
    }

    static OffsetRange toOffsetRange(Variable node) {
        Expression name = node.getName();
        while ((name instanceof Variable)) {
            while (name instanceof ArrayAccess) {
                ArrayAccess access = (ArrayAccess) name;
                name = access.getName();
            }
            if (name instanceof Variable) {
                Variable var = (Variable) name;
                name = var.getName();
            }
        }
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public List<? extends VarAssignmentImpl> getVarAssignments() {
        Collection<? extends VarAssignmentImpl> values = filter(getElements(), new ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                return element instanceof VarAssignmentImpl;
            }
        });
        return new ArrayList<>(values);
    }
    private List<? extends FieldAssignmentImpl> getFieldAssignments() {
        Collection<? extends FieldAssignmentImpl> values = filter(getElements(), new ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                return element instanceof FieldAssignmentImpl;
            }
        });
        return new ArrayList<>(values);
    }
    AssignmentImpl findVarAssignment(int offset) {
        return findAssignment(offset, true, null);
    }
    AssignmentImpl findFieldAssignment(int offset, FieldElement expectedField) {
        return findAssignment(offset, false, expectedField);
    }

    String findFieldType(int offset, String fldName) {
        String retval = null;
        int retvalOffset = -1;
        if (assignmentDatas.isEmpty() && (isGloballyVisible() || representsThis())) {
            Scope inScope = getInScope();
            if (inScope != null) {
                inScope = inScope.getInScope();
            }
            if (inScope instanceof VariableScope) {
                VariableScope varScope = (VariableScope) inScope;
                List<? extends VariableName> variables = ModelUtils.filter(varScope.getDeclaredVariables(), getName());
                if (!variables.isEmpty()) {
                    VariableName varName = ModelUtils.getFirst(variables);
                    if (varName instanceof VariableNameImpl) {
                        return ((VariableNameImpl) varName).findFieldType(offset, fldName);
                    }
                }
            }
        }
        for (LazyFieldAssignment assign : assignmentDatas) {
            if (assign.scope.getBlockRange().containsInclusive(offset)) {
                if (retval == null || retvalOffset <= assign.startOffset) {
                    if (assign.startOffset < offset) {
                        if (fldName.equals(assign.fldName)) {
                            retval = assign.typeName;
                        } else if (assign.fldName.length() > 0 && fldName.equals(assign.fldName.substring(1))) {
                            retval = assign.typeName;
                        }
                    }
                }
            }
        }

        return retval;
    }

    AssignmentImpl findAssignment(int offset, boolean varAssignment, FieldElement expectedField) {
        AssignmentImpl retval = null;
        Collection<? extends AssignmentImpl> assignments = varAssignment
                ? getVarAssignments() : getFieldAssignments();
        if (assignments.size() == 1) {
            AssignmentImpl assign = assignments.iterator().next();
            if (expectedField == null || expectedField.equals(assign.getContainer())) {
                retval = assign;
            }

        }
        if (retval == null) {
            if (assignments.isEmpty() && (isGloballyVisible() || representsThis())) {
                Scope inScope = getInScope();
                if (inScope != null) {
                    inScope = inScope.getInScope();
                    if (isGloballyVisible() && inScope instanceof ClassScope) {
                        inScope = inScope.getInScope();
                    }
                }
                if (inScope instanceof VariableScope) {
                    VariableScope varScope = (VariableScope) inScope;
                    List<? extends VariableName> variables = ModelUtils.filter(varScope.getDeclaredVariables(), getName());
                    if (!variables.isEmpty()) {
                        VariableName varName = ModelUtils.getFirst(variables);
                        if (varName instanceof VariableNameImpl) {
                            return ((VariableNameImpl) varName).findAssignment(offset, true, null);
                        }
                    }
                }
            }
            if (!assignments.isEmpty()) {
                for (AssignmentImpl assign : assignments) {
                    if (retval == null || retval.getOffset() <= assign.getOffset()) {
                        if (assign.getOffset() < offset || (retval == null && isInitAssignment(assign))) {
                            if (expectedField == null || expectedField.equals(assign.getContainer())) {
                                retval = assign;
                            }
                        }
                    }
                }

            }
        }
        return retval;
    }

    private static boolean isInitAssignment(AssignmentImpl assignment) {
        boolean result = false;
        Scope assignmentScope = assignment.getInScope();
        if (assignmentScope instanceof MethodScope) {
            MethodScope assignmentInMethodScope = (MethodScope) assignmentScope;
            result = assignmentInMethodScope.isInitiator();
        }
        return result;
    }

    @Override
    public String getNormalizedName() {
        Scope inScope = getInScope();
        if (inScope instanceof MethodScope) {
            String methodName = representsThis() ? "" : inScope.getName(); //NOI18N
            inScope = inScope.getInScope();
            return (inScope != null && !isGloballyVisible()) ? inScope.getName() + methodName + getName() : getName();
        }
        return (inScope != null && !isGloballyVisible()) ? inScope.getName() + getName() : getName();
    }

    @Override
    public Collection<? extends String> getTypeNames(int offset) {
        return getTypeNamesImpl(offset, false);
    }
    public Collection<? extends String> getArrayAccessTypeNames(int offset) {
        return getTypeNamesImpl(offset, true);
    }

    @Override
    public Collection<? extends TypeScope> getArrayAccessTypes(int offset) {
        return getTypesImpl(offset, true);
    }

    @Override
    public Collection<? extends TypeScope> getTypes(int offset) {
        return getTypesImpl(offset, false);
    }
    private Collection<? extends String> getTypeNamesImpl(int offset, boolean arrayAccess) {
        Collection<String> retval = new ArrayList<>();
        if (!arrayAccess && getIndexedElement() instanceof TypedInstanceElement /*&& indexedElement.getFileObject() != getFileObject()*/) {
            TypedInstanceElement typedInstanceElement = (TypedInstanceElement) getIndexedElement();
            Set<TypeResolver> instanceTypes = typedInstanceElement.getInstanceTypes();
            for (TypeResolver typeResolver : instanceTypes) {
                if (typeResolver.isResolved()) {
                    final QualifiedName typeName = typeResolver.getTypeName(false);
                    if (typeName != null) {
                        retval.add(typeName.toString());
                    }
                }
            }
            return retval;
        }

        if (representsThis()) {
            ClassScope classScope = (ClassScope) getInScope();
            return Collections.singletonList(classScope.getName());
        }
        TypeResolutionKind useTypeResolutionKind = arrayAccess ? TypeResolutionKind.MERGE_ASSIGNMENTS : typeResolutionKind;
        if (useTypeResolutionKind.equals(TypeResolutionKind.LAST_ASSIGNMENT)) {
            AssignmentImpl assignment = findVarAssignment(offset);
            while (assignment != null) {
                if (assignment.isConditionalBlock()) {
                    if (!assignment.getBlockRange().containsInclusive(offset)) {
                        return getMergedTypeNames();
                    }
                }
                Collection<String> typeNames = assignment.getTypeNames();
                if (typeNames.isEmpty() || assignment.isArrayAccess()) {
                    if (assignment.isArrayAccess()) {
                        retval = Collections.singleton(Type.ARRAY);
                    }
                    AssignmentImpl nextAssignment = findVarAssignment(assignment.getOffset() - 1);
                    if (nextAssignment != null && !nextAssignment.equals(assignment)) {
                        assignment = nextAssignment;
                        continue;
                    }
                    break;
                }
                return typeNames;
            }
            return retval;
        } else {
            return getMergedTypeNames();
        }
    }

    private Collection<? extends String> getMergedTypeNames() {
        Collection<String> types = new HashSet<>();
        List<? extends VarAssignmentImpl> varAssignments = getVarAssignments();
        for (VarAssignmentImpl vAssignment : varAssignments) {
            types.addAll(vAssignment.getTypeNames());
        }
        return types;
    }

    private Collection<? extends TypeScope> getTypesImpl(int offset, boolean arrayAccess) {
        if (representsThis()) {
            ClassScope classScope = (ClassScope) getInScope();
            return Collections.singletonList(classScope);
        }
        TypeResolutionKind useTypeResolutionKind = arrayAccess ? TypeResolutionKind.MERGE_ASSIGNMENTS : typeResolutionKind;
        if (useTypeResolutionKind.equals(TypeResolutionKind.LAST_ASSIGNMENT)) {
            AssignmentImpl assignment = findVarAssignment(offset);
            // for multi catch
            if (assignment != null && assignment.isCatchClause()) {
                return getSameBlockRangeTypes(assignment.getBlockRange());
            }
            while (assignment != null) {
                if (assignment.isConditionalBlock()) {
                    if (!assignment.getBlockRange().containsInclusive(offset)) {
                        return getMergedTypes();
                    }
                }
                Collection<TypeScope> types = assignment.getTypes();
                if (types.isEmpty() || assignment.isArrayAccess()) {
                    AssignmentImpl nextAssignment = findVarAssignment(assignment.getOffset() - 1);
                    if (nextAssignment != null && !nextAssignment.equals(assignment)) {
                        assignment = nextAssignment;
                        continue;
                    }
                    break;
                }
                return types;
            }
        } else {
            return getMergedTypes();
        }
        if (getIndexedElement() instanceof TypedInstanceElement) {
            Collection<TypeScope> retval = new HashSet<>();
            Collection<? extends String> typeNamesImpl = getTypeNamesImpl(offset, arrayAccess);
            for (String tName : typeNamesImpl) {
                retval.addAll(IndexScopeImpl.getTypes(QualifiedName.create(tName), getInScope()));

            }
            return retval;
        }
        return Collections.emptyList();
    }

    private Collection<TypeScope> getMergedTypes() {
        Collection<TypeScope> types = new HashSet<>();
        List<? extends VarAssignmentImpl> varAssignments = getVarAssignments();
        for (VarAssignmentImpl vAssignment : varAssignments) {
            types.addAll(vAssignment.getTypes());
        }
        return types;
    }

    private Collection<TypeScope> getSameBlockRangeTypes(OffsetRange blockRange) {
        Collection<TypeScope> types = new HashSet<>();
        List<? extends VarAssignmentImpl> varAssignments = getVarAssignments();
        varAssignments.stream()
                .filter(varAssignment -> (blockRange.equals(varAssignment.getBlockRange())))
                .forEach(varAssignment -> types.addAll(varAssignment.getTypes()));
        // empty when assignment cannot resolve types
        return types;
    }

    @Override
    public boolean isGloballyVisible() {
        String name = getName();
        if (name.startsWith("$")) {
            name = name.substring(1);
        }
        return globallyVisible || PredefinedSymbols.SUPERGLOBALS.contains(name);
    }

    /**
     * @param globallyVisible the globallyVisible to set
     */
    void setGloballyVisible(boolean globallyVisible) {
        this.globallyVisible = globallyVisible;
    }

    @Override
    public boolean representsThis() {
        Scope inScope = getInScope();
        if (inScope instanceof ClassScope && getName().equals("$this")) { //NOI18N
            return true;
        }
        return false;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        String varName = getName();
        String varNameNoDollar = varName.startsWith("$") ? varName.substring(1) : varName;
        if (!PredefinedSymbols.isSuperGlobalName(varNameNoDollar)) {
            indexDocument.addPair(PHPIndexer.FIELD_VAR, getIndexSignature(), true, true);
            indexDocument.addPair(PHPIndexer.FIELD_TOP_LEVEL, getName().toLowerCase(), true, true);
        }
    }

    private String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        final String varName = getName();
        sb.append(varName.toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(varName).append(Signature.ITEM_DELIMITER);
        //makes little sense because the variable with the same name can exists in huge number of files
        /*
        Set<String> typeNames = new HashSet<String>(getTypeNames(getNameRange().getEnd()+1));
        if (typeNames.size() == 1) {
            for (String typeName : typeNames) {
                if (!typeName.contains(VariousUtils.PRE_OPERATION_TYPE_DELIMITER)) {
                    sb.append(typeName);
                    break;
                }
            }
        }*/
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

    void createLazyFieldAssignment(FieldAccess fieldAccess, Assignment node, Scope scope) {
        String fldName = CodeUtils.extractVariableName(fieldAccess.getField());
        if (fldName != null) {
            if (!fldName.startsWith("$")) {
                fldName = "$" + fldName; //NOI18N
            }
        }
        String typeName = VariousUtils.extractVariableTypeFromAssignment(node, Collections.<String, AssignmentImpl>emptyMap());
        ASTNodeInfo<FieldAccess> fieldInfo = ASTNodeInfo.create(fieldAccess);
        final OffsetRange range = fieldInfo.getRange();
        final int startOffset = fieldAccess.getStartOffset();
        assignmentDatas.add(new LazyFieldAssignment(typeName, fldName, range, startOffset, scope));
    }

    void createLazyStaticFieldAssignment(StaticFieldAccess staticFieldAccess, Assignment node, Scope scope) {
        String fldName = CodeUtils.extractVariableName(staticFieldAccess.getField());
        if (fldName != null) {
            if (!fldName.startsWith("$")) {
                fldName = "$" + fldName; //NOI18N
            }
        }
        String typeName = VariousUtils.extractVariableTypeFromAssignment(node, Collections.<String, AssignmentImpl>emptyMap());
        ASTNodeInfo<StaticFieldAccess> fieldInfo = ASTNodeInfo.create(staticFieldAccess);
        final OffsetRange range = fieldInfo.getRange();
        final int startOffset = staticFieldAccess.getStartOffset();
        assignmentDatas.add(new LazyFieldAssignment(typeName, fldName, range, startOffset, scope));
    }

    @Override
    public Collection<? extends TypeScope> getFieldTypes(FieldElement element, int offset) {
        processFieldAssignments();
        AssignmentImpl assignment = findFieldAssignment(offset, element);
        return (assignment != null) ? assignment.getTypes() : element.getTypes(offset);
    }

    void processFieldAssignments() {
        if (!assignmentDatas.isEmpty()) {
            for (LazyFieldAssignment fieldAssignmentData : assignmentDatas) {
                fieldAssignmentData.process();
            }
            assignmentDatas.clear();
        }
    }

    private final class LazyFieldAssignment {
        private final String typeName;
        private final String fldName;
        private final OffsetRange range;
        private final int startOffset;
        private final Scope scope;

        private LazyFieldAssignment(String typeName, String fldName, OffsetRange range, int startOffset, Scope scope) {
            this.typeName = typeName;
            this.fldName = fldName;
            this.range = range;
            this.startOffset = startOffset;
            this.scope = scope;
        }

        void process() {
            Collection<? extends TypeScope> types = getTypes(startOffset);
            TypeScope type = ModelUtils.getFirst(types);
            if (type instanceof ClassScope) {
                ClassScope cls = (ClassScope) type;
                FieldElementImpl field = (FieldElementImpl) ModelUtils.getFirst(cls.getDeclaredFields(), fldName);
                if (field != null) {
                    FieldAssignmentImpl fa = new FieldAssignmentImpl(VariableNameImpl.this, (FieldElementImpl) field, scope, scope.getBlockRange(), range, typeName);
                    addElement(fa);
                }
            }
        }
    }
}

