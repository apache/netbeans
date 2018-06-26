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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class FieldElementImpl extends ScopeImpl implements FieldElement {
    String defaultType;
    private String defaultFQType;
    private String className;

    FieldElementImpl(Scope inScope, String defaultType, String defaultFQType, ASTNodeInfo<FieldAccess> nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), null, isDeprecated);
        this.defaultType = defaultType;
        this.defaultFQType = defaultFQType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
    }

    FieldElementImpl(Scope inScope, String defaultType, String defaultFQType, SingleFieldDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), null, isDeprecated);
        this.defaultType = defaultType;
        this.defaultFQType = defaultFQType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
    }

    FieldElementImpl(Scope inScope, String defaultType, String defaultFQType, PhpDocTypeTagInfo nodeInfo) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), null, inScope.isDeprecated());
        this.defaultType = defaultType;
        this.defaultFQType = defaultFQType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
    }

    FieldElementImpl(Scope inScope, org.netbeans.modules.php.editor.api.elements.FieldElement indexedConstant) {
        super(inScope, indexedConstant, PhpElementKind.FIELD);
        String in = indexedConstant.getIn();
        if (in != null) {
            className = in;
        } else {
            className = inScope.getName();
        }
        Set<TypeResolver> instanceTypes = indexedConstant.getInstanceTypes();
        for (TypeResolver typeResolver : instanceTypes) {
            if (typeResolver.isResolved()) {
                QualifiedName typeName = typeResolver.getTypeName(false);
                String type = typeName == null ? "" : typeName.toNamespaceName() + "\\" + typeName.getName(); // NOI18N
                if (this.defaultType != null) {
                    this.defaultType += String.format("|%s", type); //NOI18N
                } else {
                    this.defaultType = type;
                }
            }
        }
        Set<TypeResolver> instanceFQTypes = indexedConstant.getInstanceFQTypes();
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
    }

    private FieldElementImpl(Scope inScope, String name,
            Union2<String/*url*/, FileObject> file, OffsetRange offsetRange,
            PhpModifiers modifiers, String defaultType, boolean isDeprecated) {
        super(inScope, name, file, offsetRange, PhpElementKind.FIELD, modifiers, isDeprecated);
        this.defaultType = defaultType;
        this.defaultFQType = defaultType;
    }

    @Override
    void addElement(ModelElementImpl element) {
        //super.addElement(element);
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
            String[] allTypeNames = defaultFQType.split("\\|");
            for (String typeName : allTypeNames) {
                String modifiedTypeName = typeName;
                if (typeName.indexOf("[") != -1) {
                    modifiedTypeName = typeName.replaceAll("\\[.*\\]", ""); //NOI18N
                }
                typeScopes.addAll(IndexScopeImpl.getTypes(QualifiedName.create(modifiedTypeName), this));
            }
        }
        return typeScopes;
    }
    @Override
    public String getNormalizedName() {
        return className + super.getNormalizedName();
    }

    @Override
    public Collection<? extends String> getTypeNames(int offset) {
        AssignmentImpl assignment = findAssignment(offset);
        Collection<? extends String> retval = (assignment != null) ? assignment.getTypeNames() : Collections.emptyList();
        if  (retval.isEmpty()) {
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
        if  (retval.isEmpty()) {
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
        Collection<String> retval = Collections.<String>emptyList();
        if (defaultType != null && defaultType.length() > 0) {
            retval = new ArrayList<>();
            for (String typeName : defaultType.split("\\|")) { //NOI18N
                if (!VariousUtils.isSemiType(typeName)) {
                    retval.add(typeName);
                }
            }
        }
        return retval;
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
        sb.append(noDollarName.toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(noDollarName).append(Signature.ITEM_DELIMITER);
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        sb.append(getPhpModifiers().toFlags()).append(Signature.ITEM_DELIMITER);
        if (defaultType != null) {
            sb.append(defaultType);
        }
        sb.append(Signature.ITEM_DELIMITER);
        if (defaultFQType != null) {
            sb.append(defaultFQType);
        }
        sb.append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

    @Override
    public Collection<? extends TypeScope> getFieldTypes(FieldElement element, int offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
