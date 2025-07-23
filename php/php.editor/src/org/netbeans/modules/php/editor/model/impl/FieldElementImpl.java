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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.PropertyHookElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.PropertyHookScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import static org.netbeans.modules.php.editor.model.impl.ScopeImpl.filter;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.model.nodes.PropertyHookDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PropertyHookDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class FieldElementImpl extends ScopeImpl implements FieldElement.HookedFieldElement {

    private String defaultType;
    private String defaultFQType;
    private String className;
    private final boolean isAnnotation;
    private final boolean isHooked;
    private final List<? extends PropertyHookScope> propertyHooks;

    FieldElementImpl(Scope inScope, String defaultType, String defaultFQType, ASTNodeInfo<FieldAccess> nodeInfo, boolean isDeprecated, boolean isAnnotation) {
        super(inScope, nodeInfo, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), null, isDeprecated);
        this.defaultType = defaultType;
        this.defaultFQType = defaultFQType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
        this.isAnnotation = isAnnotation;
        this.isHooked = false;
        this.propertyHooks = List.of();
    }

    FieldElementImpl(Scope inScope, String defaultType, String defaultFQType, SingleFieldDeclarationInfo nodeInfo, boolean isDeprecated, boolean isAnnotation) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), nodeInfo.getBlock(), isDeprecated);
        this.defaultType = defaultType;
        this.defaultFQType = defaultFQType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
        this.isAnnotation = isAnnotation;
        this.isHooked = nodeInfo.isHooked();
        List<PropertyHookDeclaration> hookDeclarations = nodeInfo.getPropertyHooks();
        List<PropertyHookScope> propertyHookScopes = new ArrayList<>();
        for (PropertyHookDeclaration hookDeclaration : hookDeclarations) {
            propertyHookScopes.add(new PropertyHookScopeImpl(this, PropertyHookDeclarationInfo.create(hookDeclaration)));
        }
        this.propertyHooks = List.copyOf(propertyHookScopes);
    }

    FieldElementImpl(Scope inScope, String defaultType, String defaultFQType, PhpDocTypeTagInfo nodeInfo, boolean isAnnotation) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), null, inScope.isDeprecated());
        this.defaultType = defaultType;
        this.defaultFQType = defaultFQType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
        this.isAnnotation = isAnnotation;
        this.isHooked = false;
        this.propertyHooks = List.of();
    }

    FieldElementImpl(Scope inScope, org.netbeans.modules.php.editor.api.elements.FieldElement fieldElement) {
        super(inScope, fieldElement, PhpElementKind.FIELD);
        String in = fieldElement.getIn();
        if (in != null) {
            className = in;
        } else {
            className = inScope.getName();
        }
        Set<TypeResolver> instanceTypes = fieldElement.getInstanceTypes();
        for (TypeResolver typeResolver : instanceTypes) {
            if (typeResolver.isResolved()) {
                QualifiedName typeName = typeResolver.getTypeName(false);
                String type;
                if (typeName == null) {
                    type = ""; // NOI18N
                } else {
                    if (typeName.toNamespaceName().toString().isEmpty()
                            && Type.isPrimitive(typeName.getName())) {
                        type = typeName.getName();
                    } else {
                        type = typeName.toNamespaceName() + "\\" + typeName.getName(); // NOI18N
                    }
                }
                if (this.defaultType != null) {
                    this.defaultType += String.format("|%s", type); //NOI18N
                } else {
                    this.defaultType = type;
                }
            }
        }
        Set<TypeResolver> instanceFQTypes = fieldElement.getInstanceFQTypes();
        for (TypeResolver typeResolver : instanceFQTypes) {
            if (typeResolver.isResolved()) {
                QualifiedName typeName = typeResolver.getTypeName(false);
                String type = typeName == null ? "" : typeName.toNamespaceName() + "\\" + typeName.getName(); // NOI18N
                if (this.defaultFQType != null) {
                    this.defaultFQType += String.format("|%s", type); //NOI18N
                } else {
                    this.defaultFQType = type;
                }
            }
        }
        this.isAnnotation = fieldElement.isAnnotation();
        this.isHooked = org.netbeans.modules.php.editor.api.elements.FieldElement.isHooked(fieldElement);
        List<PropertyHookScope> propertyHookScopes = new ArrayList<>();
        if (fieldElement instanceof org.netbeans.modules.php.editor.api.elements.FieldElement.HookedFieldElement hookedField) {
            for (PropertyHookElement propertyHook : hookedField.getPropertyHooks()) {
                propertyHookScopes.add(new PropertyHookScopeImpl(this, propertyHook));
            }
        }
        this.propertyHooks = List.copyOf(propertyHookScopes);
    }

    private FieldElementImpl(Scope inScope, String name,
            Union2<String/*url*/, FileObject> file, OffsetRange offsetRange,
            PhpModifiers modifiers, String defaultType, boolean isDeprecated) {
        super(inScope, name, file, offsetRange, PhpElementKind.FIELD, modifiers, isDeprecated);
        this.defaultType = defaultType;
        this.defaultFQType = defaultType;
        this.isAnnotation = false;
        this.isHooked = false;
        this.propertyHooks = List.of();
    }

    @Override
    void addElement(ModelElementImpl element) {
        assert element instanceof PropertyHookScope : "Unexpected ModelElement: " + element.getClass().getName(); // NOI18N
        if (element instanceof PropertyHookScope) {
            super.addElement(element);
        }
    }

    static String toName(SingleFieldDeclaration node) {
        return VariableNameImpl.toName(node.getName());
    }

    static OffsetRange toOffsetRange(SingleFieldDeclaration node) {
        return VariableNameImpl.toOffsetRange(node.getName());
    }

    static PhpModifiers toAccessModifiers(FieldsDeclaration node) {
        return PhpModifiers.fromBitMask(node.getModifier());
    }

    public Collection<? extends TypeScope> getDefaultTypes() {
        Collection<TypeScope> typeScopes = new HashSet<>();
        if (defaultFQType != null && defaultFQType.length() > 0) {
            String[] allTypeNames = Type.splitTypes(defaultFQType);
            for (String typeName : allTypeNames) {
                String modifiedTypeName = typeName;
                if (typeName.indexOf("[") != -1) {
                    modifiedTypeName = typeName.replaceAll("\\[.*\\]", ""); //NOI18N
                }
                modifiedTypeName = CodeUtils.removeNullableTypePrefix(modifiedTypeName);
                if (isSpecialClassName(modifiedTypeName)) {
                    // \self or \parent
                    modifiedTypeName = modifiedTypeName.substring(1);
                    Scope inScope = getInScope();
                    if (inScope instanceof VariableScope) {
                        Collection<? extends TypeScope> types = VariousUtils.getType((VariableScope) getInScope(), modifiedTypeName, getOffset(), false);
                        for (TypeScope type : types) {
                            modifiedTypeName = type.getFullyQualifiedName().toString();
                            break;
                        }
                    }
                }
                typeScopes.addAll(IndexScopeImpl.getTypes(QualifiedName.create(modifiedTypeName), this));
            }
        }
        return typeScopes;
    }

    private boolean isSpecialClassName(String className) {
        String name = className;
        if (className.startsWith("\\")) { // NOI18N
            name = name.substring(1);
        }
        return VariousUtils.isSpecialClassName(name);
    }

    @Override
    public String getNormalizedName() {
        return className + super.getNormalizedName();
    }

    @Override
    public Collection<? extends String> getTypeNames(int offset) {
        AssignmentImpl assignment = findAssignment(offset);
        Collection<? extends String> retval = (assignment != null) ? assignment.getTypeNames() : Collections.emptyList();
        if (retval.isEmpty()) {
            retval = getDefaultTypeNames();
            if (retval.isEmpty()) {
                ClassScope classScope = (ClassScope) getInScope();
                for (VariableName variableName : classScope.getDeclaredVariables()) {
                    if (variableName.representsThis()) {
                        return variableName.getTypeNames(offset);
                    }
                }
            }
        }
        return retval;
    }

    private static Set<String> recursionDetection = new HashSet<>(); //#168868

    @Override
    public Collection<? extends TypeScope> getArrayAccessTypes(int offset) {
        return getTypes(offset);
    }

    @Override
    public Collection<? extends TypeScope> getTypes(int offset) {
        AssignmentImpl assignment = findAssignment(offset);
        Collection retval = (assignment != null) ? assignment.getTypes() : Collections.emptyList();
        if (retval.isEmpty()) {
            retval = getDefaultTypes();
            if (retval.isEmpty() && getInScope() instanceof ClassScope) {
                ClassScope classScope = (ClassScope) getInScope();
                for (VariableName variableName : classScope.getDeclaredVariables()) {
                    if (variableName.representsThis()) {
                        final String checkName = getNormalizedName();
                        boolean added = recursionDetection.add(checkName);
                        try {
                            if (added) {
                                return variableName.getFieldTypes(this, offset);
                            }
                        } finally {
                            recursionDetection.remove(checkName);
                        }
                    }
                }
            }
        }
        return retval;
    }

    @Override
    public Collection<? extends String> getDefaultTypeNames() {
        return VariousUtils.getAllTypeNames(defaultType);
    }

    @CheckForNull
    @Override
    public String getDefaultType() {
        return defaultType;
    }

    @Override
    public boolean isAnnotation() {
        return isAnnotation;
    }

    public Collection<? extends FieldAssignmentImpl> getAssignments() {
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return true;
            }
        });
    }

    public AssignmentImpl findAssignment(int offset) {
        FieldAssignmentImpl retval = null;
        Collection<? extends FieldAssignmentImpl> assignments = getAssignments();
        if (assignments.size() == 1) {
            retval = assignments.iterator().next();
        } else {
            for (FieldAssignmentImpl assignmentImpl : assignments) {
                if (assignmentImpl.getBlockRange().containsInclusive(offset)) {
                    if (retval == null || retval.getOffset() <= assignmentImpl.getOffset()) {
                        if (assignmentImpl.getOffset() < offset) {
                            retval = assignmentImpl;
                        }
                    }
                }
            }
        }
        return retval;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_FIELD, getIndexSignature(), true, true);
    }

    private String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        final String noDollarName = getName().substring(1);
        sb.append(noDollarName.toLowerCase()).append(Signature.ITEM_DELIMITER); // 0: name lowercase
        sb.append(noDollarName).append(Signature.ITEM_DELIMITER); // 1: name
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER); // 2: offset
        sb.append(getPhpModifiers().toFlags()).append(Signature.ITEM_DELIMITER); // 3: modifiers
        if (defaultType != null) {
            sb.append(defaultType); // 4: type
        }
        sb.append(Signature.ITEM_DELIMITER);
        if (defaultFQType != null) {
            sb.append(defaultFQType); // 5: FQ type
        }
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER); // 6: isDeprecated
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER); // 7: file name URL
        sb.append(isAnnotation() ? 1 : 0).append(Signature.ITEM_DELIMITER); // 8: isAnnotation
        sb.append(getPropertyHooksIndexSignature()).append(Signature.ITEM_DELIMITER); // 9: property hooks
        return sb.toString();
    }

    private String getPropertyHooksIndexSignature() {
        return PropertyHookSignatureItem.getSignatureFromScopes(getPropertyHooks());
    }

    @Override
    public Collection<? extends TypeScope> getFieldTypes(FieldElement element, int offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends PropertyHookScope> getPropertyHooks() {
        return propertyHooks;
    }

    @Override
    public boolean isHooked() {
        return isHooked;
    }
}
