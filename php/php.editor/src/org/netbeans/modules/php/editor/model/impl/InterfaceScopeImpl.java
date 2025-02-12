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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import static org.netbeans.modules.php.editor.model.impl.ScopeImpl.filter;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;

/**
 *
 * @author Radek Matous
 */
class InterfaceScopeImpl extends TypeScopeImpl implements InterfaceScope, TypeScope.FieldDeclarable {

    InterfaceScopeImpl(Scope inScope, InterfaceDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, isDeprecated);
    }

    InterfaceScopeImpl(IndexScope inScope, InterfaceElement indexedIface) {
        super(inScope, indexedIface);
    }

    @Override
    public String asString(PrintAs as) {
        StringBuilder retval = new StringBuilder();
        switch (as) {
            case NameAndSuperTypes -> {
                retval.append(getName());
                printAsSuperTypes(retval);
            }
            case SuperTypes -> printAsSuperTypes(retval);
        }
        return retval.toString();
    }

    private void printAsSuperTypes(StringBuilder sb) {
        Set<QualifiedName> superIfaces = getSuperInterfaces();
        if (!superIfaces.isEmpty()) {
            sb.append(" extends "); //NOI18N
        }
        StringBuilder ifacesBuffer = new StringBuilder();
        for (QualifiedName qualifiedName : superIfaces) {
            if (ifacesBuffer.length() > 0) {
                ifacesBuffer.append(", "); //NOI18N
            }
            ifacesBuffer.append(qualifiedName.getName());
        }
        sb.append(ifacesBuffer);
    }

    @Override
    public Collection<? extends MethodScope> getInheritedMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<InterfaceScope> interfaceScopes = new HashSet<>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Set<MethodElement> indexedFunctions =
                    org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false).filter(index.getAllMethods(iface));
            for (MethodElement classMember : indexedFunctions) {
                MethodElement indexedFunction = classMember;
                TypeElement type = indexedFunction.getType();
                if (type.isInterface()) {
                    allMethods.add(new MethodScopeImpl(new InterfaceScopeImpl(indexScope, (InterfaceElement) type), indexedFunction));
                } else {
                    allMethods.add(new MethodScopeImpl(new ClassScopeImpl(indexScope, (ClassElement) type), indexedFunction));
                }
            }
        }
        return allMethods;
    }

    @Override
    public final Collection<? extends ClassConstantElement> getInheritedConstants() {
        Set<ClassConstantElement> allConstants = new HashSet<>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<InterfaceScope> interfaceScopes = new HashSet<>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Collection<TypeConstantElement> indexedConstants = index.getInheritedTypeConstants(iface);
            for (TypeConstantElement classMember : indexedConstants) {
                TypeConstantElement indexedFunction = classMember;
                allConstants.add(new ClassConstantElementImpl(iface, indexedFunction));
            }
        }
        return allConstants;
    }

    @Override
    public final Collection<? extends MethodScope> getMethods() {
        Set<MethodScope> allMethods = new HashSet<>();
        allMethods.addAll(getDeclaredMethods());
        allMethods.addAll(getInheritedMethods());
        return allMethods;
    }

    @Override
    public void addSelfToIndex(IndexDocument indexDocument) {
        indexDocument.addPair(PHPIndexer.FIELD_IFACE, getIndexSignature(), true, true);
        Set<QualifiedName> superInterfaces = getSuperInterfaces();
        for (QualifiedName superIfaceName : superInterfaces) {
            final String name = superIfaceName.getName();
            final String namespaceName = superIfaceName.toNamespaceName().toString();
            indexDocument.addPair(PHPIndexer.FIELD_SUPER_IFACE, String.format("%s;%s;%s", name.toLowerCase(), name, namespaceName), true, true); //NOI18N
        }
        indexDocument.addPair(PHPIndexer.FIELD_TOP_LEVEL, getName().toLowerCase(), true, true);
        for (MethodScope methodScope : getDeclaredMethods()) {
            methodScope.addSelfToIndex(indexDocument);
        }
        for (ClassConstantElement constantElement : getDeclaredConstants()) {
            constantElement.addSelfToIndex(indexDocument);
        }
        // PHP 8.4 interface can declare property hooks
        for (FieldElement fieldElement : getDeclaredFields()) {
            fieldElement.addSelfToIndex(indexDocument);
        }
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(Signature.ITEM_DELIMITER);
        sb.append(getName()).append(Signature.ITEM_DELIMITER);
        sb.append(getOffset()).append(Signature.ITEM_DELIMITER);
        List<? extends String> superInterfaces = getSuperInterfaceNames();
        for (int i = 0; i < superInterfaces.size(); i++) {
            String iface = superInterfaces.get(i);
            if (i > 0) {
                sb.append(","); // NOI18N
            }
            sb.append(iface);
        }
        if (!superInterfaces.isEmpty()) {
            sb.append(Type.SEPARATOR);
            StringBuilder fqIfaceSb = new StringBuilder();
            Collection<QualifiedName> fQSuperInterfaceNames = getFQSuperInterfaceNames();
            for (QualifiedName fQSuperInterfaceName : fQSuperInterfaceNames) {
                if (fqIfaceSb.length() > 0) {
                    fqIfaceSb.append(","); //NOI18N
                }
                fqIfaceSb.append(fQSuperInterfaceName.toString()); //NOI18N
            }
            sb.append(fqIfaceSb);
        }
        sb.append(Signature.ITEM_DELIMITER);
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        assert namespaceScope != null;
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(Signature.ITEM_DELIMITER);
        sb.append(isDeprecated() ? 1 : 0).append(Signature.ITEM_DELIMITER);
        sb.append(getFilenameUrl()).append(Signature.ITEM_DELIMITER);
        return sb.toString();
    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof InterfaceElement indexedInterface) {
            return indexedInterface.getNamespaceName();
        }
        return super.getNamespaceName();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        List<? extends InterfaceScope> implementedInterfaces = getSuperInterfaceScopes();
        if (!implementedInterfaces.isEmpty()) {
            sb.append(" implements "); // NOI18N
            for (InterfaceScope interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" "); // NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        return List.of();
    }

    @Override
    public Collection<? extends FieldElement> getDeclaredFields() {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl =  ModelUtils.getIndexScope(this);
            if (indexScopeImpl instanceof IndexScope.PHP84IndexScope indexScope) {
                return indexScope.findFields(this);
            } else {
                return List.of();
            }
        }
        return filter(getElements(), (ModelElement element) -> element.getPhpElementKind().equals(PhpElementKind.FIELD));
    }

    @Override
    public Collection<? extends FieldElement> getInheritedFields() {
        Set<FieldElement> allFields = new HashSet<>();
        Map<String, FieldElement> fieldNames = new HashMap<>();
        Collection<? extends InterfaceScope> allInterfaceScopes = getAllSuperInterfaceScopes(getSuperInterfaceScopes());
        for (InterfaceScope interfaceScope : allInterfaceScopes) {
            if (interfaceScope instanceof TypeScope.FieldDeclarable ifaceScope) {
                for (FieldElement declaredField : ifaceScope.getDeclaredFields()) {
                    addField(declaredField, allFields, fieldNames);
                }
            }
        }
        return allFields;
    }

    private void addField(FieldElement declaredField, Set<FieldElement> allFields, Map<String, FieldElement> fieldNames) {
        // check duplicate fields
        FieldElement field = fieldNames.get(declaredField.getName());
        if (field != null && !field.equals(declaredField)) {
            InterfaceScope inScope = (InterfaceScope) field.getInScope();
            InterfaceScope declaredInScope = (InterfaceScope) declaredField.getInScope();
            Collection<? extends InterfaceScope> allInheritedTypes = getAllSuperInterfaceScopes(inScope.getSuperInterfaceScopes());
            if (allInheritedTypes.contains(declaredInScope)) {
                return;
            }
            allFields.remove(field);
        }
        if(allFields.add(declaredField)) {
            fieldNames.put(declaredField.getName(), declaredField);
        }
    }

   private Collection<? extends InterfaceScope> getAllSuperInterfaceScopes(Collection<? extends InterfaceScope> declaredInterfaces) {
        Set<InterfaceScope> interfaces = new HashSet<>(declaredInterfaces);
        for (InterfaceScope declaredInterface : declaredInterfaces) {
            interfaces.addAll(getAllSuperInterfaceScopes(declaredInterface.getSuperInterfaceScopes()));
        }
        return interfaces;
    }
}
